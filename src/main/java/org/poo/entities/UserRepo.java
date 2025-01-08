package org.poo.entities;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public final class UserRepo {
    private Map<String, User> users = new LinkedHashMap<>();
    private List<ExchangeRate> exchangeRates = new ArrayList<>();
    private List<Commerciant> comerciants = new ArrayList<>();
    private List<User> usersInReadOrder = new ArrayList<>();

    public UserRepo() {
    }

    public void addCommerciant(final Commerciant commerciant) {
        comerciants.add(commerciant);
    }

    public Commerciant getCommerciant(final String name) {
        for (Commerciant commerciant : comerciants) {
            if (commerciant.getCommerciant().equals(name)) {
                return commerciant;
            }
        }
        return null;
    }

    /**
     * Adauga un utilizator in lista de utilizatori
     * @param user utilizatorul care trebuie adaugat
     */
    public void addUser(final User user) {
        if (users.containsKey(user.getEmail())) {
            usersInReadOrder.add(user);
        } else {
            users.put(user.getEmail(), user);
            usersInReadOrder.add(user);
        }
    }

    /**
     * Gaseste un utilizator dupa email
     * @param email email-ul utilizatorului
     */
    public User getUser(final String email) {
        return users.get(email);
    }
    /**
     * Adauga un exchange rate in lista de exchange rates
     * @param from valuta de la care se face conversia
     * @param to valuta in care se face conversia
     * @param rate rata de schimb
     */
    public void addExchangeRate(final String from, final String to, final double rate) {
        exchangeRates.add(new ExchangeRate(from, to, rate));
    }
    /**
     * Returneaza rata de schimb dintre doua valute
     * @param from valuta de la care se face conversia
     * @param to valuta in care se face conversia
     */
    public double getExchangeRate(final String from, final String to) {
        return getExchangeRateBFS(
                (from == null ? null : from.toUpperCase()),
                (to == null ? null : to.toUpperCase())
        );
    }

    public List<Commerciant> getComerciants() {
        return comerciants;
    }

    public Commerciant getCommerciantByAccount(final String account) {
        for (Commerciant c : comerciants) {
            if (c.getAccount().equals(account)) {
                return c;
            }
        }
        return null;
    }

    public double getPlanCommissionRate(User user, double transactionAmount) {
        String plan = user.getServicePlan().toLowerCase();
        switch (plan) {
            case "standard":
                return 0.002;

            case "student":
                return 0.0;

            case "silver":
                if (transactionAmount < 500) {
                    return 0.0;
                } else {
                    return 0.001;
                }

            case "gold":
                return 0.0;

            default:
                return 0.0;
        }
    }

    private double getExchangeRateBFS(String from, String to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Currency values cannot be null");
        }
        if (from.equals(to)) {
            return 1.0;
        }

        Map<String, Map<String, Double>> adjacencyMap = new HashMap<>();
        for (ExchangeRate rate : exchangeRates) {
            String f = rate.getFrom().toUpperCase();
            String t = rate.getTo().toUpperCase();
            double r = rate.getRate();

            adjacencyMap.putIfAbsent(f, new HashMap<>());
            adjacencyMap.putIfAbsent(t, new HashMap<>());

            adjacencyMap.get(f).put(t, r);

            adjacencyMap.get(t).put(f, 1.0 / r);
        }

        Queue<String> queue = new LinkedList<>();
        Map<String, Double> dist = new HashMap<>();
        queue.add(from);
        dist.put(from, 1.0);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            double currentRate = dist.get(current);

            if (!adjacencyMap.containsKey(current)) {
                continue;
            }

            for (Map.Entry<String, Double> edge : adjacencyMap.get(current).entrySet()) {
                String neighbor = edge.getKey();
                double edgeRate = edge.getValue();

                double newRate = currentRate * edgeRate;
                if (!dist.containsKey(neighbor)) {
                    dist.put(neighbor, newRate);
                    queue.add(neighbor);
                }
                if (neighbor.equals(to)) {
                    return newRate;
                }
            }
        }

        return -1.0;
    }

    /**
     * Returneaza IBAN-ul unui cont dupa alias
     * @param alias alias-ul contului
     */
    public String getIBANByAlias(final String alias) {
        for (User user : users.values()) {
            for (Account account : user.getAccounts()) {
                if (alias.equals(account.getAlias())) {
                    return account.getIban();
                }
            }
        }
        return null;
    }

    /**
     * Returneaza un utilizator dupa IBAN
     * @param iban IBAN-ul contului
     */
    public User getUserByIBAN(final String iban) {
        for (User user : users.values()) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(iban)) {
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Returneaza toti utilizatorii
     */
    public Collection<User> getAllUsers() {
        return users.values();
    }

    /**
     * Returneaza un utilizator dupa numarul de card
     * @param cardNumber numarul de card
     */
    public User getUserByCardNumber(final String cardNumber) {
        for (User user : users.values()) {
            for (Account account : user.getAccounts()) {
                for (Card card : account.getCards()) {
                    if (card.getCardNumber().equals(cardNumber)) {
                        return user;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Sterge un cont dupa email si IBAN
     * @param email email-ul utilizatorului
     * @param iban IBAN-ul contului
     */
    public boolean deleteAccount(final String email, final String iban) {
        User user = getUser(email);
        if (user == null) {
            return false;
        }
        return user.getAccounts()
                .removeIf(account -> account.getIban().equals(iban)
                        && account.getBalance() == 0);
    }

    /**
     * Returneaza un obiect de tip ArrayNode care contine informatii despre utilizatori
     * @param objectMapper obiect de tip ObjectMapper
     */
    public ArrayNode toJson(final ObjectMapper objectMapper) {
        ArrayNode usersArray = objectMapper.createArrayNode();
        for (User user : usersInReadOrder) {
            usersArray.add(user.toJson(objectMapper));
        }
        return usersArray;
    }


    /**
     * Returneaza un cont dupa IBAN
     * @param accountIBAN IBAN-ul contului
     */
    public Account getAccountByIBAN(final String accountIBAN) {
        for (User user : users.values()) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(accountIBAN)) {
                    return account;
                }
            }
        }
        return null;
    }



}
