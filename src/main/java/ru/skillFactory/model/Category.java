package ru.skillFactory.model;

public class Category {
    protected String title;
    protected Double budget;

    public Category(String title, Double budget) {
        this.title = title;
        this.budget = budget;
    }

    public String getTitle() {
        return title;
    }

    public Double getBudget() {
        return budget;
    }

    @Override
    public String toString() {
        return "Категория '" + title + "', бюджет=" + budget;
    }

}
