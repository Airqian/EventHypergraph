package com.eventhypergraph.encoding;

import com.eventhypergraph.encoding.util.Triple;

public abstract class EncodingConstructor {
    public abstract PPBitset encoding(Triple<String, Integer, Integer> triple);
}
