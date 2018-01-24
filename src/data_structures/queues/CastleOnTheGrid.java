package data_structures.queues;

import java.util.*;

public class CastleOnTheGrid {

    static class Node {
        int x;
        int y;
        Node prev;

        public Node(int startX, int startY) {
            this.x = startX;
            this.y = startY;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return x == node.x &&
                    y == node.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        public void setPrev(Node prevNode) {
            this.prev = prevNode;
        }
    }

    static int minimumMoves(String[] grid, int startX, int startY, int goalX, int goalY) {
        // Complete this function
        List<Node> path = bfs(grid, startX, startY, goalX, goalY);
        return path.size() - 1;
    }

    private static List<Node> bfs(String[] grid, int startX, int startY, int goalX, int goalY) {
        Node start = new Node(startX, startY);
        Set<Node> nodeSet = new HashSet<>();
        Queue<Node> nodeQueue = new LinkedList<>();
        nodeQueue.add(start);

        while (!nodeQueue.isEmpty()) {
            Node first = nodeQueue.remove();
            if (first.x == goalX && first.y == goalY) return constructPath(first);
            List<Node> neighbors = new ArrayList<>();

            for (int i = first.x + 1; i < grid.length; i++) {
                if (addNode(grid[i], neighbors, i, first.y)) break;
            }

            for (int i = first.x - 1; i >= 0; i--) {
                if (addNode(grid[i], neighbors, i, first.y)) break;
            }

            for (int i = first.y + 1; i < grid.length; i++) {
                if (addNode(grid[first.x], neighbors, first.x, i)) break;
            }

            for (int i = first.y - 1; i >= 0; i--) {
                if (addNode(grid[first.x], neighbors, first.x, i)) break;
            }

            for (Node neighbor : neighbors) {
                if (!nodeSet.contains(neighbor) && neighbor.prev == null) {
                    neighbor.setPrev(first);
                    nodeQueue.add(neighbor);
                    nodeSet.add(neighbor);
                }
            }
        }

        return null;
    }

    private static boolean addNode(String s, List<Node> neighbors, int x, int y) {
        if (s.charAt(y) == 'X') return true;
        neighbors.add(new Node(x, y));
        return false;
    }

    private static List<Node> constructPath(Node walk) {
        List<Node> path = new ArrayList<>();
        path.add(walk);
//        System.out.println(walk.x + ", " + walk.y);
        while (walk.prev != null) {
            walk = walk.prev;
//            System.out.println(walk.x + ", " + walk.y);
            path.add(walk);
        }
        return path;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        String[] grid = new String[n];
        for(int grid_i = 0; grid_i < n; grid_i++){
            grid[grid_i] = in.next();
        }
        int startX = in.nextInt();
        int startY = in.nextInt();
        int goalX = in.nextInt();
        int goalY = in.nextInt();
        int result = minimumMoves(grid, startX, startY, goalX, goalY);
        System.out.println(result);
        in.close();
    }
}
