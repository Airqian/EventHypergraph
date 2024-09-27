package encoding;

import com.azul.crs.client.service.ClientService;
import com.eventhypergraph.encoding.PPBitset;
import com.eventhypergraph.indextree.hyperedge.DataHyperedge;
import com.eventhypergraph.indextree.hyperedge.Hyperedge;
import org.junit.Test;
import org.w3c.dom.CDATASection;

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

        dataHyperedges.remove(dataHyperedge);
        System.out.println(dataHyperedges.size());

        System.out.println(dataHyperedge.compareTo(dataHyperedge1)); // 1

        Collections.sort(dataHyperedges);
    }
}
