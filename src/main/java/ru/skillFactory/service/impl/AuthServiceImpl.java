package ru.skillFactory.service.impl;

import ru.skillFactory.exception.UserSaveException;
import ru.skillFactory.model.Category;
import ru.skillFactory.model.Transaction;
import ru.skillFactory.model.Wallet;
import ru.skillFactory.service.AuthService;
import ru.skillFactory.model.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class AuthServiceImpl implements AuthService {
    private static final String USER_PREFIX = "USER";
    private static final String CATEGORY_PREFIX = "CATEGORY";
    private static final String TRANSACTION_PREFIX = "TRANSACTION";
    private static final String EXIT_COMMAND = "exit";
    private static final Double OUTGOING_TRANSFERS_BUDGET = 5000.0;
    private static final Double INCOMING_TRANSFERS_BUDGET = -1.0;
    private static final Integer USERNAME_LENGTH = 3;
    private static final Integer PASSWORD_LENGTH = 3;
    private static final Integer PASSWORD_ATTEMPTS = 3;
    private final Scanner scanner;

    private final String filename;
    protected HashMap<String, User> users = new HashMap<>();

    public AuthServiceImpl(Scanner scanner, String filename) {
        this.scanner = scanner;
        this.filename = filename;
    }

    public HashMap<String, User> getUsers() {
        return users;
    }

    @Override
    public void signup() {
        System.out.println("Для регистрации придумайте логин:");
        String username;
        while (true) {
            username = scanner.next();

            boolean isExistedUsername = false;

            if (isExitCommand(username)) {
                System.out.println("Данный логин нельзя использовать, придумайте другой:");
                continue;
            }

            if (username.length() < USERNAME_LENGTH) {
                System.out.println("Логин должен содержать не менее 3 символов.");
                continue;
            }

            for (User existedUser : users.values()) {
                if (username.equals(existedUser.getUsername())) {
                    isExistedUsername = true;
                    break;
                }
            }

            if (isExistedUsername) {
                System.out.println("Данный логин уже существует, придумайте другой:");
            } else {
                break;
            }
        }
        System.out.println("Для регистрации придумайте пароль:");
        String password;
        while (true) {
            password = scanner.next();
            if (password.length() > PASSWORD_LENGTH) {
                break;
            }
            System.out.println("Пароль должен содержать не менее 3 символов.");
        }

        Wallet wallet = new Wallet();
        Category incomingCategory = new Category(INCOMING_TRANSFERS_NAME, INCOMING_TRANSFERS_BUDGET);
        Category outgoingCategory = new Category(OUTGOING_TRANSFERS_NAME, OUTGOING_TRANSFERS_BUDGET);
        wallet.getCategories().add(incomingCategory);
        wallet.getCategories().add(outgoingCategory);
        User user = new User(username, password, wallet);
        users.put(user.getUsername(), user);

        System.out.println("Вы успешно зарегистрировались");
    }

    @Override
    public User signIn() {
        System.out.println("Для авторизации введите ваш логин:");
        while (true) {
            String username = scanner.next();

            if (isExitCommand(username)) {
                return null;
            }

            User user = users.get(username);
            if (user == null) {
                System.out.println("Неверный логин, попробуйте ещё раз " +
                        "(для возврата в главное меню введите " + EXIT_COMMAND + "):");
                continue;
            }

            System.out.println("Для авторизации введите ваш пароль:");

            int attempts = PASSWORD_ATTEMPTS;
            while (attempts > 0) {
                String password = scanner.next();
                if (password.equals(user.getPassword())) {
                    System.out.println("Авторизация прошла успешно!");
                    return user;
                } else {
                    attempts--;
                    if (attempts == 0) {
                        System.out.println("Превышено количество попыток. Возврат в главное меню.");
                        return null;
                    }
                    System.out.println("Неверный пароль, осталось попыток: " + attempts);
                }
            }

        }
    }

    @Override
    public void saveUsersToFile() {
        try (BufferedWriter bf = Files.newBufferedWriter(Path.of(filename),
                StandardOpenOption.TRUNCATE_EXISTING)) {
            for (User user : users.values()) {
                bf.write(userToString(user));
                bf.newLine();

                for (Category category : user.getWallet().getCategories()) {
                    bf.write(categoriestToString(category, user.getUsername()));
                    bf.newLine();
                }

                for (Transaction transaction : user.getWallet().getTransactions()) {
                    bf.write(transactionToString(transaction, user.getUsername()));
                    bf.newLine();
                }
            }
        } catch (IOException e) {
            throw new UserSaveException("Ошибка при сохранении данных в файл", e);
        }
    }

    @Override
    public void loadUsersFromFile() {
        try (FileReader reader = new FileReader(filename, StandardCharsets.UTF_8)) {
            BufferedReader br = new BufferedReader(reader);

            while (br.ready()) {
                String line = br.readLine();
                if (line.matches("^USER\\b.*")) {
                    addUser(line);
                } else if (line.matches("^CATEGORY\\b.*")) {
                    addCategory(line);
                } else if (line.matches("^TRANSACTION\\b.*")) {
                    addTransaction(line);
                }
            }
        } catch (IOException e) {
            throw new UserSaveException("Ошибка при чтении данных из файла", e);
        }
    }

    private void addUser(String line) {
        User user = userFromString(line);
        users.put(user.getUsername(), user);
    }

    private void addCategory(String line) {
        try {
            Category category = categoryFromString(line);
            String username = usernameFromString(line);
            User user = users.get(username);
            if (user == null) {
                throw new UserSaveException("Пользователь не найден: " + username);
            }
            user.getWallet().getCategories().add(category);
        } catch (Exception e) {
            throw new UserSaveException("Ошибка обработки строки категории: " + line, e);
        }
    }

    private void addTransaction(String line) {
        try {
            String username = usernameFromString(line);
            User user = users.get(username);
            if (user == null) {
                throw new UserSaveException("Ошибка при сохранении транзакции. Пользователь не найден");
            }
            ArrayList<Category> categories = user.getWallet().getCategories();
            Transaction transaction = transactionFromString(line, categories);
            user.getWallet().getTransactions().add(transaction);
        } catch (Exception e) {
            throw new UserSaveException("Ошибка обработки строки категории: " + line, e);
        }
    }

    private String usernameFromString(String data) {
        return data.split(",")[1];
    }

    private String userToString(User user) {
        LinkedList<String> userData = new LinkedList<>();
        userData.add(USER_PREFIX);
        userData.add(user.getUsername());
        userData.add(user.getPassword());
        return String.join(",", userData);
    }

    private User userFromString(String userString) {
        String[] userData = userString.split(",");
        String username = userData[1];
        String password = userData[2];
        return new User(username, password, new Wallet());
    }

    private String categoriestToString(Category category, String username) {
        LinkedList<String> categoryData = new LinkedList<>();
        categoryData.add(CATEGORY_PREFIX);
        categoryData.add(username);
        categoryData.add(category.getTitle());
        categoryData.add(category.getBudget().toString());
        return String.join(",", categoryData);
    }

    private Category categoryFromString(String categoryString) {
        String[] categoryData = categoryString.split(",");
        String title = categoryData[2];
        Double budget = Double.valueOf(categoryData[3]);
        return new Category(title, budget);
    }

    private String transactionToString(Transaction transaction, String username) {
        LinkedList<String> transactionData = new LinkedList<>();
        transactionData.add(TRANSACTION_PREFIX);
        transactionData.add(username);
        transactionData.add(transaction.getExpense().toString());
        transactionData.add(transaction.getAmount().toString());
        transactionData.add(transaction.getCategory().getTitle());
        return String.join(",", transactionData);
    }

    private Transaction transactionFromString(String transactionString, ArrayList<Category> userCategories) {
        String[] transactionData = transactionString.split(",");
        Boolean isExpense = Boolean.valueOf(transactionData[2]);
        Double amount = Double.valueOf(transactionData[3]);
        String categoryTitle = transactionData[4];

        for (Category category : userCategories) {
            if (category.getTitle().equals(categoryTitle)) {
                return new Transaction(amount, category, isExpense);
            }
        }

        throw new UserSaveException("Ошибка при сохранении транзакции. Категория не найдена");
    }

    private boolean isExitCommand(String input) {
        return EXIT_COMMAND.equals(input);
    }
}
