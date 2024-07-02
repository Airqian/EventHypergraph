package com.eventhypergraph.indextree.hyperedge;


import com.eventhypergraph.encoding.PPBitset;
import com.eventhypergraph.encoding.util.Triple;
import com.eventhypergraph.indextree.HyperedgeEncoding;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件超边类：保存相应的编码以及时间
 */
public class DataHyperedge extends Hyperedge implements Comparable<DataHyperedge>{
    /**
     * 事件发生的时间（ms）
     */
    private long eventTime;

    private List<Long> vertexIds;

    // HACK: 用来测试排序功能的，没有实际意义
    public DataHyperedge(long eventTime) {
        this.eventTime = eventTime;
        vertexIds = new ArrayList<>();
        setEncoding(new HyperedgeEncoding(100, 10));
    }

    // HACK: 用来测试排序功能的，没有实际意义
    public DataHyperedge(long eventTime, PPBitset bitset) {
        this.eventTime = eventTime;
        vertexIds = new ArrayList<>();
        setEncoding(new HyperedgeEncoding(100, 10));
        getEncoding().addEncoding(bitset);
    }


    public DataHyperedge(long id, int eventTypeId, long eventTime) {
        super(id, eventTypeId);
        this.eventTime = eventTime;
        vertexIds = new ArrayList<>();

    }

    public DataHyperedge(long id, int eventTypeId, long eventTime, int numOfVertex, int numOfProperty,
                         List<Integer> vertexToPropOffset) {
        super(id, eventTypeId, numOfVertex, numOfProperty, vertexToPropOffset);

        this.eventTime = eventTime;
        vertexIds = new ArrayList<>();
    }

    /**
     * TODO：属性到顶点列表的初始化；
     * TODO：根据顶点类型String查找对应的属性编码在查询的时候肯定要用到，那这里在哪里实现呢
     */
//
    /**
     * 两条超边的比较规则决定超边在时间窗口（叶子节点）中的排列顺序
     * - 若该时间窗口中超边的主体属性都相同，超边应按时间顺序排列
     * - 若该时间窗口中有不同的主体属性，则超边应先按主体属性分组，分组内部依然按时间顺序排列
     * @param o the object to be compared.
     */
    @Override
    public int compareTo(DataHyperedge o) {
        if (!this.getEncoding().getProperty(0).equals(o.getEncoding().getProperty(0)))
            return this.getEncoding().getProperty(0).compareTo(o.getEncoding().getProperty(0));
        else
            return Long.compare(this.eventTime, o.eventTime);
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public List<Long> getVertexIds() {
        return vertexIds;
    }

    public void setVertexIds(List<Long> vertexIds) {
        this.vertexIds = vertexIds;
    }
}
