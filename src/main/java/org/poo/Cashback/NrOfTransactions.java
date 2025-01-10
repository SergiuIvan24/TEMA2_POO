package org.poo.Cashback;

import org.poo.entities.Account;
import org.poo.entities.UserRepo;

public final class NrOfTransactions implements CashbackStrategy {
    private final double rate1 = 0.02;
    private final double rate2 = 0.05;
    private final double rate3 = 0.10;
    private final int transactionCount1 = 2;
    private final int transactionCount2 = 5;
    private final int transactionCount3 = 10;

    /**
     * Calculeaza cashbackul
     * @param account
     * @param category
     * @param transactionAmount
     * @param userRepo
     * @return
     */
    public double calculateCashback(final Account account,
                                    final String category,
                                    final double transactionAmount,
                                    final UserRepo userRepo) {
        account.incrementNrOfTransactions(category);
        int transactionCount = account.getNrOfTransactions(category);
        double cashback = 0.0;
        if (transactionCount == transactionCount1 && !account.hasReceivedCashback(category)) {
            account.addPendingCategoryDiscount("Food", rate1);
            account.markCashbackReceived("Food");
        } else if (transactionCount == transactionCount2
                && !account.hasReceivedCashback(category)) {
            account.addPendingCategoryDiscount("Clothes", rate2);
            account.markCashbackReceived("Clothes");
        } else if (transactionCount == transactionCount3
                && !account.hasReceivedCashback(category)) {
            account.addPendingCategoryDiscount("Tech", rate3);
            account.markCashbackReceived("Tech");
        }
        return cashback;
    }
}
