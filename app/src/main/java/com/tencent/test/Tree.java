package com.tencent.test;

public class Tree {
    private TreeNode head;

    public Tree() {
    }

    public TreeNode insert(int value) {
        if (head == null) {
            head = new TreeNode(value);
        } else {
            TreeNode node = head;
            do {
                if (value == node.value) {
                    node.value = value;
                } else if (value < node.value) {
                    // enter left;
                    node = node.left;
                } else {
                    // enter right;
                    node = node.right;
                }
            } while (true);
        }

        return null;
    }
    
    public static class TreeNode {
        int value;
        TreeNode left;
        TreeNode right;

        public TreeNode(int value) {
            this.value = value;
        }
    }
}
