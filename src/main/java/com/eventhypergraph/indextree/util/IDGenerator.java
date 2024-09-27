package com.eventhypergraph.indextree.util;

public class IDGenerator {
    public static long generateNodeId() {
        SnowFlake snowFlake = new SnowFlake(1, 1);
        return snowFlake.nextId();
    }
}
