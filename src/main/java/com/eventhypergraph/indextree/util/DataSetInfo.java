package com.eventhypergraph.indextree.util;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class DataSetInfo {
    // 数据集中的事件总数
    private int eventsNum;

    // 数据集中的所有事件，按主体属性进行了分组，分析数据的时候还需要对时间进行顺序排序
    private HashMap<Long, List<String>> events;

    // 数据集中事件发生的最大时间
    private long globalMinTime;

    // 数据集中事件发生的最小时间
    private long globalMaxTime;

    private int monthDiff;

    public DataSetInfo(){}

    public DataSetInfo(int eventsNum, long globalMinTime, long globalMaxTime, int monthDiff, HashMap<Long, List<String>> events) {
        this.eventsNum = eventsNum;
        this.globalMinTime = globalMinTime;
        this.globalMaxTime = globalMaxTime;
        this.monthDiff = monthDiff;
        this.events = events;
    }

    public int getEventsNum() {
        return eventsNum;
    }

    public void setEventsNum(int eventsNum) {
        this.eventsNum = eventsNum;
    }

    public long getGlobalMinTime() {
        return globalMinTime;
    }

    public void setGlobalMinTime(long globalMinTime) {
        this.globalMinTime = globalMinTime;
    }

    public long getGlobalMaxTime() {
        return globalMaxTime;
    }

    public void setGlobalMaxTime(long globalMaxTime) {
        this.globalMaxTime = globalMaxTime;
    }

    public HashMap<Long, List<String>> getEvents() {
        return events;
    }

    public void setEvents(HashMap<Long, List<String>> events) {
        this.events = events;
    }

    public int getMonthDiff() {
        return monthDiff;
    }

    public void setMonthDiff(int monthDiff) {
        this.monthDiff = monthDiff;
    }
}
