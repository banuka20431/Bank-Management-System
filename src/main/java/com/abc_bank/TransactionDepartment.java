package com.abc_bank;


import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.InputMismatchException;
import org.json.JSONObject;

public class TransactionDepartment {

    public static final String[] accountTypes = {
            "Checking Account",
            "Saving Account",
            "Investment Account",
            "Certificated Deposit Account"
    };
    public static final String[] accountRelatedHeaders = {"Online Banking", "Debit Card", "SMS Alerts"};
    private static final String[] mainMenu = {
            "Create an account",
            "Make a Deposit",
            "Make a Withdrawal",
            "Alter an existing account",
            "Renew Cache"
    };
    public static final String[] personalDataHeaders = {
            "Title", "Full Name", "Name with Initials", "Date of Birth",
            "National Identity Card Number", "Address", "Postal Code",
            "Mobile Phone Number", "Telephone Number", "Email", "Profession"
    };
    public static final ArrayList<Account> checkingAccounts = new ArrayList<>();
    public static final ArrayList<Account> savingAccounts = new ArrayList<>();
    public static final ArrayList<Account> investmentAccounts = new ArrayList<>();
    public static final ArrayList<Account> certificatedDepositAccounts = new ArrayList<>();
    public static  ArrayList<ArrayList<Account>> allAccounts;

