package org.example.parserjob.task.sync;

public class BankAccount {
    private final String id;
    private long balance;

    public BankAccount(String id, long balance) {
        this.id = id;
        this.balance = balance;
    }

    public String getId() { return id; }
    public long getBalance() { return balance; }
    public void withdraw(long amount) { balance -= amount; }
    public void deposit(long amount) { balance += amount; }
}