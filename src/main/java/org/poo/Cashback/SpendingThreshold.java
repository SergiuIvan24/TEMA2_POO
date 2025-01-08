package org.poo.Cashback;

import org.poo.entities.Account;


public class SpendingThreshold implements CashbackStrategy {
    @Override
    public double calculateCashback(Account account, String category, double transactionAmount) {
        double totalSpentForThisMerchant = account.getSpentForMerchant(category);

        String plan = account.getUserRepo().getUser(account.getEmail()).getServicePlan();

        double rate = 0.0;

        if (totalSpentForThisMerchant >= 500
                && !account.hasReceivedCashback(category + "_500RON")) {
            rate = getRate(plan, 0.0025, 0.005, 0.007);
            account.markCashbackReceived(category + "_500RON");
        } else if (totalSpentForThisMerchant >= 300
                && !account.hasReceivedCashback(category + "_300RON")) {
            rate = getRate(plan, 0.002, 0.004, 0.0055);
            account.markCashbackReceived(category + "_300RON");
        } else if (totalSpentForThisMerchant >= 100
                && !account.hasReceivedCashback(category + "_100RON")) {
            rate = getRate(plan, 0.001, 0.003, 0.005);
            account.markCashbackReceived(category + "_100RON");
        }

        return transactionAmount * rate;
    }

    private double getRate(String plan, double standardRate, double silverRate, double goldRate) {
        return switch (plan) {
            case "standard", "student" -> standardRate;
            case "silver" -> silverRate;
            case "gold" -> goldRate;
            default -> 0.0;
        };
    }
}