    static {
        try {
            allAccounts = CacheHandler.importCache(accountTypes.length);
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void TSAD_Main(String employeeName, boolean newMonthStarted) throws SQLException, IOException {

        ArrayList<ArrayList<Account>> updatedAllAccList;
        String jsonData = FileHandler.read(Main.CONFIG_FILE_PATH);
        JSONObject jsonObject = new JSONObject(jsonData);

        if (newMonthStarted) {
            // resting monthly transaction count
            boolean resetMonthlyTransactionLimitDone = jsonObject.getBoolean("IsResetMonthlyTransactionLimitDone");
            if (!resetMonthlyTransactionLimitDone) {
                allAccounts = CacheHandler.resetMonthlyTransactionCount(allAccounts);
                FileHandler.write(String.valueOf(jsonObject), Main.CONFIG_FILE_PATH);
                // adding interest to all accounts
                CacheHandler.addInterest(allAccounts);
            }
            jsonObject.put("IsResetMonthlyTransactionLimitDone", true);
        } else {
            jsonObject.put("IsResetMonthlyTransactionLimitDone", false);
            FileHandler.write(String.valueOf(jsonObject), Main.CONFIG_FILE_PATH);
        }


        System.out.println("\nSelect an Action >");
        int selectedOptionIndex = Accessories.Menu.displayMenu(mainMenu, true);
        String selectedOption = mainMenu[selectedOptionIndex];
        switch (selectedOption) {
            case "Create an account" -> {
                updatedAllAccList = createNewBankAccount(employeeName);
                assert updatedAllAccList != null;
                CacheHandler.exportCache(updatedAllAccList, false);
            }
            case "Make a Deposit" -> depositMoney();
            case "Make a Withdrawal" -> {
                updatedAllAccList = withdrawMoney();
                assert updatedAllAccList != null;
                CacheHandler.exportCache(updatedAllAccList, false);
            }
            case "Alter an existing account" -> alterExistingBankAccount();
            case "Renew Cache" -> reGenerateCache();
            default -> throw new IllegalStateException("Unexpected value: %s".formatted(selectedOption));
        }
    }

    public static ArrayList<ArrayList<Account>> createNewBankAccount(String employeeName) throws SQLException, IOException {

        HashMap<String, String> holderData = new HashMap<>();
        String input;
        int selectedOptionIndex;

        System.out.println("\nSelect an Account Type >");
        selectedOptionIndex = Accessories.Menu.displayMenu(accountTypes, true);
        holderData.put("", Main.Titles[selectedOptionIndex]);
        String accountType = accountTypes[selectedOptionIndex];

        System.out.println("\n\t+----- HOLDER'S PERSONAL INFORMATION ------+");

        for (String header : personalDataHeaders) {
            boolean holderDataInputIter = true;
            while (holderDataInputIter) {
                if (header.equals("Title")) {
                    System.out.println("\nSelect the preferred title of the holder > ");
                    selectedOptionIndex = Accessories.Menu.displayMenu(Main.Titles, false);
                    holderDataInputIter = false;
                    holderData.put(header, Main.Titles[selectedOptionIndex]);
                    System.out.println();
                } else {
                    System.out.format("Enter the %s of holder : ", header);
                    input = Main.read.nextLine();
                    if (header.equals("Address")) {
                        try {
                            if (input.toCharArray()[input.length() - 1] != ',') {
                                input += ","; // add comma to end of the address
                            }
                        } catch (IndexOutOfBoundsException _) {}
                    }
                    if (Accessories.validate.validateBatch(input, header, personalDataHeaders)) {
                        holderDataInputIter = false;
                        holderData.put(header, input);
                    }
                }
            }
        }

        System.out.println("\n\t+----- ACCOUNT RELATED QUESTIONS ------+\n");

        for (String header : accountRelatedHeaders) {
            System.out.format("Enable %s (Y/N) : ", header);
            input = String.valueOf(Main.read.nextLine().charAt(0));
            input = (input.equalsIgnoreCase("y")) ? "1" : "0";
            holderData.put(header, input);
        }

        int accountNumber = Accessories.generate.getNewAccountNumber();
        int pinCode = Accessories.generate.getNewPinCode();
        String pinCodeHash = Accessories.generate.hashPassword(String.valueOf(pinCode));
        String balance = "0.00";

        String accountTypeId = switch (accountType) {
            case "Checking Account" -> "CHK_ACC";
            case "Saving Account" -> "SVG_ACC";
            case "Investment Account" -> "IVM_ACC";
            case "Certificated Deposit Account" -> "CD_ACC";
            default -> null;
        };

        holderData.put("Account Type Id", accountTypeId);
        holderData.put("Account Number", String.valueOf(accountNumber));
        holderData.put("Pin Code Hash", pinCodeHash);
        holderData.put("Balance", balance);

        AccountDataHandler AccountDataHandler = new AccountDataHandler(holderData);

        // this might :D create an 'Account' object and save holder's data in the database
        Account acc = AccountDataHandler.exportData();
        if (acc == null) {
            System.out.println("\nwhaattt");
            System.exit(0);
        }
        System.out.println("\n\t[+] New Account Created Successfully!\n");

        String printStr =
                String.format("""
                                
                                
                                \t\t\t\t=============================================================
                                \t\t\t\tCONFIDENTIAL DOCUMENT (AUTHORIZED PERSONAL ONLY)
                                \t\t\t\t=============================================================
                                
                                \t\t\t\t\t-- Basic Account info --
                                %s
                                
                                \t\t\t\t\t-- Access info --
                                \t\t\t\t\t+-----------------------------------------------------+
                                \t\t\t\t\t> Account Number :  %d
                                \t\t\t\t\t> Pin Code : %d
                                \t\t\t\t\t+-----------------------------------------------------+
                                
                                \t\t\t\t=================================================================
                                \t\t\t\tAdded By : %s at %s on %s
                                \t\t\t\t=================================================================
                                """,
                        acc.displayAccount(),
                        accountNumber,
                        pinCode,
                        employeeName.toUpperCase(),
                        Accessories.Date.getCurrentTime(),
                        Accessories.Date.getCurrentDate()
                );

        // write confidential data to a fricking text file :#
        FileHandler.write("%d_Account Details.txt".formatted(accountNumber), printStr, Main.ACC_DETAILS_FOLDER_PATH);

        switch (accountType) {
            case "Checking Account" -> checkingAccounts.add(acc);
            case "Saving Account" -> savingAccounts.add(acc);
            case "Investment Account" -> investmentAccounts.add(acc);
            case "Certificated Deposit Account" -> certificatedDepositAccounts.add(acc);
        }

        allAccounts.clear();
        allAccounts.add(0, checkingAccounts);
        allAccounts.add(1, savingAccounts);
        allAccounts.add(2, investmentAccounts);
        allAccounts.add(3, certificatedDepositAccounts);

        return allAccounts;
    }

    public static void depositMoney() throws SQLException {

        String holderFullName = "";
        float currentAccBalance = 0,minimumDepositAmount = 500.00f, depositAmount;
        Account matchedAccount = null;
        boolean accSearchDeposit = true;


        while (accSearchDeposit) {
            matchedAccount = new AccountDataHandler(allAccounts).searchAccount();
            if (matchedAccount == null) {
                System.out.println("\nAccount Not Found");
            } else {
                accSearchDeposit = false;
            }
        }

        Database_Handler db_handler = new Database_Handler();
        String query = "SELECT holderTitle, holderFullName, holderAccBalance FROM Holder WHERE holderAccNumber = ?";
        db_handler.prepareQuery(query, matchedAccount.getAccountNumber());
        ResultSet resultSet = db_handler.getData();
        assert resultSet != null;
        if (resultSet.next()) {
            holderFullName = "%s. %s".formatted(resultSet.getString(1), resultSet.getString(2));
            currentAccBalance = resultSet.getFloat(3);
        }

        System.out.printf(
                """
                        
                        ***********************************************
                        Holder :  [ %s ]
                        Account Number : [ %s ]
                        Current Account Balance : [ Rs.%,.2f ]
                        ***********************************************
                        """, holderFullName.toUpperCase(), matchedAccount.getAccountNumber(), currentAccBalance
        );

        depositAmount = Accessories.validate.getValidatedCreditAmount("Deposit", minimumDepositAmount);

        query = "UPDATE Holder SET holderAccBalance = ? WHERE holderAccNumber = ?";
        ArrayList<String> placeHolderValues = new ArrayList<>(
                Arrays.asList(
                        String.valueOf(currentAccBalance + depositAmount),
                        matchedAccount.getAccountNumber()
                )
        );
        db_handler.prepareQuery(query, placeHolderValues);
        db_handler.saveData();
        System.out.printf("""
                        
                         Rs. %,.2f Successfully deposited into Account Number [ %s ]
                        """,
                depositAmount,
                matchedAccount.getAccountNumber()
        );
    }

    private static ArrayList<ArrayList<Account>> withdrawMoney() throws SQLException {
        Account matchedAccount = null;
        int monthlyTransactionLimit = 0;
        float currentAccountBalance = 0;
        String pinCodeHash = "";
        int pinCodeInput = 0;
        String accTypeId;
        String holderFullName = "";
        float withdrawalAmount, minimumWithdrawalAmount = 500.00f;

        boolean accSearchWithdrawal = true;
        while (accSearchWithdrawal) {
            matchedAccount = new AccountDataHandler(allAccounts).searchAccount();
            if (matchedAccount == null) {
                System.out.println("\nAccount Not Found");
            } else {
                accSearchWithdrawal = false;
            }
        }

        Database_Handler db_handler = new Database_Handler();
        int monthlyTransactionCount = matchedAccount.getMonthlyTransactionCount();

        String query = "SELECT holderTitle, holderFullName, holderAccTypeId, holderAccBalance, holderPinCodeHash " +
                "FROM Holder WHERE holderAccNumber = ?";
        db_handler.prepareQuery(query, matchedAccount.getAccountNumber());
        ResultSet results = db_handler.getData();

        assert results != null;
        if (results.next()) {
            holderFullName = "%s. %s".formatted(results.getString(1), results.getString(2));
            accTypeId = results.getString(3);
            currentAccountBalance = results.getFloat(4);
            pinCodeHash = results.getString(5);

            query = "SELECT accTypeMonthlyTransactionLimit FROM AccountType WHERE accTypeId LIKE ?";
            db_handler.prepareQuery(query, accTypeId);
            results = db_handler.getData();
            if (results.next()) {
                monthlyTransactionLimit = results.getInt(1);
            }
        }

        if (monthlyTransactionLimit <= monthlyTransactionCount) {
            System.out.println("\nWithdrawal cannot be proceeded! -> [ Monthly transaction limit has already exceeded ]");
        } else {
            System.out.printf(
                    """
                            
                            ***********************************************
                            Holder :  [ %s ]
                            Account Number : [ %s ]
                            Current Account Balance : [ Rs.%,.2f ]
                            ***********************************************
                            """,
                    holderFullName.toUpperCase(),
                    matchedAccount.getAccountNumber(),
                    currentAccountBalance
            );

            int maxTurns = 3;
            int currentTurn = 1;
            boolean inputPinWithdrawIter = true;
            while (inputPinWithdrawIter) {
                System.out.print("\nEnter the pin code :: ");
                try {
                    pinCodeInput = Main.read.nextInt();
                    Main.read.nextLine();
                } catch (InputMismatchException e) {
                    System.out.println("\nError! Invalid Input");
                    Main.read.nextLine();
                }
                if (Accessories.generate.hashPassword(String.valueOf(pinCodeInput)).equals(pinCodeHash)) {
                    withdrawalAmount = Accessories.validate.getValidatedCreditAmount(
                            "Withdrawal", minimumWithdrawalAmount
                    );
                    if (withdrawalAmount > currentAccountBalance) {
                        System.out.println("\nError! Inefficient Account Balance");
                    } else {
                        query = "UPDATE Holder SET holderAccBalance = ? WHERE holderAccNumber = ?";
                        db_handler.prepareQuery(
                                query, new ArrayList<>(
                                        Arrays.asList(
                                                String.valueOf(currentAccountBalance - withdrawalAmount),
                                                matchedAccount.getAccountNumber()
                                        )
                                )
                        );
                        db_handler.saveData();
                        System.out.printf("""
                                        
                                         Rs. %,.2f Successfully withdrew from account Number [ %s ]
                                         -> %d transactions are done in this month
                                        """,
                                withdrawalAmount,
                                matchedAccount.getAccountNumber(),
                                ++monthlyTransactionCount
                        );

                        query = "UPDATE Holder SET monthlyTransactionCount = ? WHERE holderAccNumber = ?";
                        ArrayList<String> placeHolderValues = new ArrayList<>(
                                Arrays.asList(
                                        String.valueOf(monthlyTransactionCount),
                                        matchedAccount.getAccountNumber()
                                )
                        );
                        db_handler.prepareQuery(query, placeHolderValues);
                        db_handler.saveData();

                        matchedAccount.setMonthlyTransactionCount();
                        AccountDataHandler accountDataHandler = new AccountDataHandler(allAccounts);
                        allAccounts = accountDataHandler.insertUpdatedAcc(matchedAccount);

                    }
                    inputPinWithdrawIter = false;
                } else if (currentTurn == maxTurns) {
                    System.out.println(
                            """
                                    
                                    Withdrawal cannot be proceeded!
                                    -> [ Maximus pin code input attempts exceeded ]
                                    """
                    );
                    inputPinWithdrawIter = false;
                } else {
                    System.out.printf(
                            "\nIncorrect pin code! [ Tried %d out of %d times ]\n",
                            currentTurn++, maxTurns
                    );
                }
            }
        }

        return allAccounts;
    }

    private static void alterExistingBankAccount() throws SQLException {

        int selectedOptionIndex;
        String selectedOption;

        Account matchedAccount = new AccountDataHandler(allAccounts).searchAccount();
        if (matchedAccount == null) {
            System.out.println("\nAccount Not Found");
            System.exit(0);
        }

        System.out.println("\n");
        String _ = matchedAccount.displayAccount();

        String[] options = {"Change Email", "Change Phone Number"};

        System.out.println("\nSelect an action > ");
        selectedOptionIndex = Accessories.Menu.displayMenu(options, false);
        if (!(selectedOptionIndex > -1 && selectedOptionIndex < options.length)) {
            System.out.println("\nInvalid Selection");
            System.exit(0);
        }

        selectedOption = options[selectedOptionIndex];

        switch (selectedOption) {
            case "Change Email":
                System.out.print("\nEnter New Email : ");
                String newEmail = Main.read.next();
                if (!Accessories.validate.validateEmail(newEmail)) {
                    System.out.println("\nInvalid Email!");
                } else {
                    matchedAccount.setEmail(newEmail);
                    System.out.printf(
                            "\n\tNew Email added to account number [ %s ] successfully%n"
                            , matchedAccount.getAccountNumber());
                }
                break;
            case "Change Phone Number":
                System.out.print("\nEnter New Mobile Phone Number : ");
                String newMobilePhoneNumber = Main.read.next();
                if (!Accessories.validate.validateMobileNumber(newMobilePhoneNumber)) {
                    System.out.println("\nInvalid Mobile Phone Number!");
                } else {
                    matchedAccount.setMobilePhoneNumber(newMobilePhoneNumber);
                    System.out.printf(
                            "\n\tNew Mobile Phone Number added to account [ %s ] added successfully%n",
                            matchedAccount.getAccountNumber());
                }
                break;
        }

    }

    private static void reGenerateCache() throws SQLException {
        CacheHandler.purgeCache();
        CacheHandler.createNewCache();
    }
}
