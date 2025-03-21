package org.poo.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account;
import org.poo.entities.SplitPaymentManager;
import org.poo.entities.Transaction;
import org.poo.entities.User;
import org.poo.entities.UserRepo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SplitPayment implements Command {
    private final SplitPaymentManager manager;

    private final String splitPaymentType;
    private final List<String> accountsIban;
    private final double amount;
    private final List<Double> amountForUsers;
    private final String currency;
    private final int timestamp;
    private final UserRepo userRepo;

    private final Map<String, List<Integer>> userIndexMap = new HashMap<>();
    private final Map<String, Map<Integer, Boolean>> responses = new HashMap<>();

    private List<Double> splittedAmounts;

    public SplitPayment(final SplitPaymentManager manager,
                        final String splitPaymentType,
                        final List<String> accountsIban,
                        final double amount,
                        final List<Double> amountForUsers,
                        final String currency,
                        final int timestamp,
                        final UserRepo userRepo) {
        this.manager = manager;
        this.splitPaymentType = splitPaymentType;
        this.accountsIban = accountsIban;
        this.amount = amount;
        this.amountForUsers = amountForUsers;
        this.currency = currency;
        this.timestamp = timestamp;
        this.userRepo = userRepo;

        populateUserIndexMap();
    }

    private void populateUserIndexMap() {
        for (int i = 0; i < accountsIban.size(); i++) {
            String iban = accountsIban.get(i);
            User user = userRepo.getUserByIBAN(iban);
            if (user != null) {
                String email = user.getEmail();
                userIndexMap.putIfAbsent(email, new ArrayList<>());
                userIndexMap.get(email).add(i);
                responses.putIfAbsent(email, new HashMap<>());
                responses.get(email).put(this.timestamp, null);
            }
        }
    }

    @Override
    public void execute(final ArrayNode output) {
        manager.addSplitPayment(this);

        if (manager.getCurrentSplitPayment() != this) {
            return;
        }
        splittedAmounts = buildSplitAmounts();

        if (splittedAmounts == null) {
            manager.removeSplitPayment(this);
            processNextPayment(output);
            return;
        }

        for (int i = 0; i < accountsIban.size(); i++) {
            String iban = accountsIban.get(i);
            Account acc = userRepo.getAccountByIBAN(iban);
            if (acc == null) {
                String msg = "Account " + iban + " not found in userRepo";
                output.add(createOutputNode(msg));

                Transaction errTx = new Transaction.Builder()
                        .setTimestamp(timestamp)
                        .setDescription(String.format("Split payment of %.2f %s",
                                getTotalSplit(), currency))
                        .setSplitPaymentType(splitPaymentType)
                        .setCurrency(currency)
                        .setInvolvedAccounts(accountsIban)
                        .setError(msg)
                        .build();

                for (String ib2 : accountsIban) {
                    Account ac2 = userRepo.getAccountByIBAN(ib2);
                    if (ac2 != null) {
                        ac2.addTransaction(errTx);
                    }
                }
                manager.removeSplitPayment(this);
                processNextPayment(output);
                return;
            }

            User user = userRepo.getUserByIBAN(iban);
            if (user == null) {
                String msg = "No user found owning IBAN " + iban;
                Transaction errTx = new Transaction.Builder()
                        .setTimestamp(timestamp)
                        .setDescription(String.format("Split payment of %.2f %s",
                                getTotalSplit(), currency))
                        .setSplitPaymentType(splitPaymentType)
                        .setCurrency(currency)
                        .setInvolvedAccounts(accountsIban)
                        .setError(msg)
                        .build();

                for (String ib2 : accountsIban) {
                    Account ac2 = userRepo.getAccountByIBAN(ib2);
                    if (ac2 != null) {
                        ac2.addTransaction(errTx);
                    }
                }
                manager.removeSplitPayment(this);
                processNextPayment(output);
                return;
            }

            String email = user.getEmail();
            userIndexMap.putIfAbsent(email, new ArrayList<>());
            userIndexMap.get(email).add(i);
        }
    }

    private SplitPayment findRelevantSplitPayment(final String email, final String paymentType) {
        for (SplitPayment split : manager.getSplitPayments()) {
            if (split.splitPaymentType.equalsIgnoreCase(paymentType)) {
                if (split.userIndexMap.containsKey(email)) {
                    return split;
                }
            }
        }
        return null;
    }

    /**
     * Handles the acceptance of a split payment.
     *
     * @param email the email of the user
     * @param accepted whether the payment is accepted
     * @param output the output array node
     * @param paymentType the type of the split payment
     */
    public void acceptance(final String email, final boolean accepted,
                           final ArrayNode output,
                           final String paymentType) {
        SplitPayment targetPayment = findRelevantSplitPayment(email, paymentType);

        if (targetPayment == null || !targetPayment.userIndexMap.containsKey(email)) {
            return;
        }

        if (!accepted) {
            targetPayment.createRejectedTransaction(output,
                    "One user rejected the payment.");
            manager.removeSplitPayment(targetPayment);
            targetPayment.processNextPayment(output);
            return;
        }

        List<Integer> indices = targetPayment.userIndexMap.get(email);
        if (indices != null) {
            for (int index : indices) {
                String iban = targetPayment.accountsIban.get(index);
                Account acc = userRepo.getAccountByIBAN(iban);

                if (acc == null) {
                    targetPayment.createRejectedTransaction(output,
                            "Missing account for IBAN: " + iban);
                    manager.removeSplitPayment(targetPayment);
                    targetPayment.processNextPayment(output);
                    return;
                }

                Map<Integer, Boolean> userResponses = targetPayment.responses.get(email);
                if (userResponses != null) {
                    userResponses.put(targetPayment.getTimestamp(), true);
                }
            }
        }
        if (allAccepted()) {
            doSuccessPayment(output);
            manager.removeSplitPayment(this);
            processNextPayment(output);
        }
    }

    private void createInsufficientFundsTransaction(final ArrayNode output,
                                                    final String insufficientIban) {
        String err = "Account " + insufficientIban
                + " has insufficient funds for a split payment.";
        double share = amount / accountsIban.size();
        if (splitPaymentType.equals("equal")) {
            Transaction errorTx = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription(String.format("Split payment of %.2f %s",
                            getTotalSplit(), currency))
                    .setSplitPaymentType(splitPaymentType)
                    .setCurrency(currency)
                    .setInvolvedAccounts(accountsIban)
                    .setError(err)
                    .setAmount(share)
                    .build();
            for (String ib : accountsIban) {
                Account ac = userRepo.getAccountByIBAN(ib);
                if (ac != null) {
                    ac.addTransaction(errorTx);
                }
            }
            return;
        }
        Transaction errorTx = new Transaction.Builder()
                .setTimestamp(timestamp)
                .setDescription(String.format("Split payment of %.2f %s",
                        getTotalSplit(), currency))
                .setSplitPaymentType(splitPaymentType)
                .setCurrency(currency)
                .setInvolvedAccounts(accountsIban)
                .setAmountForUsers(splittedAmounts)
                .setError(err)
                .build();

        for (String ib : accountsIban) {
            Account ac = userRepo.getAccountByIBAN(ib);
            if (ac != null) {
                ac.addTransaction(errorTx);
            }
        }
    }

    private void createRejectedTransaction(final ArrayNode output, final String errorMsg) {
        if (splitPaymentType.equals("equal")) {
            Transaction rejectTx = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription(String.format("Split payment of %.2f %s",
                            getTotalSplit(), currency))
                    .setSplitPaymentType(splitPaymentType)
                    .setCurrency(currency)
                    .setInvolvedAccounts(accountsIban)
                    .setError(errorMsg)
                    .setAmount(amount)
                    .build();
            for (String ib : accountsIban) {
                Account ac = userRepo.getAccountByIBAN(ib);
                if (ac != null) {
                    ac.addTransaction(rejectTx);
                }
            }
            return;
        }

        Transaction rejectTx = new Transaction.Builder()
                .setTimestamp(timestamp)
                .setDescription(String.format("Split payment of %.2f %s",
                        getTotalSplit(), currency))
                .setSplitPaymentType(splitPaymentType)
                .setCurrency(currency)
                .setInvolvedAccounts(accountsIban)
                .setAmountForUsers(splittedAmounts != null ? splittedAmounts : new ArrayList<>())
                .setError(errorMsg)
                .build();

        for (String ib : accountsIban) {
            Account ac = userRepo.getAccountByIBAN(ib);
            if (ac != null) {
                ac.addTransaction(rejectTx);
            }
        }
    }

    private boolean allAccepted() {
        for (Map.Entry<String, List<Integer>> entry : userIndexMap.entrySet()) {
            String email = entry.getKey();
            Map<Integer, Boolean> emailResponses = responses.getOrDefault(email, new HashMap<>());

            for (Integer timestamp : emailResponses.keySet()) {
                Boolean response = emailResponses.get(timestamp);
                if (!Boolean.TRUE.equals(response)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void doSuccessPayment(final ArrayNode output) {
        for (int i = 0; i < accountsIban.size(); i++) {
            String ib = accountsIban.get(i);
            Account ac = userRepo.getAccountByIBAN(ib);

            if (ac == null) {
                createInsufficientFundsTransaction(output, ib);
                return;
            }

            double share;
            if ("equal".equalsIgnoreCase(splitPaymentType)) {
                share = amount / accountsIban.size();
            } else {
                share = splittedAmounts.get(i);
            }

            double convertedShare = userRepo.getExchangeRate(currency,
                    ac.getCurrency()) * share;
            if (ac.getBalance() - convertedShare < ac.getMinimumBalance()) {
                createInsufficientFundsTransaction(output, ib);
                return;
            }
        }
        for (int i = 0; i < accountsIban.size(); i++) {
            String ib = accountsIban.get(i);
            Account ac = userRepo.getAccountByIBAN(ib);
            if (ac != null) {
                double share = splittedAmounts.get(i);
                double converted = userRepo.getExchangeRate(currency,
                        ac.getCurrency()) * share;
                ac.setBalance(ac.getBalance() - converted);
            }
        }
        if (splitPaymentType.equals("equal")) {
            Transaction successTx = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription(String.format("Split payment of %.2f %s",
                            getTotalSplit(), currency))
                    .setSplitPaymentType(splitPaymentType)
                    .setCurrency(currency)
                    .setInvolvedAccounts(accountsIban)
                    .setAmount(amount / accountsIban.size())
                    .build();
            for (String ib : accountsIban) {
                Account ac = userRepo.getAccountByIBAN(ib);
                if (ac != null) {
                    ac.addTransaction(successTx);
                } else {
                    return;
                }
            }
            return;
        }
        Transaction successTx = new Transaction.Builder()
                .setTimestamp(timestamp)
                .setDescription(String.format("Split payment of %.2f %s",
                        getTotalSplit(), currency))
                .setSplitPaymentType(splitPaymentType)
                .setCurrency(currency)
                .setInvolvedAccounts(accountsIban)
                .setAmountForUsers(splittedAmounts)
                .build();

        for (String ib : accountsIban) {
            Account ac = userRepo.getAccountByIBAN(ib);
            if (ac != null) {
                ac.addTransaction(successTx);
            } else {
                return;
            }
        }
    }

    private void processNextPayment(final ArrayNode output) {
        SplitPayment next = manager.getCurrentSplitPayment();
        if (next == null) {
            return;
        }
        next.execute(output);
    }

    public int getTimestamp() {
        return timestamp;
    }

    private List<Double> buildSplitAmounts() {
        if ("equal".equalsIgnoreCase(splitPaymentType)) {
            double perUser = amount / accountsIban.size();
            List<Double> splitted = new ArrayList<>();
            for (int i = 0; i < accountsIban.size(); i++) {
                splitted.add(perUser);
            }
            return splitted;
        } else if ("custom".equalsIgnoreCase(splitPaymentType)) {
            if (amountForUsers == null
                    || amountForUsers.size() != accountsIban.size()) {
                return null;
            }
            return new ArrayList<>(amountForUsers);
        }
        return null;
    }

    private double getTotalSplit() {
        if ("equal".equalsIgnoreCase(splitPaymentType)) {
            return amount;
        } else if ("custom".equalsIgnoreCase(splitPaymentType)
                && amountForUsers != null) {
            double sum = 0.0;
            for (double val : amountForUsers) {
                sum += val;
            }
            return sum;
        }
        return 0.0;
    }

    private ObjectNode createOutputNode(final String message) {
        ObjectNode node = new ObjectMapper().createObjectNode();
        node.put("timestamp", timestamp);
        node.put("description", message);
        return node;
    }
}
