package com.eventhypergraph.indextree.hyperedge;

import cn.hutool.core.util.IdUtil;
import com.eventhypergraph.encoding.PPBitset;
import com.eventhypergraph.encoding.PropertyEncodingConstructor;
import com.eventhypergraph.encoding.util.Triple;
import com.eventhypergraph.indextree.util.IDGenerator;


import java.util.ArrayList;
import java.util.List;

public class Hyperedge {
    // 数据超边的id从数据集中获取，派生超边的id需要生成
    private long id;

    // 事件类型id
    private int eventTypeId;

    // 该事件超边包含的顶点数
    private int numOfVertex;

    private int bitsetNum;

    private HyperedgeEncoding encoding;

    // 此构造方法用于Hyperedge#clone()方法以及节点的topHyperedge的initial方法
    public Hyperedge(int bitsetNum) {
        this.id = IdUtil.getSnowflakeNextId();
        this.numOfVertex = Integer.MAX_VALUE;
        this.bitsetNum = bitsetNum;
        this.encoding = new HyperedgeEncoding(id, bitsetNum);
    }

    // 构造查询超边时会用到
    public Hyperedge(int numOfVertex, int bitsetNum) {
        this.id = IdUtil.getSnowflakeNextId();
        this.numOfVertex = numOfVertex;
        this.bitsetNum = bitsetNum;
        this.encoding = new HyperedgeEncoding(id, bitsetNum);
    }

    // 读取数据集构建超边时会用到
    public Hyperedge(long id, int numOfVertex, int bitsetNum) {
        this.id = id;
        this.numOfVertex = numOfVertex;
        this.bitsetNum = bitsetNum;
        this.encoding = new HyperedgeEncoding(id, bitsetNum);
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
        Hyperedge hyperedge = new Hyperedge(bitsetNum);
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
        if (this.bitsetNum != hyperedge.bitsetNum)
            throw new IllegalArgumentException(String.format("The number of properties contained in the two hyperedges is not equal." +
                    "hyperedge1: id = %d, numOfProperty = %d, hyperedge2: id = %d, numOfProperty = %d", id, bitsetNum, hyperedge.getId(), hyperedge.getBitsetNum()));

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
        if (this.bitsetNum > hyperedge.bitsetNum)
            return false;

        // 对每个位置的属性逐一进行判断
        for (int i = 0; i < bitsetNum; i++) {
            isBitwiseSubset(i, hyperedge);
        }

        return true;
    }

    public boolean isBitwiseSubset(int index, Hyperedge Hyperedge) {
        if (this.bitsetNum > Hyperedge.bitsetNum)
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

    public void addEncoding(PPBitset bitset) {
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

    public int getBitsetNum() {
        return bitsetNum;
    }

    public void setBitsetNum(int bitsetNum) {
        this.bitsetNum = bitsetNum;
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
