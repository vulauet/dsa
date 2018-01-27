package data_structures.trees;

public class HuffmanDecoding {
    static class Node {
        public int frequency;
        public char data;
        public Node left, right;
    }

    void decode(String S, Node root) {
        StringBuilder builder = new StringBuilder();
        Node walk = root;
        for (int i = 0; i < S.length(); i++) {
            if (S.charAt(i) == '1') walk = walk.right;
            else walk = walk.left;
            if (walk.data != '\0') {
                builder.append(walk.data);
                walk = root;
            }
        }
        System.out.println(builder.toString());
    }
}
