package utils;

import java.util.Arrays;

/**
 * Created by vula on 10/08/2017.
 */
public class PrimeUtils {
    public static boolean isPrime(int n) {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        int m = (int) Math.sqrt(n);

        for (int i = 3; i <= m; i += 2) if (n % i == 0) return false;
        return true;
    }

    public static boolean[] sieve(int n) {
        boolean[] prime = new boolean[n + 1];
        Arrays.fill(prime, true);
        prime[0] = false;
        prime[1] = false;
        int m = (int) Math.sqrt(n);
        for (int i = 2; i <= m; i++)
            if (prime[i])
                for (int j = i * i; j <= n; j += i)
                    prime[j] = false;
        return prime;
    }
}
