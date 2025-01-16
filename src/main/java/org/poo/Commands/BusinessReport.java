package org.poo.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account;
import org.poo.entities.BusinessAccount;
import org.poo.entities.Transaction;
import org.poo.entities.User;
import org.poo.entities.UserRepo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class BusinessReport implements Command {
    private String type;
    private int startTimestamp;
    private int endTimestamp;
    private String accountIban;
    private final int timestamp;
    private UserRepo userRepo;

    public BusinessReport(final String type, final int startTimestamp, final int endTimestamp,
                          final String accountIban, final int timestamp, final UserRepo userRepo) {
        this.type = type;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.accountIban = accountIban;
        this.timestamp = timestamp;
        this.userRepo = userRepo;
    }

    @Override
    public void execute(final ArrayNode output) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode resultNode = objectMapper.createObjectNode();
        resultNode.put("command", "businessReport");

        Account account = userRepo.getAccountByIBAN(accountIban);
        if (account == null) {
            resultNode.put("output", "Account not found");
            resultNode.put("timestamp", timestamp);
            output.add(resultNode);
            return;
        }

        if (!account.getAccountType().equals("business")) {
            resultNode.put("output", "Account is not of type business");
            resultNode.put("timestamp", timestamp);
            output.add(resultNode);
            return;
        }

        BusinessAccount businessAccount = (BusinessAccount) account;

        ObjectNode accountDetails = objectMapper.createObjectNode();
        accountDetails.put("IBAN", businessAccount.getIban());
        accountDetails.put("balance", businessAccount.getBalance());
        accountDetails.put("currency", businessAccount.getCurrency());
        accountDetails.put("spending limit", businessAccount.getSpendingLimit());
        accountDetails.put("deposit limit", businessAccount.getDepositLimit());

        if (type.equals("transaction")) {
            generateTransactionReport(businessAccount, accountDetails);
        } else if (type.equals("commerciant")) {
            generateCommerciantReport(businessAccount, accountDetails);
        } else {
            accountDetails.put("error", "Invalid report type");
        }

        resultNode.set("output", accountDetails);
        resultNode.put("timestamp", timestamp);
        output.add(resultNode);
    }

    private void generateTransactionReport(final BusinessAccount businessAccount,
                                           final ObjectNode accountDetails) {
        ArrayNode managersNode = new ObjectMapper().createArrayNode();
        ArrayNode employeesNode = new ObjectMapper().createArrayNode();

        double totalSpent = 0;
        double totalDeposited = 0;
        for (User employee : businessAccount.getEmployees()) {
            double spent = 0;
            double deposited = 0;
            for (Transaction transaction : businessAccount.getTransactions()) {
                if (transaction.getTimestamp() < startTimestamp
                        || transaction.getTimestamp() > endTimestamp) {
                    continue;
                }
                if (transaction.getInitiator() == employee) {
                    if ("sent".equals(transaction.getTransferType())) {
                        spent += transaction.getAmount();
                    }
                    if ("received".equals(transaction.getTransferType())) {
                        deposited += transaction.getAmount();
                    }
                    if (transaction.getDescription().equals("Card payment")) {
                        spent += transaction.getAmount();
                    }
                    if (transaction.getDescription().equals("Add funds")) {
                        deposited += transaction.getAmount();
                    }
                }
            }
            totalSpent += spent;
            totalDeposited += deposited;
            ObjectNode employeeNode = new ObjectMapper().createObjectNode();
            employeeNode.put("spent", spent);
            employeeNode.put("deposited", deposited);
            employeeNode.put("username", employee.getFullName());
            employeesNode.add(employeeNode);
        }
        for (User manager : businessAccount.getManagers()) {
            double spent = 0;
            double deposited = 0;
            for (Transaction transaction : businessAccount.getTransactions()) {
                if (transaction.getTimestamp() < startTimestamp
                        || transaction.getTimestamp() > endTimestamp) {
                    continue;
                }
                if (transaction.getInitiator() == manager) {
                    if ("sent".equals(transaction.getTransferType())) {
                        spent += transaction.getAmount();
                    }
                    if ("received".equals(transaction.getTransferType())) {
                        deposited += transaction.getAmount();
                    }
                    if (transaction.getDescription().equals("Card payment")) {
                        spent += transaction.getAmount();
                    }
                    if (transaction.getDescription().equals("Add funds")) {
                        deposited += transaction.getAmount();
                    }
                }
            }
            totalSpent += spent;
            totalDeposited += deposited;
            ObjectNode managerNode = new ObjectMapper().createObjectNode();
            managerNode.put("spent", spent);
            managerNode.put("deposited", deposited);
            managerNode.put("username", manager.getFullName());
            managersNode.add(managerNode);
        }

        accountDetails.set("managers", managersNode);
        accountDetails.set("employees", employeesNode);
        accountDetails.put("total spent", totalSpent);
        accountDetails.put("total deposited", totalDeposited);
        accountDetails.put("statistics type", "transaction");
    }

    private void generateCommerciantReport(final BusinessAccount businessAccount,
                                           final ObjectNode accountDetails) {
        ArrayNode commerciantsNode = new ObjectMapper().createArrayNode();
        Map<String, CommerciantData> commerciants = new HashMap<>();

        for (Transaction transaction : businessAccount.getTransactions()) {
            if (transaction.getTimestamp() < startTimestamp
                    || transaction.getTimestamp() > endTimestamp) {
                continue;
            }

            String commerciant = transaction.getCommerciant();
            if (commerciant == null) {
                continue;
            }

            CommerciantData data = commerciants.computeIfAbsent(commerciant,
                    k -> new CommerciantData());
            if (businessAccount.getEmployees().contains(transaction.getInitiator())
                    || businessAccount.getManagers().contains(transaction.getInitiator())) {
                data.totalReceived += transaction.getAmount();
            }

            User initiator = transaction.getInitiator();
            if (initiator != null) {
                if (businessAccount.getManagers().contains(initiator)) {
                    data.managers.add(initiator.getFullName());
                } else if (businessAccount.getEmployees().contains(initiator)) {
                    data.employees.add(initiator.getFullName());
                }
            }
        }

        List<Map.Entry<String, CommerciantData>> sortedCommerciants =
                new ArrayList<>(commerciants.entrySet());

        sortedCommerciants.sort(Map.Entry.comparingByKey());

        for (Map.Entry<String, CommerciantData> entry : sortedCommerciants) {
            ObjectNode commerciantNode = new ObjectMapper().createObjectNode();
            commerciantNode.put("commerciant", entry.getKey());
            commerciantNode.put("total received", entry.getValue().getTotalReceived());

            commerciantNode.set("managers", new ObjectMapper()
                    .valueToTree(entry.getValue().getManagers()));
            commerciantNode.set("employees", new ObjectMapper()
                    .valueToTree(entry.getValue().getEmployees()));

            commerciantsNode.add(commerciantNode);
        }

        accountDetails.set("commerciants", commerciantsNode);
        accountDetails.put("statistics type", "commerciant");
    }

    private static final class CommerciantData {
        private double totalReceived = 0;
        private final List<String> managers = new ArrayList<>();
        private final List<String> employees = new ArrayList<>();

        public double getTotalReceived() {
            return totalReceived;
        }

        public List<String> getManagers() {
            return managers;
        }

        public List<String> getEmployees() {
            return employees;
        }
    }
}
