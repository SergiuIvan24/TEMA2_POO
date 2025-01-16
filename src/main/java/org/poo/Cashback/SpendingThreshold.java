package org.poo.Cashback;

import org.poo.entities.Account;
import org.poo.entities.UserRepo;


public final class SpendingThreshold implements CashbackStrategy {
    private final int amount1 = 500;
    private final int amount2 = 300;
    private final int amount3 = 100;
    private final double rate1ForGold = 0.007;
    private final double rate2ForGold = 0.0055;
    private final double rate3ForGold = 0.005;
    private final double rate1ForSilver = 0.005;
    private final double rate2ForSilver = 0.004;
    private final double rate3ForSilver = 0.003;
    private final double rate1ForStandard = 0.0025;
    private final double rate2ForStandard = 0.002;
    private final double rate3ForStandard = 0.001;
    @Override
    public double calculateCashback(final Account account,
                                    final String category,
                                    final double transactionAmount,
                                    final UserRepo userRepo) {
        double totalSpentForThisMerchant = account.getTotalSpendingThreshold();

        String plan = account.getUserRepo().getUser(account.getEmail()).getServicePlan();

        double rate = 0.0;

        if (totalSpentForThisMerchant >= amount1) {
            rate = getRate(plan, rate1ForStandard, rate1ForSilver, rate1ForGold);
        } else if (totalSpentForThisMerchant >= amount2) {
            rate = getRate(plan, rate2ForStandard, rate2ForSilver, rate2ForGold);
        } else if (totalSpentForThisMerchant >= amount3) {
            rate = getRate(plan, rate3ForStandard, rate3ForSilver, rate3ForGold);
        }
        return transactionAmount * rate;
    }

    private double getRate(final String plan, final double standardRate,
                           final double silverRate,
                           final double goldRate) {
        return switch (plan) {
            case "standard", "student" -> standardRate;
            case "silver" -> silverRate;
            case "gold" -> goldRate;
            default -> 0.0;
        };
    }
}

