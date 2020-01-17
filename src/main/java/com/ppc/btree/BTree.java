package com.ppc.btree;

import java.util.Comparator;

public class BTree<K, V> {
    /**
     * 阶数: 子节点数量
     */
    private int degree;

    /**
     * 根节点最大关键字数量
     */
    private int ROOT_MAX_KEY_SIZE;

    /**
     * 根节点最小关键字数量
     */
    private int ROOT_MIN_KEY_SIZE;

    /**
     * 普通节点最大关键字数量
     */
    private int NODE_MAX_KEY_SIZE;

    /**
     * 普通节点最小关键字数量
     */
    private int NODE_MIN_KEY_SIZE;

    /**
     * 根节点
     */
    private BTreeNode<K, V> root;

    private Comparator<K> comparator;

    public BTree(int degree, Comparator<K> comparator) {
        this.degree = degree;
        ROOT_MAX_KEY_SIZE = degree - 1;
        ROOT_MIN_KEY_SIZE = 1;
        NODE_MAX_KEY_SIZE = degree - 1;
        NODE_MIN_KEY_SIZE = ((int) Math.ceil(degree / 2.0)) - 1;
        root = new BTreeNode<>(comparator);

        this.comparator = comparator;

        System.out.println("degree: " + degree);
        System.out.println("ROOT_MAX_KEY_SIZE: " + ROOT_MAX_KEY_SIZE);
        System.out.println("ROOT_MIN_KEY_SIZE: " + ROOT_MIN_KEY_SIZE);
        System.out.println("NODE_MAX_KEY_SIZE: " + NODE_MAX_KEY_SIZE);
        System.out.println("NODE_MIN_KEY_SIZE: " + NODE_MIN_KEY_SIZE);
        System.out.println("\n");
    }

    /**
     *
     * @param node
     * @param key
     * @param value
     * @param index -1表示不知道插入位置
     * @return
     */
    private int insertElement(BTreeNode<K, V> node, K key, V value, int index) {
        if (index == -1) {
            index = 0;
            int size = node.elements.size();

            while (index < size) {
                Element<K, V> element = node.elements.get(index);
                int comparatorResult = comparator.compare(key, element.getKey());
                if (comparatorResult >= 0) {
                    index++;
                } else {
                    break;
                }
            }
        }

        node.elements.add(index, new Element<>(key, value));
        return index;
    }

    /**
     * 如果有对应节点则更新，否则插入节点
     * @param key
     * @param value
     */
    public V put(K key, V value) {
        SearchResult<K, V> searchResult = search(key, root);
        BTreeNode<K, V> node = searchResult.getNode();
        if (searchResult.isFound()) {
            Element<K, V> element = node.elements.get(searchResult.getIndex());
            V originValue = element.getValue();
            element.setValue(value);
            return originValue;
        }

        insertElement(node, key, value, searchResult.getIndex());
        while (node.elements.size() > NODE_MAX_KEY_SIZE) {
            BTreeNode<K, V> left = new BTreeNode<>(comparator);
            for (int i = 0; i < NODE_MIN_KEY_SIZE; i++) {
                left.elements.add(node.elements.get(i));
            }
            if (!node.children.isEmpty()) {
                for (int i = 0; i < NODE_MIN_KEY_SIZE + 1; i++) {
                    left.children.add(node.children.get(i));
                }
            }

            Element<K, V> mid = node.elements.get(NODE_MIN_KEY_SIZE);

            BTreeNode<K, V> right = new BTreeNode<>(comparator);
            for (int i = NODE_MIN_KEY_SIZE + 1; i < node.elements.size(); i++) {
                right.elements.add(node.elements.get(i));
            }
            if (!node.children.isEmpty()) {
                for (int i = NODE_MIN_KEY_SIZE + 1; i < node.children.size(); i++) {
                    right.children.add(node.children.get(i));
                }
            }

            BTreeNode<K, V> parent = node.parent;
            if (null == parent) {
                parent = new BTreeNode<>(comparator);
                parent.elements.add(mid);
                parent.children.add(left);
                parent.children.add(right);
                left.parent = parent;
                right.parent = parent;
                root = parent;
                break;
            }

            int i = insertElement(parent, mid.getKey(), mid.getValue(), -1);

            // 移除child并插入新的children
            parent.children.set(i, left);
            parent.children.add(i + 1, right);
            left.parent = parent;
            right.parent = parent;
            node.parent = null;
            // do loop
            node = parent;
        }

        return null;
    }

    public V get(K key) {
        SearchResult<K, V> searchResult = search(key, root);
        if (searchResult.isFound()) {
            Element<K, V> element = searchResult.getNode().elements.get(searchResult.getIndex());
            return element.getValue();
        }
        return null;
    }

