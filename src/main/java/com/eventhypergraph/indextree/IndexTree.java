package com.eventhypergraph.indextree;

import com.eventhypergraph.encoding.util.DataAnalyzer;
import com.eventhypergraph.indextree.hyperedge.DataHyperedge;
import com.eventhypergraph.indextree.hyperedge.Hyperedge;
import com.eventhypergraph.indextree.treeNode.InternalTreeNode;
import com.eventhypergraph.indextree.treeNode.LeafTreeNode;
import com.eventhypergraph.indextree.treeNode.TreeNode;
import com.eventhypergraph.indextree.util.DataSetInfo;
import com.sun.istack.internal.NotNull;

import java.util.*;

public class IndexTree {
    private TreeNode root;

    private int layer;

    public static void main(String[] args) {
        /**
         * 读取数据集获得元数据信息和所有事件记录
         * events 所有事件记录行
         * subjectMin 所有主体中最少的事件数量，用来决策叶节点的大小
         * subjectMax 所有主体中最多的事件数量，用来决策叶节点的大小
         * globalMinTime 所有事件记录中最早的事件时间
         * globalMaxTime 所有事件记录中最晚的事件时间
         * monthCount 最早时间和最晚时间之间相差的月份数
         */

        String filePath = "/Users/wqian/Coding/Java/EventHypergraph/src/main/java/com/eventhypergraph/dataset/shopping/shoppingEvent1.txt";
        DataAnalyzer dataAnalyzer = new DataAnalyzer();

        DataSetInfo dataSetInfo = dataAnalyzer.readFile(filePath);
        HashMap<Long, List<String>> events = dataSetInfo.getEvents();
        int eventCounts = dataSetInfo.getEventsNum();
        long globalMinTime = dataSetInfo.getGlobalMinTime();
        long globalMaxTime = dataSetInfo.getGlobalMaxTime();
        int monthDiff = dataSetInfo.getMonthDiff();

        int monthAverage = (int) Math.floor(eventCounts * 1.0 / monthDiff);
    }


    public static void buildTree(String filePath) {
        // 根据指定文件读取获得事件记录和元数据


        // 对数据进行分析之后，主体的个数和其发生的事件数相比总记录数来说实在是太少了，因此目前选择的策略直接根据总记录数和时间跨度来


//        try(BufferedInputStream )
    }


    // 从根节点开始找到符合条件的叶子节点
    private List<TreeNode> findLeafTreeNode(@NotNull Hyperedge hyperedge) {
        Queue<TreeNode> queue1 = new ArrayDeque<>();
        Queue<TreeNode> queue2 = new ArrayDeque<>();

        queue1.offer(root);
        while (!queue1.isEmpty()) {
            if (queue1.peek() instanceof LeafTreeNode)
                break;

            InternalTreeNode curNode = (InternalTreeNode) queue1.poll();
            for (Hyperedge edge : curNode.getDerivedHyperedges()) {
                if (hyperedge.isBitwiseSubset(edge))
                    queue2.offer(curNode.getNodeByEdgeID(hyperedge.getId()));
            }

            if (queue1.isEmpty()) {
                queue1 = queue2;
                queue2 = new ArrayDeque<>();
            }
        }

        List<TreeNode> nodes = new ArrayList<>(queue1);
        return nodes;
    }

    // 向索引树中插入元素
    public void insert(@NotNull DataHyperedge dataHyperedge) {
        // 从根节点开始向下一个一个寻找合适的叶节点
        List<TreeNode> nodes = findLeafTreeNode(dataHyperedge);
        LeafTreeNode leafTreeNode = (LeafTreeNode) nodes.get(0);

        // 调用叶节点的插入方法
        boolean success = leafTreeNode.addHyperedge(dataHyperedge);

        // 判断插入结果，插入失败说明要进行节点分裂
        if (!success)
            splitNode(dataHyperedge, leafTreeNode);
    }

