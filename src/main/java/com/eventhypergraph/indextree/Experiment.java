package com.eventhypergraph.indextree;

import com.eventhypergraph.encoding.PPBitset;
import com.eventhypergraph.encoding.PropertyEncodingConstructor;
import com.eventhypergraph.encoding.util.PeriodType;
import com.eventhypergraph.indextree.hyperedge.DataHyperedge;
import com.eventhypergraph.indextree.util.ShoppingDataAnalyzer;
import com.eventhypergraph.indextree.util.DataSetInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.eventhypergraph.dataSetHandler.FilePathConstants.*;

public class Experiment {
    public static void main(String[] args) {
        IndexTree indexTree = buildShoppingTree(SHOPPIONG_EVENT_EXPERIMENT_FILE_PATH, PeriodType.MONTH);
        query(indexTree, SHOPPING_QUERY_FILE_PATH);
    }

    public static IndexTree buildShoppingTree(String dataFile, PeriodType type) {
        // 读取数据集，数据集格式固定第一项是超边ID
        DataSetInfo dataSetInfo = ShoppingDataAnalyzer.readShoppingFile(dataFile, type);
        // dataSetInfo.getOrganizer().printAllEvents();

        // 构建索引树
        int windowSize = 10;
        int encodingLength = 100; // 统一的编码长度
        int bitsetNum = 1; // 单条超边的最大属性个数
        int hashFuncCount = 3;
        int NInterbalNodeChilds = 2;

        IndexTree indexTree = new IndexTree(windowSize, encodingLength, hashFuncCount, NInterbalNodeChilds);
        indexTree.buildShoppingTree(dataSetInfo);
        return indexTree;
    }

    public static void query(IndexTree indexTree, String query_file) {
        // 给超边对应上主体属性的id
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new FileReader(new File(query_file)));
            String line;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            int encodingLength = indexTree.getEncodingLength();
            int hashFuncCount = indexTree.getHashFuncCount();

            while ((line = bufferedReader.readLine()) != null) {
                String[] items = line.split("\\t");
                Date date  = format.parse(items[items.length - 1]);
                long time = date.getTime();
                DataHyperedge hyperedge = new DataHyperedge(time,items.length - 2, encodingLength);

                PPBitset totalBitSet = new PPBitset(encodingLength);
                for (int i = 1, j = 0; i < items.length - 1; i++, j++) {
                    PPBitset ppBitset = PropertyEncodingConstructor.encoding(items[i], encodingLength, hashFuncCount);
                    totalBitSet.or(ppBitset);
                }
                hyperedge.setEncoding(totalBitSet);

                List<Long> ids = indexTree.singleSearch(hyperedge);
                for (Long id : ids)
                    System.out.println(id);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
