package com.ussr.pvz.server.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ussr.pvz.shared.account.AccountState;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SaveService {

    private static final String FILE_PATH =
            "users.json";

    private static final Gson gson =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private SaveService() {
    }

    public static synchronized void saveAccounts(
            List<AccountState> accounts
    ) {

        try (FileWriter writer =
                     new FileWriter(FILE_PATH)) {

            gson.toJson(
                    accounts,
                    writer
            );

        } catch (IOException e) {

            System.err.println(
                    "Critical Error: Could not save accounts to disk: "
                            + e.getMessage()
            );
        }
    }

    public static synchronized List<AccountState>
    loadAccounts() {

        File file =
                new File(FILE_PATH);

        if (!file.exists() ||
                file.length() == 0) {

            System.out.println(
                    "No existing save file found. Starting with a fresh database."
            );

            return new ArrayList<>();
        }

        try (FileReader reader =
                     new FileReader(file)) {

            Type listType =
                    new TypeToken<
                            List<AccountState>
                            >() {
                    }.getType();

            List<AccountState> loadedAccounts =
                    gson.fromJson(
                            reader,
                            listType
                    );

            return loadedAccounts != null
                    ? loadedAccounts
                    : new ArrayList<>();

        } catch (IOException e) {

            System.err.println(
                    "Critical Error: Could not read accounts from disk: "
                            + e.getMessage()
            );

            return new ArrayList<>();
        }
    }
}