    /**
     * @param dataHyperedge  新插入的数据超边
     * @param curleafNode    需要进行分裂的节点
     */
    private void splitNode(@NotNull DataHyperedge dataHyperedge, @NotNull LeafTreeNode curleafNode) {
        // 1. 创建新的叶节点以及叶节点在父节点中的DerivedHyperedge
        InternalTreeNode parentNode = curleafNode.getParentNode();
        LeafTreeNode newLeafNode = new LeafTreeNode(curleafNode.getStartTime(), curleafNode.getEndTime(), // 时间容量和curleafNode一样
                curleafNode.getCapacity());
        Hyperedge newParentEdge = new Hyperedge(dataHyperedge.getEventTypeId(), dataHyperedge.getNumOfVertex(),
                dataHyperedge.getNumOfProperty(), dataHyperedge.getVertexToPropOffset());

        // 2. 获得两条seed edge（另原节点中的seed与新插入超边进行比较）以及要进行分配的所有超边
        curleafNode.updateSeedAndCardinality(dataHyperedge);
        DataHyperedge seed1 = (DataHyperedge) curleafNode.getSeedHyperedges().get(0).getFirst();
        DataHyperedge seed2 = (DataHyperedge) curleafNode.getSeedHyperedges().get(1).getFirst();

        List<DataHyperedge> edges = curleafNode.getHyperedges();
        edges.add(dataHyperedge);
        edges.remove(seed1);
        edges.remove(seed2);

        // 3. 清空curleafNode的seed、cardinality、globalbits等相关信息
        curleafNode.clear();
        curleafNode.getParentEdge().clear();

        // 4. 新叶节点和旧叶节点分别分配一条 seed Hyperedge，接着计算权重增量开始分配超边（不要重复插入）
        curleafNode.addHyperedge(seed1);
        newLeafNode.addHyperedge(seed2);
        extracted(curleafNode, newLeafNode, seed1, seed2, edges);

        // TODO：此处逻辑再看看，理论上只要父节点往上更新就可以
        // 5. 根据新插入的超边更新两个子节点的父超边
        parentNode.updateParentEdgeByEdge(dataHyperedge);
//        newLeafNode.updateParentEdgeByEdge(dataHyperedge);

        // 6. 更新父节点的globalbits，并将新添加的属性信息在树中向上传播（继续用dataHyperedge往上更新即可，并且暂时不考虑父节点的seed hyperedge变化）
        if (parentNode != null) {
            parentNode.updateGlobalBitsLocal(dataHyperedge);
            parentNode = parentNode.getParentNode();
        }

        // 在父节点中添加新的叶节点并建立映射
        newLeafNode.setParentNode(parentNode);
        newLeafNode.setParentEdge(newParentEdge);
        parentNode.addChildNode(newParentEdge, newLeafNode);
    }

    private void extracted(LeafTreeNode curleafNode, LeafTreeNode newLeafNode, DataHyperedge seed1, DataHyperedge seed2, List<DataHyperedge> edges) {
        int threshold = curleafNode.getCapacity() / 2;
        for (int i = 0; i < edges.size(); i++) {
            if (curleafNode.size() < threshold && newLeafNode.size() < threshold) {
                // cur 为待插入超边
                DataHyperedge cur = edges.get(i);
                double res1 = cur.getWeightIncrease(seed1);
                double res2 = cur.getWeightIncrease(seed2);

                // 添加到权重增量较小的窗口中
                if (Double.compare(res1, res2) <= 0)
                    curleafNode.addHyperedge(cur);
                else
                    newLeafNode.addHyperedge(cur);

                edges.remove(i);
            } else
                break;
        }

        if (edges.size() > 0) {
            if (curleafNode.size() >= threshold)
                newLeafNode.addHyperedge(edges);
            else
                curleafNode.addHyperedge(edges);
        }
    }

    // 在索引树中查询符合条件的数据超边
    public List<Hyperedge> search(@NotNull Hyperedge hyperedge) {
        List<TreeNode> nodes = findLeafTreeNode(hyperedge);
        List<Hyperedge> edges = new ArrayList<>();

        for (TreeNode node : nodes) {
            node = (LeafTreeNode) node;
            for (Hyperedge edge : ((LeafTreeNode) node).getHyperedges()) {
                if (hyperedge.isBitwiseSubset(edge))
                    edges.add(edge);
            }
        }

        return edges;
    }
}

