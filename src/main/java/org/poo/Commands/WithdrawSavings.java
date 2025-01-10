package org.poo.Commands;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.entities.Account;
import org.poo.entities.Transaction;
import org.poo.entities.User;
import org.poo.entities.UserRepo;

import java.time.LocalDate;
import java.time.Period;

public final class WithdrawSavings implements Command {
    private static final int MINIMUM_AGE = 21;
    private String accountIban;
    private double amount;
    private String currency;
    private final int timestamp;
    private UserRepo userRepo;

    public WithdrawSavings(final String accountIban, final double amount,
                           final String currency, final int timestamp,
                           final UserRepo userRepo) {
        this.accountIban = accountIban;
        this.amount = amount;
        this.currency = currency;
        this.timestamp = timestamp;
        this.userRepo = userRepo;
    }

    @Override
    public void execute(final ArrayNode output) {
        User user = userRepo.getUserByIBAN(accountIban);
        if (user == null) {
            return;
        }

        Account savingsAccount = user.getAccount(accountIban);
        if (savingsAccount == null) {
            return;
        }

        if (!savingsAccount.getAccountType().equals("savings")) {
            Transaction errorTransaction = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription("Account is not of type savings.")
                    .build();
            savingsAccount.addTransaction(errorTransaction);
            return;
        }

        if (!hasMinimumAge(user)) {
            Transaction errorTransaction = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription("You don't have the minimum age required.")
                    .build();
            savingsAccount.addTransaction(errorTransaction);
            return;
        }

        Account classicAccount = findClassicAccount(user, currency);
        if (classicAccount == null) {
            Transaction errorTransaction = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription("You do not have a classic account.")
                    .build();
            savingsAccount.addTransaction(errorTransaction);
            return;
        }

        double exchangeRate = userRepo.getExchangeRate(savingsAccount.getCurrency(), currency);
        if (exchangeRate == -1.0) {
            Transaction errorTransaction = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription("Exchange rate not found.")
                    .build();
            savingsAccount.addTransaction(errorTransaction);
            return;
        }

        double amountInSavingsCurrency = amount
                * userRepo.getExchangeRate(currency, savingsAccount.getCurrency());
        double amountInRON = amount * userRepo.getExchangeRate(currency, "RON");
        double totalAmountWithCommission = amountInSavingsCurrency;

        if (savingsAccount.getBalance() < totalAmountWithCommission) {
            Transaction errorTransaction = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription("Insufficient funds")
                    .build();
            savingsAccount.addTransaction(errorTransaction);
            return;
        }

        savingsAccount.setBalance(savingsAccount.getBalance() - totalAmountWithCommission);
        classicAccount.setBalance(classicAccount.getBalance() + amount);

        Transaction successTransaction = new Transaction.Builder()
                .setTimestamp(timestamp)
                .setDescription("Savings withdrawal")
                .setSavingsAccountIBAN(savingsAccount.getIban())
                .setClassicAccountIBAN(classicAccount.getIban())
                .setAmount(amount)
                .build();

        savingsAccount.addTransaction(successTransaction);
        classicAccount.addTransaction(successTransaction);
    }

    private boolean hasMinimumAge(final User user) {
        LocalDate birth = LocalDate.parse(user.getBirthDate());
        LocalDate now = LocalDate.now();
        return Period.between(birth, now).getYears() >= MINIMUM_AGE;
    }

    private Account findClassicAccount(final User user, final String currency) {
        for (Account account : user.getAccounts()) {
            if (account.getAccountType().equals("classic")
                    && account.getCurrency().equals(currency)) {
                return account;
            }
        }
        return null;
    }

}
