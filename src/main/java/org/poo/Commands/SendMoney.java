package org.poo.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account;
import org.poo.entities.BusinessAccount;
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

        String realReceiverIBAN = userRepo.getIBANByAlias(receiverIBAN);
        if (realReceiverIBAN == null) {
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
