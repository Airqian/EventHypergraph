package com.eventhypergraph.dataset;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;


public class DataGenertor {
    // 需要 UserId、mallId、StoreId、ItemId、UTC
    @org.junit.Test
    public void mockShoppingEvent() {
        ArrayList<HashMap<String, ArrayList<String>>> stores = getStoresAndItems();
        ArrayList<String> malls = getMalls();
        ArrayList<String> users = getAllUsers();
        String startTime = "2019-03-01 00:00:00";
        String endTime = "2019-12-31 23:59:59";

        String shoppingEventFile = "/Users/wqian/Coding/Java/EventHypergraph/src/main/java/com/eventhypergraph/dataset/shopping/shoppingEvent1.txt";
        BufferedWriter writer;

        try {
            writer = new BufferedWriter(new FileWriter(shoppingEventFile));

            // 现对每个用户mock一个购物事件
            for (int i = 0; i < 500; i++) {
                StringBuilder builder = new StringBuilder();
                builder.append(users.get((int) (Math.random() * users.size())) + "\t");
                builder.append(malls.get((int) (Math.random() * malls.size())) + "\t");

                int storesIdx = (int) (Math.random() * stores.size());
                HashMap<String, ArrayList<String>> map = stores.get(storesIdx);
                String storeId = (((map.keySet().toArray()))[0]).toString();
                ArrayList<String> items = map.get(storeId);

                builder.append(storeId + "\t");
                builder.append(items.get((int) (Math.random() * items.size())) + "\t");
                builder.append(getRandomTime(startTime, endTime));
                builder.append("\n");

                writer.write(builder.toString());
            }

            writer.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    // 将地点所在的国家缩写换成对应的id
    public void POICountryHandler() {
        String POIFile = "/Users/wqian/kgstate/kgstate-test/datasets/CheckInEventDataset/POIData.txt";
        String POIFileCountry = "/Users/wqian/kgstate/kgstate-test/datasets/CheckInEventDataset/countryList.txt";

        BufferedReader reader;
        BufferedWriter writer;

        try{
            reader = new BufferedReader(new FileReader(POIFile));
            writer = new BufferedWriter(new FileWriter(POIFileCountry));

            HashSet<String> countries = new HashSet<>();
            String line;

            while((line = reader.readLine()) != null) {
                String[] items = line.split("\\t");
                countries.add(items[4]);
            }

            reader.close();

            for (String country : countries) {
                writer.write(country+"\n");
            }

            writer.close();


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @org.junit.Test
    public void generateUserProfile() {
        ArrayList<String> hobbys = getHobbyIds();
        ArrayList<String> countries = getCountryIds();
        HashMap<String,String> hobbyMap = getHobbyMap();
        HashMap<String,String> countryMap = getCountryId2CodeMap();

        String friendDataFile = "/Users/wqian/kgstate/kgstate-test/datasets/CheckInEventDataset/intermediate_data/friendData.txt";
        String userDataFile = "/Users/wqian/Coding/Java/EventHypergraph/src/main/java/com/eventhypergraph/dataset/shopping/userData.txt";
        BufferedReader reader;
        BufferedWriter writer;

        try{
            reader = new BufferedReader(new FileReader(friendDataFile));
            writer = new BufferedWriter(new FileWriter(userDataFile));
            String line;


            while ((line = reader.readLine()) != null) {
                StringBuilder builder = new StringBuilder();
                String hobby = hobbys.get((int) (Math.random() * hobbys.size()));
                String country = countries.get((int) (Math.random() * countries.size()));

                builder.append(line + "\t");
                builder.append(hobby + "\t" + hobbyMap.get(hobby) + "\t");
                builder.append(country + "\t" + countryMap.get(country));
                builder.append("\n");

                writer.write(builder.toString());
            }

            reader.close();
            writer.close();
        } catch (Exception e) {
            System.out.println(e.getMessage() + "is you?");
        }

    }


    public ArrayList<HashMap<String, ArrayList<String>>> getStoresAndItems() {
        String countryFilePath = "/Users/wqian/Coding/Java/EventHypergraph/src/main/java/com/eventhypergraph/dataset/shopping/storeData.txt";
        BufferedReader bufferedReader;
        ArrayList<HashMap<String, ArrayList<String>>> stores = new ArrayList<>();

        try {
            bufferedReader = new BufferedReader(new FileReader(countryFilePath));
            String line;

            while((line = bufferedReader.readLine()) != null) {
                String[] items = line.split("\t");
                HashMap map = new HashMap();
                ArrayList<String> set = new ArrayList<>();

                for (int i = 2; i < items.length; i ++ )
                    set.add(items[i]);

                map.put(items[0], set);
                stores.add(map);
            }

            System.out.println("stores.size(): " + stores.size());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return stores;
    }

    // 得到所有的用户用于 mock 购物事件
    public ArrayList<String> getAllUsers() {
        String userDataFile = "/Users/wqian/Coding/Java/EventHypergraph/src/main/java/com/eventhypergraph/dataset/shopping/userData.txt";
        BufferedReader bufferedReader;
        ArrayList<String> users = new ArrayList<>();

        try {
            bufferedReader = new BufferedReader(new FileReader(userDataFile));
            String line;

            while((line = bufferedReader.readLine()) != null) {
                String[] items = line.split("\t");
                users.add(items[0]);
            }

            System.out.println("stores.size(): " + users.size());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return users;
    }

    @Test
    // 整理友谊关系，每个用户与其所有认识的人组织在一起
    public void getFriendshipMap() {
        HashMap<String, HashSet<String>> map = new HashMap<>();

        String friendShipList = "/Users/wqian/kgstate/kgstate-test/datasets/CheckInEventDataset/intermediate_data/friendship.txt";
        String friendData = "/Users/wqian/kgstate/kgstate-test/datasets/CheckInEventDataset/intermediate_data/friendData.txt";
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
                stringBuilder.delete(stringBuilder.length()-1, stringBuilder.length());
                stringBuilder.append("\n");
            }

            writer.write(stringBuilder.toString());
            writer.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }



    @Test
    // 1 给商品列表加个 id 写入文件 itemData.txt
    public void ItemDataHandle() {
        String itemList = "/Users/wqian/kgstate/kgstate-test/datasets/ShoppingEventDataset/itemList.txt";
        String itemDataset = "/Users/wqian/kgstate/kgstate-test/datasets/ShoppingEventDataset//itemData.txt";
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

    // 2 得到 商品名-商品id 的hashmap
    public HashMap<String, String> getItemChineseNameMap() {
        HashMap<String, String> map = new HashMap<>();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader("/Users/wqian/kgstate/kgstate-test/datasets/ShoppingEventDataset/itemData.txt"));
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

    public HashMap<String, String> getItemEnglishNameMap() {
        HashMap<String, String> map = new HashMap<>();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader("/Users/wqian/kgstate/kgstate-test/datasets/ShoppingEventDataset/itemData.txt"));
            String line;

            while((line = reader.readLine()) != null) {
                String[] items = line.split("\\t");

                map.put(items[2], items[1]);
            }

            reader.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return map;
    }

    @org.junit.Test
    // 3 给商店加个 id，将商品名改成商品 id
    public void storeDataHandle(){
        // 先得到itemName到itemID的map映射
        HashMap<String, String> itemMap  = getItemChineseNameMap();
        HashMap<String, String> nameMap = getItemEnglishNameMap();

        // 构建商店-商品数据
        BufferedReader reader;
        BufferedWriter writer;
        String storeList = "/Users/wqian/kgstate/kgstate-test/datasets/ShoppingEventDataset/storeList.txt";
        String storeData = "/Users/wqian/kgstate/kgstate-test/datasets/ShoppingEventDataset/storeData.txt";

        try{
            reader = new BufferedReader(new FileReader(storeList));
            writer = new BufferedWriter(new FileWriter(storeData));
            String line;
            int j = 1;

            while ((line = reader.readLine()) != null) {
                String[] items = line.split("\\t");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Store" + j + "\t");
                stringBuilder.append(items[0] + "\t");

                // 对只售卖一种商品的商店条目分开处理
                for (int i = 1; i < items.length; i++) {
                    stringBuilder.append(itemMap.get(items[i]) + "\t");
                    stringBuilder.append(nameMap.get(items[i]));

                    if (i != (items.length - 1))
                        stringBuilder.append("\t");
                }


                writer.write(stringBuilder.toString());
                writer.write("\n");

                j++;
            }

            reader.close();
            writer.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }



    @Test
    // 给country添加id
    public void countryHandler() {
        String countryList = "/Users/wqian/kgstate/kgstate-test/datasets/CheckInEventDataset/intermediate_data/countryList.txt";
        String countryData = "/Users/wqian/kgstate/kgstate-test/datasets/CheckInEventDataset/intermediate_data/countryData.txt";
        BufferedReader bufferedReader;
        BufferedWriter bufferedWriter;

        try {
            bufferedReader = new BufferedReader(new FileReader(countryList));
            bufferedWriter = new BufferedWriter(new FileWriter(countryData));
            String line;
            int i = 1;

            while((line = bufferedReader.readLine()) != null) {
                bufferedWriter.write("country" + i);
                bufferedWriter.write("\t");
                bufferedWriter.write(line);
                bufferedWriter.write("\n");

                i++;
            }

            bufferedReader.close();
            bufferedWriter.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    // 给hobby添加id
    public void hobbyHandler() {
        String countryList = "/Users/wqian/kgstate/kgstate-test/datasets/CheckInEventDataset/intermediate_data/hobbyList.txt";
        String countryData = "/Users/wqian/kgstate/kgstate-test/datasets/CheckInEventDataset/intermediate_data/hobbyData.txt";
        BufferedReader bufferedReader;
        BufferedWriter bufferedWriter;

        try {
            bufferedReader = new BufferedReader(new FileReader(countryList));
            bufferedWriter = new BufferedWriter(new FileWriter(countryData));
            String line;
            int i = 1;

            while((line = bufferedReader.readLine()) != null) {
                String[] items = line.split(" ");
                bufferedWriter.write("hobby" + i + "\t" + items[0] + "\t"+ items[2]);
                bufferedWriter.write("\n");

                i++;
            }

            bufferedReader.close();
            bufferedWriter.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public ArrayList<String> getHobbyIds() {
        String hobbyFilePath = "/Users/wqian/kgstate/kgstate-test/datasets/CheckInEventDataset/hobbyData.txt";
        BufferedReader bufferedReader;
        ArrayList<String> hobbys = new ArrayList<>();

        try {
            bufferedReader = new BufferedReader(new FileReader(hobbyFilePath));
            String line;

            while((line = bufferedReader.readLine()) != null) {
                String[] items = line.split("\\t");
                hobbys.add(items[0]);
            }

            System.out.println("hobbys.size(): " + hobbys.size());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return hobbys;
    }

    public HashMap<String,String> getHobbyMap() {
        String hobbyFilePath = "/Users/wqian/kgstate/kgstate-test/datasets/CheckInEventDataset/hobbyData.txt";
        BufferedReader bufferedReader;
        HashMap<String,String> hobbys = new HashMap<>();

        try {
            bufferedReader = new BufferedReader(new FileReader(hobbyFilePath));
            String line;

            while((line = bufferedReader.readLine()) != null) {
                String[] items = line.split("\\t");
                hobbys.put(items[0], items[2]);
            }

            System.out.println("hobbys.size(): " + hobbys.size());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return hobbys;
    }

    public ArrayList<String> getCountryIds() {
        String countryFilePath = "/Users/wqian/kgstate/kgstate-test/datasets/CheckInEventDataset/countryData.txt";
        BufferedReader bufferedReader;
        ArrayList<String> countries = new ArrayList<>();

        try {
            bufferedReader = new BufferedReader(new FileReader(countryFilePath));
            String line;

            while((line = bufferedReader.readLine()) != null) {
                String[] items = line.split("\\t");
                countries.add(items[0]);
            }

            System.out.println("countries.size(): " + countries.size());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return countries;
    }

    public HashMap<String, String> getCountryCode2IdMap() {
        HashMap<String, String> map = new HashMap<>();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader("/Users/wqian/kgstate/kgstate-test/datasets/CheckInEventDataset/countryData.txt"));
            String line;

            while((line = reader.readLine()) != null) {
                String[] items = line.split("\\t");

                map.put(items[1], items[0]);
            }

            reader.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return map;
    }

    public HashMap<String, String> getCountryId2CodeMap() {
        HashMap<String, String> map = new HashMap<>();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader("/Users/wqian/kgstate/kgstate-test/datasets/CheckInEventDataset/countryData.txt"));
            String line;

            while((line = reader.readLine()) != null) {
                String[] items = line.split("\\t");

                map.put(items[0], items[1]);
            }

            reader.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return map;
    }

    public ArrayList<String> getMalls() {
        String countryFilePath = "/Users/wqian/Coding/Java/EventHypergraph/src/main/java/com/eventhypergraph/dataset/shopping/mallData.txt";
        BufferedReader bufferedReader;
        ArrayList<String> malls = new ArrayList<>();

        try {
            bufferedReader = new BufferedReader(new FileReader(countryFilePath));
            String line;

            while((line = bufferedReader.readLine()) != null) {
                String[] strs = line.split("\t");
                malls.add(strs[0]);
            }

            System.out.println("items.size(): " + malls.size());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return malls;
    }

    public String getRandomTime(String beginDate, String endDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date begin = format.parse(beginDate);
            Date end = format.parse(endDate);

            if (begin.getTime() >= end.getTime())
                return null;


            long temp = begin.getTime() + (long)(Math.random() * (end.getTime() - begin.getTime()));
            return format.format(new Date(temp));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    @Test
    // 国家代号换成国家id
    public void POIDataHandler() {
        HashMap<String, String> map = getCountryCode2IdMap();
        BufferedReader reader;
        BufferedWriter writer;

        try {
            reader = new BufferedReader(new FileReader("/Users/wqian/kgstate/kgstate-test/datasets/CheckInEventDataset/POIData.txt"));
            writer = new BufferedWriter(new FileWriter("/Users/wqian/kgstate/kgstate-test/datasets/CheckInEventDataset/POIData.txt"));
            String line;

            while((line = reader.readLine()) != null) {
                String[] items = line.split("\t");
                StringBuilder builder = new StringBuilder();

                for(int i =0 ;i < items.length -1; i++)
                    builder.append(items[i] + "\t");
                builder.append(map.get(items[4]));
                builder.append("\n");

                writer.write(builder.toString());
            }

            reader.close();
            writer.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }




}
