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

    // 构造查询超边时会用到
    public DataHyperedge(long eventTime, int numOfVertex, int bitsetNum) {
        super(numOfVertex, bitsetNum);

        this.eventTime = eventTime;
        vertexIds = new ArrayList<>();
    }

    // 读取数据集构建树时会遇到
    public DataHyperedge(long id, long eventTime, int numOfVertex, int bitsetNum) {
        super(id, numOfVertex, bitsetNum);

        this.eventTime = eventTime;
        vertexIds = new ArrayList<>();
    }

    // 计算与给定超边之间的权重增量
    public double getWeightIncrease( DataHyperedge dataHyperedge) {
        double res = 0.0;
        int len = dataHyperedge.getEncodingLength();

        for (int i = 0; i < dataHyperedge.getEncoding().size(); i++) {
            PPBitset tmp = this.getEncodingAt(i).or(dataHyperedge.getEncodingAt(i));
            double diff = (tmp.cardinality() - dataHyperedge.getEncodingAt(i).cardinality()) * 1.0;
            res += (tmp.length() * 1.0) / len * diff;
        }
        return res;
    }

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
