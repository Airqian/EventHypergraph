package com.eventhypergraph.indextree.hyperedge;

import com.eventhypergraph.encoding.PPBitset;
import com.eventhypergraph.encoding.PropertyEncodingConstructor;
import com.eventhypergraph.encoding.util.Triple;


import java.util.ArrayList;
import java.util.List;


/**
 * 这个类保存事件超边的编码，并封装了 PPBitset 类的位操作。
 * 编码的顺序必须严格与属性的顺序对齐。
 */
public class HyperedgeEncoding {
    /**
     * 预先在此处保存一份，其是下划线形式，如"propertyNameA_propertyNameB_propertyNameC"
     * 查询编码的时候要指定propertyName
     */
//    private String vertexType;

    private long hyperedgeId;

    private List<PPBitset> propertyBitsets;

    private int bitsetNum;

    public HyperedgeEncoding(int bitsetNum) {
        this.bitsetNum = bitsetNum;
        propertyBitsets = new ArrayList<>();
    }

    /* 创建hyperedge时就需要传入属性个数以初始化bitsetNum */
    public HyperedgeEncoding(long hyperedgeId, int bitsetNum) {
        this.hyperedgeId = hyperedgeId;
        this.bitsetNum = bitsetNum;
        propertyBitsets = new ArrayList<>();
    }

    public void addEncoding(PPBitset bitset) {
        propertyBitsets.add(bitset);
    }

    public PPBitset andOperation(int index, PPBitset another) {
        PPBitset bitset = propertyBitsets.get(index);
        return bitset.and(another);
    }

    public PPBitset orOperation(int index, PPBitset another) {
        PPBitset bitset = propertyBitsets.get(index);
        return bitset.or(another);
    }

    public PPBitset orOperation(int index, HyperedgeEncoding hyperedgeEncoding) {
        PPBitset bitset = propertyBitsets.get(index);
        return bitset.or(hyperedgeEncoding.getProperty(index));
    }

    public boolean isBitwiseSubset(int index, HyperedgeEncoding hyperedgeEncoding) {

        PPBitset bitset = propertyBitsets.get(index);
        return bitset.isBitwiseSubset(hyperedgeEncoding.getProperty(index));
    }

    public int cardinality(int index) {
        return propertyBitsets.get(index).cardinality();
    }

    public int size() {
        return propertyBitsets.size();
    }

    public PPBitset getProperty(int index) {
        return propertyBitsets.get(index);
    }

    public List<PPBitset> getPropertyBitsets() {
        return propertyBitsets;
    }

    public void setHyperedgeId(long id) {
        this.hyperedgeId = id;
    }

    public long getHyperedgeId() {
        return hyperedgeId;
    }

    public String printEncoding() {
        StringBuilder builder = new StringBuilder();
        for (PPBitset ppBitset : propertyBitsets) {
            builder.append(ppBitset.toString());
            builder.append(" ");
        }
        return builder.toString();
    }
}