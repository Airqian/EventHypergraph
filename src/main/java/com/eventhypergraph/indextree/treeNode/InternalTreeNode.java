package com.eventhypergraph.indextree.treeNode;

import com.eventhypergraph.encoding.util.Pair;
import com.eventhypergraph.indextree.hyperedge.DataHyperedge;
import com.eventhypergraph.indextree.hyperedge.DerivedHyperedge;
import com.eventhypergraph.indextree.treeNode.TreeNode;

import java.util.*;

/**
 * 非叶子节点窗口中的一条超边对应一个子节点，Map中保存超边id到子节点id的映射
 * 非叶子节点在容量变满时也要进行节点分裂，同理也可以保存两条权重值最大的超边
 */
public class InternalTreeNode extends TreeNode {
    private List<DerivedHyperedge> derivedHyperedges;

    private List<TreeNode> childNodes;

    /**
     * 键是超边id，值是子节点窗口id
     */
    private Map<Long, Long> edgeToNode;

    // 保存用于节点分裂使用的两个seed hyperedge
    List<Pair<DataHyperedge, Integer>> seedDataHyperedges;

    private int maxCardinality;

    public InternalTreeNode(int capacity) {
        super(capacity);
    }

    public InternalTreeNode(long startTime, long endTime, int capacity) {
        super(startTime, endTime, capacity);

        derivedHyperedges = new ArrayList<>();
        childNodes = new ArrayList<>();
        edgeToNode = new HashMap<>();
    }

    // 在非叶节点中添加超边和子节点（需同时添加）
    // TODO  中间节点globalbits的更新
    public void addChild(DerivedHyperedge hyperedge, TreeNode node) {
        check();

        if (!isFull()) {
            derivedHyperedges.add(hyperedge);
            childNodes.add(node);
            edgeToNode.put(hyperedge.getId(), node.getId());

            // 更新节点的时间范围
            if (getStartTime() > node.getStartTime())
                setStartTime(node.getStartTime());
            if (getEndTime() < node.getEndTime())
                setEndTime(node.getEndTime());

            // updateCardinality(dataHyperedge);
            // updateGlobalBits(dataHyperedge);
        } else {
            // TODO 节点分裂
        }
    }

    private void check() {
        assert (derivedHyperedges.size() == childNodes.size());
        assert (derivedHyperedges.size() == edgeToNode.size());
    }

    public int size() {
        check();
        return derivedHyperedges.size();
    }

    public boolean isFull() {
        return derivedHyperedges.size() == getCapacity();
    }




}
