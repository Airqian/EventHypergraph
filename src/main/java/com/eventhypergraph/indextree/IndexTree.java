package com.eventhypergraph.indextree;

import com.eventhypergraph.encoding.PPBitset;
import com.eventhypergraph.encoding.PropertyEncodingConstructor;
import com.eventhypergraph.encoding.util.PeriodType;
import com.eventhypergraph.indextree.util.DataAnalyzer;
import com.eventhypergraph.indextree.hyperedge.DataHyperedge;
import com.eventhypergraph.indextree.hyperedge.Hyperedge;
import com.eventhypergraph.indextree.treeNode.InternalTreeNode;
import com.eventhypergraph.indextree.treeNode.LeafTreeNode;
import com.eventhypergraph.indextree.treeNode.TreeNode;
import com.eventhypergraph.indextree.util.DataSetInfo;
import com.eventhypergraph.indextree.util.Event;
import com.sun.istack.internal.NotNull;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.eventhypergraph.dataset.FilePathConstants.QUERY_FILE_PATH;
import static com.eventhypergraph.dataset.FilePathConstants.SHOPPIONG_EVENT_EXPERIMENT_FILE_PATH;
public class IndexTree {
    private TreeNode root;

    private int windowSize;

    // 元数据：超边中包含的顶点数量
    int numOfVertex;

    // 元数据：该索引树超边中包含的最大属性数
    private int maxPropertyNum;

    /**
     * 元数据：顶点到属性映射偏移量，属性总数为右边界-1
     * 假设用户实体的定义包括属性 userName、phoneNumber 和 IDCard，而 AOI 实体的定义包括属性 AOIName 和 city。
     * vertexToPropOffset 数字列表为 [4, 6]。总共有 6 个属性，其中用户的属性集合为 [1, 4)，AOI 的属性集合为 [4, 6)。
     */
    private int[] vertexToPropOffset;

    // 元数据：属性编码的长度
    private int[] propEncodingLength;

    private int layer;

    public IndexTree(int windowSize, int numOfVertex, int maxPropertyNum, int[] propEncodingLength, int[] vertexToPropOffset) {
        if (maxPropertyNum != propEncodingLength.length)
            throw new IllegalArgumentException("属性个数与编码长度列表不匹配");

        this.windowSize = windowSize;
        this.propEncodingLength = propEncodingLength;
        this.numOfVertex = numOfVertex;
        this.maxPropertyNum = maxPropertyNum;
        this.vertexToPropOffset = vertexToPropOffset;
    }

