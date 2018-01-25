package data_structures.trees;

public class IsBinarySearchTree {

    /**
     * For the purposes of this challenge, we define a binary tree to be a binary search tree
     * with the following ordering requirements:
     * <p>
     * The data value of every node in a node's left subtree is less than the data value of that
     * node.
     * The data value of every node in a node's right subtree is greater than the data value of that
     * node.
     * Given the root node of a binary tree, can you determine if it's also a binary search tree?
     * <p>
     * Complete the function in your editor below, which has 1 parameter: a pointer to the root of
     * a binary tree. It must return a boolean denoting whether or not the binary tree is a binary
     * search tree. You may have to write one or more helper functions to complete this challenge.
     */

    private static final int MAX_VAL = 10001;
    private static final int MIN_VAL = -1;

    static class Node {
        int data;
        Node left;
        Node right;
    }

    boolean checkBST(Node root) {
        int left = treeMax(root.left);
        int right = treeMin(root.right);
        return root.data > left && root.data < right;
    }

    private int treeMin(Node node) {
        if (node.left == null && node.right == null) return node.data;
        if ((node.left != null && node.data == node.left.data)
                || (node.right != null && node.data == node.right.data)) return MIN_VAL;
        int left = node.left != null ? treeMax(node.left) : node.data;
        int right = node.right != null ? treeMin(node.right) : node.data;
        if (node.data >= left && node.data <= right) return treeMin(node.left);
        return MIN_VAL;
    }

    private int treeMax(Node node) {
        if (node.left == null && node.right == null) return node.data;
        if ((node.left != null && node.data == node.left.data)
                || (node.right != null && node.data == node.right.data)) return MAX_VAL;
        int left = node.left != null ? treeMax(node.left) : node.data;
        int right = node.right != null ? treeMin(node.right) : node.data;
        if (node.data >= left && node.data <= right) return treeMax(node.right);
        return MAX_VAL;
    }
}
