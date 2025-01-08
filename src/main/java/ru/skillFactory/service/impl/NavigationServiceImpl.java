package ru.skillFactory.service.impl;

import ru.skillFactory.model.User;
import ru.skillFactory.service.AuthService;
import ru.skillFactory.service.NavigationService;
import ru.skillFactory.service.PersonalFinanceService;

import java.util.InputMismatchException;
import java.util.Scanner;

public class NavigationServiceImpl implements NavigationService {
    private final PersonalFinanceService personalFinanceService;
    private final AuthService authService;
    private User currentUser;
    private final Scanner scanner;

    public NavigationServiceImpl(String filename) {
        this.scanner = new Scanner(System.in);
        this.personalFinanceService = new PersonalFinanceServiceImpl(scanner);
        this.authService = new AuthServiceImpl(scanner, filename);
    }

    @Override
    public void generalNavigation() {
        System.out.println("Сервис личных финансов приветствует вас!");
        authService.loadUsersFromFile();

        while (true) {
            printCurrentUser();
            printMainMenu();

            int i = -1;
            try {
                i = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Ошибка: Введите целое число!");
                scanner.nextLine();
                continue;
            }

            if (i == 1) {
                authNavigation();
            } else if (i == 2) {
                financeNavigation();
            } else if (i == 0) {
                System.out.println("Пока!");
                authService.saveUsersToFile();
                scanner.close();
                return;
            } else {
                System.out.println("Такой команды нет");
            }
        }
    }

    @Override
    public void authNavigation() {
        while (true) {
            printCurrentUser();
            printAuthMenu();

            int i = -1;
            try {
                i = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Ошибка: Введите целое число!");
                scanner.nextLine();
                continue;
            }

            if (i == 1) {
                authService.signup();
                authService.saveUsersToFile();
            } else if (i == 2) {
                currentUser = authService.signIn();
            } else if (i == 3) {
                currentUser = null;
            } else if (i == 0) {
                return;
            } else {
                System.out.println("Такой команды нет");
            }
        }
    }

    @Override
    public void financeNavigation() {
        if (currentUser == null) {
            System.out.println("\nВход не выполнен, доступ в раздел запрещён!\n");
            return;
        }
        while (true) {
            printCurrentUser();
            printFinanceMenu();

            int i = -1;
            try {
                i = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Ошибка: Введите целое число!");
                scanner.nextLine();
                continue;
            }

            if (i == 1) {
                personalFinanceService.createCategoryOfIncome(currentUser);
                authService.saveUsersToFile();
            } else if (i == 2) {
                personalFinanceService.createCategoryOfExpense(currentUser);
                authService.saveUsersToFile();
            } else if (i == 3) {
                personalFinanceService.createIncome(currentUser);
                authService.saveUsersToFile();
            } else if (i == 4) {
                personalFinanceService.createExpense(currentUser);
                authService.saveUsersToFile();
            } else if (i == 5) {
                personalFinanceService.totalExpensesAndIncome(currentUser);
            } else if (i == 6) {
                personalFinanceService.incomeByAllCategories(currentUser);
            } else if (i == 7) {
                personalFinanceService.expenseByAllCategories(currentUser);
            } else if (i == 8) {
                personalFinanceService.budgetByAllCategories(currentUser);
            } else if (i == 9) {
                personalFinanceService.calculateExpenseBySpecificCategories(currentUser);
            } else if (i == 10) {
                personalFinanceService.transferToAnotherUser(currentUser, authService.getUsers());
            } else if (i == 0) {
                return;
            } else {
                System.out.println("Такой команды нет");
            }
        }
    }

    private void printCurrentUser() {
        if (currentUser != null) {
            System.out.println("(Текущий пользователь: " + currentUser.getUsername() + ")\n");
        } else {
            System.out.println("(Вход не выполнен)\n");
        }
    }

    private void printMainMenu() {
        System.out.println("Текущий раздел - главное меню\n");
        System.out.println("Что вы хотите сделать?");
        System.out.println("1 - Личный кабинет");
        System.out.println("2 - Работа с финансами");
        System.out.println("0 - Выйти из приложения");
    }

    private void printAuthMenu() {
        System.out.println("Текущий раздел - Личный кабинет\n");
        System.out.println("Что вы хотите сделать?");
        System.out.println("1 - Зарегистрироваться");
        System.out.println("2 - Авторизироваться");
        System.out.println("3 - Разлогиниться");
        System.out.println("0 - Назад в главное меню");
    }

    private void printFinanceMenu() {
        System.out.println("Текущий раздел - Работа с финансами\n");
        System.out.println("Что вы хотите сделать?");
        System.out.println("1 - Создать категорию доходов");
        System.out.println("2 - Создать категорию расходов");
        System.out.println("3 - Добавить доход");
        System.out.println("4 - Добавить расход");
        System.out.println("5 - Вывести общий доход и расход");
        System.out.println("6 - Вывести доходы по всем категориям");
        System.out.println("7 - Вывести расходы по всем категориям");
        System.out.println("8 - Вывести бюджет по всем активным категориям");
        System.out.println("9 - Вывести бюджет по определённым категориям");
        System.out.println("10 - Перевод другому пользователю");
        System.out.println("0 - Назад в главное меню");
    }
}
