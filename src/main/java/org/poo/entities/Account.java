package org.poo.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Account {
    private String iban;
    private String currency;
    private double balance;
    private ArrayList<Card> cards = new ArrayList<Card>();
    private double minimumBalance;
    private ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private String alias;
    private Map<String, Integer> nrOfTransactions;
    private double totalSpendingThreshold;
    private Map<String, Boolean> cashbackReceived;
    private String email;
    private UserRepo userRepo;
    private Map<String, Double> spendingThresholdTotals = new HashMap<>();
    private Map<String, Double> pendingCategoryDiscounts = new HashMap<>();

    /**
     * Returneaza totalul cheltuit pe cont la comercianti de tipul spendingThreshold
     * @return
     */

    public double getTotalSpendingThreshold() {
        return totalSpendingThreshold;
    }

    /**
     * Adauga o suma la totalul cheltuit pe cont la comercianti de tipul spendingThreshold
     * @param amount
     */
    public void addTotalSpendingThreshold(final double amount) {
        this.totalSpendingThreshold += amount;
    }

    /**
     * Adauga o tranzactie in lista de tranzactii a contului
     * @param category
     */
    public void incrementNrOfTransactions(final String category) {
        this.nrOfTransactions.put(category, this.nrOfTransactions.getOrDefault(category, 0) + 1);
    }

    /**
     * Adauga un discount in asteptare pentru o anumita categorie
     * @param category
     * @param amount
     */
    public void addPendingCategoryDiscount(final String category, final double amount) {
        this.pendingCategoryDiscounts.put(category,
                this.pendingCategoryDiscounts
                        .getOrDefault(category, 0.0) + amount);
    }

    /**
     * Returneaza discountul in asteptare pentru o anumita categorie
     * @param category
     * @return
     */
    public double getPendingCategoryDiscount(final String category) {
        return this.pendingCategoryDiscounts.getOrDefault(category, 0.0);
    }

    /**
     * Adauga suma cheltuita la totalul cheltuit pe un anumit comerciant
     * @param commerciant
     * @param amount
     */
    public void addSpendingThresholdTotal(final String commerciant, final double amount) {
        this.spendingThresholdTotals.put(commerciant,
                this.spendingThresholdTotals
                        .getOrDefault(commerciant, 0.0) + amount);
    }

    /**
     * Returneaza suma cheltuita pe un anumit comerciant
     * @param merchant
     * @return
     */
    public double getSpentForMerchant(final String merchant) {
        return spendingThresholdTotals.getOrDefault(merchant, 0.0);
    }

    /**
     * Returneaza numarul de tranzactii efectuate pe un anumit comerciant
     * @param category
     * @return
     */
    public int getNrOfTransactions(final String category) {
        return this.nrOfTransactions.getOrDefault(category, 0);
    }

    /**
     * Returneaza daca s-a primit cashback pentru o anumita categorie
     * @param category
     * @return
     */
    public boolean hasReceivedCashback(final String category) {
        return this.cashbackReceived.getOrDefault(category, false);
    }

    /**
     * Marcheaza ca s-a primit cashback pentru o anumita categorie
     * @param category
     */
    public void markCashbackReceived(final String category) {
        this.cashbackReceived.put(category, true);
    }

    /**
     * @return tranzactiile efectuate pe cont
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }
    /**
     * Adauga o tranzactie in lista de tranzactii a contului
     * @param transaction tranzactia de adaugat
     */
    public void addTransaction(final Transaction transaction) {
        transactions.add(transaction);
        User initiator = transaction.getInitiator();
        if (initiator != null) {
            if (this.getAccountType().equals("business")) {
                BusinessAccount businessAccount = (BusinessAccount) this;
                if (businessAccount.getManagers().contains(initiator)) {
                    if (transaction.getDescription().equals("Card payment")) {
                        businessAccount.addManagerSpent(initiator, transaction.getAmount());
                    } else {
                        businessAccount.addManagerDeposit(initiator, transaction.getAmount());
                    }
                } else if (businessAccount.getEmployees().contains(initiator)) {
                    if (transaction.getDescription().equals("Card payment")) {
                        businessAccount.addEmployeeSpent(initiator, transaction.getAmount());
                    } else {
                        businessAccount.addEmployeeDeposit(initiator, transaction.getAmount());
                    }
                } else {
                    return;
                }
            }
        }
    }

    public Account(final String iban, final String currency, final double balance,
                   final String email, final UserRepo userRepo) {
        this.iban = iban;
        this.currency = currency;
        this.balance = balance;
        this.email = email;
        this.userRepo = userRepo;
        this.nrOfTransactions = new HashMap<>(); // Inițializare în constructor
        this.cashbackReceived = new HashMap<>();
        this.spendingThresholdTotals = new HashMap<>();
        this.pendingCategoryDiscounts = new HashMap<>();
        this.cashbackReceived.put("Food", false);
        this.cashbackReceived.put("Tech", false);
        this.cashbackReceived.put("Clothes", false);
        this.totalSpendingThreshold = 0.0;
    }


    /**
     * Adauga un card in lista de carduri a contului
     * @param card cardul de adaugat
     */
    public void addCard(final Card card) {
        cards.add(card);
    }
    /**
     * Returneaza lista de carduri a contului
     * @return lista de carduri a contului
     */
    public List<Card> getCards() {
        return cards;
    }
    /**
     * Returneaza IBAN-ul contului
     * @return IBAN-ul contului
     */
    public String getIban() {
        return iban;
    }
    /**
     * Returneaza valuta contului
     * @return valuta contului
     */
    public String getCurrency() {
        return currency;
    }
    /**
     * Returneaza soldul contului
     * @return soldul contului
     */
    public double getBalance() {
        return balance;
    }
    /**
     * Seteaza soldul contului
     * @param balance soldul contului
     */
    public void setBalance(final double balance) {
        this.balance = balance;
    }
    /**
     * Returneaza cardul cu numarul cardNumber
     * @param cardNumber numarul cardului
     * @return cardul cu numarul cardNumber
     */
    public Card getCard(final String cardNumber) {
        for (Card card : cards) {
            if (card.getCardNumber().equals(cardNumber)) {
                return card;
            }
        }
        return null;
    }
    /**
     * Sterge cardul card din lista de carduri a contului
     * @param card cardul de sters
     */
    public void removeCard(final Card card) {
        cards.remove(card);
    }
    /**
     * Seteaza valoarea minima a soldului contului
     * @param minimumBalance valoarea minima a soldului contului
     */
    public void setMinimumBalance(final double minimumBalance) {
        this.minimumBalance = minimumBalance;
    }
    /**
     * Returneaza valoarea minima a soldului contului
     * @return valoarea minima a soldului contului
     */
    public double getMinimumBalance() {
        return minimumBalance;
    }
    /**
     * Returneaza rata dobanzii
     * @return rata dobanzii
     */
    public void setInterestRate(final double interestRate) {
        throw new UnsupportedOperationException(
                "Interest rate not supported for this account type.");
    }
    /**
     * Returneaza un obiect de tip ObjectNode care contine informatii despre cont.
     *
     * @param objectMapper obiect de tip ObjectMapper
     * @return obiect de tip ObjectNode care contine informatii despre cont
     */
    public ObjectNode toJson(final ObjectMapper objectMapper) {
        ObjectNode accountNode = objectMapper.createObjectNode();
        accountNode.put("IBAN", iban);
        accountNode.put("balance", balance);
        accountNode.put("currency", currency);
        accountNode.put("type", getAccountType());

        ArrayNode cardsArray = objectMapper.createArrayNode();
        for (Card card : cards) {
            cardsArray.add(card.toJson(objectMapper));
        }
        accountNode.set("cards", cardsArray);

        return accountNode;
    }

    /**
     * Returneaza aliasul contului
     * @return aliasul contului
     */
    public String getAlias() {
        return alias;
    }
    /**
     * Seteaza aliasul contului
     * @param alias aliasul contului
     */
    public void setAlias(final String alias) {
        this.alias = alias;
    }

    /**
     * Returneaza tipul contului
     * @return tipul contului
     */
    public abstract String getAccountType();

    /**
     * Returneaza emailul utilizatorului
     * @return
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returneaza repo-ul de utilizatori
     * @return
     */
    public UserRepo getUserRepo() {
        return userRepo;
    }

    /**
     * Sterge discountul in asteptare pentru o anumita categorie
     * @param merchantCategory
     */
    public void clearPendingCategoryDiscount(final String merchantCategory) {
        this.pendingCategoryDiscounts.put(merchantCategory, 0.0);
    }
}
