package com.abc_bank;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Accessories {

    public static class Text {
        public static String capitalize(String word) {
            return getFirstLetter(word).toUpperCase() + word.substring(1);
        }

        public static String getFirstLetter(String word) {
            return word.substring(0, 1);
        }
    }

    public static class Menu {

        public static int displayMenu(String[] menuItems, boolean printVertical) {
            return displayMenu(Arrays.stream(menuItems).iterator(), printVertical, menuItems.length);
        }

        public static int displayMenu(ArrayList<String> menuItems, boolean printVertical) {
            return displayMenu(menuItems.iterator(), printVertical, menuItems.size());
        }

        public static int displayMenu(Iterator<String> menuIter, boolean printVertical, int menuItemCount) {
            int selectedOptionIndex = -1;
            System.out.println();
            if (printVertical) {
                printVertical(menuIter);
            } else {
                printHorizontal(menuIter);
            }
            while (selectedOptionIndex == -1) {
                selectedOptionIndex = getInput(menuItemCount);
            }
            return selectedOptionIndex;
        }

        private static void printVertical(Iterator<String> menuIter) {
            int i = 1;
            while (menuIter.hasNext()) {
                System.out.printf("%d. %s\n", i++, Text.capitalize(menuIter.next()));
            }
        }

        private static void printHorizontal(Iterator<String> menuIter) {
            int i = 1;
            while (menuIter.hasNext()) {
                System.out.printf("\t%d. %s\t", i++, Text.capitalize(menuIter.next()));
                if (!menuIter.hasNext()) {
                    System.out.println();
                }
            }
        }

        private static int getInput(int menuItemCount) {
            System.out.print("\n>_ ");
            String input = Main.read.next();
            int selectedOptionIndex;
            try {
                selectedOptionIndex = Integer.parseInt(input) - 1;
                if (selectedOptionIndex >= menuItemCount || selectedOptionIndex < 0) {
                    System.out.println("\nError! Invalid Selection");
                    return -1;
                }
            } catch (NumberFormatException e) {
                System.out.println("\nError! Invalid Input");
                return -1;
            } finally {
                Main.read.nextLine();
            }

            return selectedOptionIndex;
        }
    }

    public static class generate {

        private static final Random random = new Random();
        private static final MessageDigest sha224;

        static {
            try {
                sha224 = MessageDigest.getInstance("SHA-224");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        public static String hashPassword(String password) {
            byte[] hashBytes = sha224.digest(password.getBytes());
            // Convert byte array to hex string
            StringBuilder hexStr = new StringBuilder();
            for (byte _byte : hashBytes) {
                String hex = Integer.toHexString(0xff & _byte);
                if (hex.length() == 1) {
                    hexStr.append('0');
                }
                hexStr.append(hex);
            }
            return hexStr.toString();
        }

        public static int getNewAccountNumber() throws SQLException {

            int generatedAccountNumber;
            ArrayList<String> givenAccountNumbers = new ArrayList<>();
            String query = "SELECT holderAccNumber FROM Holder";

            Database_Handler db_handler = new Database_Handler();
            db_handler.prepareQuery(query);
            ResultSet results = db_handler.getData();

            while (true) {
                assert results != null;
                if (!results.next())
                    break;
                givenAccountNumbers.add(results.getString(1));
            }

            results.close();

            do {
                generatedAccountNumber = random.nextInt(10000, 999999999);
            } while (givenAccountNumbers.contains(String.valueOf(generatedAccountNumber)));

            return generatedAccountNumber;
        }

        public static int getNewPinCode() {
            return random.nextInt(999, 99999);
        }

    }

    public static class validate {

        private static boolean matchFound = false;

        public static boolean validateBatch(String input, String headerName, String[] personalDataHeaders) {

            if (headerName.equals(personalDataHeaders[1])) {
                return validateFullName(input);
            } else if (headerName.equals(personalDataHeaders[2])) {
                return validateName(input);
            } else if (headerName.equals(personalDataHeaders[3])) {
                return validateDOB(input);
            } else if (headerName.equals(personalDataHeaders[4])) {
                return validateNIC(input);
            } else if (headerName.equals(personalDataHeaders[5])) {
                return validateAddress(input);
            } else if (headerName.equals(personalDataHeaders[6])) {
                return validatePostalCode(input);
            } else if (headerName.equals(personalDataHeaders[7])) {
                return validateMobileNumber(input);
            } else if (headerName.equals(personalDataHeaders[8])) {
                return validateTelephoneNumber(input);
            } else if (headerName.equals(personalDataHeaders[9])) {
                return validateEmail(input);
            } else if (headerName.equals(personalDataHeaders[10])) {
                return validateProfession(input);
            } else {
                System.out.println("What the f.");
            }
            return false;
        }

        public static boolean validateFullName(String fullName) {
            Matcher m = Pattern.compile("^([a-zA-Z]{2,} *)+$").matcher(fullName);
            matchFound = m.find();
            if (!matchFound) {
                System.out.println("\n(!) Invalid Input > [ Invalid characters contains ]\n");
            }
            return matchFound;
        }

        public static boolean validateName(String Name) {
            Matcher m = Pattern.compile("^([a-zA-Z]\\.)+ *([a-zA-Z]{2,} *)+$").matcher(Name);
            matchFound = m.find();
            if (!matchFound) {
                System.out.println("\n(!) Invalid Input > [ need to contain one or more initials ]\n");
            }
            return matchFound;
        }

        public static boolean validateDOB(String Date) {

            Matcher m = Pattern.compile("^([0-9]{4})[/.-]([0-9]{2})[/.-]([0-9]{2})$").matcher(Date);
            String currentYear = Accessories.Date.getCurrentDate().split("/")[2];
            boolean incorrectDate = false;
            matchFound = m.find();

            if (!matchFound) {
                System.out.println("\n(!) Invalid Input > [ Invalid Date ]\n");
            } else {
                int year = Integer.parseInt(m.group(1));
                int month = Integer.parseInt(m.group(2));
                int day = Integer.parseInt(m.group(3));

                if (year > Integer.parseInt(currentYear)) {
                    System.out.println("\n(!) Invalid Input > [ Future Date ]\n");
                    return false;
                } else {
                    // check if the day and the month values in the valid range
                    if (!(day >= 1 && day <= 31 && month >= 1 && month <= 12)) {
                        incorrectDate = true;

                    } else {
                        ArrayList<Integer> thirties = new ArrayList<>(Arrays.asList(4, 6, 9, 11));
                        // check if the month is a month that should have 30 maximum days
                        if (thirties.contains(month)) {
                            if (day > 30) {
                                incorrectDate = true;
                            }
                            // check if the month is february
                        } else if (month == 2) {
                            // check if year is a leap year and if it is, if the days not exceeds 29
                            if (year % 4 == 0 && day > 29) {
                                incorrectDate = true;
                                // if the year is not a leap year, the day shouldn't exceed 28.
                            } else {
                                if (day > 28) {
                                    incorrectDate = true;
                                }
                            }
                        }
                    }
                }
            }

            if (incorrectDate) {
                System.out.println("\n(!) Invalid Input > [ Incorrect Date ]\n");
                return false;
            }

            return matchFound;
        }

        public static boolean validateNIC(String NIC) {
            Matcher m = Pattern.compile("^[0-9]{9}[vV]?([0-9]{3})?$").matcher(NIC);
            matchFound = m.find();
            if (!matchFound) {
                System.out.println("\n(!) Invalid Input > [ Invalid NIC Number ]\n");
            }
            return matchFound;
        }

        public static boolean validateAddress(String Address) {
            Matcher m = Pattern.compile("^([a-zA-Z0-9 ./']+[, ] {0,5})+$").matcher(Address);
            matchFound = m.find();
            if (!matchFound) {
                System.out.println("\n(!) Invalid Input > [ Invalid Characters Contains ]\n");
            }
            return matchFound;
        }

        public static boolean validatePostalCode(String postalCode) {
            Matcher m = Pattern.compile("^[1-9][0-9]{3,11}$").matcher(postalCode);
            matchFound = m.find();
            if (!matchFound) {
                System.out.println("\n(!) Invalid Input > [ Invalid Characters Contains ]\n");
            }
            return matchFound;
        }

        public static boolean validateMobileNumber(String mobileNumber) {
            Matcher m = Pattern.compile("^(\\+?94|0)7[01245678][0-9]{7}$").matcher(mobileNumber);
            matchFound = m.find();
            if (!matchFound) {
                System.out.println("\n(!) Invalid Input > [ Invalid Phone Number ]\n");
            }
            return matchFound;
        }

        public static boolean validateTelephoneNumber(String telephoneNumber) {
            Matcher m = Pattern.compile("^(\\+?94|0)[0-9]{9}$").matcher(telephoneNumber);
            matchFound = m.find();
            if (!matchFound) {
                System.out.println("\n(!) Invalid Input > [ Invalid Phone Number ]\n");
            }
            return matchFound;
        }

        public static boolean validateEmail(String Email) {
            Matcher m = Pattern.compile("^(https?://)?(www.)?([a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z]{2,})$")
                    .matcher(Email);
            matchFound = m.find();
            if (!matchFound) {
                System.out.println("\n(!) Invalid Input > [ Invalid Email ]\n");
            }
            return matchFound;
        }

        public static boolean validateProfession(String Profession) {
            return validateFullName(Profession);
        }

        public static float getValidatedCreditAmount(String action, float minimumAllowance) {
            float amount = 0;
            boolean inputCreditAmountIter = true;
            while (inputCreditAmountIter) {
                System.out.print("\nEnter " + action + " Amount : ");
                try {
                    amount = Float.parseFloat(Main.read.next());
                    if (amount < minimumAllowance) {
                        if (amount < 0) {
                            System.out.println("\nError! Invalid " + action + " Credit Amount");
                        } else {
                            System.out.printf("\nError! Minimum " + action + " Credit Amount Limit is Rs. %,.2f\n",
                                    minimumAllowance);
                        }
                    } else {
                        inputCreditAmountIter = false;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("\nError! Invalid Input");
                }
            }
            return amount;
        }

        public static int getValidatedInteger() {
            int returnInt = -1;
            try {
                returnInt = Main.read.nextInt();
                if (returnInt < 0) {
                    return -1;
                }
            } catch (InputMismatchException _) {
                System.out.println("\nInvalid Input");
            } finally {
                Main.read.nextLine();
            }
            return returnInt;
        }

    }

    public static class Date {

        public static String getCurrentDate() {
            java.util.Date date = new java.util.Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return dateFormat.format(date);
        }

        public static String getCurrentTime() {
            java.util.Date date = new java.util.Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
            return dateFormat.format(date);
        }

        public static boolean startOfNewMonth() {
            return Integer.parseInt(getCurrentDate().split("/")[0].trim()) == 1;
        }

        public static int dateDiff(String date1, String date2) throws ParseException {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date dateOne, dateTwo;
            dateOne = dateFormat.parse(date1);
            dateTwo = dateFormat.parse(date2);

            long time_difference = dateOne.getTime() - dateTwo.getTime();

            return  Integer.parseInt(String.valueOf(time_difference / (1000L*60*60*24*365))) * 12;
        }
    }

}
