package ru.skillFactory.model;

import java.util.ArrayList;

public class WalletInfo {
    ArrayList<Transaction> transactions;
    ArrayList<Category> categories;

    public WalletInfo(ArrayList<Transaction> transactions, ArrayList<Category> categories) {
        this.transactions = transactions;
        this.categories = categories;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }
}
