package com.example.tripkey;
import java.util.List;

public class DateGroup {
    private String date;
    private List<Expense> expenses;

    public DateGroup(String date, List<Expense> expenses) {
        this.date = date;
        this.expenses = expenses;
    }

    public String getDate() { return date; }
    public List<Expense> getExpenses() { return expenses; }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }
}
