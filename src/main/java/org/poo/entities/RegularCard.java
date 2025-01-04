package org.poo.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class RegularCard extends Card {

    public RegularCard(final String cardNumber) {
        super(cardNumber);
    }
    /**
     * Returneaza daca se poate efectua o tranzactie cu cardul.
     *
     * @return daca se poate efectua o tranzactie cu cardul
     */
    @Override
    public boolean canPerformTransaction() {
        return !isBlocked();
    }
    /**
     * Returneaza un obiect de tip ObjectNode care contine informatii despre card.
     *
     * @param mapper obiect de tip ObjectMapper
     * @return obiect de tip ObjectNode care contine informatii despre card
     */
    @Override
    public ObjectNode toJson(final ObjectMapper mapper) {
        ObjectNode cardNode = mapper.createObjectNode();
        cardNode.put("cardNumber", getCardNumber());
        cardNode.put("status", isBlocked() ? "frozen" : "active");
        return cardNode;
    }
}
