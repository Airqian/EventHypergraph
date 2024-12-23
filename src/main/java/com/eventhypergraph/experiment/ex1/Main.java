package com.eventhypergraph.experiment.ex1;

import com.eventhypergraph.DataHandler.TempralGraphDataHandler.IndexTreeBuilder;
import com.eventhypergraph.encoding.PPBitset;
import com.eventhypergraph.encoding.PropertyEncodingConstructor;
import com.eventhypergraph.indextree.IndexTree;
import com.eventhypergraph.indextree.hyperedge.DataHyperedge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.eventhypergraph.experiment.ex1.RandomHyperedgeSelector.NUM_EDGE_PER_GROUP;

public class Main {
    public static void main(String[] args) {
        final int nProperty = 5;
        String hyperedgeIdFile = "src/main/java/com/eventhypergraph/dataset/temporal-restricted/congress-bills/hyperedge-id-unique.txt";
        String hyperedgeLabelFile = "src/main/java/com/eventhypergraph/dataset/temporal-restricted/congress-bills/hyperedge-label-unique.txt";
        String propertyFile = "src/main/java/com/eventhypergraph/dataset/temporal-restricted/congress-bills/node-property" + nProperty + ".txt";
        String queryFile = "src/main/java/com/eventhypergraph/experiment/ex1/files/congress-bills-selectedEdges.txt";
        String treeInfo = "src/main/java/com/eventhypergraph/experiment/ex1/files/congress-bills-TreeInfo.txt";

        // 构建索引树所需要的参数
        int windowSize = 128;
        int encodingLength = 70; // 编码长度
        int hashFuncCount = 2;
        int secondaryIndexSize = 8;
        int minInternalNodeChilds = 15;
        int maxInternalNodeChilds = 15;
        Map<String, List<String>> proMap = getId2PropertyMap(propertyFile);

        // 构造索引树
//        IndexTree indexTree = IndexTreeBuilder.build(hyperedgeIdFile, hyperedgeLabelFile, propertyFile,
//                windowSize, encodingLength, hashFuncCount, minInternalNodeChilds, maxInternalNodeChilds,
//                secondaryIndexSize, treeInfo);
//        long start = System.currentTimeMillis();
//        queryParallel(indexTree, queryFile, proMap);
//        long end = System.currentTimeMillis();
//        System.out.println("耗费的时间：" + (end - start));

        int[] arr = new int[]{140, 145, 146, 147};
        for (int i = 0; i < arr.length; i++) {
            System.out.println("编码长度：" + arr[i]);
            IndexTree indexTree = IndexTreeBuilder.build(hyperedgeIdFile, hyperedgeLabelFile, propertyFile,
                    windowSize, arr[i], hashFuncCount, minInternalNodeChilds, maxInternalNodeChilds, secondaryIndexSize, treeInfo);
            queryParallel(indexTree, queryFile, proMap);
            System.out.println();
        }
    }

    public static void queryParallel(IndexTree indexTree, String queryFile, Map<String, List<String>> proMap) {
        // 获取文件中的所有超边，并计算好编码
        BufferedReader bufferedReader;
        List<DataHyperedge> dataHyperedges = new ArrayList<>();

        try {
            bufferedReader = new BufferedReader(new FileReader(new File(queryFile)));
            String line;

            int encodingLength = indexTree.getEncodingLength();
            int hashFuncCount = indexTree.getHashFuncCount();

            while ((line = bufferedReader.readLine()) != null) {
                String[] items = line.split("\\t");
                long time = Long.parseLong(items[items.length - 1]);
                DataHyperedge hyperedge = new DataHyperedge(time, encodingLength);

                // 对该超边中包含的所有顶点的所有属性进行编码，合成超边编码
                PPBitset bitset = new PPBitset(encodingLength);
                for (int j = 1; j < items.length - 1; j++) {
                    String vertexId = items[j];
                    hyperedge.addVertexId(Long.valueOf(vertexId));
                    for (String prop : proMap.get(vertexId)) {
                        PPBitset tmp = PropertyEncodingConstructor.encoding(prop, encodingLength, hashFuncCount);
                        bitset.or(tmp);
                    }
                }
                hyperedge.setEncoding(bitset);
                dataHyperedges.add(hyperedge);
            }

            bufferedReader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 并行处理
        int numProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numProcessors);

        List<Future<Integer>> futureResults = new ArrayList<>();

        for (int i = 0; i < dataHyperedges.size() / NUM_EDGE_PER_GROUP; i++) {
            int start = i * NUM_EDGE_PER_GROUP;
            int end = Math.min((i + 1) * NUM_EDGE_PER_GROUP, dataHyperedges.size());

            futureResults.add(executor.submit(() -> {
                int sum = 0;
                for (int j = start; j < end; j++) {
                    sum += indexTree.singleEdgeSearch(dataHyperedges.get(j)).size();
                }
                return sum;
            }));
        }

        // 收集结果并打印
        try {
            for (int i = 0; i < futureResults.size(); i++) {
                System.out.println("组别 " + i + " 的总候选数：" + futureResults.get(i).get());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    public static void query(IndexTree indexTree, String queryFile, Map<String, List<String>> proMap) {
        // 获取文件中的所有超边，并计算好编码
        BufferedReader bufferedReader;
        List<DataHyperedge> dataHyperedges = new ArrayList<>();

        try {
            bufferedReader = new BufferedReader(new FileReader(new File(queryFile)));
            String line;

            int encodingLength = indexTree.getEncodingLength();
            int hashFuncCount = indexTree.getHashFuncCount();

            while ((line = bufferedReader.readLine()) != null) {
                String[] items = line.split("\\t");
                long time = Long.parseLong(items[items.length - 1]);
                DataHyperedge hyperedge = new DataHyperedge(time, encodingLength);

                // 对该超边中包含的所有顶点的所有属性进行编码，合成超边编码
                PPBitset bitset = new PPBitset(encodingLength);
                for (int j = 1; j < items.length - 1; j++) {
                    String vertexId = items[j];
                    hyperedge.addVertexId(Long.valueOf(vertexId));
                    for (String prop : proMap.get(vertexId)) {
                        PPBitset tmp = PropertyEncodingConstructor.encoding(prop, encodingLength, hashFuncCount);
                        bitset.or(tmp);
                    }
                }
                hyperedge.setEncoding(bitset);
                dataHyperedges.add(hyperedge);
            }

            bufferedReader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < dataHyperedges.size() / NUM_EDGE_PER_GROUP; i++) {
            System.out.print("组别 " + i + " 的总候选数：");

            int start = i * NUM_EDGE_PER_GROUP;
            int end = (i + 1) * NUM_EDGE_PER_GROUP;
            int sum = 0;

            for (; start < end; start++) {
                sum += indexTree.singleEdgeSearch(dataHyperedges.get(start)).size();
            }
            System.out.println(sum);
        }
    }

    private static Map<String, List<String>> getId2PropertyMap(String propertyFile) {
        Map<String, List<String>> map = new HashMap<>();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(propertyFile));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] items = line.split("\\t");
                map.putIfAbsent(items[0], new ArrayList<>());

                for (int i = 1; i < items.length; i++) {
                    map.get(items[0]).add(items[i]);
                }
            }

            reader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return map;
    }
}
