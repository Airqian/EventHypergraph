package com.eventhypergraph.indextree.hyperedge;

import com.eventhypergraph.encoding.PPBitset;
import com.eventhypergraph.encoding.PropertyEncodingConstructor;
import com.eventhypergraph.encoding.util.Triple;
import com.eventhypergraph.indextree.util.IDGenerator;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Hyperedge {
    private long id;

    /* 事件类型id，4字节 */
    private int eventTypeId;

    /* 元数据：该事件超边包含的顶点数 */
    private int numOfVertex;

    /* 元数据：该事件超边包含的属性数（算是冗余存储） */
    private int maxPropertyNum;

    /**
     * 元数据：
     * Assuming the definition of the User entity (with properties userName, phoneNumber, IDCard) and
     * the AOI entity (with properties AOIName, city), the vertexToPropOffset numerical list is [4, 6].
     * There are a total of 6 properties, with the User's property set being [1, 4) and the AOI's property set being [4, 6).
     */
    private int[] vertexToPropOffset;

    private HyperedgeEncoding encoding;

    public void Hyperedge() {

    }


    public Hyperedge(int numOfVertex, int maxPropertyNum, int[] vertexToPropOffset) {
        this.id = IDGenerator.generateNodeId();
        this.numOfVertex = numOfVertex;
        this.maxPropertyNum = maxPropertyNum;
        this.vertexToPropOffset = vertexToPropOffset;
        this.encoding = new HyperedgeEncoding(id, maxPropertyNum);
    }

    public Hyperedge(int eventTypeId, int numOfVertex, int maxPropertyNum, int[] vertexToPropOffset) {
        this(numOfVertex, maxPropertyNum, vertexToPropOffset);

        this.eventTypeId = eventTypeId;
    }

    public PPBitset getEncodingAt(int index) {
        if (index >= encoding.size() || index < 0)
            throw new ArrayIndexOutOfBoundsException("索引出界");
        return this.encoding.getProperty(index);
    }

    // 将编码中所有的1全部置为0
    public void clear() {
        for (int i = 0; i < encoding.size(); i++)
            encoding.getProperty(i).clear();
    }

    public Hyperedge clone() {
        Hyperedge hyperedge = new Hyperedge(numOfVertex, maxPropertyNum, vertexToPropOffset);
        for (PPBitset bitset : this.getEncoding().getPropertyBitsets()) {
            PPBitset ppBitset = (PPBitset) bitset.clone();
            hyperedge.addEncoding(ppBitset);
        }
        return hyperedge;
    }

    /**
     * 两条超边之间的属性编码OR操作，在构建索引树阶段同一个子节点中的所有超边向上汇聚成父节点时会用到（叶节点和非叶节点都会用到）
     * @param hyperedge
     * @return 执行 OR 操作获得的超边编码结果
     */
    public HyperedgeEncoding orEncoding(Hyperedge hyperedge) {
        if (this.maxPropertyNum != hyperedge.maxPropertyNum)
            throw new IllegalArgumentException(String.format("The number of properties contained in the two hyperedges is not equal." +
                    "hyperedge1: id = %d, numOfProperty = %d, hyperedge2: id = %d, numOfProperty = %d", id, maxPropertyNum, hyperedge.getId(), hyperedge.getMaxPropertyNum()));

        HyperedgeEncoding hyperedgeEncoding = new HyperedgeEncoding(maxPropertyNum);
        for (int i = 0; i < maxPropertyNum; i++) {
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
        if (this.maxPropertyNum != hyperedge.maxPropertyNum)
            throw new IllegalArgumentException(String.format("The number of properties contained in the two hyperedges is not equal." +
                    "hyperedge1: id = %d, numOfProperty = %d, hyperedge2: id = %d, numOfProperty = %d", id, maxPropertyNum, hyperedge.getId(), hyperedge.getMaxPropertyNum()));

        for (int i = 0; i < maxPropertyNum; i++) {
            isBitwiseSubset(i, hyperedge);
        }

        return true;
    }

    // 特定位置的属性遍码
    public boolean isBitwiseSubset(int index, Hyperedge Hyperedge) {
        if (this.maxPropertyNum != Hyperedge.maxPropertyNum)
            throw new IllegalArgumentException(String.format("The number of properties contained in the two hyperedges is not equal." +
                    "hyperedge1: id = %d, numOfProperty = %d, hyperedge2: id = %d, numOfProperty = %d", id, maxPropertyNum, Hyperedge.getId(), Hyperedge.getMaxPropertyNum()));

        return encoding.isBitwiseSubset(index, Hyperedge.getEncoding());
    }

    // 获得超边编码的总长度
    public int getEncodingLength() {
        int sum = 0;
        for (int i = 0; i < encoding.size(); i++)
            sum += encoding.getProperty(i).length();
        return sum;
    }

    public int cardinality() {
        int sum = 0;
        for (int i = 0; i < encoding.size(); i++)
            sum += encoding.cardinality(i);
        return sum;
    }

    public void addEncoding(String content, int length, int hashFuncCount) {
        PPBitset ppBitset = PropertyEncodingConstructor.encoding(content, length, hashFuncCount);
        encoding.addEncoding(ppBitset);
    }

    public void addEncoding(int index, String content, int length, int hashFuncCount) {
        PPBitset ppBitset = PropertyEncodingConstructor.encoding(content, length, hashFuncCount);
        encoding.addEncoding(index, ppBitset);
    }



    public void addEncoding(@NotNull PPBitset bitset) {
        encoding.addEncoding(bitset);
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

    public int getMaxPropertyNum() {
        return maxPropertyNum;
    }

    public void setMaxPropertyNum(int maxPropertyNum) {
        this.maxPropertyNum = maxPropertyNum;
    }

    public int[] getVertexToPropOffset() {
        return vertexToPropOffset;
    }

    public void setVertexToPropOffset(int[] vertexToPropOffset) {
        this.vertexToPropOffset = vertexToPropOffset;
    }

    public HyperedgeEncoding getEncoding() {
        return encoding;
    }

    public void setEncoding(HyperedgeEncoding encoding) {
        this.encoding = encoding;
    }

    public void printEncoding() {
        encoding.printEncoding();
    }





}
