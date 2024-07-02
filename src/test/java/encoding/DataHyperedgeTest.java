package encoding;

import com.eventhypergraph.encoding.PPBitset;
import com.eventhypergraph.indextree.hyperedge.DataHyperedge;
import org.junit.Test;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

public class DataHyperedgeTest {
    @Test
    public void comparetoTest() {
        // 测试超边类的compareTo方法
        DataHyperedge dataHyperedge = new DataHyperedge(1000);
        DataHyperedge dataHyperedge1 = new DataHyperedge(1000);
        DataHyperedge dataHyperedge2 = new DataHyperedge(1200);

        PPBitset bitset = new PPBitset(40);
        bitset.set(1);
        PPBitset bitset1 = new PPBitset(40);
        bitset1.set(0);

        dataHyperedge.addEncoding(bitset);
        dataHyperedge1.addEncoding(bitset1);
        dataHyperedge2.addEncoding(bitset1);

        List<DataHyperedge> dataHyperedges = new ArrayList() {{
            add(dataHyperedge);
            add(dataHyperedge1);
            add(dataHyperedge2);
        }};

        System.out.println(dataHyperedge.compareTo(dataHyperedge1)); // 1

        Collections.sort(dataHyperedges);
        assert (dataHyperedges.get(0).getEventTime() == 1000 && dataHyperedges.get(0).getEncoding().getProperty(0).get(0));
        assert (dataHyperedges.get(1).getEventTime() == 1200 && dataHyperedges.get(1).getEncoding().getProperty(0).get(0));
        assert (dataHyperedges.get(2).getEventTime() == 1000 && dataHyperedges.get(2).getEncoding().getProperty(0).get(1));
    }
}
