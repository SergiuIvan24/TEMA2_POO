package org.poo.Cashback;

import org.poo.entities.Account;
import org.poo.entities.UserRepo;

public interface CashbackStrategy {
    /**
     * Calculeaza cashbackul in functie de strategia specifica
     * @param account
     * @param category
     * @param transactionAmount
     * @param userRepo
     * @return
     */
    double calculateCashback(Account account,
                             String category,
                             double transactionAmount,
                             UserRepo userRepo);
}
