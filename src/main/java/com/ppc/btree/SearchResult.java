package com.ppc.btree;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class SearchResult<K, V> {
    /**
     * 是否找到对应关键字
     */
    private boolean found;

    /**
     * 对应的节点
     */
    private BTreeNode<K, V> node;

    /**
     * 关键字在node的index, -1代表不再该节点中
     */
    private int index;
}
