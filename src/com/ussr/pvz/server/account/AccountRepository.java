package com.ussr.pvz.server.account;

import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.Collection;
import com.ussr.pvz.shared.account.AccountState;

import java.util.ArrayList;
import java.util.List;

public class AccountRepository {

    private final List<Account> accounts;

    public AccountRepository() {

        this.accounts =
                new ArrayList<>(
                        SaveService.loadAccounts()
                                .stream()
                                .map(state ->
                                        new Account(
                                                state,
                                                new Collection(
                                                        new ArrayList<>(),
                                                        new ArrayList<>()
                                                )
                                        )
                                )
                                .toList()
                );
    }

    public synchronized Account findByUsername(
            String username
    ) {

        if (username == null) {
            return null;
        }

        return accounts.stream()
                .filter(account ->
                        account.getName() != null
                                && account.getName()
                                .equalsIgnoreCase(username)
                )
                .findFirst()
                .orElse(null);
    }

    public synchronized boolean usernameExists(
            String username
    ) {

        return findByUsername(username) != null;
    }

    public synchronized void add(
            Account account
    ) {

        if (account == null) {
            return;
        }

        accounts.add(account);

        save();
    }

    public synchronized void save() {

        List<AccountState> states =
                accounts.stream()
                        .map(Account::toState)
                        .toList();

        SaveService.saveAccounts(
                states
        );
    }

    public synchronized List<Account>
    getAccounts() {

        return new ArrayList<>(
                accounts
        );
    }

    public synchronized int size() {

        return accounts.size();
    }
}