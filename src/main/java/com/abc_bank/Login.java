package com.abc_bank;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Login {

    private static ArrayList<String> getEmployeeData(String employeeUsername) throws SQLException {
        ArrayList<String> employeeData = new ArrayList<>();
        Database_Handler db_handler = new Database_Handler();
        String query = "SELECT empName, depId, empTitle FROM Employee WHERE empUsername = ?";

        db_handler.prepareQuery(query, employeeUsername);
        ResultSet results = db_handler.getData();

        while (true) {
            assert results != null;
            if (!results.next()) break;
            employeeData.add(results.getString(1));
            employeeData.add(results.getString(2));
            employeeData.add(results.getString(3));
        }

        results.close();
        return employeeData;
    }

    public static ArrayList<String> employeeLogin() throws SQLException {

        ArrayList<String> employeeData = new ArrayList<>();
        Database_Handler db_handler = new Database_Handler();
        String query = "SELECT empPasswordHash FROM Employee WHERE empUsername = ?";

        String InputEmployeeUsername, InputPassword, passwordHash;

        System.out.println("\n\t+----- PLEASE LOGIN TO YOUR EMPLOYEE ACCOUNT ------+");

        boolean loginIteration = true;
        while (loginIteration) {
            System.out.print("\n- USERNAME : ");
            InputEmployeeUsername = Main.read.next().trim();
            db_handler.prepareQuery(query, InputEmployeeUsername);
            ResultSet results = db_handler.getData();
            assert results != null;
            if (!results.next()) {
                System.out.println("\nIncorrect Username!");
            } else {
                passwordHash = results.getString(1);
                System.out.print("\n- PASSWORD : ");
                InputPassword = Main.read.next().trim();
                // check if password matches
                if (passwordHash.equals(Accessories.generate.hashPassword(InputPassword))) {
                    System.out.println("\n\t\tLogin Successful!");
                    employeeData = getEmployeeData(InputEmployeeUsername); // getting employee's data
                    loginIteration = false;
                } else {
                    System.out.println("\nError! Incorrect password for entered username\n");
                }
            }
        }
        return employeeData;
    }
}
