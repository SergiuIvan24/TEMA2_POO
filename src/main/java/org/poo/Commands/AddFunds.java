package org.poo.Commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.entities.Account;
import org.poo.entities.BusinessAccount;
import org.poo.entities.Transaction;
import org.poo.entities.User;
import org.poo.entities.UserRepo;

class AddFunds implements Command {
    private final String accountIBAN;
    private final double amount;
    private final UserRepo userRepo;
    private final int timestamp;
    private final String email;

    AddFunds(final String email, final String accountIBAN, final double amount,
             final int timestamp, final UserRepo userRepo) {
        this.accountIBAN = accountIBAN;
        this.amount = amount;
        this.userRepo = userRepo;
        this.timestamp = timestamp;
        this.email = email;
    }

    @Override
    public void execute(final ArrayNode output) {
        User user = userRepo.getUser(email);

        Account account = user.getAccount(accountIBAN);

        if (account == null) {
            return;

        }
        if (account.getAccountType().equals("business")) {
            if (((BusinessAccount) account).getEmployees().contains(user)) {
                if (amount > ((BusinessAccount) account).getDepositLimit()) {
                    return;
                }
            }
        }
            account.setBalance(account.getBalance() + amount);

            if (account.getAccountType().equals("business")) {
                Transaction transaction = new Transaction.Builder()
                        .setTimestamp(timestamp)
                        .setDescription("Add funds")
                        .setAmount(amount)
                        .setInitiator(user)
                        .build();
                account.addTransaction(transaction);
            }

    }
}
