package data_structures.trees;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LowestCommonAncestor {
    static class Node {
        int data;
        Node left;
        Node right;
    }

    static Map<Node, Node> prevMap;

    static Node lca(Node root, int v1, int v2) {
        prevMap = new HashMap<>();
        linkPrev(root);
        List<Node> ancestor1 = new ArrayList<>();
        List<Node> ancestor2 = new ArrayList<>();
        for (Node node : prevMap.keySet()) {
            System.out.println(node.data + "," + prevMap.get(node).data);
            if (node.data == v1) ancestor1 = traceAncestor(node);
            if (node.data == v2) ancestor2 = traceAncestor(node);
        }

        if (ancestor1.size() <= 1 || ancestor2.size() <= 1) return root;
        int cursor1 = ancestor1.size() - 2;
        int cursor2 = ancestor2.size() - 2;
        while (cursor1 >= 0 && cursor2 >= 0) {
            if (ancestor1.get(cursor1--).data != ancestor2.get(cursor2--).data)
                return ancestor1.get(cursor1 + 2);
        }
        return ancestor1.get(cursor1 + 1);
    }

    private static List<Node> traceAncestor(Node node) {
        List<Node> ancestor = new ArrayList<>();
        while (prevMap.get(node) != null) {
            node = prevMap.get(node);
            ancestor.add(node);
        }
        return ancestor;
    }

    private static void linkPrev(Node node) {
        if (node.left != null) {
            prevMap.put(node.left, node);
            linkPrev(node.left);
        }
        if (node.right != null) {
            prevMap.put(node.right, node);
            linkPrev(node.right);
        }
    }
}
