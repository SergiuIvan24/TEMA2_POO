package org.poo.Cashback;

import org.poo.entities.User;

public interface CashbackStrategy {
    double calculateCashback(User user, String Category, double transactionAmount);
}
