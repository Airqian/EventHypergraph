package com.eventhypergraph.indextree.hyperedge;

import com.eventhypergraph.encoding.PPBitset;
import com.eventhypergraph.encoding.util.Triple;
import com.eventhypergraph.indextree.HyperedgeEncoding;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Hyperedge {
    private long id;

    /* 事件类型id，4字节 */
    private int eventTypeId;

    /* 元数据：该事件超边包含的顶点数 */
    private int numOfVertex;

    /* 元数据：该事件超边包含的属性数 */
    private int numOfProperty;

    /**
     * 元数据：
     * Assuming the definition of the User entity (with properties userName, phoneNumber, IDCard) and
     * the AOI entity (with properties AOIName, city), the vertexToPropOffset numerical list is [4, 6].
     * There are a total of 6 properties, with the User's property set being [0, 4) and the AOI's property set being [4, 6).
     */
    private List<Integer> vertexToPropOffset;

    private HyperedgeEncoding encoding;

    public Hyperedge() {}

    public Hyperedge (long id, int eventTypeId) {
        this.id = id;
        this.eventTypeId = eventTypeId;
        setNumOfVertex(requireVertexCounts());
        setNumOfProperty(requirePropertyCounts());
        setVertexToPropOffset(requireVertexToPropOffset());
        setEncoding(new HyperedgeEncoding(id, getNumOfProperty()));

    }

    public Hyperedge(long id, int eventTypeId, int numOfVertex, int numOfProperty, List<Integer> vertexToPropOffset) {
        this.id = id;
        this.eventTypeId = eventTypeId;
        this.numOfVertex = numOfVertex;
        this.numOfProperty = numOfProperty;
        this.vertexToPropOffset = vertexToPropOffset;
        this.encoding = new HyperedgeEncoding(id, numOfProperty);
    }






    // ---------------------------- Getter 和 Setter ----------------------------

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(int eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public int getNumOfVertex() {
        return numOfVertex;
    }

    public void setNumOfVertex(int numOfVertex) {
        this.numOfVertex = numOfVertex;
    }

    public int getNumOfProperty() {
        return numOfProperty;
    }

    public void setNumOfProperty(int numOfProperty) {
        this.numOfProperty = numOfProperty;
    }

    public List<Integer> getVertexToPropOffset() {
        return vertexToPropOffset;
    }

    public void setVertexToPropOffset(List<Integer> vertexToPropOffset) {
        this.vertexToPropOffset = vertexToPropOffset;
    }

    public HyperedgeEncoding getEncoding() {
        return encoding;
    }

    public void setEncoding(HyperedgeEncoding encoding) {
        this.encoding = encoding;
    }

    /**
     * 两条超边之间的属性编码OR操作，在构建索引树阶段同一个子节点中的所有超边向上汇聚成父节点时会用到（叶节点和非叶节点都会用到）
     * @param hyperedge
     * @return 执行 OR 操作获得的超边编码结果
     */
    public HyperedgeEncoding orEncoding(Hyperedge hyperedge) {
        if (this.numOfProperty != hyperedge.numOfProperty)
            throw new IllegalArgumentException(String.format("The number of properties contained in the two hyperedges is not equal." +
                    "hyperedge1: id = %d, numOfProperty = %d, hyperedge2: id = %d, numOfProperty = %d", id, numOfProperty, hyperedge.getId(), hyperedge.getNumOfProperty()));

        HyperedgeEncoding hyperedgeEncoding = new HyperedgeEncoding(numOfProperty);
        for (int i = 0; i < numOfProperty; i++) {
            PPBitset ppBitset = encoding.orOperation(i, hyperedge.getEncoding());
            hyperedgeEncoding.addEncoding(ppBitset);
        }
        return hyperedgeEncoding;
    }

    /**
     * 两条超边进行与操作，在索引树中进行自上而下的超边匹配会将用到，测试 hyperedge & ano_hyperedge == hyperedge
     * @param hyperedge
     * @return
     */
    public boolean isBitwiseSubset(Hyperedge hyperedge) {
        if (this.numOfProperty != hyperedge.numOfProperty)
            throw new IllegalArgumentException(String.format("The number of properties contained in the two hyperedges is not equal." +
                    "hyperedge1: id = %d, numOfProperty = %d, hyperedge2: id = %d, numOfProperty = %d", id, numOfProperty, hyperedge.getId(), hyperedge.getNumOfProperty()));

        for (int i = 0; i < numOfProperty; i++) {
            isBitwiseSubset(i, hyperedge);
        }

        return true;
    }

    public boolean isBitwiseSubset(int index, Hyperedge Hyperedge) {
        if (this.numOfProperty != Hyperedge.numOfProperty)
            throw new IllegalArgumentException(String.format("The number of properties contained in the two hyperedges is not equal." +
                    "hyperedge1: id = %d, numOfProperty = %d, hyperedge2: id = %d, numOfProperty = %d", id, numOfProperty, Hyperedge.getId(), Hyperedge.getNumOfProperty()));

        return encoding.isBitwiseSubset(index, Hyperedge.getEncoding());
    }


    public int cardinality() {
        int sum = 0;
        for (int i = 0; i < encoding.size(); i++)
            sum += encoding.cardinality(i);
        return sum;
    }

    public void addEncoding(@NotNull Triple<String, Integer, Integer> triple) {
        encoding.addEncoding(triple);
    }

    public void addEncoding(int index, @NotNull Triple<String, Integer, Integer> triple) {
        encoding.addEncoding(index, triple);
    }

    public void addAllEncoding(List<Triple<String, Integer, Integer>> triples) {
        encoding.addAllEncoding(triples);
    }

    public void addEncoding(@NotNull PPBitset bitset) {
        encoding.addEncoding(bitset);
    }

    // TODO：根据 eventTypeId 获得该事件超边的顶点数量
    private int requireVertexCounts() {
        return numOfVertex;
    }

    /**
     * TODO: 根据eventTypeId获得属性列表的长度
     */
    private int requirePropertyCounts() {
        return numOfProperty;
    }

    /**
     * TODO: 根据eventTypeId获得顶点到属性的偏移映射区间
     */
    private List<Integer> requireVertexToPropOffset() {
        return new ArrayList<>();
    }
}
