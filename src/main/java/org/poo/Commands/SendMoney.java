package org.poo.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.Cashback.CashbackStrategy;
import org.poo.Cashback.NrOfTransactions;
import org.poo.Cashback.Processor;
import org.poo.Cashback.SpendingThreshold;
import org.poo.entities.Account;
import org.poo.entities.BusinessAccount;
import org.poo.entities.Commerciant;
import org.poo.entities.Transaction;
import org.poo.entities.User;
import org.poo.entities.UserRepo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class SendMoney implements Command {
    private String senderIBAN;
    private String receiverIBAN;
    private double amount;
    private final int timestamp;
    private String senderEmail;
    private String description;
    private UserRepo userRepo;

    public SendMoney(final String senderIBAN, final String receiverIBAN,
                     final double amount, final int timestamp,
                     final String email, final String description,
                     final UserRepo userRepo) {
        this.senderIBAN = senderIBAN;
        this.receiverIBAN = receiverIBAN;
        this.amount = amount;
        this.timestamp = timestamp;
        this.senderEmail = email;
        this.description = description;
        this.userRepo = userRepo;
    }

    /**
     * Mică metodă ajutătoare pentru a rotunji la 2 zecimale
     */
    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double calculateCashback(Account account, String commerciant, double transactionAmount) {
        Commerciant c = userRepo.getCommerciant(commerciant);
        if (c == null) {
            return 0.0;
        }
        String cashbackType = c.getCashbackStrategy();

        CashbackStrategy strategy;
        if (cashbackType.equals("nrOfTransactions")) {
            strategy = new NrOfTransactions();
        } else if (cashbackType.equals("spendingThreshold")) {
            strategy = new SpendingThreshold();
        } else {
            return 0.0;
        }

        Processor processor = new Processor(strategy);
        return processor.processCashback(account, commerciant, transactionAmount);
    }


    @Override
    public void execute(final ArrayNode output) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode resultNode = objectMapper.createObjectNode();
        resultNode.put("command", "sendMoney");
        User sender = userRepo.getUser(senderEmail);
        if (sender == null) {
            return;
        }

        Account senderAccount = sender.getAccount(senderIBAN);
        if (senderAccount == null) {
            return;
        }
        boolean receiverIsComerciant = false;
        String realReceiverIBAN = userRepo.getIBANByAlias(receiverIBAN);
        if (realReceiverIBAN == null) {
            for (Commerciant c : userRepo.getComerciants()) {
                if (c.getAccount().equals(receiverIBAN)) {
                    realReceiverIBAN = receiverIBAN;
                    receiverIsComerciant = true;
                    break;
                }
            }
            if (!receiverIsComerciant) {
                Account receiverAccountCheck = userRepo.getAccountByIBAN(receiverIBAN);
                if (receiverAccountCheck != null) {
                    realReceiverIBAN = receiverIBAN;
                } else {
                    ObjectNode errorNode = objectMapper.createObjectNode();
                    errorNode.put("description", "User not found");
                    errorNode.put("timestamp", timestamp);

                    resultNode.set("output", errorNode);
                    resultNode.put("timestamp", timestamp);

                    output.add(resultNode);
                    return;
                }
            }
        }
        if(receiverIsComerciant){
            Commerciant receiver = userRepo.getCommerciantByAccount(realReceiverIBAN);
            double amountInRON = amount * userRepo.getExchangeRate(senderAccount.getCurrency(), "RON");
            double commissionRate = userRepo.getPlanCommissionRate(sender, amountInRON);
            double commission = round2(commissionRate * amount);
            senderAccount.setBalance(senderAccount.getBalance() - (amount + commission));
            User user = sender;
            if(amountInRON >= 300 && user.getServicePlan().equals("silver")){
                user.addNrOfTransactionsOver300RON();
                if(user.getNrOfTransactionsOver300RON() == 5 && user.getServicePlan().equals("silver")){
                    user.setServicePlan("gold");
                }
            }
            if(receiver.getCashbackStrategy().equals("spendingThreshold")) {
                senderAccount.addSpent(amountInRON);
                senderAccount.addSpendingThresholdTotal(receiver.getCommerciant(), amountInRON);
            }
            double cashback = calculateCashback(senderAccount, receiver.getCommerciant(), amountInRON);
            double convertedCashback = cashback / userRepo.getExchangeRate("RON", senderAccount.getCurrency());
            senderAccount.setBalance(senderAccount.getBalance() + convertedCashback);
            Transaction senderTransaction = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription(description)
                    .setSenderIBAN(senderIBAN)
                    .setReceiverIBAN(receiverIBAN)
                    .setAmountPlusCurrency(String.format("%.1f %s", amount, senderAccount.getCurrency()))
                    .setTransferType("sent")
                    .build();
            senderAccount.addTransaction(senderTransaction);
            return;
        }
        User receiver = userRepo.getUserByIBAN(realReceiverIBAN);
        if (receiver == null) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("description", "User not found");
            errorNode.put("timestamp", timestamp);

            resultNode.set("output", errorNode);
            resultNode.put("timestamp", timestamp);

            output.add(resultNode);
            return;
        }

        Account receiverAccount = receiver.getAccount(realReceiverIBAN);

        if (receiverAccount == null) {
            return;
        }

        double newSenderBalance = senderAccount.getBalance() - amount;

        if (senderAccount.getBalance() < amount) {
            Transaction insufficientFundsTransaction = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription("Insufficient funds")
                    .build();
            senderAccount.addTransaction(insufficientFundsTransaction);
            return;
        }

        double amountInRON = 0.0;
        if (senderAccount.getCurrency().equalsIgnoreCase("RON")) {
            amountInRON = amount;
        } else {
            double toRONRate = userRepo.getExchangeRate(senderAccount.getCurrency(), "RON");
            amountInRON = round2(amount * toRONRate);
        }

        sender.addSpent(amountInRON);

        double commissionRate = userRepo.getPlanCommissionRate(sender, amountInRON);

        double commission = round2(commissionRate * amount);
        System.out.println("The user " + sender.getFullName() + " has a commission of " + commission + " RON");

        if (senderAccount.getBalance() < (amount + commission)) {
            Transaction insufficientFundsTransaction = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription("Insufficient funds")
                    .build();
            senderAccount.addTransaction(insufficientFundsTransaction);
            return;
        }

        newSenderBalance = senderAccount.getBalance() - (amount + commission);
        senderAccount.setBalance(newSenderBalance);
        User initiator = null;
        if(senderAccount.getAccountType().equals("business")) {
            initiator = userRepo.getUser(senderEmail);
        }
        User user = sender;
        if(amountInRON >= 300 && user.getServicePlan().equals("silver")){
            user.addNrOfTransactionsOver300RON();
            if(user.getNrOfTransactionsOver300RON() == 5 && user.getServicePlan().equals("silver")){
                user.setServicePlan("gold");
            }
        }

        double rate = userRepo.getExchangeRate(senderAccount.getCurrency(), receiverAccount.getCurrency());
        double convertedAmount = amount * rate;

        double newReceiverBalance = receiverAccount.getBalance() + convertedAmount;
        receiverAccount.setBalance(newReceiverBalance);

        String senderAmountWithCurrency = String.format("%.1f %s", amount, senderAccount.getCurrency());
        String receiverAmountWithCurrency = String.format("%.1f %s", convertedAmount, receiverAccount.getCurrency());
        Transaction senderTransaction = new Transaction.Builder()
                .setTimestamp(timestamp)
                .setDescription(description)
                .setSenderIBAN(senderIBAN)
                .setReceiverIBAN(receiverIBAN)
                .setAmountPlusCurrency(senderAmountWithCurrency)
                .setTransferType("sent")
                .setInitiator(initiator)
                .build();
        senderAccount.addTransaction(senderTransaction);
        initiator = null;
        if(receiverAccount.getAccountType().equals("business")) {
            initiator = userRepo.getUser(receiver.getEmail());
        }
        Transaction receiverTransaction = new Transaction.Builder()
                .setTimestamp(timestamp)
                .setDescription(description)
                .setSenderIBAN(senderIBAN)
                .setReceiverIBAN(receiverIBAN)
                .setAmountPlusCurrency(receiverAmountWithCurrency)
                .setTransferType("received")
                .setInitiator(initiator)
                .build();
        receiverAccount.addTransaction(receiverTransaction);
    }
}
