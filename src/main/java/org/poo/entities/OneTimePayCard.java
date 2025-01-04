package org.poo.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class OneTimePayCard extends Card {
    private boolean used;

    public OneTimePayCard(final String cardNumber) {
        super(cardNumber);
        this.used = false;
    }
    public boolean isUsed() {
        return used;
    }
    @Override
    public boolean canPerformTransaction() {
        return !isBlocked() && !isUsed();
    }
    public void setUsed(final boolean used) {
        this.used = used;
    }

    @Override
    public ObjectNode toJson(final ObjectMapper mapper) {
        ObjectNode cardNode = mapper.createObjectNode();
        cardNode.put("cardNumber", getCardNumber());
        String status = isBlocked() ? "blocked" : "active";
        cardNode.put("status", status);
        return cardNode;
    }

}
