package com.eventhypergraph.indextree;

import com.eventhypergraph.encoding.PPBitset;
import com.eventhypergraph.encoding.PropertyEncodingConstructor;
import com.eventhypergraph.encoding.util.Triple;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;


/**
 * This class saves the encoding of event hyperedges and encapsulates the bit operations of the PPBitset class.
 * The order of the encodings must strictly align with the order of the vertices.
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

    /**
     * Store the maximum number of property encodings that this hyperedge can contain.
     */
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

    public void addEncoding(@NotNull Triple<String, Integer, Integer> triple) {
        if (!isFull()) {
            PPBitset ppBitset = new PropertyEncodingConstructor().encoding(triple);
            propertyBitsets.add(ppBitset);
        } else {
            throw new IndexOutOfBoundsException(String.format("The property list of this event hyperedge is full.The max size is %d" , maxPropertyCount));
        }
    }

    public void addEncoding(int index, @NotNull Triple<String, Integer, Integer> triple) {
        if (!isFull()) {
            PPBitset ppBitset = new PropertyEncodingConstructor().encoding(triple);
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

    public void addAllEncoding(@NotNull List<Triple<String, Integer, Integer>> triples) {
        if (triples.size() == 0)
            throw new IllegalArgumentException("The number of property cannot be zero");

        for(Triple<String, Integer, Integer> triple : triples) {
            if (!isFull()) {
                addEncoding(triple);
            } else {
                throw new IndexOutOfBoundsException(String.format("The property list of this event hyperedge is full.The max size is %d" , maxPropertyCount));
            }
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

    public boolean isBitwiseSubset(int index, @NotNull PPBitset another) {
        checkIndex(index);

        PPBitset bitset = propertyBitsets.get(index);
        return bitset.isBitwiseSubset(another);
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
}