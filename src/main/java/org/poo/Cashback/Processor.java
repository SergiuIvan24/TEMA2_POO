package org.poo.Cashback;

import org.poo.entities.User;

public class Processor {
    private CashbackStrategy strategy;

    public Processor(CashbackStrategy strategy) {
        this.strategy = strategy;
    }

    public double processCashback(User user, String Category, double transactionAmount) {
        return strategy.calculateCashback(user, Category, transactionAmount);
    }
}
