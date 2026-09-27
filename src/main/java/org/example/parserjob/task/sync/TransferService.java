package org.example.parserjob.task.sync;

public class TransferService {

    // DEADLOCK
    public static void deadlockTransfer(BankAccount from, BankAccount to, long amount) {
        synchronized (from) {
            sleep(50);
            synchronized (to) {
                from.withdraw(amount);
                to.deposit(amount);
            }
        }
    }

    // БЕЗ DEADLOCK
    public static void safeTransfer(BankAccount from, BankAccount to, long amount) {
        BankAccount first = from.getId().compareTo(to.getId()) < 0 ? from : to;
        BankAccount second = first == from ? to : from;
        synchronized (first) {
            synchronized (second) {
                from.withdraw(amount);
                to.deposit(amount);
            }
        }
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}