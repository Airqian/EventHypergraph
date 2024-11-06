package com.eventhypergraph.indextree;

import com.eventhypergraph.encoding.PPBitset;
import com.eventhypergraph.encoding.PropertyEncodingConstructor;
import com.eventhypergraph.encoding.util.PeriodType;
import com.eventhypergraph.indextree.hyperedge.DataHyperedge;
import com.eventhypergraph.indextree.treeNode.TreeNode;
import com.eventhypergraph.indextree.util.DataAnalyzer;
import com.eventhypergraph.indextree.util.DataSetInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.eventhypergraph.dataset.FilePathConstants.*;

public class Experiment {
    public static void main(String[] args) {
        IndexTree indexTree = buildShoppingTree(SHOPPIONG_EVENT_EXPERIMENT_FILE_PATH, PeriodType.MONTH);
        query(indexTree, SHOPPING_QUERY_FILE_PATH);
    }

    public static IndexTree buildShoppingTree(String dataFile, PeriodType type) {
        // 读取数据集，数据集格式固定第一项是超边ID，第二项是主体属性，最后一项是超边时间
        DataSetInfo dataSetInfo = DataAnalyzer.readFile(dataFile, type);
        // dataSetInfo.getOrganizer().printAllEvents();

        // 构建索引树
        int windowSize = 10;
        int numOfVertex = 4;
        int maxPropertyNum = dataSetInfo.getMaxPropertyNum(); // 单条超边的最大属性个数
        int[] propEncodingLength = new int[maxPropertyNum];   // 每个属性的编码长度
        Arrays.fill(propEncodingLength, 10);

        // TODO 需要修改这个
        int[] vertexToPropOffset = new int[]{1, 2, 3, 4};
        int hashFuncCount = 3;

        IndexTree indexTree = new IndexTree(windowSize, numOfVertex, maxPropertyNum, propEncodingLength, vertexToPropOffset, hashFuncCount);
        indexTree.buildTree(dataSetInfo);
        return indexTree;
    }

    public static void query(IndexTree indexTree, String query_file) {
        // 给超边对应上主体属性的id
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new FileReader(new File(query_file)));
            String line;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            int numOfVertex = indexTree.getNumOfVertex();
            int maxPropertyNum = indexTree.getMaxPropertyNum();
            int[] propEncodingLength = indexTree.getPropEncodingLength();
            int hashFuncCount = indexTree.getHashFuncCount();

            while ((line = bufferedReader.readLine()) != null) {
                String[] items = line.split("\\t");
                Date date  = format.parse(items[items.length - 1]);
                long time = date.getTime();
                DataHyperedge hyperedge = new DataHyperedge(time, numOfVertex, items.length - 2, propEncodingLength);

                for (int i = 1, j = 0; i < items.length - 1; i++, j++) {
                    PPBitset ppBitset = PropertyEncodingConstructor.encoding(items[i], propEncodingLength[j], hashFuncCount);
                    hyperedge.addEncoding(ppBitset);
                }

                List<Long> ids = indexTree.singleSearch(hyperedge);
                for (Long id : ids)
                    System.out.println(id);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
