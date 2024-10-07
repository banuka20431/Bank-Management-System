package com.abc_bank;

import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class LoanDepartment {
    private static final String[] mainMenu = {"Check Eligibility", "Claim a Loan", "Cancel a Loan"};
    private static final ArrayList<String> loanTypeNames = new ArrayList<>();
    private static final ArrayList<Integer> defaultInterestRates = new ArrayList<>();
    private static final ArrayList<Integer> defaultAllowedTimePeriods = new ArrayList<>();
    private static final ArrayList<Float> minimumAmounts = new ArrayList<>();
    private static final ArrayList<String> loanDefaultIds = new ArrayList<>();
    private static final Database_Handler db_handler = new Database_Handler();

    public static void LAD_Main() throws SQLException, IOException {

        collectMonthlyInstalments();

        JSONObject reqLoanData = new JSONObject(FileHandler.read(Main.LOAN_REQUESTS_JSON_PATH));

        ResultSet result;
        boolean accountStatus = false;

        String query = "SELECT loanTypeName, loanTypeDefInterestRate, loanTypeDefAllowedTimePeriod, loanTypeMinimumAmount, loanTypeDefId "
                +
                "FROM LoanType";
        db_handler.prepareQuery(query);
        result = db_handler.getData();
        while (true) {
            assert result != null;
            if (!result.next())
                break;
            loanTypeNames.add(result.getString(1));
            defaultInterestRates.add(result.getInt(2));
            defaultAllowedTimePeriods.add(result.getInt(3));
            minimumAmounts.add(result.getFloat(4));
            loanDefaultIds.add(result.getString(5));
        }
        ArrayList<ArrayList<Account>> allAccounts = CacheHandler.importCache(TransactionDepartment.accountTypes.length);
        AccountDataHandler accHandler = new AccountDataHandler(allAccounts);
        Account matchedAccount = accHandler.searchAccount();
        if (matchedAccount == null) {
            System.out.println("\nAccount Not Found!");
        } else {
            System.out.println("\nAccount Found!\n");
            String ignoredStr = matchedAccount.displayAccount();

            query = "SELECT status FROM holder where holderAccNumber = ?";
            db_handler.prepareQuery(query, matchedAccount.getAccountNumber());
            result = db_handler.getData();
            if (result.next()) {
                accountStatus = result.getBoolean(1);
            }

            if (!accountStatus) {
                System.out.printf(
                        "\nCannot proceed  \n\t(!) Matched account [ ACCOUNT NUMBER : %s ] isn't active at the moment!\n",
                        matchedAccount.getAccountNumber());
            } else {

                System.out.println("\nSelect an Action >");
                int selectedOptionIndex = Accessories.Menu.displayMenu(mainMenu, true);
                String selectedOption = mainMenu[selectedOptionIndex];
                switch (selectedOption) {
                    case "Check Eligibility" -> checkEligibility(matchedAccount, reqLoanData);
                    case "Claim a Loan" -> claimLoan(matchedAccount, reqLoanData);
                    case "Cancel a Loan" -> cancelLoan();
                }
                FileHandler.write(reqLoanData.toString(), Main.LOAN_REQUESTS_JSON_PATH);
            }
        }
    }

    private static void checkEligibility(Account matchedAccount, JSONObject reqLoanData) throws SQLException {
        System.out.print("\nChecking for past eligibility checks..: ");
        if (isPastCheckExists(reqLoanData, matchedAccount)) {
            System.out.println("FOUND");
            displayReqLoanDetails(reqLoanData.getJSONObject(matchedAccount.getAccountNumber()));
            String[] actions = {"Delete Record", "Claim Loan"};
            System.out.println("\nSelect an action >");
            String action = actions[Accessories.Menu.displayMenu(actions, true)];
            switch (action) {
                case "Delete Record": {
                    reqLoanData.remove(matchedAccount.getAccountNumber());
                    System.out.println("\nRecord Deleted Successfully!");
                    getNewCheckProcess(matchedAccount, reqLoanData);
                }
                case "Claim Loan": {
                    claimLoan(matchedAccount, reqLoanData);
                }
            }
        } else {
            System.out.println("NOT FOUND");
            getNewCheckProcess(matchedAccount, reqLoanData);
        }
    }

    private static void claimLoan(Account matchedAccount, JSONObject reqLoanDetails) throws SQLException {
        if (isPastCheckExists(reqLoanDetails, matchedAccount)) {
            JSONObject matchedReqLoanDetails = reqLoanDetails.getJSONObject(matchedAccount.getAccountNumber());
            String query = "INSERT INTO Loan(" +
                    "loanId, loanTypeId, loanAmount, loanInterestRate, loanMonthlyInterestAmount, loanMonthlyInstalmentAmount," +
                    " loanAllowedTimePeriod, loanDebtorAccNumber, loanGainedInterestAmount, loanClaimedDate" +
                    ")" +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ArrayList<String> placeholderValueArr = new ArrayList<>();
            Object[] values = {
                    matchedReqLoanDetails.get("Generated loan Id"),
                    matchedReqLoanDetails.get("Loan Id"),
                    matchedReqLoanDetails.getFloat("Loan Amount"),
                    matchedReqLoanDetails.get("Interest Rate"),
                    matchedReqLoanDetails.get("Interest Amount per Installment"),
                    matchedReqLoanDetails.getFloat("Value per Installment"),
                    matchedReqLoanDetails.getInt("Installment Count"),
                    matchedAccount.getAccountNumber(),
                    "0.00",
                    getDate()
            };
            for (Object val : values) {
                placeholderValueArr.add(String.valueOf(val));
            }
            try {
                System.out.print("Inserting data into the database..: ");
                db_handler.prepareQuery(query, placeholderValueArr);
                db_handler.saveData();
                System.out.println("OK");
                reqLoanDetails.remove(matchedAccount.getAccountNumber());
            } catch (SQLException e) {
                System.out.println("FAILED");
                System.out.printf(" - Error Code > %s", e.getErrorCode());
            }
        } else {
            System.out.println("\nCouldn't find any eligibility checks on this account!");
            getNewCheckProcess(matchedAccount, reqLoanDetails);
        }
    }

    private static void cancelLoan() {

    }

    private static void collectMonthlyInstalments() throws SQLException {
        float monthlyInterestAmount, monthlyInstalmentAmount;
        String debtorAccNumber, claimedDate, thisDate;
        int allowedTimePeriod;
        String[] today = getDate().split("-");
        thisDate = String.format("'%%-%s-%s'", today[1], today[2]);
        System.out.println(thisDate);

        String query = "SELECT loanMonthlyInterestAmount, loanClaimedDate, loanDebtorAccNumber, loanAllowedTimePeriod," +
                " loanMonthlyInstalmentAmount FROM Loan WHERE loanClaimedDate LIKE " + thisDate;
        db_handler.prepareQuery(query);
        ResultSet results = db_handler.getData();

        while (true) {
            assert results != null;
            if (!results.next()) break;
            monthlyInterestAmount = results.getFloat(1);
            claimedDate = results.getString(2);
            debtorAccNumber = results.getString(3);
            allowedTimePeriod = results.getInt(4);
            monthlyInstalmentAmount = results.getFloat(5);

            query = "SELECT holderAccBalance, loanOverDraft FROM Holder h INNER JOIN Loan l ON h.holderAccNumber=l.loanDebtorAccNumber";
            db_handler.prepareQuery(query, debtorAccNumber);
            ResultSet debtorAccInfo = db_handler.getData();
            debtorAccInfo.next();
            System.out.println(debtorAccInfo.getFloat(1) + " - " + monthlyInstalmentAmount);
            if (debtorAccInfo.getFloat(1) < monthlyInstalmentAmount) {
                query = "UPDATE Holder SET status = false WHERE holderAccNumber = ?";
                db_handler.prepareQuery(query, debtorAccNumber);
                db_handler.saveData();
                query = "UPDATE Holder SET loanOverDraft = ?";
                float overDraftAmount = debtorAccInfo.getFloat(2) + monthlyInstalmentAmount;
                db_handler.prepareQuery(query, String.valueOf(overDraftAmount));
            }
        }
    }

    private static String getDate() {
        String[] date = Accessories.Date.getCurrentDate().split("/");
        return date[2] + "-" + date[1] + "-" + date[0];
    }

    private static void getNewCheckProcess(Account matchedAccount, JSONObject reqLoanData) throws SQLException {
        System.out.print("\nDo you want to proceed to make new loan eligibility check (Y/N) : ");
        boolean proceed = Accessories.Text.getFirstLetter(Main.read.next().trim()).equalsIgnoreCase("y");
        if (proceed) {
            newCheckProcess(matchedAccount, reqLoanData);
        }
    }

    private static boolean isPastCheckExists(JSONObject reqLoanData, Account matchedAccount) {
        return reqLoanData.keySet().contains(matchedAccount.getAccountNumber())
                && !(reqLoanData.getJSONObject(matchedAccount.getAccountNumber()).keySet().isEmpty());
    }

    private static void newCheckProcess(Account matchedAccount, JSONObject reqLoanData) throws SQLException {

        String loanTypeName, loanTypeId, loanId;
        float requiredAccBalancePercentage = 25.0f;
        float currentAccBalance;
        float minimumLoanAmount, loanDesiredAmount, defaultInterestRate, calculatedInterestRate,
                calculatedInstallmentValue, interestAmountPerInstallment;
        int defaultAllowedTimePeriod, loanTypeNameIndex, desiredNoOfInstallments, installmentDiff;
        boolean isCustomLoan;

        loanTypeNameIndex = Accessories.Menu.displayMenu(loanTypeNames, true);
        loanTypeName = loanTypeNames.get(loanTypeNameIndex);
        loanTypeId = loanDefaultIds.get(loanTypeNameIndex);
        minimumLoanAmount = minimumAmounts.get(loanTypeNameIndex);
        defaultAllowedTimePeriod = defaultAllowedTimePeriods.get(loanTypeNameIndex);
        defaultInterestRate = defaultInterestRates.get(loanTypeNameIndex);

        displaySelectedLoanInfo(minimumLoanAmount, defaultAllowedTimePeriod, defaultInterestRate);

        loanDesiredAmount = getDesiredLoanAmount(minimumLoanAmount);
        desiredNoOfInstallments = getDesiredInstallmentCount();
        installmentDiff = desiredNoOfInstallments - defaultAllowedTimePeriod;
        isCustomLoan = installmentDiff != 0;
        loanId = assignLoanId(isCustomLoan, loanTypeId, matchedAccount.getAccountNumber());
        calculatedInterestRate = calculateInterest(defaultInterestRate, installmentDiff);
        calculatedInstallmentValue = calculateMonthlyInstallmentValue(
                loanDesiredAmount, calculatedInterestRate, desiredNoOfInstallments);
        currentAccBalance = getApplierAccBalance(matchedAccount.getAccountNumber());
        interestAmountPerInstallment = calculatedInstallmentValue - loanDesiredAmount / desiredNoOfInstallments;


        if (currentAccBalance * (requiredAccBalancePercentage / 100) < calculatedInstallmentValue) {
            System.out.println("\n - This user is not eligible for the specified loan specifics!");
            System.out.println("\n\t [ Low credit balance ]\n");
        } else {
            System.out.println("\nEligible!");
            System.out.print("\nDo you want to save the checked loan request (Y/N) : ");
            boolean saveToJSON = Accessories.Text.getFirstLetter(Main.read.next().trim()).equalsIgnoreCase("y");
            if (saveToJSON) {
                JSONObject newReqLoanData = new JSONObject();
                System.out.print("\nSaving the loan request data..: ");
                newReqLoanData.put("Loan Id", loanTypeId);
                newReqLoanData.put("Loan Amount", loanDesiredAmount);
                newReqLoanData.put("Interest Rate", calculatedInterestRate);
                newReqLoanData.put("Installment Count", desiredNoOfInstallments);
                newReqLoanData.put("Value per Installment", calculatedInstallmentValue);
                newReqLoanData.put("Interest Amount per Installment", interestAmountPerInstallment);
                newReqLoanData.put("Generated loan Id", loanId);
                reqLoanData.put(matchedAccount.getAccountNumber(), newReqLoanData);
                System.out.print("OK\n");

                System.out.print("\n Do you want to proceed to claim the loan (Y/N) : ");
                boolean proceed = Accessories.Text.getFirstLetter(Main.read.next().trim()).equalsIgnoreCase("y");
                if (proceed) {
                    claimLoan(matchedAccount, reqLoanData);
                }
            }
        }
    }

    private static void displayReqLoanDetails(JSONObject reqLoanData) {
        System.out.println("\n+-------------------------------------------------------+\n");
        System.out.printf("Loan Type Id : %s\n", reqLoanData.get("Loan Id"));
        System.out.printf("Loan Amount : Rs. %,.2f\n", reqLoanData.getFloat("Loan Amount"));
        System.out.printf("Interest Rate : %s%%\n", reqLoanData.get("Interest Rate"));
        System.out.printf("Number of Installments : %d\n", reqLoanData.getInt("Installment Count"));
        System.out.printf("Monthly Installment Value : %,.2f\n", reqLoanData.getFloat("Value per Installment"));
        System.out.printf("Interest Amount per Installment : %,.2f\n", reqLoanData.getFloat("Interest Amount per Installment"));
        System.out.printf("Appointed Loan Id : %s\n", reqLoanData.get("Generated loan Id"));
        System.out.println("\n+-------------------------------------------------------+\n");
    }

    private static String assignLoanId(boolean isCustomLoan, String loanTypeId, String accNo) {
        String prefix = "CST";
        if (isCustomLoan) {
            String[] p = loanTypeId.split("-");
            return String.format("%s-%s-%s-%s", prefix, p[1], p[2], accNo);
        }
        return String.format("%s-%s", loanTypeId, accNo);
    }

    private static void displaySelectedLoanInfo(
            float minimumLoanAmount, int defaultAllowedTimePeriod, float defaultInterestRate) {
        System.out.printf(
                """
                        
                         ----------------------------------------------------------------------
                        
                                        +---------------------------------+
                                         DETAILS OF THE SELECTED LOAN TYPE
                                        +---------------------------------+
                        
                          # Minimum Loan Amount : %,.2f
                          # Default Allowed Payback Time Period : Years [ %d ] Months [ %d ]
                          # Default Interest Rate : %s%%
                        
                         ----------------------------------------------------------------------
                        """,
                minimumLoanAmount,
                Math.round((float) defaultAllowedTimePeriod / 12),
                defaultAllowedTimePeriod - Math.round((float) defaultAllowedTimePeriod / 12) * 12,
                defaultInterestRate);
    }

    private static float getDesiredLoanAmount(float minimumLoanAmount) {
        return Accessories.validate.getValidatedCreditAmount("Desired Loan", minimumLoanAmount);
    }

    private static int getDesiredInstallmentCount() {
        int years, months;
        System.out.println("\nEnter desired number of installments >");
        do {
            System.out.print("\nYears : ");
            years = Accessories.validate.getValidatedInteger();
            if (years == -1)
                continue;
            System.out.print("Months : ");
            months = Accessories.validate.getValidatedInteger();
            if ((months + years == 0)) {
                System.out.println("\n Error : installment count cannot be zero!");
            }
            if (months == -1)
                continue;
            break;
        } while (true);
        if (months >= 12) {
            years += Math.round((float) months / 12);
            months = months - Math.round((float) months / 12) * 12;
        }

        return years * 12 + months;
    }

    private static float calculateInterest(float defaultInterestRate, int installmentDiff) {
        float deltaValueOfInterestRate = 0.005f * installmentDiff;
        if (installmentDiff == 0) {
            return defaultInterestRate;
        }
        return defaultInterestRate + deltaValueOfInterestRate;
    }

    private static float calculateMonthlyInstallmentValue(
            float loanDesiredAmount, float calculatedInterestRate, int desiredNoOfInstallments
    ) {
        float calculatedInstallmentValue;
        calculatedInstallmentValue = loanDesiredAmount / desiredNoOfInstallments;
        calculatedInstallmentValue += calculatedInstallmentValue * (calculatedInterestRate / 100);
        return calculatedInstallmentValue;
    }

    private static float getApplierAccBalance(String accNo) throws SQLException {
        String query = "SELECT holderAccBalance from Holder WHERE holderAccNumber = ?";
        db_handler.prepareQuery(query, accNo);
        ResultSet results = db_handler.getData();
        assert results != null;
        if (!results.next())
            System.out.println("fk");
        return results.getFloat(1);
    }

}
