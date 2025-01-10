package org.poo.entities;

import org.poo.Commands.SplitPayment;

import java.util.LinkedList;
import java.util.Queue;

public final class SplitPaymentManager {

    private final Queue<SplitPayment> splitQueue = new LinkedList<>();

    public SplitPaymentManager() {

    }
    /**
     * Adaugă un SplitPayment la finalul cozii.
     */
    public void addSplitPayment(final SplitPayment payment) {
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
    public void removeSplitPayment(final SplitPayment payment) {
        splitQueue.remove(payment);
    }

    public Queue<SplitPayment> getSplitPayments() {
        return splitQueue;
    }
}
