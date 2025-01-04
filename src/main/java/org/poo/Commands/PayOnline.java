package org.poo.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.Cashback.CashbackStrategy;
import org.poo.Cashback.NrOfTransactions;
import org.poo.Cashback.Processor;
import org.poo.Cashback.SpendingThreshold;
import org.poo.entities.Account;
import org.poo.entities.Card;
import org.poo.entities.Commerciant;
import org.poo.entities.OneTimePayCard;
import org.poo.entities.Transaction;
import org.poo.entities.User;
import org.poo.entities.UserRepo;
import org.poo.utils.Utils;

public final class PayOnline implements Command {
    private String cardNumber;
    private double amount;
    private String currency;
    private final int timestamp;
    private String description;
    private String commerciant;
    private String email;
    private UserRepo userRepo;

    public PayOnline(final String cardNumber, final double amount,
                     final String currency, final int timestamp,
                     final String description, final String commerciant,
                     final String email, final UserRepo userRepo) {
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.currency = currency;
        this.timestamp = timestamp;
        this.description = description;
        this.commerciant = commerciant;
        this.email = email;
        this.userRepo = userRepo;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double calculateCashback(User user, String commerciant, double transactionAmount) {
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
        return processor.processCashback(user, commerciant, transactionAmount);
    }

    @Override
    public void execute(final ArrayNode output) {
        ObjectMapper objectMapper = new ObjectMapper();

        User user = userRepo.getUser(email);
        if (user == null) {
            return;
        }

        Account selectedAccount = null;
        Card selectedCard = null;

        for (Account acc : user.getAccounts()) {
            for (Card card : acc.getCards()) {
                System.out.println("Card: " + card.getCardNumber());
                if (card.getCardNumber().equals(cardNumber)) {
                    selectedAccount = acc;
                    selectedCard = card;
                    break;
                }
            }
            if (selectedAccount != null) break;
        }

        if (selectedCard == null) {
            ObjectMapper mapper = new ObjectMapper();

            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "Card not found");
            outputNode.put("timestamp", timestamp);

            ObjectNode commandResponse = mapper.createObjectNode();
            commandResponse.put("command", "payOnline");
            commandResponse.set("output", outputNode);
            commandResponse.put("timestamp", timestamp);

            output.add(commandResponse);

            return;
        }

        if(selectedAccount == null) {
            return;
        }

        if (selectedCard.isBlocked()) {
            Transaction frozenTx = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription("The card is frozen")
                    .build();
            selectedAccount.addTransaction(frozenTx);
            return;
        }

        double convertedAmount;
        if (currency.equalsIgnoreCase(selectedAccount.getCurrency())) {
            convertedAmount = amount;
        } else {
            double rate = userRepo.getExchangeRate(currency, selectedAccount.getCurrency());
            convertedAmount = amount * rate;
        }
        convertedAmount = round2(convertedAmount);
        double convertedAmountInRON = round2(amount * userRepo.getExchangeRate(currency, "RON"));
        double commissionRate = userRepo.getPlanCommissionRate(user, convertedAmountInRON);
        double commission = round2(commissionRate * convertedAmount);
        double newBalance = round2(selectedAccount.getBalance() - (convertedAmount + commission));

        if (newBalance < 0) {
            Transaction insufficientFundsTx = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription("Insufficient funds")
                    .build();
            selectedAccount.addTransaction(insufficientFundsTx);
            return;
        }

        if(newBalance < selectedAccount.getMinimumBalance()) {
            selectedCard.setBlocked(true);
        }

        selectedAccount.setBalance(newBalance);
        User initiator = null;
        if(selectedAccount.getAccountType().equals("business")) {
            initiator = userRepo.getUser(email);
        }

        Transaction payTx = new Transaction.Builder()
                .setTimestamp(timestamp)
                .setDescription("Card payment")
                .setAmount(convertedAmount)
                .setCommerciant(commerciant)
                .setInitiator(initiator)
                .build();

        selectedAccount.addTransaction(payTx);

        double amountInRON = round2(amount * userRepo.getExchangeRate(currency, "RON"));
        Commerciant c = userRepo.getCommerciant(commerciant);
        String cashbackType = c.getCashbackStrategy();
        if(cashbackType.equals("spendingThreshold")) {
            user.addSpent(amountInRON);
        }
        double rawCashback = calculateCashback(user, commerciant, amountInRON);
        double finalCashback = round2(rawCashback);

        if (finalCashback > 0) {
            double afterCashbackBalance = round2(selectedAccount.getBalance() + finalCashback);
            selectedAccount.setBalance(afterCashbackBalance);
        }

        if (selectedCard.getCardType().equals("OneTimePayCard")) {
            selectedAccount.removeCard(selectedCard);

            Transaction removeCardTx = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setAccount(selectedAccount.getIban())
                    .setCard(selectedCard.getCardNumber())
                    .setCardHolder(email)
                    .setDescription("The card has been destroyed")
                    .build();
            selectedAccount.addTransaction(removeCardTx);

            OneTimePayCard newOneTime = new OneTimePayCard(Utils.generateCardNumber());
            selectedAccount.addCard(newOneTime);

            Transaction addCardTx = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setCardHolder(email)
                    .setCard(newOneTime.getCardNumber())
                    .setAccount(selectedAccount.getIban())
                    .setDescription("New card created")
                    .build();
            selectedAccount.addTransaction(addCardTx);

        }
    }
}

