package org.poo.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class Card {
    private String cardNumber;
    private boolean blocked;

    public Card(final String cardNumber) {
        this.cardNumber = cardNumber;
        this.blocked = false;
    }
    /**
     * Returneaza tipul cardului.
     *
     * @return tipul cardului
     */
    public String getCardType() {
        return this.getClass().getSimpleName();
    }

    /**
     * Returneaza numarul cardului.
     *
     * @return numarul cardului
     */
    public String getCardNumber() {
        return cardNumber;
    }
    /**
     * Seteaza numarul cardului.
     *
     * @param cardNumber numarul cardului
     */
    public void setCardNumber(final String cardNumber) {
        this.cardNumber = cardNumber;
    }
    /**
     * Returneaza daca cardul este blocat sau nu.
     *
     * @return daca cardul este blocat sau nu
     */
    public boolean isBlocked() {
        return blocked;
    }
    /**
     * Seteaza daca cardul este blocat sau nu.
     *
     * @param blocked daca cardul este blocat sau nu
     */
    public void setBlocked(final boolean blocked) {
        this.blocked = blocked;
    }
    /**
     * Returneaza daca se poate efectua o tranzactie cu cardul.
     *
     * @return daca se poate efectua o tranzactie cu cardul
     */
    public abstract boolean canPerformTransaction();
    /**
     * Returneaza un obiect de tip ObjectNode care contine informatii despre card.
     *
     * @param mapper obiect de tip ObjectMapper
     * @return obiect de tip ObjectNode care contine informatii despre card
     */
    public abstract ObjectNode toJson(ObjectMapper mapper);
}
