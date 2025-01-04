package org.poo.entities;

import java.util.List;

public final class Transaction {
    private final int timestamp;
    private final String description;
    private final String senderIBAN;
    private final String receiverIBAN;
    private final double amount;
    private final String currency;
    private final String transferType;
    private final String card;
    private final String cardHolder;
    private final String account;
    private final String commerciant;
    private final String amountPlusCurrency;
    private final List<String> involvedAccounts;
    private final String classicAccountIBAN;
    private final String savingsAccountIBAN;
    private final String newPlanType;
    private final String accountIBAN;
    private final String splitPaymentType;
    private final List<Double> amountForUsers;
    private final User initiator;


    private String error;

    private Transaction(final Builder builder, final String error) {
        this.timestamp = builder.timestamp;
        this.description = builder.description;
        this.senderIBAN = builder.senderIBAN;
        this.receiverIBAN = builder.receiverIBAN;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.transferType = builder.transferType;
        this.card = builder.card;
        this.cardHolder = builder.cardHolder;
        this.account = builder.account;
        this.commerciant = builder.commerciant;
        this.amountPlusCurrency = builder.amountPlusCurrency;
        this.involvedAccounts = builder.involvedAccounts;
        this.error = builder.error;
        this.classicAccountIBAN = builder.classicAccountIBAN;
        this.savingsAccountIBAN = builder.savingsAccountIBAN;
        this.newPlanType = builder.newPlanType;
        this.accountIBAN = builder.accountIBAN;
        this.splitPaymentType = builder.splitPaymentType;
        this.amountForUsers = builder.amountForUsers;
        this.initiator = builder.initiator;
    }

    public User getInitiator() {
        return initiator;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getError() {
        return error;
    }

    public String getDescription() {
        return description;
    }

    public String getSenderIBAN() {
        return senderIBAN;
    }

    public String getReceiverIBAN() {
        return receiverIBAN;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTransferType() {
        return transferType;
    }

    public String getCard() {
        return card;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public String getAccount() {
        return account;
    }

    public String getCommerciant() {
        return commerciant;
    }

    public String getAmountPlusCurrency() {
        return amountPlusCurrency;
    }

    public List<String> getInvolvedAccounts() {
        return involvedAccounts;
    }

    public String getClassicAccountIBAN() {
        return classicAccountIBAN;
    }

    public String getSavingsAccountIBAN() {
        return savingsAccountIBAN;
    }

    public String getNewPlanType() {
        return newPlanType;
    }

    public String getAccountIBAN() {
        return accountIBAN;
    }

    public String getSplitPaymentType() {
        return splitPaymentType;
    }

    public List<Double> getAmountForUsers() {
        return amountForUsers;
    }

    public static final class Builder {
        private int timestamp;
        private String description;
        private String senderIBAN;
        private String receiverIBAN;
        private double amount = -1;
        private String currency;
        private String transferType;
        private String card;
        private String cardHolder;
        private String account;
        private String commerciant;
        private String amountPlusCurrency;
        private List<String> involvedAccounts;
        private String error;
        private String classicAccountIBAN;
        private String savingsAccountIBAN;
        private String newPlanType;
        private String accountIBAN;
        private String splitPaymentType;
        private List<Double> amountForUsers;
        private User initiator;

        public Builder setInitiator(final User initiator) {
            this.initiator = initiator;
            return this;
        }

        public Builder setAmountForUsers(final List<Double> amountForUsers) {
            this.amountForUsers = amountForUsers;
            return this;
        }

        public Builder setSplitPaymentType(final String splitPaymentType) {
            this.splitPaymentType = splitPaymentType;
            return this;
        }

        public Builder setAccountIban(final String accountIBAN) {
            this.accountIBAN = accountIBAN;
            return this;
        }

        public Builder setClassicAccountIBAN(final String classicAccountIBAN) {
            this.classicAccountIBAN = classicAccountIBAN;
            return this;
        }

        public Builder setSavingsAccountIBAN(final String savingsAccountIBAN) {
            this.savingsAccountIBAN = savingsAccountIBAN;
            return this;
        }

        public Builder setNewPlanType(final String newPlanType) {
            this.newPlanType = newPlanType;
            return this;
        }

        /**
         * Seteaza timestamp-ul tranzactiei.
         * @param timestamp
         * @return
         */
        public Builder setTimestamp(final int timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        /**
         * Seteaza eroarea tranzactiei.
         * @param error
         * @return
         */
        public Builder setError(final String error) {
            this.error = error;
            return this;
        }
        /**
         * Seteaza descrierea tranzactiei.
         * @param description
         * @return
         */
        public Builder setDescription(final String description) {
            this.description = description;
            return this;
        }
        /**
         * Seteaza IBAN-ul expeditorului.
         * @param senderIBAN
         * @return
         */
        public Builder setSenderIBAN(final String senderIBAN) {
            this.senderIBAN = senderIBAN;
            return this;
        }
        /**
         * Seteaza IBAN-ul destinatarului.
         * @param receiverIBAN
         * @return
         */
        public Builder setReceiverIBAN(final String receiverIBAN) {
            this.receiverIBAN = receiverIBAN;
            return this;
        }
        /**
         * Seteaza suma tranzactiei.
         * @param amount
         * @return
         */
        public Builder setAmount(final double amount) {
            this.amount = amount;
            return this;
        }
        /**
         * Seteaza valuta tranzactiei.
         * @param currency
         * @return
         */
        public Builder setCurrency(final String currency) {
            this.currency = currency;
            return this;
        }
        /**
         * Seteaza tipul tranzactiei.
         * @param transferType
         * @return
         */
        public Builder setTransferType(final String transferType) {
            this.transferType = transferType;
            return this;
        }
        /**
         * Seteaza cardul tranzactiei.
         * @param card
         * @return
         */
        public Builder setCard(final String card) {
            this.card = card;
            return this;
        }
        /**
         * Seteaza detinatorul cardului.
         * @param cardHolder
         * @return
         */
        public Builder setCardHolder(final String cardHolder) {
            this.cardHolder = cardHolder;
            return this;
        }
        /**
         * Seteaza contul tranzactiei.
         * @param account
         * @return
         */
        public Builder setAccount(final String account) {
            this.account = account;
            return this;
        }
        /**
         * Seteaza comerciantul tranzactiei.
         * @param commerciant
         * @return
         */
        public Builder setCommerciant(final String commerciant) {
            this.commerciant = commerciant;
            return this;
        }
        /**
         * Seteaza suma si valuta tranzactiei ca un string.
         * @param amountPlusCurrency
         * @return
         */
        public Builder setAmountPlusCurrency(final String amountPlusCurrency) {
            this.amountPlusCurrency = amountPlusCurrency;
            return this;
        }
        /**
         * Seteaza conturile implicate in tranzactie(pt split).
         * @param involvedAccounts
         * @return
         */
        public Builder setInvolvedAccounts(final List<String> involvedAccounts) {
            this.involvedAccounts = involvedAccounts;
            return this;
        }
        /**
         * Construieste tranzactia.
         * @return
         */
        public Transaction build() {
            return new Transaction(this, null);
        }
    }
}
