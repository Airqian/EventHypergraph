package com.eventhypergraph.dataset;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;

public class StackOverFlowData {
    // 节点标签文件
    private static final String NODE_LABELS = "src/main/java/com/eventhypergraph/dataset/coauth-MAG-Geology-full/coauth-MAG-Geology-full-node-labels.txt";

    // 每条超边包含的顶点数文件
    private static final String NVERTS = "src/main/java/com/eventhypergraph/dataset/coauth-MAG-Geology-full/coauth-MAG-Geology-full-nverts.txt";

    // 超边包含的具体顶点
    private static final String SIMPLICES = "src/main/java/com/eventhypergraph/dataset/coauth-MAG-Geology-full/coauth-MAG-Geology-full-simplices.txt";

    // 超边标签文件
    private static final String SIMPLICES_LABLES = "src/main/java/com/eventhypergraph/dataset/coauth-MAG-Geology-full/coauth-MAG-Geology-full-simplex-labels.txt";

    private static final String TIMES = "src/main/java/com/eventhypergraph/dataset/coauth-MAG-Geology-full/coauth-MAG-Geology-full-times.txt";

    public static void main(String[] args) {
        readNverts();
        readSimplexLabels();
        readSimplices();
        readTime();
        readNodeLabels();
//        LinkedList<Long> nverts = readNverts();
//        LinkedList<Long> HElabels = readSimplexLabels();
//        LinkedList<Long> simplices = readSimplices();
//        LinkedList<Long> times = readTime();
//        handle(nverts, times, HElabels, simplices);
    }

    // 处理超边到节点的映射
    private static void handle(LinkedList<Long> nverts, LinkedList<Long> times, LinkedList<Long> HElabels, LinkedList<Long> simplices) {
        String output = "/Users/wqian/Coding/Java/EventHypergraph/src/main/java/com/eventhypergraph/dataset/tags-stack-overflow/output.txt";
        BufferedWriter writer;

        try {
            writer = new BufferedWriter(new FileWriter(new File(output)));
            StringBuilder builder = new StringBuilder();

            int j = 0; // 在 simplices 中的下标
            for (int i = 0; i < 10; i++) {
                Long nvertex = nverts.get(i);
                if (nvertex != 1l) {
                    j++;
                    continue;
                }

                builder.append(HElabels.get(i));
                builder.append("\t");
                for (int k = j; k < j + nvertex; k++, j++) {
                    builder.append(simplices.get(i));
                    builder.append("\t");
                }

                builder.append(times.get(i));
                builder.append("\n");
                writer.write(builder.toString());
            }

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LinkedList<Long> readNverts() {
        BufferedReader reader;
        LinkedList<Long> list = new LinkedList<>();

        try {
            reader = new BufferedReader(new FileReader(new File(NVERTS)));
            String line;

            int not_one_rows = 0;
            int total = 0;
            while ((line = reader.readLine()) != null) {
                if (!line.equals("1"))
                    not_one_rows++;
                total ++;
                list.add(Long.parseLong(line));
            }

            System.out.println("总超边数：" + total);
            System.out.println("节点数大于1的超边数：" + not_one_rows);
            reader.close();
            return list;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void readNodeLabels() {
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(new File(NODE_LABELS)));
            String line;

            int total = 0;
            while ((line = reader.readLine()) != null) {
                total ++;
            }

            System.out.println("总顶点数：" + total);
            reader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LinkedList<Long> readSimplexLabels() {
        BufferedReader reader;
        LinkedList<Long> list = new LinkedList<>();

        try {
            reader = new BufferedReader(new FileReader(new File(SIMPLICES_LABLES)));
            String line;

            int total = 0;
            while ((line = reader.readLine()) != null) {
                total ++;
//                list.add(Long.parseLong(line));
            }

            System.out.println("超边的标签数：" + total);
            reader.close();
            return list;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LinkedList<Long> readSimplices() {
        BufferedReader reader;
        LinkedList<Long> list = new LinkedList<>();

        try {
            reader = new BufferedReader(new FileReader(new File(SIMPLICES)));
            String line;

            HashSet<String> set = new HashSet<>();
            while ((line = reader.readLine()) != null) {
                set.add(line);
                list.add(Long.parseLong(line));
            }

            System.out.println("不同顶点标签数量为：" + set.size());
            reader.close();
            return list;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LinkedList<Long> readTime() {
        BufferedReader reader;
        LinkedList<Long> list = new LinkedList<>();

        try {
            reader = new BufferedReader(new FileReader(new File(TIMES)));
            String line;

            int total = 0;
            while ((line = reader.readLine()) != null) {
                list.add(Long.parseLong(line));
                total++;
            }

            System.out.println("时间的数量为：" + total);
            reader.close();
            return list;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
