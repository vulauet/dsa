package data_structures.queues;

import java.util.*;

public class Down2ZeroPreCompute {

    /**
     * You are given Q queries. Each query consists of a single number N. You can perform any of
     * the 2 operations on  in each move:
     * <p>
     * 1: If we take 2 integers a and b where N = a x b (a != 1, b != 1), then we can change N =
     * max(a, b)
     * <p>
     * 2: Decrease the value of N by 1.
     * <p>
     * Determine the minimum number of moves required to reduce the value of N to 0.
     * <p>
     * Input Format
     * <p>
     * 2
     * 3
     * 4
     * <p>
     * The first line contains the integer Q.
     * The next Q lines each contain an integer, N.
     * <p>
     * Sample Output
     * <p>
     * 3
     * 3
     */

//    static Map<Integer, Integer> down2ZeroCount = new HashMap<>();
    public static final int MAX_VAL = 1000 * 1000;
    static int[] cacheVal = new int[MAX_VAL + 1];

    static {
        cacheVal[0] = 0;
        for (int i = 1; i <= MAX_VAL; i++) {
            if (cacheVal[i] == 0 || cacheVal[i] > cacheVal[i - 1] + 1)
                cacheVal[i] = cacheVal[i - 1] + 1;
            for (int j = 2; j <= i && j * i <= MAX_VAL; j++) {
                if (cacheVal[j * i] == 0 || cacheVal[j * i] > cacheVal[i] + 1) {
                    cacheVal[j * i] = cacheVal[i] + 1;
                }
            }
        }
    }

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        int Q = in.nextInt();
        for (int a0 = 0; a0 < Q; a0++) {
            int N = in.nextInt();
            System.out.println(cacheVal[N]);
//            System.out.println(down2ZeroCount.get(N));
        }
    }

//    private static int down2Zero(int n) {
//        if (n <= 3) return n;
//        if (!down2ZeroCount.containsKey(n)) {
//            int stepCount = 1 + down2Zero(n - 1);
//            for (int i = n / 2; i >= Math.sqrt(n); i--) {
//                if (n % i == 0) {
//                    int curStepCount = 1 + down2Zero(i);
//                    if (curStepCount < stepCount) stepCount = curStepCount;
//                }
//            }
//            down2ZeroCount.put(n, stepCount);
//        }
//        return down2ZeroCount.get(n);
//
//    }
}
