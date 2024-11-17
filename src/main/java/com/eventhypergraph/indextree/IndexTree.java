package com.eventhypergraph.indextree;

import com.eventhypergraph.encoding.PPBitset;
import com.eventhypergraph.encoding.PropertyEncodingConstructor;
import com.eventhypergraph.indextree.hyperedge.DataHyperedge;
import com.eventhypergraph.indextree.hyperedge.Hyperedge;
import com.eventhypergraph.indextree.treeNode.InternalTreeNode;
import com.eventhypergraph.indextree.treeNode.LeafTreeNode;
import com.eventhypergraph.indextree.treeNode.TreeNode;
import com.eventhypergraph.indextree.util.DataSetInfo;
import com.eventhypergraph.indextree.util.Event;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.eventhypergraph.dataSetHandler.FilePathConstants.SHOPPIONG_TREE_IOFO;
import static com.eventhypergraph.indextree.util.GlobalConstants.HE_ID_INDEX;

public class IndexTree {
    private TreeNode root;

    private int windowSize;

    // 元数据：该索引树超边中包含的最大属性数
//    private int bitsetNum;

    private int encodingLength;

    private int hashFuncCount;

    // 叶子节点的子节点的最大个数
    private int NInterbalNodeChilds;

    private int layer;

    public IndexTree(int windowSize, int encodingLength, int hashFuncCount, int NInterbalNodeChilds) {
        this.windowSize = windowSize;
        this.encodingLength = encodingLength;
        this.hashFuncCount = hashFuncCount;
        this.NInterbalNodeChilds = NInterbalNodeChilds;
    }

    // TODO: 构建倒排索引表，点到边的映射和边到点的映射
    /**
     * 根据时序超图构建索引树
     * @param idToTime 顶点到属性的映射
     * @param idMap 将超边按照时间升序排序
     * @param labelMap 超边id到整条超边的映射（包含顶点id以及属性）
     * @param proMap 超边id到整条超边的映射（包含顶点label以及属性）
     */
    public void buildTemporalHypergraphTree(List<long[]> idToTime, Map<String, String> idMap, Map<String, String> labelMap, Map<String, List<String>> proMap) {
        Queue<TreeNode> treeNodes = new ArrayDeque<>();
        LeafTreeNode leafTreeNode = new LeafTreeNode(windowSize, encodingLength);
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;

        int size = 0;
        for (int i = 0; i < idMap.size(); i++) {
            String edgeId = String.valueOf(idToTime.get(i)[0]);
            long edgeTime = idToTime.get(i)[1];

        }
    }

