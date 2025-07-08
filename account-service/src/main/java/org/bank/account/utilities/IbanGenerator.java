package org.bank.account.utilities;

public class IbanGenerator {
    public static String generateValidIban(String bankCode, String branchCode, String customerNumber) {
        bankCode = String.format("%05d", Integer.parseInt(bankCode));
        branchCode = String.format("%05d", Integer.parseInt(branchCode));
        customerNumber = String.format("%016d", Long.parseLong(customerNumber));

        String base = bankCode + branchCode + customerNumber;

        String tempIban = base + convertLettersToDigits("TR00");

        int mod97 = mod97(tempIban);
        int checkDigits = 98 - mod97;

        return String.format("TR%02d%s", checkDigits, base);
    }

    private static String convertLettersToDigits(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isLetter(c)) {
                sb.append((c - 'A') + 10);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static int mod97(String numericIban) {
        String remainder = numericIban;
        String block;

        while (remainder.length() > 9) {
            block = remainder.substring(0, 9);
            remainder = Integer.parseInt(block) % 97 + remainder.substring(9);
        }
        return Integer.parseInt(remainder) % 97;
    }
}