package org.poo.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class User {
    private String firstName;
    private String lastName;
    private String email;
    private String birthDate;
    private String occupation;
    private String servicePlan;
    private ArrayList<Account> accounts;
    private HashMap<String, String> aliases = new HashMap<>();
    private HashMap<String, Integer> nrOfTransactions = new HashMap<>();
    private int nrOfTransactionsOver300RON;
    private Map<String, Boolean> cashbackReceived;
    private ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private double totalSpent;


    public User(final String firstName, final String lastName, final String email,
                final String birthDate, final String occupation) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.occupation = occupation;
        if (occupation.equals("student")) {
            this.servicePlan = "student";
        } else {
            this.servicePlan = "standard";
        }
        this.accounts = new ArrayList<>();
        this.totalSpent = 0.0;
        this.cashbackReceived = new HashMap<>();
        this.nrOfTransactionsOver300RON = 0;
    }

    /**
     * Adauga o tranzactie in lista de tranzactii a utilizatorului
     * @param transaction
     */
    public void addTransaction(final Transaction transaction) {
        transactions.add(transaction);
    }

    /**
     * Returneaza tranzactiile utilizatorului
     * @return
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * Returneaza totalul cheltuit de utilizator
     * @param amount
     */
    public void addSpent(final double amount) {
        totalSpent += amount;
    }

    /**
     * Incrementeaza numarul de tranzactii peste 300RON
     */
    public void addNrOfTransactionsOver300RON() {
        nrOfTransactionsOver300RON++;
    }

    /**
     * Returneaza numarul de tranzactii peste 300RON
     * @return
     */
    public int getNrOfTransactionsOver300RON() {
        return nrOfTransactionsOver300RON;
    }

    /**
     * Adauga un cont nou
     * @param account contul de adaugat
     */
    public void addAccount(final Account account) {
        accounts.add(account);
    }
    /**
     * Returneaza conturile utilizatorului
     * @return conturile utilizatorului
     */
    public List<Account> getAccounts() {
        return accounts;
    }
    /**
     * Sterge un cont
     * @param accountIBAN IBAN-ul contului de sters
     */
    public void deleteAccount(final String accountIBAN) {
        accounts.removeIf(account -> account.getIban().equals(accountIBAN));
    }
    /**
     * Returneaza prenumele utilizatorului
     * @return prenumele utilizatorului
     */
    public String getFirstName() {
        return firstName;
    }
    /**
     * Seteaza prenumele utilizatorului
     * @param firstName prenumele de setat
     */
    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }
    /**
     * Returneaza numele utilizatorului
     * @return numele utilizatorului
     */
    public String getLastName() {
        return lastName;
    }
    /**
     * Seteaza numele utilizatorului
     * @param lastName numele de setat
     */
    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }
    /**
     * Returneaza emailul utilizatorului
     * @return emailul utilizatorului
     */
    public String getEmail() {
        return email;
    }
    /**
     * Seteaza emailul utilizatorului
     * @param email emailul de setat
     */
    public void setEmail(final String email) {
        this.email = email;
    }
    /**
     * Returneaza contul cu IBAN-ul dat
     * @return contul cu IBAN-ul dat
     */
    public Account getAccount(final String iban) {
        for (Account account : accounts) {
            if (account.getIban().trim().equals(iban.trim())) {
                return account;
            }
        }
        return null;
    }
    /**
     * Returneaza contul dupa un numar de card
     * @return contul cu cardul dat
     */
    public Account getAccountByCardNumber(final String cardNumber) {
        for (Account account : accounts) {
            if (account.getCard(cardNumber) != null) {
                return account;
            }
        }
        return null;
    }
    /**
     * Seteaza un alias pentru un cont
     * @param alias aliasul de setat
     */
    public void setAlias(final String alias, final String iban) {
        boolean accountExists = accounts.stream()
                .anyMatch(account -> account.getIban().equals(iban));
        if (!accountExists) {
            throw new IllegalArgumentException("IBAN not found for this user");
        }
        aliases.put(alias, iban);
    }
    /**
     * Returneaza un obiect de tip ObjectNode care contine informatii despre utilizator.
     * @param objectMapper obiect de tip ObjectMapper
     */
    public ObjectNode toJson(final ObjectMapper objectMapper) {
        ObjectNode userNode = objectMapper.createObjectNode();
        userNode.put("firstName", firstName);
        userNode.put("lastName", lastName);
        userNode.put("email", email);

        ArrayNode accountsArray = objectMapper.createArrayNode();
        Set<String> seenIban = new HashSet<>();

        for (Account account : accounts) {
            if (account.getAccountType().equals("business")
                    && ((BusinessAccount) account).getOwner() != this) {
                continue;
            }

            String iban = account.getIban();
            if (!seenIban.contains(iban)) {
                seenIban.add(iban);
                accountsArray.add(account.toJson(objectMapper));
            }
        }
        userNode.set("accounts", accountsArray);

        return userNode;
    }

    public String getServicePlan() {
        return servicePlan;
    }

    public void setServicePlan(final String servicePlan) {
        this.servicePlan = servicePlan;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(final String occupation) {
        this.occupation = occupation;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(final String birthDate) {
        this.birthDate = birthDate;
    }

    public String getFullName() {
        return lastName + " " + firstName;
    }

}
