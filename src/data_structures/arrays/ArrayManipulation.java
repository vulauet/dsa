package data_structures.arrays;

import java.util.*;

/**
 * You are given a list(1-indexed) of size n, initialized with zeroes. You have to perform m
 * operations on the list and output the maximum of final values of all the n elements in the
 * list. For every operation, you are given three integers a, b and k, and you have to add value
 * k to all the elements ranging from index a to b (both inclusive).
 * 
 * For example, consider a list a of size 3. The initial list would be a = [0, 0, 0] and after
 * performing the update 0(a, b, k) = (2, 3, 30), the new list would be a = [0, 30, 30]. Here,
 * we've added value 30 to elements between indices 2 and 3. Note the index of the list starts
 * from 1.
 * 
 * Input Format
 * 
 * The first line will contain two integers n and m separated by a single space.
 * Next m lines will contain three integers, and separated by a single space.
 * Numbers in list are numbered from 1 to n.
 * 
 * Sample Input
 * 
 * 5 3
 * 1 2 100
 * 2 5 100
 * 3 4 100
 * 
 * Sample Output
 * 
 * 200
 */


public class ArrayManipulation {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        long[] values = new long[n];
        for (int i = 0; i < n; i++)
            values[i] = 0;
        int m = in.nextInt();
        for (int a0 = 0; a0 < m; a0++) {
            int a = in.nextInt();
            int b = in.nextInt();
            int k = in.nextInt();
            values[a - 1] += k;
            if (b < values.length) values[b] -= k;

        }
        long max = values[0];
        long sum = 0;
        for (int i = 0; i < n; i++) {
            sum += values[i];
            if (sum > max) max = sum;
        }
        System.out.println(max);
        in.close();
    }
}
