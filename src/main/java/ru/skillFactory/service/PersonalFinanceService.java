package ru.skillFactory.service;

import ru.skillFactory.model.User;

import java.util.HashMap;

public interface PersonalFinanceService {

    void createCategoryOfIncome(User user);

    void createCategoryOfExpense(User user);

    void createIncome(User user);

    void createExpense(User user);

    void totalExpensesAndIncome(User user);

    void incomeByAllCategories(User user);

    void expenseByAllCategories(User user);

    void budgetByAllCategories(User user);

    void calculateExpenseBySpecificCategories(User user);

    void transferToAnotherUser(User currentUser, HashMap<String, User> allUsers);
}
