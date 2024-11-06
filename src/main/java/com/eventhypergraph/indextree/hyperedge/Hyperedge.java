package com.eventhypergraph.indextree.hyperedge;

import cn.hutool.core.util.IdUtil;
import com.eventhypergraph.encoding.PPBitset;
import com.eventhypergraph.encoding.PropertyEncodingConstructor;
import com.eventhypergraph.encoding.util.Triple;
import com.eventhypergraph.indextree.util.IDGenerator;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Hyperedge {
    // 数据超边的id从数据集中获取，派生超边的id需要生成
    private long id;

    // 事件类型id
    private int eventTypeId;

    // 该事件超边包含的顶点数
    private int numOfVertex;

    // 元数据：该事件超边包含的属性数
    private int maxPropertyNum;

    /**
     * 元数据：顶点到属性映射偏移量，属性总数为右边界-1
     * 假设用户实体的定义包括属性 userName、phoneNumber 和 IDCard，而 AOI 实体的定义包括属性 AOIName 和 city。
     * vertexToPropOffset 数字列表为 [4, 6]。总共有 6 个属性，其中用户的属性集合为 [1, 4)，AOI 的属性集合为 [4, 6)。
     */
    private int[] vertexToPropOffset;

    private HyperedgeEncoding encoding;

    // 此构造方法用于Hyperedge#clone()方法
    public Hyperedge(int numOfVertex, int maxPropertyNum, int[] vertexToPropOffset) {
        this.id = IdUtil.getSnowflakeNextId();
        this.numOfVertex = numOfVertex;
        this.maxPropertyNum = maxPropertyNum;
        this.vertexToPropOffset = vertexToPropOffset;
        this.encoding = new HyperedgeEncoding(id, maxPropertyNum);
    }

    public Hyperedge(long id, int numOfVertex, int maxPropertyNum, int[] vertexToPropOffset) {
        this.id = id;
        this.numOfVertex = numOfVertex;
        this.maxPropertyNum = maxPropertyNum;
        this.vertexToPropOffset = vertexToPropOffset;
        this.encoding = new HyperedgeEncoding(id, maxPropertyNum);
    }

    public Hyperedge(long id, int eventTypeId, int numOfVertex, int maxPropertyNum, int[] vertexToPropOffset) {
        this(id, numOfVertex, maxPropertyNum, vertexToPropOffset);

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
     * @param hyperedge 新插入的超边
     */
    public void encodingOr(Hyperedge hyperedge) {
        if (this.maxPropertyNum != hyperedge.maxPropertyNum)
            throw new IllegalArgumentException(String.format("The number of properties contained in the two hyperedges is not equal." +
                    "hyperedge1: id = %d, numOfProperty = %d, hyperedge2: id = %d, numOfProperty = %d", id, maxPropertyNum, hyperedge.getId(), hyperedge.getMaxPropertyNum()));

        for (int i = 0; i < hyperedge.getEncoding().size(); i++) {
            List<Integer> bits = hyperedge.getEncoding().getProperty(i).getAllOneBits();
            for (int bit : bits) {
                this.getEncoding().getProperty(i).set(bit, true);
            }
        }
    }

    /**
     * 两条超边进行与操作，在索引树中进行自上而下的超边匹配会将用到，测试 hyperedge & ano_hyperedge == hyperedge
     * @param hyperedge
     * @return
     */
    public boolean isBitwiseSubset(Hyperedge hyperedge) {
        if (this.maxPropertyNum > hyperedge.maxPropertyNum)
            return false;

        // 对每个位置的属性逐一进行判断
        for (int i = 0; i < maxPropertyNum; i++) {
            isBitwiseSubset(i, hyperedge);
        }

        return true;
    }

    public boolean isBitwiseSubset(int index, Hyperedge Hyperedge) {
        if (this.maxPropertyNum > Hyperedge.maxPropertyNum)
            return false;

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

    public String printEncoding() {
        return encoding.printEncoding();
    }
}
