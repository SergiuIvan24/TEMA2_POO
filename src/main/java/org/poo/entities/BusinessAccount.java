package org.poo.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BusinessAccount extends Account {
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
    private Map<User, Integer> timestampWhenBecameAssociate = new HashMap();
    private UserRepo userRepo;

    public BusinessAccount(final String iban, final String currency, final double balance,
                           final User owner, final String accountType, final UserRepo userRepo) {
        super(iban, currency, balance, owner.getEmail(), userRepo);
        this.owner = owner;
        this.managers = new ArrayList<>();
        this.employees = new ArrayList<>();
        this.accountType = accountType;
        this.userRepo = userRepo;
        if(currency.equals("RON")) {
            this.spendingLimit = 500;
            this.depositLimit = 500;
        } else {
            double conversionRate = userRepo.getExchangeRate("RON", currency);
            this.spendingLimit = round2(500 * conversionRate);
            this.depositLimit = round2(500 * conversionRate);
        }
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public Map<User, Integer> getTimestampWhenBecameAssociate() {
        return timestampWhenBecameAssociate;
    }

    public void addTimestampWhenBecameAssociate(User user, int timestamp) {
        timestampWhenBecameAssociate.put(user, timestamp);
    }

    @Override
    public String getAccountType() {
        return "business";
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public ArrayList<User> getManagers() {
        return managers;
    }

    public void setManagers(ArrayList<User> managers) {
        this.managers = managers;
    }

    public ArrayList<User> getEmployees() {
        return employees;
    }

    public void setEmployees(ArrayList<User> employees) {
        this.employees = employees;
    }

    public void addEmployee(User employee) {
        this.employees.add(employee);
    }

    public void addManager(User manager) {
        this.managers.add(manager);
    }

    public void removeEmployee(User employee) {
        this.employees.remove(employee);
    }

    public void removeManager(User manager) {
        this.managers.remove(manager);
    }

    public double getSpendingLimit() {
        return spendingLimit;
    }

    public void setSpendingLimit(double spendingLimit) {
        this.spendingLimit = spendingLimit;
    }

    public double getDepositLimit() {
        return depositLimit;
    }

    public void setDepositLimit(double depositLimit) {
        this.depositLimit = depositLimit;
    }

    @Override
    public void update(boolean accepted) {
        return;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void addManagerDeposit(User user, double amount) {
        if (managersDeposited.containsKey(user)) {
            managersDeposited.put(user, managersDeposited.get(user) + amount);
        } else {
            managersDeposited.put(user, amount);
           }
    }

    public void addEmployeeDeposit(User user, double amount) {
        if (employeesDeposited.containsKey(user)) {
            employeesDeposited.put(user, employeesDeposited.get(user) + amount);
        } else {
            employeesDeposited.put(user, amount);
        }
    }

    public void addManagerSpent(User user, double amount) {
        if (managersSpent.containsKey(user)) {
            managersSpent.put(user, managersSpent.get(user) + amount);
        } else {
            managersSpent.put(user, amount);
        }
    }

    public void addEmployeeSpent(User user, double amount) {
        if (employeesSpent.containsKey(user)) {
            employeesSpent.put(user, employeesSpent.get(user) + amount);
        } else {
            employeesSpent.put(user, amount);
        }
    }
}
