package utils;

/**
 * Created by vula on 11/08/2017.
 */
public class GCD {
    public static int GCD(int a, int b) {
        if (a == 0 && b == 0) return 0;
        if (b == 0) return a;
        return GCD(b, a % b);
    }

    public static int LCM(int a, int b) {
        return b * a / GCD(a, b);
    }
}