    /**
     * 根据购物数据集构建索引树
     */
    public void buildShoppingTree(DataSetInfo dataSetInfo) {
        // 根据读取到的数据集统计信息构建索引树
        // 观察到，如果为了保证一个叶子节点内只有一个主体，那么可能会导致树的叶子层非常稀疏
        // 因此，树的构建原则是时间优先，在将编码插入叶子节点时会按照主体属性和时间顺序进行相对排序，保证主体属性相同的编码放置在一块
        Map<String, Map<Long, List<Event>>> eventMap = dataSetInfo.getOrganizer().getEventMap();
        Queue<TreeNode> treeNodes = new ArrayDeque<>();
        LeafTreeNode leafTreeNode = new LeafTreeNode(windowSize, encodingLength);
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 首先构建叶子结点
        int size = 0;
        for (Map.Entry<String, Map<Long, List<Event>>> periodEntry : eventMap.entrySet()) {
            for (Map.Entry<Long, List<Event>> userEntry : periodEntry.getValue().entrySet()) {
                for (Event event : userEntry.getValue()) {
                    // 对事件进行编码（编码信息保存在树上不是在树节点上）
                    String eventDetail = event.getEventDetail();
                    String[] items = eventDetail.split("\\t");
                    System.out.println(eventDetail);

                    try {
                        Date date  = format.parse(items[items.length - 1]);
                        long time = date.getTime();

                        DataHyperedge hyperedge = new DataHyperedge(Long.valueOf(items[HE_ID_INDEX]), time, items.length - 2, encodingLength);
                        minTime = Math.min(minTime, time);  // 更新窗口时间
                        maxTime = Math.max(maxTime, time);
                        leafTreeNode.setStartTime(minTime);
                        leafTreeNode.setEndTime(maxTime);

                        PPBitset totalBitSet = new PPBitset(encodingLength);
                        for (int i = 1, j = 0; i < items.length - 1; i++, j++) {
                            PPBitset temp = PropertyEncodingConstructor.encoding(items[i], encodingLength, hashFuncCount);
                            totalBitSet = totalBitSet.or(temp);
                        }
                        hyperedge.setEncoding(totalBitSet);
                        leafTreeNode.addHyperedge(hyperedge);
                        size++;

                        // 当窗口达到了最大容量则新建下一个窗口，这个容量可以使用类似负载因子的优化方法，避免下一次插入直接造成节点分裂
                        if (size == windowSize) {
                            treeNodes.offer(leafTreeNode);
                            leafTreeNode = new LeafTreeNode(windowSize, encodingLength);
                            minTime = Long.MAX_VALUE;
                            maxTime = Long.MIN_VALUE;

                            size = 0;
                        }
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        // 从叶节点层自底向上构建索引树
        Queue<TreeNode> treeNodes2 = new ArrayDeque<>();
        while (!treeNodes.isEmpty()) {
            if (treeNodes.size() == 1) {
                TreeNode node = treeNodes.poll();
                if (node instanceof InternalTreeNode) {
                    this.root = node;
                    break;
                }
            }

            InternalTreeNode parentNode = new InternalTreeNode(windowSize,  encodingLength);
            // TODO 取出对应数量的子节点构建父节点(后续需要算法优化，子节点的数量有范围)
            for (int i = 1; i <= NInterbalNodeChilds; i++) {
                TreeNode childNode = treeNodes.poll();
                Hyperedge parentEdge = childNode.getTopHyperedge().clone();
                parentNode.addChildNode(parentEdge, childNode);

                childNode.setParentNode(parentNode);
                childNode.setParentEdge(parentEdge);
            }
            treeNodes2.offer(parentNode);

            if (treeNodes.isEmpty()) {
                treeNodes = treeNodes2;
                treeNodes2 = new ArrayDeque<>();
            }
        }

        File file = new File(SHOPPIONG_TREE_IOFO);
        if (file.exists()) file.delete();
        printTree();
    }

    // 从根节点开始找到符合条件的叶子节点
    public List<Long> singleSearch(Hyperedge hyperedge) {
        DataHyperedge dataHyperedge = (DataHyperedge) hyperedge;
        Queue<TreeNode> queue1 = new ArrayDeque<>();
        Queue<TreeNode> queue2 = new ArrayDeque<>();

        queue1.offer(root);
        while (!queue1.isEmpty()) {
            if (queue1.peek() instanceof LeafTreeNode)
                break;

            InternalTreeNode curNode = (InternalTreeNode) queue1.poll();
            // TODO 采用遍历的方式进行匹配，应该可以优化成二分的方式
            for (Hyperedge edge : curNode.getDerivedHyperedges()) {
                TreeNode node = curNode.getNodeByEdgeID(edge.getId());
                if (dataHyperedge.isBitwiseSubset(edge) && node.getStartTime() <= dataHyperedge.getEventTime() && node.getEndTime() >= dataHyperedge.getEventTime())
                    queue2.offer(node);
            }

            if (queue1.isEmpty()) {
                queue1 = queue2;
                queue2 = new ArrayDeque<>();
            }
        }

        List<Long> res = new LinkedList<>();
        if (!queue1.isEmpty()) {
            LeafTreeNode leafTreeNode = (LeafTreeNode) queue1.poll();
            for (DataHyperedge edge : leafTreeNode.getHyperedges()) {
                if (dataHyperedge.isBitwiseSubset(edge) && dataHyperedge.getEventTime() == edge.getEventTime())
                    res.add(edge.getId());
            }
        }

        List<TreeNode> nodes = new ArrayList<>(queue1);
        return res;
    }

    // 向索引树中插入元素
//    public void insert(@NotNull DataHyperedge dataHyperedge) {
//        // 从根节点开始向下一个一个寻找合适的叶节点
//        List<TreeNode> nodes = singleSearch(dataHyperedge);
//        LeafTreeNode leafTreeNode = (LeafTreeNode) nodes.get(0);
//
//        // 调用叶节点的插入方法
//        boolean success = leafTreeNode.addHyperedge(dataHyperedge);
//
//        // 判断插入结果，插入失败说明要进行节点分裂
//        if (!success)
//            splitNode(dataHyperedge, leafTreeNode);
//    }

    /**
     * @param dataHyperedge  新插入的数据超边
     * @param curleafNode    需要进行分裂的节点
     */
    private void splitNode(DataHyperedge dataHyperedge, LeafTreeNode curleafNode) {
//        // 1. 创建新的叶节点以及叶节点在父节点中的DerivedHyperedge
//        InternalTreeNode parentNode = curleafNode.getParentNode();
//        LeafTreeNode newLeafNode = new LeafTreeNode(curleafNode.getStartTime(), curleafNode.getEndTime(), // 时间容量和curleafNode一样
//                curleafNode.getCapacity(), bitsetNum, vertexToPropOffset, propEncodingLength);
//        Hyperedge newParentEdge = new Hyperedge(dataHyperedge.getEventTypeId(), dataHyperedge.getNumOfVertex(),
//                dataHyperedge.getMaxPropertyNum(), dataHyperedge.getVertexToPropOffset());
//
//        // 2. 获得两条seed edge（另原节点中的seed与新插入超边进行比较）以及要进行分配的所有超边
//        curleafNode.updateSeedAndCardinality(dataHyperedge);
//        DataHyperedge seed1 = (DataHyperedge) curleafNode.getSeedHyperedges().get(0).getFirst();
//        DataHyperedge seed2 = (DataHyperedge) curleafNode.getSeedHyperedges().get(1).getFirst();
//
//        List<DataHyperedge> edges = curleafNode.getHyperedges();
//        edges.add(dataHyperedge);
//        edges.remove(seed1);
//        edges.remove(seed2);
//
//        // 3. 清空curleafNode的seed、cardinality、globalbits等相关信息
//        curleafNode.clear();
//        curleafNode.getParentEdge().clear();
//
//        // 4. 新叶节点和旧叶节点分别分配一条 seed Hyperedge，接着计算权重增量开始分配超边（不要重复插入）
//        curleafNode.addHyperedge(seed1);
//        newLeafNode.addHyperedge(seed2);
//        extracted(curleafNode, newLeafNode, seed1, seed2, edges);
//
//        // TODO：此处逻辑再看看，理论上只要父节点往上更新就可以
//        // 5. 根据新插入的超边更新两个子节点的父超边
//        parentNode.updateParent(dataHyperedge);
//        // newLeafNode.updateParentEdgeByEdge(dataHyperedge);
//
//        // 6. 更新父节点的globalbits，并将新添加的属性信息在树中向上传播（继续用dataHyperedge往上更新即可，并且暂时不考虑父节点的seed hyperedge变化）
//        if (parentNode != null) {
//            parentNode.updateTopHyperedge(dataHyperedge);
//            parentNode = parentNode.getParentNode();
//        }
//
//        // 在父节点中添加新的叶节点并建立映射
//        newLeafNode.setParentNode(parentNode);
//        newLeafNode.setParentEdge(newParentEdge);
//        parentNode.addChildNode(newParentEdge, newLeafNode);
    }

//    private void extracted(LeafTreeNode curleafNode, LeafTreeNode newLeafNode, DataHyperedge seed1, DataHyperedge seed2, List<DataHyperedge> edges) {
//        int threshold = curleafNode.getCapacity() / 2;
//        for (int i = 0; i < edges.size(); i++) {
//            if (curleafNode.size() < threshold && newLeafNode.size() < threshold) {
//                // cur 为待插入超边
//                DataHyperedge cur = edges.get(i);
//                double res1 = cur.getWeightIncrease(seed1);
//                double res2 = cur.getWeightIncrease(seed2);
//
//                // 添加到权重增量较小的窗口中
//                if (Double.compare(res1, res2) <= 0)
//                    curleafNode.addHyperedge(cur);
//                else
//                    newLeafNode.addHyperedge(cur);
//
//                edges.remove(i);
//            } else
//                break;
//        }
//
//        if (edges.size() > 0) {
//            if (curleafNode.size() >= threshold)
//                newLeafNode.addHyperedge(edges);
//            else
//                curleafNode.addHyperedge(edges);
//        }
//    }

    // 从根节点开始打印树
    public void printTree() {
        Queue<TreeNode> treeNodes = new ArrayDeque<>();
        treeNodes.offer(this.root);

        while (!treeNodes.isEmpty()) {
            TreeNode node = treeNodes.poll();
            node.print();

            if (node instanceof InternalTreeNode) {
                for (TreeNode child : ((InternalTreeNode) node).getChildNodes())
                    treeNodes.offer(child);
            }
        }
    }

    public TreeNode getRoot() {
        return this.root;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public int getEncodingLength() {
        return encodingLength;
    }

    public int getLayer() {
        return layer;
    }

    public int getHashFuncCount() {
        return hashFuncCount;
    }


}

