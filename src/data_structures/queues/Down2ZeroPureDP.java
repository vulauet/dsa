package data_structures.queues;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Down2ZeroPureDP {
    static Map<Integer, Integer> down2ZeroCount = new HashMap<>();
    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        int Q = in.nextInt();
        for (int a0 = 0; a0 < Q; a0++) {
            int N = in.nextInt();
            System.out.println(down2Zero(N));
        }
    }

    private static int down2Zero(int n) {
        if (n <= 3) return n;
        if (!down2ZeroCount.containsKey(n)) {
            int stepCount = 1 + down2Zero(n - 1);
            for (int i = n / 2; i >= Math.sqrt(n); i--) {
                if (n % i == 0) {
                    int curStepCount = 1 + down2Zero(i);
                    if (curStepCount < stepCount) stepCount = curStepCount;
                }
            }
            down2ZeroCount.put(n, stepCount);
        }
        return down2ZeroCount.get(n);
    }
}
