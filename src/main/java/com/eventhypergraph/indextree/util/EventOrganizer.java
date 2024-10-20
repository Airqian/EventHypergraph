package com.eventhypergraph.indextree.util;

import java.util.*;

public class EventOrganizer {
    // 数据结构：Map<月份, Map<用户, List<事件>>>
    private Map<String, Map<String, List<String>>> eventMap;

    public EventOrganizer() {
        eventMap = new TreeMap<>();
    }

    public void addEvent(String month, String user, String event) {
        // 如果月份不存在，创建新的月份
        eventMap.putIfAbsent(month, new HashMap<>());
        // 如果用户不存在，创建新的用户事件列表
        eventMap.get(month).putIfAbsent(user, new ArrayList<>());
        // 向用户的事件列表中添加事件
        eventMap.get(month).get(user).add(event);
    }

    public List<String> getEvents(String month, String user) {
        // 获取某个月份某个用户的事件
        return eventMap.getOrDefault(month, new HashMap<>()).getOrDefault(user, new ArrayList<>());
    }

    public Map<String, List<String>> getMonthlyEvents(String month) {
        // 获取某个月份所有用户的事件
        return eventMap.getOrDefault(month, new HashMap<>());
    }
}

