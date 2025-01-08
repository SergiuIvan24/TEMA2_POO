package org.poo.Cashback;

import org.poo.entities.Account;

public class NrOfTransactions implements CashbackStrategy {
    public double calculateCashback(Account account, String category, double transactionAmount) {
        if (account.hasReceivedCashback(category)) {
            return transactionAmount * getCategoryRate(category);
        }

        int transactionCount = account.getNrOfTransactions(category);
        double cashback = 0.0;

        if (category.equals("Food") && transactionCount >= 2) {
            cashback = transactionAmount * 0.02;
        } else if (category.equals("Clothes") && transactionCount >= 5) {
            cashback = transactionAmount * 0.05;
        } else if (category.equals("Tech") && transactionCount >= 10) {
            cashback = transactionAmount * 0.10;
        }

        if (cashback > 0.0) {
            account.markCashbackReceived(category);
        }
        return cashback;
    }

    private double getCategoryRate(String category) {
        return switch (category) {
            case "Food" -> 0.02;
            case "Clothes" -> 0.05;
            case "Tech" -> 0.10;
            default -> 0.0;
        };
    }

}
