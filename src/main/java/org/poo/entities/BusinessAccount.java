package org.poo.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class BusinessAccount extends Account {
    private static final int DEFAULT_LIMIT = 500;

    private User owner;
    private ArrayList<User> managers;
    private ArrayList<User> employees;
    private String accountType;
    private double spendingLimit;
    private double depositLimit;
    private Map<User, Double> managersSpent = new HashMap<>();
    private Map<User, Double> managersDeposited = new HashMap<>();
    private Map<User, Double> employeesSpent = new HashMap<>();
    private Map<User, Double> employeesDeposited = new HashMap<>();
    private Map<User, Integer> timestampWhenBecameAssociate = new HashMap<>();
    private UserRepo userRepo;

    public BusinessAccount(final String iban, final String currency, final double balance,
                           final User owner, final String accountType, final UserRepo userRepo) {
        super(iban, currency, balance, owner.getEmail(), userRepo);
        this.owner = owner;
        this.managers = new ArrayList<>();
        this.employees = new ArrayList<>();
        this.accountType = accountType;
        this.userRepo = userRepo;
        if (currency.equals("RON")) {
            this.spendingLimit = DEFAULT_LIMIT;
            this.depositLimit = DEFAULT_LIMIT;
        } else {
            double conversionRate = userRepo.getExchangeRate("RON", currency);
            this.spendingLimit = DEFAULT_LIMIT * conversionRate;
            this.depositLimit = DEFAULT_LIMIT * conversionRate;
        }
    }

    /**
     * Returneaza timestamp-ul cand un user a devenit asociat.
     *
     * @return timestamp-ul cand un user a devenit asociat
     */
    public Map<User, Integer> getTimestampWhenBecameAssociate() {
        return timestampWhenBecameAssociate;
    }

    /**
     * Adauga timestamp-ul cand un user a devenit asociat.
     *
     * @param user      the user
     * @param timestamp the timestamp
     */
    public void addTimestampWhenBecameAssociate(final User user, final int timestamp) {
        timestampWhenBecameAssociate.put(user, timestamp);
    }

    @Override
    public String getAccountType() {
        return "business";
    }

    public User getOwner() {
        return owner;
    }

    public ArrayList<User> getManagers() {
        return managers;
    }

    public void setManagers(final ArrayList<User> managers) {
        this.managers = managers;
    }

    public ArrayList<User> getEmployees() {
        return employees;
    }

    public void setEmployees(final ArrayList<User> employees) {
        this.employees = employees;
    }

    /**
     * Adauga un angajat in lista de angajati.
     * @param employee
     */
    public void addEmployee(final User employee) {
        this.employees.add(employee);
    }

    /**
     * Adauga un manager in lista de manageri.
     * @param manager
     */
    public void addManager(final User manager) {
        this.managers.add(manager);
    }

    /**
     * Sterge un angajat din lista de angajati.
     * @param employee
     */
    public void removeEmployee(final User employee) {
        this.employees.remove(employee);
    }

    /**
     * Sterge un manager din lista de manageri.
     * @param manager
     */
    public void removeManager(final User manager) {
        this.managers.remove(manager);
    }

    /**
     * Returneaza limita de cheltuieli.
     * @return
     */
    public double getSpendingLimit() {
        return spendingLimit;
    }

    /**
     * Seteaza limita de cheltuieli.
     * @param spendingLimit
     */
    public void setSpendingLimit(final double spendingLimit) {
        this.spendingLimit = spendingLimit;
    }

    /**
     * Returneaza limita de depunere.
     * @return
     */
    public double getDepositLimit() {
        return depositLimit;
    }

    /**
     * Seteaza limita de depunere.
     * @param depositLimit
     */
    public void setDepositLimit(final double depositLimit) {
        this.depositLimit = depositLimit;
    }

    public void setAccountType(final String accountType) {
        this.accountType = accountType;
    }

    /**
     * Adauga o suma la depozitul unui manager.
     * @param user
     * @param amount
     */
    public void addManagerDeposit(final User user, final double amount) {
        if (managersDeposited.containsKey(user)) {
            managersDeposited.put(user, managersDeposited.get(user) + amount);
        } else {
            managersDeposited.put(user, amount);
        }
    }

    /**
     * Adauga o suma la depozitul unui angajat.
     * @param user
     * @param amount
     */
    public void addEmployeeDeposit(final User user, final double amount) {
        if (employeesDeposited.containsKey(user)) {
            employeesDeposited.put(user, employeesDeposited.get(user) + amount);
        } else {
            employeesDeposited.put(user, amount);
        }
    }

    /**
     * Adauga o suma la cheltuielile unui manager.
     * @param user
     * @param amount
     */
    public void addManagerSpent(final User user, final double amount) {
        if (managersSpent.containsKey(user)) {
            managersSpent.put(user, managersSpent.get(user) + amount);
        } else {
            managersSpent.put(user, amount);
        }
    }

    /**
     * Adauga o suma la cheltuielile unui angajat.
     * @param user
     * @param amount
     */
    public void addEmployeeSpent(final User user, final double amount) {
        if (employeesSpent.containsKey(user)) {
            employeesSpent.put(user, employeesSpent.get(user) + amount);
        } else {
            employeesSpent.put(user, amount);
        }
    }
}
