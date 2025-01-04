package org.poo.Cashback;

import org.poo.entities.User;

public class NrOfTransactions implements CashbackStrategy {
    @Override
    public double calculateCashback(User user, String Category, double transactionAmount) {
        int transactionCount = user.getNrOfTransactions(Category);

        if (Category.equals("Food") && transactionCount >= 2) {
            return transactionAmount * 0.02;
        } else if (Category.equals("Clothes") && transactionCount >= 5) {
            return transactionAmount * 0.05;
        } else if (Category.equals("Tech") && transactionCount >= 10) {
            return transactionAmount * 0.10;
        }
        return 0.0;
    }
}
