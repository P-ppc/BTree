package com.ppc.btree;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class Element<K, V> {
    private K key;

    private V value;

    @Override
    public String toString() {
        return key + ": " + value;
    }
}
