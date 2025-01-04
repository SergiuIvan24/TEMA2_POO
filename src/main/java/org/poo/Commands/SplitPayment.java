package org.poo.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.Observer.Observer;
import org.poo.Observer.Subject;
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

    private final String splitPaymentType;    // "equal" / "custom"
    private final List<String> accountsIban;  // IBAN-urile implicate
    private final double amount;              // Doar pt "equal"
    private final List<Double> amountForUsers;// Doar pt "custom"
    private final String currency;
    private final int timestamp;
    private final UserRepo userRepo;

    private final Map<String, Integer> userIndexMap = new HashMap<>();
    private final Map<String, Boolean> responses = new HashMap<>();

    private boolean transactionCancelled = false;
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

    }

    @Override
    public void execute(final ArrayNode output) {
        manager.addSplitPayment(this);

        if (manager.getCurrentSplitPayment() != this) {
            return;
        }
        splittedAmounts = buildSplitAmounts();

        if (splittedAmounts == null) {
            transactionCancelled = true;
            output.add(createOutputNode("Custom splitPayment: missing or invalid 'amountsForUsers'."));
            Transaction errTx = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription(String.format("Split payment of %.2f %s", getTotalSplit(), currency))
                    .setSplitPaymentType(splitPaymentType)
                    .setCurrency(currency)
                    .setInvolvedAccounts(accountsIban)
                    .setError("Invalid splitted amounts (custom).")
                    .build();

            for (String ib : accountsIban) {
                Account ac = userRepo.getAccountByIBAN(ib);
                if (ac != null) {
                    ac.addTransaction(errTx);
                }
            }

            manager.removeSplitPayment(this);
            processNextPayment(output);
            return;
        }

        for (int i = 0; i < accountsIban.size(); i++) {
            String iban = accountsIban.get(i);
            Account acc = userRepo.getAccountByIBAN(iban);
            if (acc == null) {
                transactionCancelled = true;
                String msg = "Account " + iban + " not found in userRepo";
                output.add(createOutputNode(msg));

                Transaction errTx = new Transaction.Builder()
                        .setTimestamp(timestamp)
                        .setDescription(String.format("Split payment of %.2f %s", getTotalSplit(), currency))
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
                transactionCancelled = true;
                String msg = "No user found owning IBAN " + iban;
                Transaction errTx = new Transaction.Builder()
                        .setTimestamp(timestamp)
                        .setDescription(String.format("Split payment of %.2f %s", getTotalSplit(), currency))
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
            userIndexMap.put(email, i);
            responses.put(email, null);
        }
    }

    public void handleAcceptance(String email, boolean accepted, final ArrayNode output) {
        if (this != manager.getCurrentSplitPayment()) {
            return;
        }

        if (!accepted) {
            transactionCancelled = true;
            createRejectedTransaction(output, "One user rejected the payment.");
            manager.removeSplitPayment(this);
            processNextPayment(output);
            return;
        }

        Integer idx = userIndexMap.get(email);
        if (idx == null) {
            return;
        }

        double share = splittedAmounts.get(idx);
        String iban = accountsIban.get(idx);

        Account acc = userRepo.getAccountByIBAN(iban);
        if (acc == null) {
            transactionCancelled = true;
            manager.removeSplitPayment(this);
            processNextPayment(output);
            return;
        }

        double convertedShare = userRepo.getExchangeRate(currency, acc.getCurrency()) * share;
        double newBalanceIf = acc.getBalance() - convertedShare;
        if (newBalanceIf < acc.getMinimumBalance()) {
            transactionCancelled = true;
            createInsufficientFundsTransaction(output, iban);
            manager.removeSplitPayment(this);
            processNextPayment(output);
            return;
        }

        responses.put(email, true);

        if (allAccepted()) {
            doSuccessPayment(output);
            manager.removeSplitPayment(this);
            processNextPayment(output);
        }
    }

    private void createInsufficientFundsTransaction(ArrayNode output, String insufficientIban) {
        String err = "Account " + insufficientIban + " has insufficient funds for a split payment.";
        if(splitPaymentType.equals("equal")){
            Transaction errorTx = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription(String.format("Split payment of %.2f %s", getTotalSplit(), currency))
                    .setSplitPaymentType(splitPaymentType)
                    .setCurrency(currency)
                    .setInvolvedAccounts(accountsIban)
                    .setError(err)
                    .setAmount(amount)
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
                .setDescription(String.format("Split payment of %.2f %s", getTotalSplit(), currency))
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

    private void createRejectedTransaction(ArrayNode output, String errorMsg) {
        if(splitPaymentType.equals("equal")){
            Transaction rejectTx = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription(String.format("Split payment of %.2f %s", getTotalSplit(), currency))
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
                .setDescription(String.format("Split payment of %.2f %s", getTotalSplit(), currency))
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
        output.add(createOutputNode(errorMsg));
    }

    private boolean allAccepted() {
        for (Map.Entry<String, Boolean> e : responses.entrySet()) {
            if (!Boolean.TRUE.equals(e.getValue())) {
                return false;
            }
        }
        return true;
    }

    private void doSuccessPayment(ArrayNode output) {
        for (int i = 0; i < accountsIban.size(); i++) {

            String ib = accountsIban.get(i);
            Account ac = userRepo.getAccountByIBAN(ib);
            if (ac != null) {
                double share = splittedAmounts.get(i);
                double converted = userRepo.getExchangeRate(currency, ac.getCurrency()) * share;
                ac.setBalance(ac.getBalance() - converted);
            }
        }
        if(splitPaymentType.equals("equal")){
            Transaction successTx = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription(String.format("Split payment of %.2f %s", getTotalSplit(), currency))
                    .setSplitPaymentType(splitPaymentType)
                    .setCurrency(currency)
                    .setInvolvedAccounts(accountsIban)
                    .setAmount(amount)
                    .build();
            for (String ib : accountsIban) {
                Account ac = userRepo.getAccountByIBAN(ib);
                if (ac != null) {
                    ac.addTransaction(successTx);
                } else {
                    System.out.println("CONTUL NU A FOST GASIT PT TRANZACTIA DE SUCCESS");
                }
            }
            return;
        }
        Transaction successTx = new Transaction.Builder()
                .setTimestamp(timestamp)
                .setDescription(String.format("Split payment of %.2f %s", getTotalSplit(), currency))
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
                System.out.println("CONTUL NU A FOST GASIT PT TRANZACTIA DE SUCCESS");
            }
        }
    }

    private void processNextPayment(ArrayNode output) {
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
        System.out.println("[DEBUG] buildSplitAmounts() called for SplitPaymentType: " + splitPaymentType);
        if ("equal".equalsIgnoreCase(splitPaymentType)) {

            double perUser = amount / accountsIban.size();
            List<Double> splitted = new ArrayList<>();
            System.out.println("[DEBUG] Splitting amount equally: " + splitted);
            for (int i = 0; i < accountsIban.size(); i++) {
                splitted.add(perUser);
            }
            return splitted;
        } else if ("custom".equalsIgnoreCase(splitPaymentType)) {
            System.out.println("[DEBUG] Custom amounts provided: " + amountForUsers);
            if (amountForUsers == null || amountForUsers.size() != accountsIban.size()) {
                return null;
            }
            return new ArrayList<>(amountForUsers);
        }
        return null;
    }

    private double getTotalSplit() {
        if ("equal".equalsIgnoreCase(splitPaymentType)) {
            return amount;
        } else if ("custom".equalsIgnoreCase(splitPaymentType) && amountForUsers != null) {
            double sum = 0.0;
            for (double val : amountForUsers) {
                sum += val;
            }
            return sum;
        }
        return 0.0;
    }

    private ObjectNode createOutputNode(String message) {
        ObjectNode node = new ObjectMapper().createObjectNode();
        node.put("timestamp", timestamp);
        node.put("description", message);
        return node;
    }
}