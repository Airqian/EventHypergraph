package com.eventhypergraph.encoding;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class DataHandler {
    public static void main(String[] args) {
        DataHandler dataHandler = new DataHandler();
//        dataHandler.ItemDataHandle();
//        dataHandler.storeDataHandle();

        dataHandler.generateUserProfile();

    }

    // 根据爱好数据集、国家数据集以及友谊数据集生成用户画像数据集
    public void generateUserProfile() {
        ArrayList<String> hobbys = getHobbys();
        ArrayList<String> countries = getCountry();

        String friendDataFile = "dataset/CheckInEventDataset/friendData.txt";
        String userDataFile = "dataset/CheckInEventDataset/userData.txt";
        BufferedReader reader;
        BufferedWriter writer;

        try{
            reader = new BufferedReader(new FileReader(friendDataFile));
            writer = new BufferedWriter(new FileWriter(userDataFile));
            String line;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    public ArrayList<String> getHobbys() {
        String hobbyFilePath = "dataset/CheckInEventDataset/hobby.txt";
        BufferedReader bufferedReader;
        ArrayList<String> hobbys = new ArrayList<>();

        try {
            bufferedReader = new BufferedReader(new FileReader(hobbyFilePath));
            String line;

            while((line = bufferedReader.readLine()) != null) {
                String[] items = line.split(" ");
                hobbys.add(items[2]);
            }

            System.out.println(hobbys.size());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return hobbys;
    }

    public ArrayList<String> getCountry() {
        String countryFilePath = "dataset/CheckInEventDataset/country.txt";
        BufferedReader bufferedReader;
        ArrayList<String> countries = new ArrayList<>();

        try {
            bufferedReader = new BufferedReader(new FileReader(countryFilePath));
            String line;

            while((line = bufferedReader.readLine()) != null) {
                String[] items = line.split("-");
                countries.add(items[0]);
            }

            System.out.println(countries.size());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return countries;
    }

    // 生成用户画像数据集
    public void getfriendData() {
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader("dataset/CheckInEventDataset/friendData.txt"));
            String line;

            System.out.println("sss");
            int i = 0;
            while ((line = reader.readLine()) != null) {
                String[] items = line.split("\\t");
                System.out.println(i++);

            }
            reader.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }



    // 整理友谊关系，每个用户单独站一行
    public void getFriendshipMap() {
        HashMap<String, HashSet<String>> map = new HashMap<>();

        String friendShipList = "dataset/CheckInEventDataset/new_friendship.txt";
        String friendData = "dataset/CheckInEventDataset/friendData.txt";
        BufferedReader reader;
        BufferedWriter writer;
        String line;

        try {
            reader = new BufferedReader(new FileReader(friendShipList));
            writer = new BufferedWriter(new FileWriter(friendData));

            // 先把认识关系整成一个set
            while ((line = reader.readLine()) != null) {
                String[] items = line.split("\\t");

                if (!map.containsKey(items[0])) {
                    HashSet<String> set = new HashSet<>();
                    set.add(items[1]);
                    map.put(items[0], set);
                } else {
                    map.get(items[0]).add(items[1]);
                }

                if (!map.containsKey(items[1])) {
                    HashSet<String> set = new HashSet<>();
                    set.add(items[0]);
                    map.put(items[1], set);
                } else {
                    map.get(items[1]).add(items[0]);
                }
            }

            reader.close();

            StringBuilder stringBuilder = new StringBuilder();

            for (String key : map.keySet()) {
                HashSet<String> set = map.get(key);
                stringBuilder.append(key + "\t" + set.size() + "\t");

                for (String value : set)
                    stringBuilder.append(value + "\t");
                stringBuilder.append("\n");
            }

            writer.write(stringBuilder.toString());
            writer.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // 根据商品列表生成商品ID，写入itemData.txt
    public void ItemDataHandle() {
        String itemList = "dataset/ShoppingEventDataset/itemList.txt";
        String itemDataset = "dataset/ShoppingEventDataset/itemData.txt";
        BufferedReader bufferedReader;
        BufferedWriter bufferedWriter;

        try {
            bufferedReader = new BufferedReader(new FileReader(itemList));
            bufferedWriter = new BufferedWriter(new FileWriter(itemDataset));
            String line;
            int i = 1;

            while((line = bufferedReader.readLine()) != null) {
                String[] items = line.split("\\t");
                bufferedWriter.write("item" + i + "\t" + items[0] + "\t"+ items[1]);
                bufferedWriter.write("\n");

                i++;
            }

            bufferedReader.close();
            bufferedWriter.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private HashMap<String, String> getItemMap() {
        HashMap<String, String> map = new HashMap<>();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader("dataset/ShoppingEventDataset/itemData.txt"));
            String line;

            while((line = reader.readLine()) != null) {
                String[] items = line.split("\\t");

                map.put(items[2], items[0]);
            }

            reader.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return map;
    }

    // 根据商店列表生成商店ID，根据得到的商品数据组织商店数据
    public void storeDataHandle(){
        // 先得到itemName到itemID的map映射
        HashMap<String, String> itemMap  = getItemMap();

        // 构建商店-商品数据
        BufferedReader reader;
        BufferedWriter writer;
        String storeList = "dataset/ShoppingEventDataset/storeList.txt";
        String storeData = "dataset/ShoppingEventDataset/storeData.txt";

        try{
            reader = new BufferedReader(new FileReader(storeList));
            writer = new BufferedWriter(new FileWriter(storeData));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] items = line.split("\\t");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(items[0] + "\t");

                // 对只售卖一种商品的商店条目分开处理
                for (int i = 1; i < items.length; i++) {
                    stringBuilder.append(itemMap.get(items[i]));

                    if (i != (items.length - 1))
                        stringBuilder.append("\t");
                }


                writer.write(stringBuilder.toString());
                writer.write("\n");
            }

            reader.close();
            writer.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}


