package org.poo.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.Observer.Observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Account implements Observer {
    private String iban;
    private String currency;
    private double balance;
    private ArrayList<Card> cards = new ArrayList<Card>();
    private double minimumBalance;
    private ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private String alias;
    private Map<String, Integer> nrOfTransactions;
    private double totalSpent;
    private Map<String, Boolean> cashbackReceived;
    private String email;
    private UserRepo userRepo;
    private Map<String, Double> spendingThresholdTotals = new HashMap<>();

    public void addSpendingThresholdTotal(String commerciant, double amount) {
        this.spendingThresholdTotals.put(commerciant, this.spendingThresholdTotals.getOrDefault(commerciant, 0.0) + amount);
    }

    public double getSpentForMerchant(String merchant) {
        return spendingThresholdTotals.getOrDefault(merchant, 0.0);
    }

    public void addSpent(double amount) {
        this.totalSpent += amount;
    }

    public Map<String, Boolean> getCashbackReceived() {
        return cashbackReceived;
    }

    public double getTotalSpent() {
        return this.totalSpent;
    }

    public void incrementTransaction(String category) {
        this.nrOfTransactions.put(category, this.nrOfTransactions.getOrDefault(category, 0) + 1);
    }

    public int getNrOfTransactions(String category) {
        return this.nrOfTransactions.getOrDefault(category, 0);
    }

    public boolean hasReceivedCashback(String category) {
        return this.cashbackReceived.getOrDefault(category, false);
    }

    public void markCashbackReceived(String category) {
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
                        businessAccount.addManagerSpent(initiator, Math.abs(transaction.getAmount()));
                    } else {
                        businessAccount.addManagerDeposit(initiator, transaction.getAmount());
                    }
                }
                else if (businessAccount.getEmployees().contains(initiator)) {
                    if (transaction.getDescription().equals("Card payment")) {
                        businessAccount.addEmployeeSpent(initiator, Math.abs(transaction.getAmount()));
                    } else {
                        businessAccount.addEmployeeDeposit(initiator, transaction.getAmount());
                    }
                } else {
                    if(initiator == businessAccount.getOwner()) {
                        System.out.println("DEBUG: Initiator " + initiator.getEmail() + " is the owner of this business account.");
                    }else {
                        System.out.println("DEBUG: Initiator " + initiator.getEmail() + " not part of this business account.");
                    }
                }
            }
        }
    }

    public Account(final String iban, final String currency, final double balance, final String email, UserRepo userRepo) {
        this.iban = iban;
        this.currency = currency;
        this.balance = balance;
        this.email = email;
        this.userRepo = userRepo;
        this.nrOfTransactions = new HashMap<>(); // Inițializare în constructor
        this.cashbackReceived = new HashMap<>();
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
        accountNode.put("balance", Math.round(balance * 100.0) / 100.0);
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

    public String getEmail() {
        return email;
    }

    public UserRepo getUserRepo() {
        return userRepo;
    }

    public Map<String, Double> getSpendingThresholdTotals() {
        return spendingThresholdTotals;
    }

    public void setSpendingThresholdTotals(Map<String, Double> spendingThresholdTotals) {
        this.spendingThresholdTotals = spendingThresholdTotals;
    }
}
