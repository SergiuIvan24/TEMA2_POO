package org.poo.entities;

import org.poo.Commands.SplitPayment;

import java.util.LinkedList;
import java.util.Queue;

public class SplitPaymentManager {

    private final Queue<SplitPayment> splitQueue = new LinkedList<>();

    public SplitPaymentManager() {

    }

    /**
     * Adaugă un SplitPayment la finalul cozii.
     */
    public void addSplitPayment(SplitPayment payment) {
        splitQueue.add(payment);
    }

    /**
     * Returnează Payment-ul curent (primul din coadă).
     */
    public SplitPayment getCurrentSplitPayment() {
        return splitQueue.peek();
    }

    /**
     * Scoate un Payment din coadă (dacă există).
     */
    public void removeSplitPayment(SplitPayment payment) {
        splitQueue.remove(payment);
    }

    /**
     * Verifică dacă există încă Payment-uri active.
     */
    public boolean hasActivePayments() {
        return !splitQueue.isEmpty();
    }

    public Queue<SplitPayment> getSplitPayments() {
        return splitQueue;
    }
}
