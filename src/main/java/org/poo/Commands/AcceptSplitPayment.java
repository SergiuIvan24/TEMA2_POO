package org.poo.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.SplitPaymentManager;
import org.poo.entities.User;
import org.poo.entities.UserRepo;

public final class AcceptSplitPayment implements Command {
    private final String email;
    private final int timestamp;
    private final String splitPaymentType;

    private final SplitPaymentManager splitPaymentManager;
    private final UserRepo userRepo;

    public AcceptSplitPayment(final String email,
                              final int timestamp,
                              final String splitPaymentType,
                              final SplitPaymentManager splitPaymentManager,
                              final UserRepo userRepo) {
        this.email = email;
        this.timestamp = timestamp;
        this.splitPaymentManager = splitPaymentManager;
        this.userRepo = userRepo;
        this.splitPaymentType = splitPaymentType;
    }

    @Override
    public void execute(final ArrayNode output) {

        SplitPayment currentPayment = splitPaymentManager.getCurrentSplitPayment();

        if (currentPayment == null) {
            return;
        }

        User user = userRepo.getUser(email);
        if (user == null) {
            ObjectNode errorNode = new ObjectMapper().createObjectNode();
            errorNode.put("command", "acceptSplitPayment");

            ObjectNode outputNode = new ObjectMapper().createObjectNode();
            outputNode.put("description", "User not found");
            outputNode.put("timestamp", timestamp);

            errorNode.set("output", outputNode);
            errorNode.put("timestamp", timestamp);

            output.add(errorNode);
            return;
        }
        currentPayment.acceptance(email, true, output, splitPaymentType);
    }

}
