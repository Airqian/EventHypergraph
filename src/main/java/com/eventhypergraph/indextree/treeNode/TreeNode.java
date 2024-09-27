package com.eventhypergraph.indextree.treeNode;

import com.eventhypergraph.encoding.util.Pair;
import com.eventhypergraph.indextree.hyperedge.Hyperedge;
import com.eventhypergraph.indextree.util.IDGenerator;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 树节点父类，定义了时间范围、节点容量等公共字段
 *
 * （可以再优化的一点是窗口里的编码可以通过链式进行组织）
 */
public class TreeNode {
    private long id;

    // Time Range
    private long startTime;

    private long endTime;

    private int capacity;

    private boolean isRootFlag;

    /**
     * 用这两个字段记录该窗口的父节点以及在父节点中的父超边
     */
    private InternalTreeNode parentNode;

    private  Hyperedge parentEdge;

    // 保存用于节点分裂使用的两个seed hyperedge
    // TODO：如果是非叶节点分裂，这里的类型就应该是Hyperedge了
    private List<Pair<Hyperedge, Integer>> seedHyperedges;

    // 记录seedHyperedges当中cardinality较小的那一个
    private int minCardinality;

    // 它记录该节点中所有超边属性编码上的位为1的情况
    // TODO 之后可以用来优化索引查询过程
    private List<Set<Integer>> globalbits;

    public TreeNode() {}

    public TreeNode(int capacity) {
        this.capacity = capacity;
    }

    // 叶节点和中间节点的容量可以用全局静态常量去设置
    public TreeNode(long startTime, long endTime,int capacity) {
        if (capacity <= 0)
            throw new IllegalArgumentException(String.format("The capacity cannot be 0. capacity = %d", capacity));

        if (startTime >= endTime)
            throw new IllegalArgumentException(String.format("endTime = %d cannot be less than startTime = %d.", endTime, startTime));

        id = IDGenerator.generateNodeId();
        this.capacity = capacity;
        this.startTime = startTime;
        this.endTime = endTime;
        this.globalbits = new ArrayList<>();
        this.seedHyperedges = new ArrayList<>(2);
        this.minCardinality = 1000;
    }

    // 用hyperedge更新本节点globalbits
    public void updateGlobalBitsLocal(@NotNull Hyperedge hyperedge) {
        for (int i = 0; i < hyperedge.getEncoding().size(); i++) {
            if (globalbits.get(i) == null)
                globalbits.set(i, new HashSet<>());

            String bitStr = hyperedge.getEncoding().getProperty(i).toString();
            if (bitStr.length() > 2) {
                String[] bits = bitStr.substring(1).split(",");
                for (int j = 0; j < bits.length; j++)
                    globalbits.get(i).add(Integer.valueOf(bits[j]));
            }
        }
    }

    // 根据新插入的超边更新对应的父超边(具有传递性)
    public void updateParentEdgeByEdge(@NotNull Hyperedge hyperedge) {
        TreeNode pNode = getParentNode();
        Hyperedge pEdge = getParentEdge();

        while (pNode != null) {
            pEdge.orEncoding(hyperedge);
            pEdge = pNode.getParentEdge();
            pNode = pNode.getParentNode();
        }
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setIsRoot(boolean isRootFlag) {
        this.isRootFlag = isRootFlag;
    }

    public boolean getIsRoot() {
        return isRootFlag;
    }

    public long getId() {
        return id;
    }

    public Hyperedge getParentEdge() {
        return parentEdge;
    }

    public InternalTreeNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(InternalTreeNode parentNode) {
        this.parentNode = parentNode;
    }

    public void setParentEdge(Hyperedge parentEdge) {
        this.parentEdge = parentEdge;
    }

    public List<Set<Integer>> getGlobalbits() {
        return globalbits;
    }

    public List<Pair<Hyperedge, Integer>> getSeedHyperedges() {
        return seedHyperedges;
    }

    public int getMinCardinality() {
        return minCardinality;
    }

    public void setMinCardinality(int minCardinality) {
        this.minCardinality = minCardinality;
    }


}