    /**
     * 删除key对应的元素并返回其值
     * @param key
     * @return
     */
    public V remove(K key) {
        SearchResult<K, V> searchResult = search(key, root);
        if (!searchResult.isFound()) {
            return null;
        }

        BTreeNode<K, V> node = searchResult.getNode();
        V value = node.elements.get(searchResult.getIndex()).getValue();
        if (!node.isLeaf()) {
            SearchResult<K, V> replaceSearchResult = searchPrev(node, searchResult.getIndex());
            BTreeNode<K, V> replaceNode = replaceSearchResult.getNode();
            if (replaceNode.elements.size() == NODE_MIN_KEY_SIZE) {
                replaceSearchResult = searchNext(node, searchResult.getIndex());
                replaceNode = replaceSearchResult.getNode();

            }
            node.elements.set(searchResult.getIndex(), replaceNode.elements.remove(replaceSearchResult.getIndex()));
            adjust(replaceNode);
        } else {
            node.elements.remove(searchResult.getIndex());
            adjust(node);
        }

        return value;
    }

    private void adjust(BTreeNode<K, V> node) {
        if (root == node || node.elements.size() >= NODE_MIN_KEY_SIZE) {
            return;
        }

        BTreeNode<K, V> parent = node.parent;
        int index = parent.children.indexOf(node);

        BTreeNode<K, V> leftSibling = index > 0 ? parent.children.get(index - 1) : null;
        if (null != leftSibling && leftSibling.elements.size() > NODE_MIN_KEY_SIZE) {
            // 右旋
            Element<K, V> parentElement = parent.elements.get(index - 1);
            parent.elements.set(index - 1, leftSibling.elements.remove(leftSibling.elements.size() - 1));
            node.elements.add(0, parentElement);
            if (!node.isLeaf()) {
                node.children.add(0, leftSibling.children.remove(leftSibling.children.size() - 1));
            }
            return;
        }

        BTreeNode<K, V> rightSibling = index < parent.children.size() - 1 ? parent.children.get(index + 1) : null;
        if (null != rightSibling && rightSibling.elements.size() > NODE_MIN_KEY_SIZE) {
            // 左旋
            Element<K, V> parentElement = parent.elements.get(index);
            parent.elements.set(index, rightSibling.elements.remove(0));
            node.elements.add(parentElement);
            if (!node.isLeaf()) {
                node.children.add(rightSibling.children.remove(0));
            }
            return;
        }

        if (null != leftSibling) {
            // 向左合并
            Element<K, V> parentElement = parent.elements.remove(index - 1);
            parent.children.remove(index);
            leftSibling.elements.add(parentElement);
            leftSibling.elements.addAll(node.elements);
            if (!node.isLeaf()) {
                leftSibling.children.addAll(node.children);
            }

            if (root == parent && parent.elements.size() == 0) {
                root = leftSibling;
            } else {
                adjust(parent);
            }
        } else if (null != rightSibling) {
            // 向右合并
            Element<K, V> parentElement = parent.elements.remove(index);
            parent.children.remove(index);
            rightSibling.elements.add(0, parentElement);
            rightSibling.elements.addAll(0, node.elements);
            if (!node.isLeaf()) {
                rightSibling.children.addAll(0, node.children);
            }

            if (root == parent && parent.elements.size() == 0) {
                root = rightSibling;
            } else {
                adjust(parent);
            }
        }
    }

    private SearchResult<K, V> search(K key, BTreeNode<K, V> node) {
        int i = 0;
        while (i < node.elements.size()) {
            Element<K, V> element = node.elements.get(i);
            int comparatorResult = comparator.compare(key, element.getKey());
            if (comparatorResult == 0) {
                return new SearchResult<>(true, node, i);
            } else if (comparatorResult > 0) {
                i++;
            } else {
                break;
            }
        }

        if (node.isLeaf()) {
            return new SearchResult<>(false, node, i);
        } else {
            return search(key, node.children.get(i));
        }
    }

    /**
     * 获取前驱
     * @return
     */
    private SearchResult<K, V> searchPrev(BTreeNode<K, V> node, int index) {
        BTreeNode<K, V> child = node.children.get(index);
        while (!child.isLeaf()) {
            child = child.children.get(child.children.size() - 1);
        }

        int prevIndex = child.elements.size() - 1;
        return new SearchResult<>(true, child, prevIndex);
    }

    /**
     * 获取后继
     * @return
     */
    private SearchResult<K, V> searchNext(BTreeNode<K, V> node, int index) {
        BTreeNode<K, V> child = node.children.get(index + 1);
        while (!child.isLeaf()) {
            child = child.children.get(0);
        }

        return new SearchResult<>(true, child, 0);
    }

    public static void main(String[] args) {
        BTree<Integer, Integer> bTree = new BTree<>(5, Integer::compareTo);
        for (int i = 0; i <= 10; i++) {
            bTree.put(i, i);
            System.out.println(bTree.root);
        }
        bTree.put(10, 11);
        System.out.println(bTree.root);

        for (int i = 0; i <= 10; i++) {
            bTree.remove(i);
            System.out.println(bTree.root);
        }
    }
}
