package org.poo.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account;
import org.poo.entities.Card;
import org.poo.entities.OneTimePayCard;
import org.poo.entities.Transaction;
import org.poo.entities.User;
import org.poo.entities.UserRepo;

public final class CashWithdrawal implements Command {
    private String cardNumber;
    private double amount;
    private String email;
    private String location;
    private final int timestamp;
    private UserRepo userRepo;

    public CashWithdrawal(final String cardNumber, final double amount,
                          final String email, final String location,
                          final int timestamp, final UserRepo userRepo) {
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.email = email;
        this.location = location;
        this.timestamp = timestamp;
        this.userRepo = userRepo;
    }

    @Override
    public void execute(final ArrayNode output) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        ObjectNode commandResponse = mapper.createObjectNode();

        if (email.equals("unknown")) {
            response.put("description", "User not found");
            response.put("timestamp", timestamp);
            commandResponse.put("command", "cashWithdrawal");
            commandResponse.set("output", response);
            commandResponse.put("timestamp", timestamp);
            output.add(commandResponse);
            return;
        }

        Account account = null;
        User userWithCard = null;

        User user = userRepo.getUser(email);
        if (user == null) {
            response.put("description", "User not found");
            response.put("timestamp", timestamp);

            commandResponse.put("command", "cashWithdrawal");
            commandResponse.set("output", response);
            commandResponse.put("timestamp", timestamp);

            output.add(commandResponse);
            return;
        }

        for (Account acc : user.getAccounts()) {
            for (Card card : acc.getCards()) {
                if (card.getCardNumber().equals(cardNumber)) {
                    account = acc;
                    userWithCard = user;
                    break;
                }
            }
        }

        if (account == null) {
            response.put("description", "Card not found");
            response.put("timestamp", timestamp);

            commandResponse.put("command", "cashWithdrawal");
            commandResponse.set("output", response);
            commandResponse.put("timestamp", timestamp);

            output.add(commandResponse);
            return;
        }

        for (Card card : account.getCards()) {
            if (card.getCardNumber().equals(cardNumber)) {
                if (card.isBlocked()) {
                    response.put("message", "The card is blocked");
                    response.put("timestamp", timestamp);
                    output.add(response);
                    return;
                }
                if (card.getCardType().equalsIgnoreCase("OneTimePay")) {
                    OneTimePayCard oneTimeCard = (OneTimePayCard) card;
                    if (oneTimeCard.isUsed()) {
                        response.put("message", "Card has already been used");
                        response.put("timestamp", timestamp);
                        output.add(response);
                        return;
                    }
                    oneTimeCard.setUsed(true);
                }
            }
        }

        String accountCurrency = account.getCurrency();
        double exchangeRate = 1.0;
        if (!accountCurrency.equalsIgnoreCase("RON")) {
            exchangeRate = userRepo.getExchangeRate(accountCurrency, "RON");
            if (exchangeRate <= 0) {
                response.put("message", "Cannot convert currency");
                output.add(response);
                return;
            }
        }

        double neededInAccountCurrency;
        if (accountCurrency.equalsIgnoreCase("RON")) {
            neededInAccountCurrency = amount;
        } else {
            neededInAccountCurrency = amount / exchangeRate;
        }

        double feeRate = userRepo.getPlanCommissionRate(user, amount);
        double feeValue = neededInAccountCurrency * feeRate;
        double totalNeeded = neededInAccountCurrency + feeValue;

        if (account.getBalance() < totalNeeded) {
            Transaction transaction = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription("Insufficient funds")
                    .build();
            account.addTransaction(transaction);
            return;
        }

        account.setBalance(account.getBalance() - totalNeeded);

        user.addSpent(amount);

        Transaction transaction = new Transaction.Builder()
                .setTimestamp(timestamp)
                .setDescription("Cash withdrawal of " + amount)
                .setAmount(amount)
                .build();
        account.addTransaction(transaction);

    }

}
