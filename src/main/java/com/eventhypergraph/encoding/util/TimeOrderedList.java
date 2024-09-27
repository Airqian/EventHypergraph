package com.eventhypergraph.encoding.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// 一个按照时间排序的列表
public class TimeOrderedList<T> {
    private final List<T> list;
    private final Comparator<T> comparator;

    public TimeOrderedList(Comparator<T> comparator) {
        this.list = new ArrayList<>();
        this.comparator = comparator;
    }

    public void add(T element) {
        // 找到插入位置
        int index = 0;
        while (index < list.size() && comparator.compare(list.get(index), element) < 0) {
            index++;
        }
        list.add(index, element); // 插入元素
    }

    public List<T> getList() {
        return list;
    }
}
