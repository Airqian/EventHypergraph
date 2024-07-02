package com.eventhypergraph.indextree.hyperedge;

import com.eventhypergraph.indextree.HyperedgeEncoding;

import java.util.List;

/**
 * 中间节点的超边不存在时间
 * 可以生成id（是否利用id和子节点进行绑定）
 */
public class DerivedHyperedge extends Hyperedge{
    private long id;

    private int eventTypeId;

    // 由子节点窗口中所有超边OR操作得到
    private HyperedgeEncoding encoding;

    private List<Integer> vertexToPropOffset;

    public DerivedHyperedge(long id, int eventTypeId, int numOfVertex, int numOfProperty, List<Integer> vertexToPropOffset) {
        super(id, eventTypeId, numOfVertex, numOfProperty, vertexToPropOffset);
    }

}
