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

        /**
         * Seteaza initiatorul tranzactiei.
         * @param newInitiator
         * @return
         */
        public Builder setInitiator(final User newInitiator) {
            this.initiator = newInitiator;
            return this;
        }

        /**
         * Seteaza amountul pentru useri.
         * @param newAmountForUsers
         * @return
         */
        public Builder setAmountForUsers(final List<Double> newAmountForUsers) {
            this.amountForUsers = newAmountForUsers;
            return this;
        }

        /**
         * Seteaza tipul de plata pentru split.
         * @param newSplitPaymentType
         * @return
         */
        public Builder setSplitPaymentType(final String newSplitPaymentType) {
            this.splitPaymentType = newSplitPaymentType;
            return this;
        }

        /**
         * Seteaza IBAN-ul contului.
         * @param newAccountIBAN
         * @return
         */
        public Builder setAccountIban(final String newAccountIBAN) {
            this.accountIBAN = newAccountIBAN;
            return this;
        }

        /**
         * Seteaza ibanul pentru contul clasic.
         * @param newClassicAccountIBAN
         * @return
         */
        public Builder setClassicAccountIBAN(final String newClassicAccountIBAN) {
            this.classicAccountIBAN = newClassicAccountIBAN;
            return this;
        }

        /**
         * Seteaza ibanul pentru contul de economii.
         * @param newSavingsAccountIBAN
         * @return
         */
        public Builder setSavingsAccountIBAN(final String newSavingsAccountIBAN) {
            this.savingsAccountIBAN = newSavingsAccountIBAN;
            return this;
        }

        /**
         * Seteaza noul plan de economii.
         * @param newPlanType1
         * @return
         */
        public Builder setNewPlanType(final String newPlanType1) {
            this.newPlanType = newPlanType1;
            return this;
        }

        /**
         * Seteaza timestamp-ul tranzactiei.
         * @param newTimestamp
         * @return
         */
        public Builder setTimestamp(final int newTimestamp) {
            this.timestamp = newTimestamp;
            return this;
        }
        /**
         * Seteaza eroarea tranzactiei.
         * @param newError
         * @return
         */
        public Builder setError(final String newError) {
            this.error = newError;
            return this;
        }
        /**
         * Seteaza descrierea tranzactiei.
         * @param newDescription
         * @return
         */
        public Builder setDescription(final String newDescription) {
            this.description = newDescription;
            return this;
        }
        /**
         * Seteaza IBAN-ul expeditorului.
         * @param newSenderIBAN
         * @return
         */
        public Builder setSenderIBAN(final String newSenderIBAN) {
            this.senderIBAN = newSenderIBAN;
            return this;
        }
        /**
         * Seteaza IBAN-ul destinatarului.
         * @param newReceiverIBAN
         * @return
         */
        public Builder setReceiverIBAN(final String newReceiverIBAN) {
            this.receiverIBAN = newReceiverIBAN;
            return this;
        }
        /**
         * Seteaza suma tranzactiei.
         * @param newAmount
         * @return
         */
        public Builder setAmount(final double newAmount) {
            this.amount = newAmount;
            return this;
        }
        /**
         * Seteaza valuta tranzactiei.
         * @param newCurrency
         * @return
         */
        public Builder setCurrency(final String newCurrency) {
            this.currency = newCurrency;
            return this;
        }
        /**
         * Seteaza tipul tranzactiei.
         * @param newTransferType
         * @return
         */
        public Builder setTransferType(final String newTransferType) {
            this.transferType = newTransferType;
            return this;
        }
        /**
         * Seteaza cardul tranzactiei.
         * @param newCard
         * @return
         */
        public Builder setCard(final String newCard) {
            this.card = newCard;
            return this;
        }
        /**
         * Seteaza detinatorul cardului.
         * @param newCardHolder
         * @return
         */
        public Builder setCardHolder(final String newCardHolder) {
            this.cardHolder = newCardHolder;
            return this;
        }
        /**
         * Seteaza contul tranzactiei.
         * @param newAccount
         * @return
         */
        public Builder setAccount(final String newAccount) {
            this.account = newAccount;
            return this;
        }
        /**
         * Seteaza comerciantul tranzactiei.
         * @param newCommerciant
         * @return
         */
        public Builder setCommerciant(final String newCommerciant) {
            this.commerciant = newCommerciant;
            return this;
        }
        /**
         * Seteaza suma si valuta tranzactiei ca un string.
         * @param newAmountPlusCurrency
         * @return
         */
        public Builder setAmountPlusCurrency(final String newAmountPlusCurrency) {
            this.amountPlusCurrency = newAmountPlusCurrency;
            return this;
        }
        /**
         * Seteaza conturile implicate in tranzactie(pt split).
         * @param newInvolvedAccounts
         * @return
         */
        public Builder setInvolvedAccounts(final List<String> newInvolvedAccounts) {
            this.involvedAccounts = newInvolvedAccounts;
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
