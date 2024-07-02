package com.eventhypergraph.encoding;

import com.eventhypergraph.encoding.util.HashFunction;
import com.eventhypergraph.encoding.util.HashFunctionInterface;
import com.eventhypergraph.encoding.util.Triple;
import com.sun.istack.internal.NotNull;


public class PropertyEncodingConstructor extends EncodingConstructor{
    // Triple的三个元素分别表示待映射字符串，编码长度，哈希映射函数个数
    @Override
    public PPBitset encoding(@NotNull Triple<String, Integer, Integer> triple) {
        String content = triple.getFirst();
        int length = triple.getSecond();
        int hashCounts = triple.getThird();

        if (content == null || content.length() == 0)
            throw new IllegalArgumentException("The property value is null");
        if (length <= 0 || hashCounts <= 0)
            throw new IllegalArgumentException(String.format("the value of encoding length or counts of hash function cannot be zero." +
                    "length=%d, hashCounts=%d", length, hashCounts));

        PPBitset bitSet = new PPBitset(length);

        for (int i = 0; i < hashCounts; i++) {
            HashFunctionInterface hashFunction = HashFunction.HASHFUNCTIONS[i];
            int bit = (int) (hashFunction.apply(content) % length);
            bitSet.set(bit);
        }

        return bitSet;
    }
}





