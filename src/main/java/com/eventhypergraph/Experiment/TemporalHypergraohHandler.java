package com.eventhypergraph.Experiment;

import com.eventhypergraph.indextree.IndexTree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class TemporalHypergraohHandler {
    private final static String HYPEREDGE_ID_UNIQUE = "src/main/java/com/eventhypergraph/dataset/temporal-restricted/NDC-classes/hyperedge-id-unique.txt";
    private final static String HYPEREDGE_LABEL_UNIQUE = "src/main/java/com/eventhypergraph/dataset/temporal-restricted/NDC-classes/hyperedge-label-unique.txt";
    private final static String PROPERTY_1_FILE = "src/main/java/com/eventhypergraph/dataset/temporal-restricted/NDC-classes/node-property1.txt";
    private final static String PROPERTY_3_FILE = "src/main/java/com/eventhypergraph/dataset/temporal-restricted/NDC-classes/node-property3.txt";
    private final static String PROPERTY_5_FILE = "src/main/java/com/eventhypergraph/dataset/temporal-restricted/NDC-classes/node-property5.txt";

    public static void main(String[] args) {

    }

    private static IndexTree buildTree(Map<String, List<String>> propertyMap) {
        Map<String, List<String>> proMap = readProperty(PROPERTY_1_FILE); // 顶点到属性的映射
        List<long[]> idToTime = getAllEdgeTimeASC(); // 将超边按照时间升序排序
        Map<String, String> idMap = readEdgeIds(); // 超边id到整条超边的映射（包含顶点id以及属性）
        Map<String, String> labelMap = readEdgeLabels(); // 超边id到整条超边的映射（包含顶点label以及属性）

        // 构建索引树
        int windowSize = 10;
        int encodingLength = 100; // 统一的编码长度
        int hashFuncCount = 3;
        int NInterbalNodeChilds = 10;

        IndexTree indexTree = new IndexTree(windowSize, encodingLength, hashFuncCount, NInterbalNodeChilds);
        indexTree.buildTemporalHypergraphTree(idToTime, idMap, labelMap, proMap);
        return indexTree;
    }

    private static Map<String, String> readEdgeIds() {
        Map<String, String> map = new HashMap<>();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(HYPEREDGE_ID_UNIQUE));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] items = line.split("\\t");
                map.put(items[0], line);
            }

            reader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return map;
    }

    private static Map<String, String> readEdgeLabels() {
        Map<String, String> map = new HashMap<>();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(HYPEREDGE_LABEL_UNIQUE));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] items = line.split("\\t");
                map.put(items[0], line);
            }

            reader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return map;
    }

    private static Map<String, List<String>> readProperty(String propertyFile) {
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

    private static List<long[]> getAllEdgeTimeASC() {
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(HYPEREDGE_ID_UNIQUE));
            String line;
            List<long[]> list = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                String[] items = line.split("\\t");
                // 第 0 位是超边id，第 1 位是超边时间
                list.add(new long[] {Long.parseLong(items[0]), Long.parseLong(items[items.length - 1])});
            }

            Collections.sort(list, new Comparator<long[]>() {
                @Override
                public int compare(long[] o1, long[] o2) {
                    return Long.compare(o1[1], o2[1]);
                }
            });
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
