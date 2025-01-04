package org.poo.BankManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.Commands.Command;
import org.poo.Commands.CommandFactory;
import org.poo.entities.Commerciant;
import org.poo.entities.SplitPaymentManager;
import org.poo.entities.User;
import org.poo.entities.UserRepo;
import org.poo.utils.Utils;

public final class Bank {
    private static Bank instance;
    private final UserRepo userRepo;
    private final CommandFactory commandFactory;
    private final ArrayNode output;
    private final SplitPaymentManager splitPaymentManager;

    private Bank(final JsonNode inputData) {
        Utils.resetRandom();
        ObjectMapper objectMapper = new ObjectMapper();
        this.userRepo = new UserRepo();
        this.splitPaymentManager = new SplitPaymentManager();
        this.commandFactory = new CommandFactory(userRepo, splitPaymentManager);
        this.output = objectMapper.createArrayNode();

        for (JsonNode userNode : inputData.get("users")) {
            String firstName = userNode.get("firstName").asText();
            String lastName = userNode.get("lastName").asText();
            String email = userNode.get("email").asText();
            String birthDate = userNode.get("birthDate").asText();
            String occupation = userNode.get("occupation").asText();
            userRepo.addUser(new User(firstName, lastName, email, birthDate, occupation));
        }

        for (JsonNode commerciantNode : inputData.get("commerciants")) {
            String commerciant = commerciantNode.get("commerciant").asText();
            int id = commerciantNode.get("id").asInt();
            String account = commerciantNode.get("account").asText();
            String type = commerciantNode.get("type").asText();
            String cashbackStrategy = commerciantNode.get("cashbackStrategy").asText();
            userRepo.addCommerciant(new Commerciant(commerciant, id, account, type, cashbackStrategy));
        }

        for (JsonNode rateNode : inputData.get("exchangeRates")) {
            String from = rateNode.get("from").asText();
            String to = rateNode.get("to").asText();
            double rate = rateNode.get("rate").asDouble();

            userRepo.addExchangeRate(from, to, rate);
        }
    }

    public static Bank getInstance(final JsonNode inputData, boolean reset) {
        if (reset || instance == null) {
            synchronized (Bank.class) {
                if (reset || instance == null) {
                    instance = new Bank(inputData);
                }
            }
        }
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }


    /**
     * Executa comenzile din input
     * @param commands comenzi de executat
     */
    public void executeCommands(final JsonNode commands) {
        for (JsonNode commandData : commands) {
            String commandType = commandData.get("command").asText();
                Command command = commandFactory.createCommand(commandType, commandData);
                command.execute(output);
        }
    }
    /**
     * @return output JSON-ul final
     */
    public ArrayNode getOutput() {
        return output;
    }
}
