package org.poo.Commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.entities.SplitPaymentManager;
import org.poo.entities.UserRepo;

import java.util.ArrayList;
import java.util.List;

public final class CommandFactory {
    private final UserRepo userRepo;
    private final SplitPaymentManager splitPaymentManager;

    public CommandFactory(final UserRepo userRepo,
                          final SplitPaymentManager splitPaymentManager) {
        this.userRepo = userRepo;
        this.splitPaymentManager = splitPaymentManager;
    }

    private List<String> getAccountsList(final JsonNode accountsNode) {
        List<String> accounts = new ArrayList<>();
        for (JsonNode accountNode : accountsNode) {
            accounts.add(accountNode.asText());
        }
        return accounts;
    }

    /**
     * Creates a command based on the provided command type and data.
     *
     * @param commandType the type of command to create
     * @param commandData the data required to create the command
     * @return the created command
     * @throws IllegalArgumentException if the command type is invalid
     */

    public Command createCommand(final String commandType, final JsonNode commandData) {
        switch (commandType) {
            case "addAccount":
                Double interestRate = commandData.has("interestRate")
                        && !commandData.get("interestRate").isNull()
                        ? commandData.get("interestRate").asDouble()
                        : null;
                return new AddAccount(
                        userRepo,
                        commandData.get("email").asText(),
                        commandData.get("currency").asText(),
                        commandData.get("accountType").asText(),
                        commandData.get("timestamp").asInt(),
                        interestRate
                );
            case "createCard":
                return new CreateCard(
                        commandData.get("account").asText(),
                        commandData.get("email").asText(),
                        userRepo,
                        commandData.get("timestamp").asInt()
                );
            case "addFunds":
                return new AddFunds(
                        commandData.get("email").asText(),
                        commandData.get("account").asText(),
                        commandData.get("amount").asDouble(),
                        commandData.get("timestamp").asInt(),
                        userRepo
                );
            case "printUsers":
                return new PrintUsers(userRepo, commandData.get("timestamp").asInt());
            case "deleteAccount":
                return new DeleteAccount(
                        userRepo,
                        commandData.get("email").asText(),
                        commandData.get("account").asText(),
                        commandData.get("timestamp").asInt()
                );
            case "createOneTimeCard":
                return new CreateOneTimeCard(
                        commandData.get("account").asText(),
                        commandData.get("email").asText(),
                        userRepo,
                        commandData.get("timestamp").asInt()
                );
            case "deleteCard":
                return new DeleteCard(
                        commandData.get("email").asText(),
                        commandData.get("cardNumber").asText(),
                        userRepo,
                        commandData.get("timestamp").asInt()
                );
            case "setMinimumBalance":
                return new SetMinimumBalance(
                        commandData.get("account").asText(),
                        commandData.get("amount").asDouble(),
                        userRepo,
                        commandData.get("timestamp").asInt()
                );
            case "payOnline":
                return new PayOnline(
                        commandData.get("cardNumber").asText(),
                        commandData.get("amount").asDouble(),
                        commandData.get("currency").asText(),
                        commandData.get("timestamp").asInt(),
                        commandData.get("description").asText(),
                        commandData.get("commerciant").asText(),
                        commandData.get("email").asText(),
                        userRepo
                );
            case "sendMoney":
                return new SendMoney(
                        commandData.get("account").asText(),
                        commandData.get("receiver").asText(),
                        commandData.get("amount").asDouble(),
                        commandData.get("timestamp").asInt(),
                        commandData.get("email").asText(),
                        commandData.get("description").asText(),
                        userRepo
                );
            case "printTransactions":
                return new PrintTransactions(
                        commandData.get("email").asText(),
                        commandData.get("timestamp").asInt(),
                        userRepo
                );
            case "setAlias":
                return new SetAlias(
                        commandData.get("email").asText(),
                        commandData.get("account").asText(),
                        commandData.get("alias").asText(),
                        commandData.get("timestamp").asInt(),
                        userRepo
                );
            case "checkCardStatus":
                return new CheckCardStatus(
                        commandData.get("cardNumber").asText(),
                        commandData.get("timestamp").asInt(),
                        userRepo
                );
            case "changeInterestRate":
                return new ChangeInterestRate(
                        commandData.get("account").asText(),
                        commandData.get("interestRate").asDouble(),
                        commandData.get("timestamp").asInt(),
                        userRepo
                );
            case "splitPayment":
                List<Double> amountsForUsers = new ArrayList<>();
                if (commandData.has("amountForUsers")) {
                    ArrayNode amountForUsersNode = (ArrayNode) commandData.get("amountForUsers");
                    for (int i = 0; i < amountForUsersNode.size(); i++) {
                        amountsForUsers.add(amountForUsersNode.get(i).asDouble());
                    }
                }
                return new SplitPayment(
                        splitPaymentManager,
                        commandData.get("splitPaymentType").asText(),
                        getAccountsList(commandData.get("accounts")),
                        commandData.get("amount").asDouble(),
                        amountsForUsers,
                        commandData.get("currency").asText(),
                        commandData.get("timestamp").asInt(),
                        userRepo
                );
            case "report":
                return new Report(
                        commandData.get("startTimestamp").asInt(),
                        commandData.get("endTimestamp").asInt(),
                        commandData.get("account").asText(),
                        userRepo,
                        commandData.get("timestamp").asInt()
                );
            case "spendingsReport":
                return new SpendingReport(
                        commandData.get("startTimestamp").asInt(),
                        commandData.get("endTimestamp").asInt(),
                        commandData.get("account").asText(),
                        commandData.get("timestamp").asInt(),
                        userRepo
                );
                case "addInterest":
                return new AddInterest(
                        commandData.get("account").asText(),
                        commandData.get("timestamp").asInt(),
                        userRepo
                );
                case "upgradePlan":
                return new UpgradePlan(
                        commandData.get("account").asText(),
                        commandData.get("newPlanType").asText(),
                        commandData.get("timestamp").asInt(),
                        userRepo
                );
                case "withdrawSavings":
                return new WithdrawSavings(
                        commandData.get("account").asText(),
                        commandData.get("amount").asDouble(),
                        commandData.get("currency").asText(),
                        commandData.get("timestamp").asInt(),
                        userRepo
                );
                case "changeDepositLimit":
                return new ChangeDepositLimit(
                        commandData.get("email").asText(),
                        commandData.get("account").asText(),
                        commandData.get("amount").asDouble(),
                        commandData.get("timestamp").asInt(),
                        userRepo
                );
                case "changeSpendingLimit":
                return new ChangeSpendingLimit(
                        commandData.get("email").asText(),
                        commandData.get("account").asText(),
                        commandData.get("amount").asDouble(),
                        commandData.get("timestamp").asInt(),
                        userRepo
                );
                case "addNewBusinessAssociate":
                return new AddNewBusinessAssociate(
                        commandData.get("account").asText(),
                        commandData.get("role").asText(),
                        commandData.get("email").asText(),
                        commandData.get("timestamp").asInt(),
                        userRepo
                );
                case "cashWithdrawal":
                    String email = commandData.has("email") ? commandData.get("email").asText() : "unknown";
                    return new CashWithdrawal(
                        commandData.get("cardNumber").asText(),
                        commandData.get("amount").asDouble(),
                        email,
                        commandData.get("location").asText(),
                        commandData.get("timestamp").asInt(),
                        userRepo
                );
            case "acceptSplitPayment":
                return new AcceptSplitPayment(
                        commandData.get("email").asText(),
                        commandData.get("timestamp").asInt(),
                        commandData.get("splitPaymentType").asText(),
                        splitPaymentManager,
                        userRepo
                );

            case "rejectSplitPayment":
                return new RejectSplitPayment(
                        commandData.get("email").asText(),
                        commandData.get("timestamp").asInt(),
                        commandData.get("splitPaymentType").asText(),
                        splitPaymentManager,
                        userRepo
                );
            case "businessReport":
                return new BusinessReport(
                        commandData.get("type").asText(),
                        commandData.get("startTimestamp").asInt(),
                        commandData.get("endTimestamp").asInt(),
                        commandData.get("account").asText(),
                        commandData.get("timestamp").asInt(),
                        userRepo
                );

            default:
                throw new IllegalArgumentException("Invalid command: " + commandType);
        }
    }
}
