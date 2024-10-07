package com.abc_bank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class CacheHandler {

    private static final File CHECKING_ACC_CACHE_FILE = new File("cache/checking_Accounts.ser");
    private static final File SAVING_ACC_CACHE_FILE = new File("cache/saving_Accounts.ser");
    private static final File INVESTMENT_ACC_CACHE_FILE = new File("cache/investment_Accounts.ser");
    private static final File CD_CACHE_FILE = new File("cache/certificated_Deposit_Accounts.ser");
    private static final ArrayList<File> files = new ArrayList<>(
            Arrays.asList(CHECKING_ACC_CACHE_FILE, SAVING_ACC_CACHE_FILE, INVESTMENT_ACC_CACHE_FILE, CD_CACHE_FILE));

    private static boolean fileIsEmpty(File file) {
        boolean status = false;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            if (br.readLine() == null) {
                status = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }

    public static void exportCache(ArrayList<ArrayList<Account>> allAccounts, boolean reExport) {
        int accountTypeCount = allAccounts.size();
        for (int i = 0; i < accountTypeCount; i++) {
            File file = files.get(i);
            try {
                if (file.createNewFile() && !reExport) {
                    // recreating all the cache files and importing data to them from the db
                    // if even one of the cache files doesn't exist
                    System.out.println("\nError! Corrupted Cache");
                    purgeCache();
                    createNewCache();
                }
                Serializer.serialize(allAccounts.get(i), file);
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<ArrayList<Account>> importCache(int accountTypeCount) throws IOException, SQLException {
        ArrayList<ArrayList<Account>> allAccounts = new ArrayList<>();
        for (int i = 0; i < accountTypeCount; i++) {
            File file = files.get(i);
            if (file.createNewFile() || fileIsEmpty(file)) {
                // recreating all the cache files and importing data to them from the db
                // if even one of the cache files doesn't exist
                System.out.println("\nError! Corrupted Cache");
                purgeCache();
                createNewCache();
            }
            ArrayList<Account> accounts = Serializer.deserialize(file);
            allAccounts.add(accounts);
        }
        return allAccounts;
    }

    public static ArrayList<ArrayList<Account>> resetMonthlyTransactionCount(ArrayList<ArrayList<Account>> allAccList) {
        ArrayList<ArrayList<Account>> updatedAllAccList = new ArrayList<>();
        ArrayList<Account> updatedAccList = new ArrayList<>();
        for(ArrayList<Account> accList : allAccList) {
            updatedAccList.clear();
            for(Account acc: accList) {
                acc.resetMonthlyTransactionCount();
                updatedAccList.add(acc);
            }
            updatedAllAccList.add(updatedAccList);
        }

        return updatedAllAccList;
    }

    public static void addInterest(ArrayList<ArrayList<Account>> allAccList) throws SQLException {
        Database_Handler db_handler = new Database_Handler();
        float currentAccBalance = 0;
        float newAccBalance;
        float interestRate = 0;

        for(ArrayList<Account> accList : allAccList) {
            for(Account acc: accList) {
                String query =
                        "SELECT holderAccBalance, accTypeInterestRate " +
                        "FROM Holder INNER JOIN AccountType " +
                        "WHERE holderAccNumber = ? " +
                        "AND holderAccTypeId = accTypeId";
                db_handler.prepareQuery(query, acc.getAccountNumber());
                ResultSet resultSet = db_handler.getData();
                assert resultSet != null;
                if(resultSet.next()) {
                    currentAccBalance = resultSet.getFloat(1);
                    interestRate = resultSet.getFloat(2);
                } else {
                    System.out.println("\nError while adding interests\n");
                }
                newAccBalance = currentAccBalance + currentAccBalance * interestRate / 100;
                query = "UPDATE Holder SET holderAccBalance = ?";
                db_handler.prepareQuery(query, String.valueOf(newAccBalance));
                db_handler.saveData();
            }
        }
    }

    public static void purgeCache() {
        for (File file : files) {
            boolean _ = file.delete();
        }
    }

    public static void createNewCache() throws SQLException {
        String query =
                "SELECT holderName, holderMobileNumber, holderEmail, holderAccNumber, holderAccTypeId, monthlyTransactionCount FROM Holder";
        Database_Handler db_handler = new Database_Handler();
        String Name, mobilePhoneNumber, Email, accountNumber, accountType;
        int monthlyTransactionCount;
        ArrayList<Account> checkingAccounts = new ArrayList<>();
        ArrayList<Account> savingAccounts = new ArrayList<>();
        ArrayList<Account> investmentAccounts = new ArrayList<>();
        ArrayList<Account> certificatedDepositAccounts = new ArrayList<>();
        ArrayList<ArrayList<Account>> allAccounts = new ArrayList<>();

        db_handler.prepareQuery(query);
        ResultSet results = db_handler.getData();
        while (true) {
            assert results != null;
            if (!results.next()) break;
            Name = results.getString(1);
            mobilePhoneNumber = results.getString(2);
            Email = results.getString(3);
            accountNumber = results.getString(4);
            accountType = results.getString(5);
            monthlyTransactionCount = results.getInt(6);
            Account newAcc;

            switch (accountType) {
                case "CHK_ACC" -> {
                    newAcc = new CheckingAccount(Name, mobilePhoneNumber, Email, Integer.parseInt(accountNumber));
                    newAcc.setMonthlyTransactionCount(monthlyTransactionCount);
                    checkingAccounts.add(newAcc);
                }
                case "SVG_ACC" -> {
                    newAcc = new SavingAccount(Name, mobilePhoneNumber, Email, Integer.parseInt(accountNumber));
                    newAcc.setMonthlyTransactionCount(monthlyTransactionCount);
                    savingAccounts.add(newAcc);
                }
                case "IVM_ACC" -> {
                    newAcc = new InvestmentAccount(Name, mobilePhoneNumber, Email, Integer.parseInt(accountNumber));
                    newAcc.setMonthlyTransactionCount(monthlyTransactionCount);
                    investmentAccounts.add(newAcc);
                }
                case "CD_ACC" -> {
                    newAcc = new CertifiedDepositAccount(Name, mobilePhoneNumber, Email, Integer.parseInt(accountNumber));
                    newAcc.setMonthlyTransactionCount(monthlyTransactionCount);
                    certificatedDepositAccounts.add(newAcc);
                }
            }
        }

        results.close();

        allAccounts.add(0, checkingAccounts);
        allAccounts.add(1, savingAccounts);
        allAccounts.add(2, investmentAccounts);
        allAccounts.add(3, certificatedDepositAccounts);

        exportCache(allAccounts, true);
        System.out.println("\n\tCache Restored Successfully!");
    }

}