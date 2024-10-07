package com.abc_bank;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

class Main {

    public static final Scanner read = new Scanner(System.in);
    public static final String[] Titles = {"Mr", "Ms", "Miss", "Dr", "Rev"};
    public static final String CONFIG_FILE_PATH =
            "src\\main\\java\\com\\abc_bank\\Admin\\info.json";
    public static final String ACC_DETAILS_FOLDER_PATH = "src\\main\\java\\com\\abc_bank\\output_txt\\";
    public static final String LOAN_REQUESTS_JSON_PATH = "cache/loan_req.json";


    public static void main(String[] ignoredArgs) throws InputMismatchException, IOException, SQLException, ParseException {

        ArrayList<String> employeeData = new ArrayList<>();

        System.out.println(
                """
                        \n\t\t =================================================
                        \t\t       |||  ABC Bank Management System  |||
                        \t\t =================================================
                        """);
        try {
            employeeData = Login.employeeLogin();
        } catch (Exception _) {
            System.exit(0);
        }

        String currentDate = Accessories.Date.getCurrentDate();
        String currentTime = Accessories.Date.getCurrentTime();
        boolean newMonthStarted = Accessories.Date.startOfNewMonth();

        String employeeDepartmentId = employeeData.get(1);
        String employeeFullName = "%s. %s".formatted(employeeData.get(2), employeeData.get(0));
        String employeeDepartment = switch (employeeDepartmentId) {
            case "TA" -> "Transaction";
            case "LN" -> "Loan";
            case "PN" -> "Pawn";
            default -> "";
        };

        System.out.printf("\nWelcome %s [ %s Department ]%n", employeeFullName.toUpperCase(), employeeDepartment);
        System.out.printf("\n - Logged in at %s on %s%n", currentTime, currentDate);

        if (employeeDepartment.equals("Transaction")) {
            boolean transactionIter = true;
            while (transactionIter) {
                TransactionDepartment.TSAD_Main(employeeFullName, newMonthStarted);
                System.out.print("\nExit (Y/N) : ");
                transactionIter = !read.next().equalsIgnoreCase("y");
            }
        } else if (employeeDepartment.equals("Loan")) {
            System.out.print("\nRecreating caches..");
            CacheHandler.purgeCache();
            CacheHandler.createNewCache();
            LoanDepartment.collectMonthlyInstalments();
            boolean loanIter = true;
            while (loanIter) {
                LoanDepartment.LAD_Main();
                System.out.print("\nExit (Y/N) : ");
                loanIter = !read.next().equalsIgnoreCase("y");
            }
        }
    }
}