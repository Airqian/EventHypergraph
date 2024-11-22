package com.eventhypergraph.indextree.hyperedge;


import com.eventhypergraph.encoding.PPBitset;


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

    // 超边包含的顶点id
    private List<Long> vertexIds;

    // 根据数据集构建查询超边时会用到构造查询超边时会用到
    public DataHyperedge(long eventTime, int encodingLength) {
        super(encodingLength);

        this.eventTime = eventTime;
        vertexIds = new ArrayList<>();
    }

    // 读取数据集构建树时会遇到
    public DataHyperedge(long id, long eventTime, int encodingLength) {
        super(id, encodingLength);

        this.eventTime = eventTime;
        vertexIds = new ArrayList<>();
    }

    public void addVertexId(long vertexId) {
        this.vertexIds.add(vertexId);
    }

    // 计算与给定超边之间的权重增量
    public double getWeightIncrease(DataHyperedge dataHyperedge) {
        double diff = (this.getEncoding().cardinality() - dataHyperedge.cardinality()) * 1.0;
        return diff;
    }

    /**
     * 两条超边的比较规则决定超边在时间窗口（叶子节点）中的排列顺序
     * - 若该时间窗口中超边的主体属性都相同，超边应按时间顺序排列
     * - 若该时间窗口中有不同的主体属性，则超边应先按主体属性分组，分组内部依然按时间顺序排列
     * @param o the object to be compared.
     */
    @Override
    public int compareTo(DataHyperedge o) {
        if (!this.getEncoding().equals(o.getEncoding()))
            return this.getEncoding().compareTo(o.getEncoding());
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
