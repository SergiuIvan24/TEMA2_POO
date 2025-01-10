package org.poo.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account;
import org.poo.entities.Transaction;
import org.poo.entities.User;
import org.poo.entities.UserRepo;

public final class UpgradePlan implements Command {
    private static final int FEE_STANDARD_TO_SILVER = 100;
    private static final int FEE_STANDARD_TO_GOLD = 350;
    private static final int FEE_SILVER_TO_GOLD = 250;

    private String newPlanType;
    private String accountIBAN;
    private final int timestamp;
    private final UserRepo userRepo;

    public UpgradePlan(final String accountIBAN, final String newPlanType,
                       final int timestamp, final UserRepo userRepo) {
        this.newPlanType = newPlanType;
        this.accountIBAN = accountIBAN;
        this.timestamp = timestamp;
        this.userRepo = userRepo;
    }

    @Override
    public void execute(final ArrayNode output) {
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

    private boolean isUpgradeValid(final String currentPlan,
                                   final String newPlanType) {
        String currentPlanUpper = currentPlan.toUpperCase();
        String newPlanTypeUpper = newPlanType.toUpperCase();
        switch (currentPlanUpper) {
            case "STANDARD":
            case "STUDENT":
                return newPlanTypeUpper.equalsIgnoreCase("SILVER")
                        || newPlanTypeUpper.equalsIgnoreCase("GOLD");
            case "SILVER":
                return newPlanTypeUpper.equalsIgnoreCase("GOLD");
            case "GOLD":
                return false;
            default:
                return false;
        }
    }

    private int calculateUpgradeFee(final String currentPlan, final String newPlanType) {
        String currentPlanUpper = currentPlan.toUpperCase();
        String newPlanTypeUpper = newPlanType.toUpperCase();
        if (currentPlanUpper.equals("STANDARD") || currentPlanUpper.equals("STUDENT")) {
            if (newPlanTypeUpper.equals("SILVER")) {
                return FEE_STANDARD_TO_SILVER;
            } else if (newPlanTypeUpper.equals("GOLD")) {
                return FEE_STANDARD_TO_GOLD;
            }
        } else if (currentPlanUpper.equals("SILVER") && newPlanTypeUpper.equals("GOLD")) {
            return FEE_SILVER_TO_GOLD;
        }
        return 0;
    }
}
