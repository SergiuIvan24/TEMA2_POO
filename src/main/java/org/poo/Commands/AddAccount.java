package org.poo.Commands;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.entities.Account;
import org.poo.entities.BusinessAccount;
import org.poo.entities.Transaction;
import org.poo.entities.User;
import org.poo.entities.UserRepo;
import org.poo.entities.ClassicAccount;
import org.poo.entities.SavingsAccount;


import org.poo.utils.Utils;

class AddAccount implements Command {
    private final UserRepo userRepo;
    private final String email;
    private final String currency;
    private final String accountType;
    private final int timestamp;
    private final Double interestRate;

    AddAccount(final UserRepo userRepo, final String email, final String currency,
               final String accountType, final int timestamp, final Double interestRate) {
        this.userRepo = userRepo;
        this.email = email;
        this.currency = currency;
        this.accountType = accountType;
        this.timestamp = timestamp;
        this.interestRate = interestRate;
    }

    @Override
    public void execute(final ArrayNode output) {
        User user = userRepo.getUser(email);
        Account newAccount;

        if (accountType.equalsIgnoreCase("savings")) {
            newAccount = new SavingsAccount(
                    Utils.generateIBAN(),
                    currency,
                    0,
                    "savings",
                    interestRate,
                    email,
                    userRepo
            );
        } else if (accountType.equalsIgnoreCase("classic")) {
            newAccount = new ClassicAccount(
                    Utils.generateIBAN(),
                    currency,
                    0,
                    "classic",
                    email,
                    userRepo
            );
        } else if (accountType.equalsIgnoreCase("business")) {
            newAccount = new BusinessAccount(
                    Utils.generateIBAN(),
                    currency,
                    0,
                    userRepo.getUser(email),
                    "business",
                    userRepo
            );
        } else {
            return;
        }

        user.addAccount(newAccount);

        Transaction transaction = new Transaction.Builder()
                .setTimestamp(timestamp)
                .setDescription("New account created")
                .build();

        newAccount.addTransaction(transaction);
    }
}

