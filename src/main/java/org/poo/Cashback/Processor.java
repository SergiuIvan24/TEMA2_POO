package org.poo.Cashback;

import org.poo.entities.Account;
import org.poo.entities.UserRepo;

public final class Processor {
    private CashbackStrategy strategy;

    public Processor(final CashbackStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Calculeaza cashbackul in functie de strategia specifica
     * @param account
     * @param category
     * @param transactionAmount
     * @param userRepo
     * @return
     */
    public double processCashback(final Account account, final String category,
                                  final double transactionAmount,
                                  final UserRepo userRepo) {
        return strategy.calculateCashback(account, category,
                transactionAmount, userRepo);
    }
}
