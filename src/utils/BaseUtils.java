package utils;

/**
 * Created by vula on 16/08/2017.
 */
public class BaseUtils {

    /**
     * Convert a number n in base b to a decimal number
     * 2 <= b <= 10
     * */
    public static int toDecimal(int n, int b) {
        int result = 0;
        int multiplier = 1;

        while (n > 0) {
            result += n % 10 * multiplier;
            multiplier *= b;
            n /= 10;
        }
        return result;
    }

    public static int javaToDecimal(int n, int b) {
        return Integer.parseInt("" + n, b);
    }

    /**
     * Convert a decimal number n to a number in base b
     * 2 <= b <= 10
     * */
    public static int fromDecimal(int n, int b) {
        int result = 0;
        int multiplier = 1;

        while (n > 0) {
            result += n % b * multiplier;
            multiplier *= 10;
            n /= b;
        }
        return result;
    }

    /**
     * Convert a decimal number to a number in base b
     * 2 <= b <= 20
     * */
    public String fromDecimal2(int n, int b) {
        String result = "";
        String chars = "0123456789ABCDEFGHIJ";

        while (n > 0) {
            result = chars.charAt(n % b) + result;
            n /= b;
        }
        return result;
    }


//    Integer.toBinaryString(n);
//    Integer.toOctalString(n);
//    Integer.toHexString(n);

}
