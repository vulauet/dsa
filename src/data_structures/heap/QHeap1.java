package data_structures.heap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class QHeap1 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        QHeap qheap = new QHeap();
        int q = scanner.nextInt();
        for (int i = 0; i < q; i++) {
            int p = scanner.nextInt();
            switch (p) {
                case 1:
                    qheap.add(scanner.nextInt());
                    break;
                case 2:
                    qheap.remove(scanner.nextInt());
                    break;
                case 3:
                    System.out.println(qheap.getMin());
                    break;
            }
        }
    }


    private static class QHeap {
        private Set<Integer> dataSet;
        private int min;

        public QHeap() {
            dataSet = new HashSet<>();
            min = Integer.MAX_VALUE;
        }

        public void add(int i) {
            dataSet.add(i);
            if (i < min) min = i;
        }

        public void remove(int i) {
            dataSet.remove(i);
            if (min == i) {
                if (!dataSet.isEmpty())
                    min = Collections.min(dataSet);
                else min = Integer.MAX_VALUE;
            }
        }

        public int getMin() {
            return min;
        }
    }
}
