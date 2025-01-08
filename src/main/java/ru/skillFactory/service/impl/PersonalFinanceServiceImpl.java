package ru.skillFactory.service.impl;

import ru.skillFactory.model.*;
import ru.skillFactory.service.AuthService;
import ru.skillFactory.service.PersonalFinanceService;

import java.util.*;

public class PersonalFinanceServiceImpl implements PersonalFinanceService {
    private final Scanner scanner;
    private static final Double BUDGET_FOR_INCOME = -1.0;

    public PersonalFinanceServiceImpl(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void createCategoryOfIncome(User user) {
        createCategoryOfTransactions(user, false);
    }

    @Override
    public void createCategoryOfExpense(User user) {
        createCategoryOfTransactions(user, true);
    }

    public void createCategoryOfTransactions(User user, boolean needBudget) {
        ArrayList<Category> userCategories = user.getWallet().getCategories();
        String categoryTitle = comeUpWithCategoryTitle(userCategories);
        double budget;
        if (needBudget) {
            budget = comeUpWithCategoryBudget();
        } else {
            budget = BUDGET_FOR_INCOME;
        }

        userCategories.add(new Category(categoryTitle, budget));
        user.getWallet().setCategories(userCategories);
        System.out.println("Категория успешно создана!");
    }

    @Override
    public void createIncome(User user) {
        createTransaction(user, false);
    }

    @Override
    public void createExpense(User user) {
        createTransaction(user, true);
    }

    @Override
    public void totalExpensesAndIncome(User user) {
        ArrayList<Transaction> transactions = user.getWallet().getTransactions();

        if (transactions.isEmpty()) {
            System.out.println("Данных не найдено!");
            return;
        }
        Double totalExpense = 0.0;
        Double totalIncome = 0.0;

        for (Transaction transaction : transactions) {
            if (transaction.getExpense()) {
                totalExpense += transaction.getAmount();
            } else {
                totalIncome += transaction.getAmount();
            }
        }

        System.out.println("Общий доход состовляет: " + totalIncome);
        System.out.println("Общий расход состовляет: " + totalExpense);
    }

    @Override
    public void incomeByAllCategories(User user) {
        calculateTransactionsByAllCategories(user, false);
    }

    @Override
    public void expenseByAllCategories(User user) {
        calculateTransactionsByAllCategories(user, true);
    }

    @Override
    public void budgetByAllCategories(User user) {
        WalletInfo walletInfo = getWalletInfo(user);
        if (walletInfo != null) {
            budgetByCategories(walletInfo.getTransactions(), walletInfo.getCategories());
        }
    }

    @Override
    public void calculateExpenseBySpecificCategories(User user) {
        WalletInfo walletInfo = getWalletInfo(user);
        if (walletInfo == null) {
            return;
        }


        ArrayList<Category> neededCategories = getNeededCategoriesFromUser(walletInfo.getCategories());
        if (neededCategories.isEmpty()) {
            System.out.println("Отсутствуют категории по которым нужно вывести информацию");
            return;
        }

        budgetByCategories(walletInfo.getTransactions(), neededCategories);
    }

    @Override
    public void transferToAnotherUser(User currentUser, HashMap<String, User> allUsers) {
        System.out.println("Введите имя пользователя, которому хотите сделать перевод:");
        String targetUsername;
        while (true) {
            targetUsername = scanner.next();

            if (allUsers.containsKey(targetUsername) && !targetUsername.equals(currentUser.getUsername())) {
                break;
            }
            System.out.println("Пользователь не найден, попробуйте ещё раз:");
        }

        double amount = getAmountFromUser();

        Double totalExpense = 0.0;
        Double totalIncome = 0.0;

        for (Transaction transaction : currentUser.getWallet().getTransactions()) {
            if (transaction.getExpense()) {
                totalExpense += transaction.getAmount();
            } else {
                totalIncome += transaction.getAmount();
            }
        }

        if (totalIncome - totalExpense < amount) {
            System.out.println("Недостаточно средст");
            return;
        }

        for (Category currentUserCategory : currentUser.getWallet().getCategories()) {
            if (currentUserCategory.getTitle().equals(AuthService.OUTGOING_TRANSFERS_NAME)) {
                currentUser.getWallet().getTransactions()
                        .add(new Transaction(amount, currentUserCategory, true));
                break;
            }
        }

        for (Category targetUserCategory : allUsers.get(targetUsername).getWallet().getCategories()) {
            if (targetUserCategory.getTitle().equals(AuthService.INCOMING_TRANSFERS_NAME)) {
                allUsers.get(targetUsername).getWallet().getTransactions()
                        .add(new Transaction(amount, targetUserCategory, false));
            }
        }

        System.out.println("Перевод выполнен!");
    }

    public WalletInfo getWalletInfo(User user) {
        ArrayList<Transaction> transactions = user.getWallet().getTransactions();
        ArrayList<Category> categories = user.getWallet().getCategories();

        if (categories.isEmpty()) {
            System.out.println("Сперва необходимо создать хоть одну категорию!");
            return null;
        }

        if (transactions.isEmpty()) {
            System.out.println("Сперва необходимо ввести доходы/расходы!");
            return null;
        }
        return new WalletInfo(transactions, categories);
    }

    private ArrayList<Category> getNeededCategoriesFromUser(ArrayList<Category> userCategories) {
        System.out.println("Введите категории, по которым нужно вывести информацию " +
                "(укажите их названия через запятую). Доступныее варианты:");
        for (Category userCategory : userCategories) {
            if (userCategory.getBudget() > 0) {
                System.out.println(userCategory);
            }
        }

        ArrayList<Category> neededCategories = new ArrayList<>();

        try {
            String categoriesTitle = scanner.next();
            String[] categoriesTitleArray = categoriesTitle.split(",");
            for (String categoriesTitleItem : categoriesTitleArray) {
                boolean isFounded = false;
                for (Category existedCategory : userCategories) {
                    if (categoriesTitleItem.equals(existedCategory.getTitle())) {
                        neededCategories.add(existedCategory);
                        isFounded = true;
                    }
                }
                if (!isFounded) {
                    System.out.println("Категория " + categoriesTitleItem + " не найдена!");
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка ввода!");
        }
        return neededCategories;
    }

    private void budgetByCategories(ArrayList<Transaction> transactions, ArrayList<Category> categories) {
        for (Category category : categories) {
            double expense = 0.0;
            for (Transaction transaction : transactions) {
                if (transaction.getExpense() && category.getTitle().equals(transaction.getCategory().getTitle())) {
                    expense += transaction.getAmount();
                }
            }
            if (expense != 0.0) {
                System.out.println(category.getTitle() + ": "
                        + category.getBudget() + ", Оставшийся бюджет:" + (category.getBudget() - expense));
            }
        }
    }

    private void calculateTransactionsByAllCategories(User user, boolean isExpense) {
        WalletInfo walletInfo = getWalletInfo(user);
        if (walletInfo == null) {
            return;
        }

        String nameOfOperation = isExpense ? "Расход" : "Доход";

        for (Category category : walletInfo.getCategories()) {
            double totalAmount = 0.0;
            for (Transaction transaction : walletInfo.getTransactions()) {
                if (transaction.getExpense().equals(isExpense)
                        && category.getTitle().equals(transaction.getCategory().getTitle())) {
                    totalAmount += transaction.getAmount();
                }
            }
            if (totalAmount != 0.0) {
                System.out.println(nameOfOperation + " по категории '"
                        + category.getTitle() + "' составляет " + totalAmount);
            }
        }
    }

    private void createTransaction(User user, boolean isExpense) {
        Wallet userWallet = user.getWallet();
        ArrayList<Category> userCategories = userWallet.getCategories();
        ArrayList<Transaction> userTransactions = userWallet.getTransactions();

        if (userCategories.isEmpty()) {
            System.out.println("Сперва необходимо создать хоть одну категорию!");
            return;
        }

        Category newCategory = getCategoryFromUser(userCategories, isExpense);

        if (isExpense && newCategory.getBudget() == -1) {
            System.out.println("В категорию доходов нельзя записать расход!");
            return;
        }

        double amount = getAmountFromUser();

        Transaction newTransaction = new Transaction(amount, newCategory, isExpense);
        userTransactions.add(newTransaction);
        userWallet.setTransactions(userTransactions);

        if (isExpense) {
            checkBudgetByCategory(newCategory, userTransactions);
        }
        System.out.println("Данные успешно записаны!");
    }

    private double getAmountFromUser() {
        System.out.println("Введите сумму:");
        double amount;
        while (true) {
            try {
                amount = scanner.nextDouble();
                break;
            } catch (InputMismatchException e) {
                System.out.println("Ошибка ввода");
                scanner.nextLine();
            }
        }
        return amount;
    }

    private Category getCategoryFromUser(ArrayList<Category> userCategories, boolean isExpense) {
        System.out.println("Введите название категории.\nДоступные варианты:");

        for (Category userCategory : userCategories) {
            if (isExpense && userCategory.getBudget() > 0) {
                System.out.println(userCategory);
            }
            if (!isExpense && Objects.equals(userCategory.getBudget(), BUDGET_FOR_INCOME)) {
                System.out.println("Категория '" + userCategory.getTitle() + "'");
            }
        }

        String categoryTitle;
        while (true) {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            categoryTitle = scanner.nextLine();
            for (Category existedCategory : userCategories) {
                if (categoryTitle.equals(existedCategory.getTitle())) {
                    return existedCategory;
                }
            }
            System.out.println("Введённая категория не найдена, попробуйте ещё раз");
        }
    }

    private void checkBudgetByCategory(Category category, ArrayList<Transaction> userTransactions) {
        Double expense = 0.0;
        for (Transaction transaction : userTransactions) {
            if (transaction.getExpense() && transaction.getCategory().getTitle().equals(category.getTitle())) {
                expense += transaction.getAmount();
            }
        }

        if (category.getBudget() < expense) {
            System.out.println("Внимание! Превышен бюджет по категории " + category.getTitle());
        }
    }

    private String comeUpWithCategoryTitle(ArrayList<Category> userCategories) {
        System.out.println("Придумайте название категории");

        while (true) {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }

            String categoryTitle = scanner.nextLine();
            boolean isExistedCategory = false;
            for (Category existedCategory : userCategories) {
                if (categoryTitle.equals(existedCategory.getTitle())) {
                    isExistedCategory = true;
                    break;
                }
            }

            if (isExistedCategory) {
                System.out.println("Данная категория уже существует, придумайте другую:");
            } else {
                return categoryTitle;
            }
        }
    }

    private Double comeUpWithCategoryBudget() {
        System.out.println("Устновите бюджет для категории (бюджет должен быть больше 0):");

        while (true) {
            try {
                double budget = scanner.nextDouble();
                if (budget <= 0) {
                    throw new InputMismatchException();
                }
                return budget;
            } catch (InputMismatchException e) {
                System.out.println("Ошибка ввода");
                scanner.nextLine();
            }
        }
    }
}
