package org.poo.Cashback;

import org.poo.entities.User;

public class SpendingThreshold implements CashbackStrategy {
    @Override
    public double calculateCashback(User user, String category, double transactionAmount) {
        double totalSpent = user.getTotalSpent();
        String plan = user.getServicePlan();
        double rate = 0.0;

        if (totalSpent >= 500) {
            rate = getRate(plan, 0.0025, 0.005, 0.007);
        } else if (totalSpent >= 300) {
            rate = getRate(plan, 0.002, 0.004, 0.0055);
        } else if (totalSpent >= 100) {
            rate = getRate(plan, 0.001, 0.003, 0.005);
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

