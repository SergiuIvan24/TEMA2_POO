package org.poo.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account;
import org.poo.entities.Transaction;
import org.poo.entities.User;
import org.poo.entities.UserRepo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class UpgradePlan implements Command {
    private String newPlanType;
    private String accountIBAN;
    private final int timestamp;
    private UserRepo userRepo;

    public UpgradePlan(final String accountIBAN, final String newPlanType, final int timestamp, UserRepo userRepo) {
        this.newPlanType = newPlanType;
        this.accountIBAN = accountIBAN;
        this.timestamp = timestamp;
        this.userRepo = userRepo;
    }

    @Override
    public void execute(ArrayNode output) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        ObjectNode commandResponse = mapper.createObjectNode();


        User user = userRepo.getUserByIBAN(accountIBAN);
        if (user == null) {
            response.put("description", "Account not found");
            response.put("timestamp", timestamp);
            commandResponse.put("command", "upgradePlan");
            commandResponse.set("output", response);
            commandResponse.put("timestamp", timestamp);
            output.add(commandResponse);
            return;
        }

        Account account = user.getAccount(accountIBAN);
        if (account == null) {
            response.put("description", "Account not found");
            response.put("timestamp", timestamp);
            commandResponse.put("command", "upgradePlan");
            commandResponse.set("output", response);
            commandResponse.put("timestamp", timestamp);
            output.add(commandResponse);
            return;
        }

        String currentPlan = user.getServicePlan();

        if (currentPlan.equalsIgnoreCase(newPlanType)) {
            Transaction transaction = new Transaction.Builder()
                    .setTimestamp(timestamp)
                    .setDescription("The user already has the " + newPlanType + " plan.")
                    .build();
            account.addTransaction(transaction);
            return;
        }

        if (!isUpgradeValid(currentPlan, newPlanType)) {
            return;
        }

        int fee = calculateUpgradeFee(currentPlan, newPlanType);
        String accountCurrency = account.getCurrency();

        double exchangeRate = userRepo.getExchangeRate(accountCurrency, "RON");

        double feeInAccountCurrency = fee / exchangeRate;

        if (account.getBalance() < feeInAccountCurrency) {
           Transaction transaction = new Transaction.Builder()
                   .setTimestamp(timestamp)
                   .setDescription("Insufficient funds")
                   .build();
              account.addTransaction(transaction);
            return;
        }

        double newBalance = account.getBalance() - feeInAccountCurrency;

        newBalance = BigDecimal.valueOf(newBalance)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        account.setBalance(newBalance);
        user.setServicePlan(newPlanType);

        Transaction transaction = new Transaction.Builder()
                .setTimestamp(timestamp)
                .setDescription("Upgrade plan")
                .setNewPlanType(newPlanType)
                .setAccountIban(accountIBAN)
                .build();
        account.addTransaction(transaction);

    }

    private boolean isUpgradeValid(String currentPlan, String newPlanType) {
        currentPlan = currentPlan.toUpperCase();
        newPlanType = newPlanType.toUpperCase();
        switch (currentPlan) {
            case "STANDARD":
            case "STUDENT":
                return newPlanType.equalsIgnoreCase("SILVER") || newPlanType.equalsIgnoreCase("GOLD");
            case "SILVER":
                return newPlanType.equalsIgnoreCase("GOLD");
            case "GOLD":
                return false;
            default:
                return false;
        }
    }

    private int calculateUpgradeFee(String currentPlan, String newPlanType) {
        currentPlan = currentPlan.toUpperCase();
        newPlanType = newPlanType.toUpperCase();
        if (currentPlan.equals("STANDARD") || currentPlan.equals("STUDENT")) {
            if (newPlanType.equals("SILVER")) {
                return 100;
            } else if (newPlanType.equals("GOLD")) {
                return 350;
            }
        } else if (currentPlan.equals("SILVER") && newPlanType.equals("GOLD")) {
            return 250;
        }
        return 0;
    }
}

