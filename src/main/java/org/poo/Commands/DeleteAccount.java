package org.poo.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account;
import org.poo.entities.Transaction;
import org.poo.entities.User;
import org.poo.entities.UserRepo;

public final class DeleteAccount implements Command {
    private final String accountIBAN;
    private final int timestamp;
    private final String email;
    private final UserRepo userRepo;

    public DeleteAccount(final UserRepo userRepo, final String email,
                         final String accountIBAN, final int timestamp) {
        this.userRepo = userRepo;
        this.email = email;
        this.accountIBAN = accountIBAN;
        this.timestamp = timestamp;
    }

    @Override
    public void execute(final ArrayNode output) {
        ObjectMapper objectMapper = new ObjectMapper();

        User user = userRepo.getUser(email);
        Account account = user.getAccount(accountIBAN);

        if (account.getBalance() != 0) {
            Transaction transaction = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription("Account couldn't be deleted - there are funds remaining")
                    .build();
            account.addTransaction(transaction);
        }
        boolean deleted = userRepo.deleteAccount(email, accountIBAN);
        ObjectNode resultNode = objectMapper.createObjectNode();
        resultNode.put("command", "deleteAccount");
        if (deleted) {
            ObjectNode successNode = objectMapper.createObjectNode();
            successNode.put("success", "Account deleted");
            successNode.put("timestamp", timestamp);
            resultNode.set("output", successNode);
        } else {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error",
                    "Account couldn't be deleted - see org.poo.transactions for details");
            errorNode.put("timestamp", timestamp);
            resultNode.set("output", errorNode);
        }
        resultNode.put("timestamp", timestamp);
        output.add(resultNode);
    }
}
