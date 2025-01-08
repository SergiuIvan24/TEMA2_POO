package org.poo.Cashback;

import org.poo.entities.Account;
import org.poo.entities.User;

public interface CashbackStrategy {
    double calculateCashback(Account account, String Category, double transactionAmount);
}
