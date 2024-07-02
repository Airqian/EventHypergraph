package com.eventhypergraph.indextree.treeNode;

import com.eventhypergraph.encoding.Exception.TimeOutOfBoundException;
import com.eventhypergraph.encoding.util.Pair;
import com.eventhypergraph.indextree.hyperedge.DataHyperedge;
import com.eventhypergraph.indextree.hyperedge.DerivedHyperedge;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * - 叶节点存储真实的事件数据超边，每条数据超边都有一个指向超边对象的指针，目前表示为Hyperedge的Id
 * - 叶节点有且仅有一个父节点，不存在子节点。在其父节点中存在一个 Map 将汇聚产生的派生超边和该子节点进行映射绑定
 * - 叶节点的操作有：节点插入、节点分裂
 * - 节点插入时，需要将编码的变化同等的传播上上层节点
 * - 节点分裂时，需要在待分配的超边中选出两条权重值最大的超边分别作为两个新分裂节点的seed hyperedge，为了简化seed hyperedge的挑选过程，
 *   直接在叶节点中设置两个字段，在节点插入时进行实时更新（用Pair结构表示一条权重超边，第一个参数为超边对象，第二个参数为其权重），用新的超边替换
 *   权重值较小的那条超边
 */
public class LeafTreeNode extends TreeNode {
    private List<DataHyperedge> dataHyperedges;

    // 保存用于节点分裂使用的两个seed hyperedge
    List<Pair<DataHyperedge, Integer>> seedDataHyperedges;

    private int maxCardinality;

    public LeafTreeNode(long startTime, long endTime, int capacity) {
        super(startTime, endTime, capacity);

        dataHyperedges = new ArrayList<>();
        seedDataHyperedges = new ArrayList<>(2);
        maxCardinality = -1;
    }

    /**
     * 一个叶节点中所有超边的主体属性都相同
     * 每一个叶节点都应该选定一个seed hyperedge，它是满足该时间窗口且 cardinality 最大的超边。
     * 叶节点插入逻辑：首先按超边比较顺序在特定位置进行插入，同时更新seed hyperedge 和 seed cardinality，并将变化从下至上进行传播直到根节点
     * 在构造索引树时，我们应首先获取相同主体事件的最早和最晚时间，根据这两个时间切分时间窗口，以尽量平均每个时间窗口含有的编码数。
     * 整体的逻辑是，先看该时间范围内有多少个不同的主体属性，对这些主体属性进行分组
     * @param dataHyperedge
     */
    public void addHyperedge(@NotNull DataHyperedge dataHyperedge) {
        if (dataHyperedge.getEventTime() < getStartTime() || dataHyperedge.getEventTime() > getEndTime())
            throw new TimeOutOfBoundException("The occurrence time of the event to be inserted is not within the time window.");

        if (!isFull()) {
            int index = binarySort(dataHyperedge);
            dataHyperedges.add(index, dataHyperedge);

            // 判断seedHyperedge是否要更新（将原seedHyperedge与新插入的hyperedge进行比较）
            updateCardinality(dataHyperedge);
            updateGlobalBits(dataHyperedge);
            updateParent();
        } else {
            /**
             * TODO 叶节点分裂
             * 能够分配到此叶节点窗口来说明时间范围是满足的，只需要将该窗口中的超边和新插入的超边按照设计的weight increase公式进行分组即可
             * 当然，前提是要选出两个seed hyperedge，分别作为两个新节点的seed hyperedge
             */
        }
    }

    private void updateCardinality(@NotNull DataHyperedge dataHyperedge) {
        int curCardinality = dataHyperedge.cardinality();

        if (seedDataHyperedges.size() < 2) {
            seedDataHyperedges.add(new Pair(dataHyperedge, curCardinality));
        } else {
            if (curCardinality > maxCardinality) {
                if (seedDataHyperedges.get(0).getSecond() < curCardinality) {
                    seedDataHyperedges.set(0, new Pair(dataHyperedge, curCardinality));
                } else {
                    seedDataHyperedges.set(1, new Pair(dataHyperedge, curCardinality));
                }
            }
        }
        maxCardinality = Math.max(maxCardinality, curCardinality);
    }

    // 用hyperedge更新globalbits
    private void updateGlobalBits(@NotNull DataHyperedge dataHyperedge) {
        for (int i = 0; i < dataHyperedge.getEncoding().size(); i++) {
            if (getGlobalbits().get(i) == null)
                getGlobalbits().set(i, new HashSet<>());

            String bitStr = dataHyperedge.getEncoding().getProperty(i).toString();
            if (bitStr.length() > 2) {
                String[] bits = bitStr.substring(1).split(",");
                for (int j = 0; j < bits.length; j++)
                    getGlobalbits().get(i).add(Integer.valueOf(bits[j]));
            }
        }
    }

    /**
     * TODO 将新插入超边的变化同步到父超边中
     * 用子节点的globalbits更新父节点的hyperedge以及父节点的globalbits，依此递归
     */
    public void updateParent() {
        TreeNode pNode = getParentNode();
        DerivedHyperedge pEdge = getParentEdge();
        List<Set<Integer>> gbits = this.getGlobalbits();

        while (pNode != null) {
            // 用子节点的gbits更新父节点的Hyperedge,再令globalbits与其同步
            for (int i = 0; i < gbits.size(); i++) {
                for (int bit : gbits.get(i)) {
                    pEdge.getEncoding().getProperty(i).set(bit);
                }
            }

            // FIXME:pNode.updateGlobalBits(pEdge);
            pNode = pNode.getParentNode();
            pEdge = pNode.getParentEdge();
            gbits = pNode.getGlobalbits();
        }
    }

    // 二分查找搜索给定hyperedge可插入的位置
    public int binarySort(DataHyperedge edge) {
        int left = 0;
        int right = dataHyperedges.size() - 1;

        while (left <= right) {
            int mid = (left + right) >> 1;
            int res = edge.compareTo(dataHyperedges.get(mid));

            // res < 0 说明bit位1的下标更加靠前，应该排在前面
            if (res < 0) {
                right = mid - 1;
            } else if (res > 0) {
                left = mid + 1;
            } else {
                return mid;
            }
        }

        return left;
    }

    public List<DataHyperedge> getHyperedges() {
        return dataHyperedges;
    }

    public boolean isFull() {
        return getHyperedges().size() == getCapacity();
    }

    public boolean isEmpty() {
        return getHyperedges().size() == 0;
    }


}

