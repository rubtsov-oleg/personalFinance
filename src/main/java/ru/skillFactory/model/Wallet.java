package ru.skillFactory.model;

import java.util.ArrayList;

public class Wallet {
    protected ArrayList<Transaction> transactions = new ArrayList<>();
    protected ArrayList<Category> categories = new ArrayList<>();

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }
}
