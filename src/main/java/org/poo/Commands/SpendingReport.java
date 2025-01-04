package org.poo.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account;
import org.poo.entities.Transaction;
import org.poo.entities.UserRepo;

import java.util.*;

public final class SpendingReport implements Command {
    private int startTimestamp;
    private int endTimestamp;
    private String accountIBAN;
    private final int timestamp;
    private UserRepo userRepo;

    public SpendingReport(final int startTimestamp, final int endTimestamp,
                          final String accountIBAN, final int timestamp,
                          final UserRepo userRepo) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.accountIBAN = accountIBAN;
        this.timestamp = timestamp;
        this.userRepo = userRepo;
    }

    @Override
    public void execute(final ArrayNode output) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode outputNode = objectMapper.createObjectNode();
        outputNode.put("command", "spendingsReport");
        outputNode.put("timestamp", timestamp);

        Account account = userRepo.getAccountByIBAN(accountIBAN);

        if (account != null && account.getAccountType().equals("savings")) {
            ObjectNode errorOutput = objectMapper.createObjectNode();
            errorOutput.put("error", "This kind of report is not supported for a saving account");
            outputNode.set("output", errorOutput);
            outputNode.put("timestamp", timestamp);
            output.add(outputNode);
            return;
        }

        if (account == null) {
            ObjectNode errorOutput = objectMapper.createObjectNode();
            errorOutput.put("description", "Account not found");
            errorOutput.put("timestamp", timestamp);
            outputNode.set("output", errorOutput);
            output.add(outputNode);
            return;
        }

        List<Transaction> wantedTransactions = new ArrayList<>();
        for (Transaction transaction : account.getTransactions()) {
            int transactionTimestamp = transaction.getTimestamp();
            if (transactionTimestamp >= startTimestamp && transactionTimestamp <= endTimestamp) {
                if (transaction.getCommerciant() != null) {
                    wantedTransactions.add(transaction);
                }
            }
        }

        wantedTransactions.sort(Comparator.comparing(Transaction::getTimestamp));

        Map<String, Double> commerciants = new TreeMap<>();
        ArrayNode transactionsArray = objectMapper.createArrayNode();
        for (Transaction transaction : wantedTransactions) {
            if (transaction.getCurrency() == null || account.getCurrency() == null
                    || transaction.getCurrency().equals(account.getCurrency())) {
                commerciants.merge(transaction.getCommerciant(), transaction.getAmount(),
                        Double::sum);
                transactionsArray.add(createTransactionNode(transaction, account.getCurrency(),
                        transaction.getAmount()));
                continue;
            }

            double exchangeRate = userRepo.getExchangeRate(transaction.getCurrency(),
                    account.getCurrency());

            if (exchangeRate != -1) {
                double convertedAmount = transaction.getAmount() * exchangeRate;
                commerciants.merge(transaction.getCommerciant(), convertedAmount, Double::sum);
                transactionsArray.add(createTransactionNode(transaction, account.getCurrency(),
                        convertedAmount));
            }

        }

        ObjectNode reportNode = objectMapper.createObjectNode();
        reportNode.put("IBAN", account.getIban());
        reportNode.put("balance", account.getBalance());
        reportNode.put("currency", account.getCurrency());
        reportNode.set("transactions", transactionsArray);

        ArrayNode commerciantsArray = objectMapper.createArrayNode();
        for (Map.Entry<String, Double> entry : commerciants.entrySet()) {
            ObjectNode commerciantNode = objectMapper.createObjectNode();
            commerciantNode.put("commerciant", entry.getKey());
            commerciantNode.put("total", entry.getValue());
            commerciantsArray.add(commerciantNode);
        }
        reportNode.set("commerciants", commerciantsArray);

        outputNode.set("output", reportNode);
        output.add(outputNode);
    }

    private ObjectNode createTransactionNode(final Transaction transaction,
                                             final String accountCurrency, final double amount) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode transactionNode = mapper.createObjectNode();
        transactionNode.put("timestamp", transaction.getTimestamp());
        transactionNode.put("description", transaction.getDescription());
        transactionNode.put("amount", amount);
        transactionNode.put("commerciant", transaction.getCommerciant());

        return transactionNode;
    }

}
