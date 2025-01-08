package org.poo.Cashback;

import org.poo.entities.Account;

public class Processor {
    private CashbackStrategy strategy;

    public Processor(CashbackStrategy strategy) {
        this.strategy = strategy;
    }

    public double processCashback(Account account, String Category, double transactionAmount) {
        return strategy.calculateCashback(account, Category, transactionAmount);
    }
}
