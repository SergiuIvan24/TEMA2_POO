package org.poo.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account;
import org.poo.entities.Transaction;
import org.poo.entities.User;
import org.poo.entities.UserRepo;

public final class ChangeInterestRate implements Command {
    private String accountIBAN;
    private double interestRate;
    private final int timestamp;
    private UserRepo userRepo;

    public ChangeInterestRate(final String accountIBAN, final double interestRate,
                              final int timestamp, final UserRepo userRepo) {
        this.accountIBAN = accountIBAN;
        this.interestRate = interestRate;
        this.timestamp = timestamp;
        this.userRepo = userRepo;
    }

    @Override
    public void execute(final ArrayNode output) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode result = objectMapper.createObjectNode();
        result.put("command", "changeInterestRate");
        result.put("timestamp", timestamp);
        User user = userRepo.getUserByIBAN(accountIBAN);

        Account account = user.getAccount(accountIBAN);

        if (!account.getAccountType().equals("savings")) {
            ObjectNode errorOutput = objectMapper.createObjectNode();
            errorOutput.put("description", "This is not a savings account");
            errorOutput.put("timestamp", timestamp);
            result.set("output", errorOutput);
            output.add(result);
            return;
        }
            account.setInterestRate(interestRate);
            Transaction transaction = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription(String.format(
                            "Interest rate of the account changed to %.2f",
                            interestRate))
                    .build();
            account.addTransaction(transaction);

    }
}
