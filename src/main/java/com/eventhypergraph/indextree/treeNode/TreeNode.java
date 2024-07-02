package com.eventhypergraph.indextree.treeNode;

import com.eventhypergraph.indextree.hyperedge.DerivedHyperedge;
import com.eventhypergraph.indextree.util.IDGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 树节点父类，定义了时间范围、节点容量等公共字段
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
    private TreeNode parentNode;

    private DerivedHyperedge parentEdge;

    // 它记录该节点中所有超边属性编码上的位为1的情况
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

    public DerivedHyperedge getParentEdge() {
        return parentEdge;
    }

    public TreeNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(TreeNode parentNode) {
        this.parentNode = parentNode;
    }

    public void setParentEdge(DerivedHyperedge parentEdge) {
        this.parentEdge = parentEdge;
    }

    public List<Set<Integer>> getGlobalbits() {
        return globalbits;
    }
}
