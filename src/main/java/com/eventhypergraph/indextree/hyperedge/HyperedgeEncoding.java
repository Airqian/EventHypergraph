package com.eventhypergraph.indextree.hyperedge;

import com.eventhypergraph.encoding.PPBitset;
import com.eventhypergraph.encoding.PropertyEncodingConstructor;
import com.eventhypergraph.encoding.util.Triple;
import com.sun.istack.internal.NotNull;

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
     * HACK：实际上EventType Table里也会保存特定事件类型的属性列表，等把table实现之后评估一下此处还需不需要保存。
     *      如果超边中某属性为空，PPBiteset初始为null还是，还是假定所有属性必须存在
     *      按逻辑细想下来在root部分保存propertyType即可，因为查询从root开始
     */
//    private String vertexType;

    private long hyperedgeId;

    private List<PPBitset> propertyBitsets;

    private int maxPropertyCount;

    public HyperedgeEncoding(int maxPropertyCount) {
        this.maxPropertyCount = maxPropertyCount;
        propertyBitsets = new ArrayList<>();
    }

    /* 创建hyperedge时就需要传入属性个数以初始化maxPropertyCount */
    public HyperedgeEncoding(long hyperedgeId, int maxPropertyCount) {
        this.hyperedgeId = hyperedgeId;
        this.maxPropertyCount = maxPropertyCount;
        propertyBitsets = new ArrayList<>();
    }

    public void addEncoding(String content, int length, int hashFuncCount) {
        if (!isFull()) {
            PPBitset ppBitset = PropertyEncodingConstructor.encoding(content, length, hashFuncCount);
            propertyBitsets.add(ppBitset);
        } else {
            throw new IndexOutOfBoundsException(String.format("The property list of this event hyperedge is full.The max size is %d" , maxPropertyCount));
        }
    }

    public void addEncoding(int index, String content, int length, int hashFuncCount) {
        if (!isFull()) {
            PPBitset ppBitset = PropertyEncodingConstructor.encoding(content, length, hashFuncCount);
            propertyBitsets.add(index, ppBitset);
        } else {
            throw new IndexOutOfBoundsException(String.format("The property list of this event hyperedge is full.The max size is %d" , maxPropertyCount));
        }
    }

    public void addEncoding(@NotNull PPBitset bitset) {
        if (!isFull()) {
            propertyBitsets.add(bitset);
        } else {
            throw new IndexOutOfBoundsException(String.format("The property list of this event hyperedge is full.The max size is %d" , maxPropertyCount));
        }
    }

    public void addEncoding(int index, @NotNull PPBitset bitset) {
        if (!isFull()) {
            propertyBitsets.add(index, bitset);
        } else {
            throw new IndexOutOfBoundsException(String.format("The property list of this event hyperedge is full.The max size is %d" , maxPropertyCount));
        }
    }


    public PPBitset andOperation(int index, @NotNull PPBitset another) {
        checkIndex(index);

        PPBitset bitset = propertyBitsets.get(index);
        return bitset.and(another);
    }

    public PPBitset orOperation(int index, @NotNull PPBitset another) {
        checkIndex(index);

        PPBitset bitset = propertyBitsets.get(index);
        return bitset.or(another);
    }

    public PPBitset orOperation(int index, @NotNull HyperedgeEncoding hyperedgeEncoding) {
        checkIndex(index);

        PPBitset bitset = propertyBitsets.get(index);
        return bitset.or(hyperedgeEncoding.getProperty(index));
    }

    public boolean isBitwiseSubset(int index, @NotNull HyperedgeEncoding hyperedgeEncoding) {
        checkIndex(index);

        PPBitset bitset = propertyBitsets.get(index);
        return bitset.isBitwiseSubset(hyperedgeEncoding.getProperty(index));
    }

    public int cardinality(int index) {
        checkIndex(index);

        return propertyBitsets.get(index).cardinality();
    }

    public int size() {
        return propertyBitsets.size();
    }

    public int length() {
        return this.maxPropertyCount;
    }

    public boolean isFull() {
        return propertyBitsets.size() == maxPropertyCount;
    }

    public PPBitset getProperty(int index) {
        checkIndex(index);

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

    private void checkIndex(int index) {
        if (index < 0)
            throw new IllegalArgumentException("The index cannot be less than 0.");

        if (index > propertyBitsets.size() || index > maxPropertyCount)
            throw new IndexOutOfBoundsException(String.format("The index exceeds the length of the property list. index = %d, actual property length = %d, max length = %d", index, propertyBitsets.size(), maxPropertyCount));
    }

    public void printEncoding() {
        StringBuilder builder = new StringBuilder();
        for (PPBitset ppBitset : propertyBitsets) {
            builder.append(ppBitset.toString());
            builder.append(" ");
        }
        System.out.println(builder);
    }
}