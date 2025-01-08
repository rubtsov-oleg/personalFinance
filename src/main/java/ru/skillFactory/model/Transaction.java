package ru.skillFactory.model;

public class Transaction {
    protected Double amount;
    protected Category category;

    protected Boolean isExpense;

    public Double getAmount() {
        return amount;
    }

    public Transaction(Double amount, Category category, Boolean isExpense) {
        this.amount = amount;
        this.category = category;
        this.isExpense = isExpense;
    }

    public Category getCategory() {
        return category;
    }

    public Boolean getExpense() {
        return isExpense;
    }
}
