package org.poo.Commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.entities.UserRepo;

public final class SetAlias implements Command {
    private String email;
    private String accountIBAN;
    private String alias;
    private final int timestamp;
    private UserRepo userRepo;

    public SetAlias(final String email, final String accountIBAN,
                    final String alias, final int timestamp, final UserRepo userRepo) {
        this.email = email;
        this.accountIBAN = accountIBAN;
        this.alias = alias;
        this.timestamp = timestamp;
        this.userRepo = userRepo;
    }

    @Override
    public void execute(final ArrayNode output) {
        userRepo.getUser(email).setAlias(alias, accountIBAN);
    }
}