    public static void main(String[] args) {
        int windowSize = 10;
        int numOfVertex = 4;
        int maxPropertyNum = 4;
        int[] propEncodingLength = new int[] {15, 15, 15, 15};
        int[] dataSetPropIndex = new int[]{1,2,3,4};
        int[] vertexToPropOffset = new int[]{1, 2, 3, 4};
        int hashFuncCount = 3;

        IndexTree indexTree = new IndexTree(windowSize, numOfVertex, maxPropertyNum, propEncodingLength, vertexToPropOffset);

        int idIndex = 0;
        int userIndex = 1;
        int timeIndex = 5;
        indexTree.buildTree(SHOPPIONG_EVENT_EXPERIMENT_FILE_PATH, idIndex, userIndex, timeIndex, PeriodType.MONTH, dataSetPropIndex, hashFuncCount);

        // 给超边对应上主体属性的id
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new FileReader(new File(QUERY_FILE_PATH)));
            String line;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            while ((line = bufferedReader.readLine()) != null) {
                String[] items = line.split("\\t");
                Date date  = format.parse(items[timeIndex]);
                long time = date.getTime();
                DataHyperedge hyperedge = new DataHyperedge(time, numOfVertex, maxPropertyNum, propEncodingLength);
                for (int i = 0, j = 0; i < dataSetPropIndex.length; i++, j++) {
                    PPBitset ppBitset = PropertyEncodingConstructor.encoding(items[dataSetPropIndex[i]], propEncodingLength[j], hashFuncCount);
                    hyperedge.addEncoding(ppBitset);
                }

                List<TreeNode> leafTreeNode = indexTree.findLeafTreeNode(hyperedge);
                System.out.println(leafTreeNode.size());
                for (TreeNode node : leafTreeNode)
                    node.print();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 给定数据集构建索引树
     * @param filePath 文件路径
     * @param userIndex 主体属性下标
     * @param timeIndex 时间属性下标
     * @param periodType 时间跨度
     * @param dataSetPropIndex 属性在数据集中的下标，默认主体属性在第一位，构建索引时按照该下标顺序拼接编码
     * @param hashFuncCount 所使用的哈希函数个数
     */
    public void buildTree(String filePath, int idIndex, int userIndex, int timeIndex, PeriodType periodType, int[] dataSetPropIndex, int hashFuncCount) {
        // 根据指定文件读取获得事件记录和元数据
        DataSetInfo dataSetInfo = DataAnalyzer.readFile(filePath, userIndex, timeIndex, periodType);
        dataSetInfo.getOrganizer().printAllEvents();

        // 根据读取到的数据集统计信息构建索引树
        // 观察到，如果为了保证一个叶子节点内只有一个主体，那么可能会导致树的叶子层非常稀疏
        // 因此，树的构建原则是时间优先，在将编码插入叶子节点时会按照主体属性和时间顺序进行相对排序，保证主体属性相同的编码放置在一块
        Map<String, Map<Long, List<Event>>> eventMap = dataSetInfo.getOrganizer().getEventMap();
        Queue<TreeNode> treeNodes = new ArrayDeque<>();
        LeafTreeNode leafTreeNode = new LeafTreeNode(windowSize, numOfVertex, maxPropertyNum,
                vertexToPropOffset, propEncodingLength);
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;

        int size = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 构建叶子节点
        for (Map.Entry<String, Map<Long, List<Event>>> periodEntry : eventMap.entrySet()) {
            for (Map.Entry<Long, List<Event>> userEntry : periodEntry.getValue().entrySet()) {
                for (Event event : userEntry.getValue()) {
                    size++;
                    // 对事件进行编码（编码信息保存在树上不是在树节点上）
                    String eventDetail = event.getEventDetail();
//                    System.out.println(eventDetail);
                    String[] items = eventDetail.split("\\t");
                    try {
                        Date date  = format.parse(items[timeIndex]);
                        long time = date.getTime();
                        DataHyperedge hyperedge = new DataHyperedge(Long.valueOf(items[idIndex]), time, numOfVertex, maxPropertyNum, propEncodingLength);
                        minTime = Math.min(minTime, time);  // 更新窗口时间
                        maxTime = Math.max(maxTime, time);
                        leafTreeNode.setStartTime(minTime);
                        leafTreeNode.setEndTime(maxTime);

                        for (int i = 0, j = 0; i < dataSetPropIndex.length; i++, j++) {
                            PPBitset ppBitset = PropertyEncodingConstructor.encoding(items[dataSetPropIndex[i]], propEncodingLength[j], hashFuncCount);
                            hyperedge.addEncoding(ppBitset);
                        }
                        leafTreeNode.addHyperedge(hyperedge);

                        // 当窗口达到了最大容量则新建下一个窗口，这个容量可以使用类似负载因子的优化方法，避免下一次插入直接造成节点分裂
                        if (size == windowSize) {
                            treeNodes.offer(leafTreeNode);
                            leafTreeNode = new LeafTreeNode(windowSize, numOfVertex, maxPropertyNum, vertexToPropOffset, propEncodingLength);
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
        int k = 2; // 每层子节点的最大个数
        while (!treeNodes.isEmpty()) {
            if (treeNodes.size() == 1) {
                TreeNode node = treeNodes.poll();
                if (node instanceof InternalTreeNode) {
                    this.root = node;
                    break;
                }
            }

            InternalTreeNode parentNode = new InternalTreeNode(windowSize, numOfVertex, maxPropertyNum, vertexToPropOffset, propEncodingLength);
            // TODO 取出对应数量的子节点构建父节点(后续需要算法优化，子节点的数量有范围)
            for (int i = 1; i <= k; i++) {
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

        printTree();
    }

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

    // 从根节点开始找到符合条件的叶子节点
    private List<TreeNode> findLeafTreeNode(@NotNull Hyperedge hyperedge) {
        DataHyperedge dataHyperedge = (DataHyperedge) hyperedge;
        Queue<TreeNode> queue1 = new ArrayDeque<>();
        Queue<TreeNode> queue2 = new ArrayDeque<>();

        queue1.offer(root);
        while (!queue1.isEmpty()) {
            if (queue1.peek() instanceof LeafTreeNode)
                break;

            InternalTreeNode curNode = (InternalTreeNode) queue1.poll();
            for (Hyperedge edge : curNode.getDerivedHyperedges()) {
                TreeNode node = curNode.getNodeByEdgeID(edge.getId());
                if (hyperedge.isBitwiseSubset(edge) && node.getStartTime() <= dataHyperedge.getEventTime() && node.getEndTime() >= dataHyperedge.getEventTime())
                    queue2.offer(node);
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
                curleafNode.getCapacity(), numOfVertex, maxPropertyNum, vertexToPropOffset, propEncodingLength);
        Hyperedge newParentEdge = new Hyperedge(dataHyperedge.getEventTypeId(), dataHyperedge.getNumOfVertex(),
                dataHyperedge.getMaxPropertyNum(), dataHyperedge.getVertexToPropOffset());

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
        parentNode.updateParent(dataHyperedge);
        // newLeafNode.updateParentEdgeByEdge(dataHyperedge);

        // 6. 更新父节点的globalbits，并将新添加的属性信息在树中向上传播（继续用dataHyperedge往上更新即可，并且暂时不考虑父节点的seed hyperedge变化）
        if (parentNode != null) {
            parentNode.updateTopHyperedge(dataHyperedge);
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

    public TreeNode getRoot() {
        return this.root;
    }
}

