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
import org.poo.entities.Card;
import org.poo.entities.Commerciant;
import org.poo.entities.OneTimePayCard;
import org.poo.entities.Transaction;
import org.poo.entities.User;
import org.poo.entities.UserRepo;
import org.poo.utils.Utils;

public final class PayOnline implements Command {
    private final int MIN_AMOUNT = 300;
    private final int MIN_TRANSACTIONS_NR = 5;

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

    private double calculateCashback(final Account account, final String commerciant,
                                     final double transactionAmount) {
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
        return processor.processCashback(account, commerciant, transactionAmount, userRepo);
    }

    @Override
    public void execute(final ArrayNode output) {
        User user = userRepo.getUser(email);
        if (user == null) {
            return;
        }

        Account selectedAccount = null;
        Card selectedCard = null;

        for (Account acc : user.getAccounts()) {
            for (Card card : acc.getCards()) {
                if (card.getCardNumber().equals(cardNumber)) {
                    selectedAccount = acc;
                    selectedCard = card;
                    break;
                }
            }
            if (selectedAccount != null) {
                break;
            }
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

        if (selectedAccount == null) {
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
            double rate = userRepo.getExchangeRate(currency,
                    selectedAccount.getCurrency());
            convertedAmount = amount * rate;
        }
        double convertedAmountInRON = convertedAmount * userRepo
                .getExchangeRate(selectedAccount.getCurrency(), "RON");
        double commissionRate;

        if (selectedAccount.getAccountType().equals("business")) {
            commissionRate = userRepo.getPlanCommissionRate(((BusinessAccount)selectedAccount).getOwner(), convertedAmountInRON);
        } else {
            commissionRate = userRepo.getPlanCommissionRate(user, convertedAmountInRON);
        }

        double commission = commissionRate * convertedAmount;
        double newBalance = selectedAccount.getBalance() - (convertedAmount + commission);
        if (newBalance < 0) {
            Transaction insufficientFundsTx = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription("Insufficient funds")
                    .build();
            selectedAccount.addTransaction(insufficientFundsTx);
            return;
        }

        if (selectedAccount.getAccountType().equals("business")
                && ((BusinessAccount) selectedAccount).getEmployees().contains(user)) {
            BusinessAccount businessAccount = (BusinessAccount) selectedAccount;
            if (businessAccount.getSpendingLimit() < convertedAmount) {
                return;
            }
        }

        selectedAccount.setBalance(newBalance);

        if (convertedAmount > 0) {
            User initiator = null;
            if (selectedAccount.getAccountType().equals("business")) {
                initiator = user;
            }
            Transaction payTx = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription("Card payment")
                    .setAmount(convertedAmount)
                    .setCommerciant(commerciant)
                    .setInitiator(initiator)
                    .build();
            selectedAccount.addTransaction(payTx);
        }

        Commerciant c = userRepo.getCommerciant(commerciant);
        double discountInAccountCurrency = 0.0;
        if (c != null) {
            String merchantCategory = c.getType();
            double pendingDiscountInRONRate = selectedAccount
                    .getPendingCategoryDiscount(merchantCategory);
            if (pendingDiscountInRONRate > 0) {
                double amountInRON = amount * userRepo.getExchangeRate(currency, "RON")
                        * pendingDiscountInRONRate;
                discountInAccountCurrency = amountInRON
                        * userRepo.getExchangeRate("RON", selectedAccount.getCurrency());
                selectedAccount.markCashbackReceived(merchantCategory);
                selectedAccount.clearPendingCategoryDiscount(merchantCategory);
            }
        }

        double amountInRON = amount * userRepo.getExchangeRate(currency, "RON");
        if (c != null && c.getCashbackStrategy().equals("spendingThreshold")) {
            selectedAccount.addSpendingThresholdTotal(c.getCommerciant(), convertedAmountInRON);
            selectedAccount.addTotalSpendingThreshold(convertedAmountInRON);
        }


        double rawCashback = calculateCashback(selectedAccount, commerciant, amountInRON);
        double finalConvertedCashback = rawCashback
                * userRepo.getExchangeRate("RON", selectedAccount.getCurrency());
        double afterCashbackBalance = selectedAccount.getBalance()
                + finalConvertedCashback + discountInAccountCurrency;
        selectedAccount.setBalance(afterCashbackBalance);

        if (amountInRON >= MIN_AMOUNT && user.getServicePlan().equals("silver")) {
            user.addNrOfTransactionsOver300RON();
            if (user.getNrOfTransactionsOver300RON() == MIN_TRANSACTIONS_NR
                    && user.getServicePlan().equals("silver")) {
                user.setServicePlan("gold");
                Transaction upgradePlanTx = new Transaction.Builder()
                        .setTimestamp(timestamp)
                        .setDescription("Upgrade plan")
                        .setAccountIban(selectedAccount.getIban())
                        .setNewPlanType("gold")
                        .build();
                selectedAccount.addTransaction(upgradePlanTx);
            }
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

