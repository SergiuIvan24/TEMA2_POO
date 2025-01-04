package org.poo.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.UserRepo;

public final class PrintUsers implements Command {
    private final UserRepo userRepo;
    private final int timestamp;

    public PrintUsers(final UserRepo userRepo, final int timestamp) {
        this.userRepo = userRepo;
        this.timestamp = timestamp;
    }

    @Override
    public void execute(final ArrayNode output) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode result = objectMapper.createObjectNode();
        result.put("command", "printUsers");
        result.set("output", userRepo.toJson(objectMapper));
        result.put("timestamp", timestamp);
        output.add(result);
    }
}
