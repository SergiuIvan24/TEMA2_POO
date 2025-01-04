package org.poo.Commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.entities.UserRepo;

public final class SetMinimumBalance implements Command {
    private String accountIBAN;
    private double minimumBalance;
    private UserRepo userRepo;
    private final int timestamp;

    public SetMinimumBalance(final String accountIBAN, final double minimumBalance,
                             final UserRepo userRepo, final int timestamp) {
        this.accountIBAN = accountIBAN;
        this.minimumBalance = minimumBalance;
        this.userRepo = userRepo;
        this.timestamp = timestamp;
    }

    @Override
    public void execute(final ArrayNode output) {
        User user = userRepo.getUserByIBAN(accountIBAN);
        Account account = user.getAccount(accountIBAN);
        account.setMinimumBalance(minimumBalance);
    }
}
