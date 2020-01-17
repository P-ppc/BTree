package com.ppc.btree;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
class BTreeNode<K, V> {
    public List<Element<K, V>> elements;
    public List<BTreeNode<K, V>> children;

    @ToString.Exclude
    public BTreeNode<K, V> parent;

    @ToString.Exclude
    private Comparator<K> comparator;

    BTreeNode(Comparator<K> comparator) {
        this.comparator = comparator;
        elements = new ArrayList<>();
        children = new ArrayList<>();
        this.parent = null;
    }

    /**
     * 返回是否为叶子节点
     * @return
     */
    boolean isLeaf() {
        return children.isEmpty();
    }
}
