package org.poo.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account;
import org.poo.entities.BusinessAccount;
import org.poo.entities.User;
import org.poo.entities.UserRepo;

public class ChangeSpendingLimit implements Command {
    private String email;
    private String accountIban;
    private double amount;
    private final int timestamp;
    private UserRepo userRepo;

    public ChangeSpendingLimit(final String email, final String accountIban, final double amount, final int timestamp, final UserRepo userRepo) {
        this.email = email;
        this.accountIban = accountIban;
        this.amount = amount;
        this.timestamp = timestamp;
        this.userRepo = userRepo;
    }

    @Override
    public void execute(ArrayNode output) {
        User user = userRepo.getUser(email);
        BusinessAccount account = (BusinessAccount)user.getAccount(accountIban);
        if(account.getOwner() != user) {
            ObjectNode errorOutput = new ObjectMapper().createObjectNode();
            errorOutput.put("description", "You must be owner in order to change spending limit.");
            errorOutput.put("timestamp", timestamp);

            ObjectNode commandOutput = new ObjectMapper().createObjectNode();
            commandOutput.put("command", "changeSpendingLimit");
            commandOutput.set("output", errorOutput);
            commandOutput.put("timestamp", timestamp);

            output.add(commandOutput);
            return;
        }
        if (account == null) {
            System.out.println("Business Account with IBAN " + accountIban + " not found");
            return;

        }
        account.setSpendingLimit(amount);
    }

}
