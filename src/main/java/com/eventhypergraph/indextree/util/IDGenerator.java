package com.eventhypergraph.indextree.util;

public class IDGenerator {
    private static long initialId = 1537074805834256649l;
    public static long generateNodeId() {

        return ++initialId;
    }
}
