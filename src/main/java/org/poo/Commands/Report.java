package org.poo.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account;
import org.poo.entities.Transaction;
import org.poo.entities.UserRepo;

public final class Report implements Command {
    private final int startTimestamp;
    private final int endTimestamp;
    private final String accountIBAN;
    private final int timestamp;
    private final UserRepo userRepo;

    public Report(final int startTimestamp, final int endTimestamp,
                  final String accountIBAN, final UserRepo userRepo,
                  final int timestamp) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.accountIBAN = accountIBAN;
        this.userRepo = userRepo;
        this.timestamp = timestamp;
    }

    @Override
    public void execute(final ArrayNode output) {
        Account account = userRepo.getAccountByIBAN(accountIBAN);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode result = objectMapper.createObjectNode();

        result.put("command", "report");
        result.put("timestamp", timestamp);

        if (account == null) {
            ObjectNode errorOutput = objectMapper.createObjectNode();
            errorOutput.put("timestamp", timestamp);
            errorOutput.put("description", "Account not found");
            result.set("output", errorOutput);
            output.add(result);
            return;
        }

        ObjectNode accountDetails = objectMapper.createObjectNode();
        accountDetails.put("IBAN", account.getIban());
        accountDetails.put("balance", account.getBalance());
        accountDetails.put("currency", account.getCurrency());

        ArrayNode transactionsArray = objectMapper.createArrayNode();
        for (Transaction transaction : account.getTransactions()) {
            if (transaction.getTimestamp() >= startTimestamp
                    && transaction.getTimestamp() <= endTimestamp) {
                transactionsArray.add(createReportOutput(transaction, objectMapper));
            }
        }

        accountDetails.set("transactions", transactionsArray);

        result.set("output", accountDetails);
        output.add(result);
    }

    private ObjectNode createReportOutput(final Transaction transaction,
                                          final ObjectMapper objectMapper) {
        ObjectNode transactionNode = objectMapper.createObjectNode();
        transactionNode.put("timestamp", transaction.getTimestamp());
        transactionNode.put("description", transaction.getDescription());

        if (transaction.getAmount() != -1) {
            transactionNode.put("amount", transaction.getAmount());
        }
        if (transaction.getAmountPlusCurrency() != null) {
            transactionNode.put("amount", transaction.getAmountPlusCurrency());
        }
        if (transaction.getCurrency() != null) {
            transactionNode.put("currency", transaction.getCurrency());
        }
        if (transaction.getSenderIBAN() != null) {
            transactionNode.put("senderIBAN", transaction.getSenderIBAN());
        }
        if (transaction.getReceiverIBAN() != null) {
            transactionNode.put("receiverIBAN", transaction.getReceiverIBAN());
        }
        if (transaction.getCommerciant() != null) {
            transactionNode.put("commerciant", transaction.getCommerciant());
        }
        if (transaction.getCard() != null) {
            transactionNode.put("card", transaction.getCard());
        }
        if (transaction.getCardHolder() != null) {
            transactionNode.put("cardHolder", transaction.getCardHolder());
        }
        if (transaction.getAccount() != null) {
            transactionNode.put("account", transaction.getAccount());
        }
        if (transaction.getTransferType() != null) {
            transactionNode.put("transferType", transaction.getTransferType());
        }
        if (transaction.getError() != null && !transaction.getError().isEmpty()) {
            transactionNode.put("error", transaction.getError());
        }
        if (transaction.getInvolvedAccounts() != null && !transaction
                .getInvolvedAccounts().isEmpty()) {
            ArrayNode involvedAccountsNode = objectMapper.createArrayNode();
            for (String iban : transaction.getInvolvedAccounts()) {
                involvedAccountsNode.add(iban);
            }
            transactionNode.set("involvedAccounts", involvedAccountsNode);
        }
        return transactionNode;
    }
}
