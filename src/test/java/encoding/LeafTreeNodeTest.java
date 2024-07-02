package encoding;

import com.eventhypergraph.encoding.PPBitset;
import com.eventhypergraph.indextree.hyperedge.DataHyperedge;
import com.eventhypergraph.indextree.treeNode.LeafTreeNode;
import org.junit.Test;

import java.util.List;

public class LeafTreeNodeTest {
    @Test
    public void binarySortTest() {
        LeafTreeNode node = new LeafTreeNode(1000, 2000, 10);

        DataHyperedge dataHyperedge = new DataHyperedge(1000);  // 01
        DataHyperedge dataHyperedge1 = new DataHyperedge(1000); // 11
        DataHyperedge dataHyperedge2 = new DataHyperedge(1200); // 11
        DataHyperedge dataHyperedge3 = new DataHyperedge(1000); // 10

        // 11
        PPBitset bitset = new PPBitset(40);
        bitset.set(0);
        bitset.set(1);

        // 10
        PPBitset bitset1 = new PPBitset(40);
        bitset1.set(0);

        // 01
        PPBitset bitset2 = new PPBitset(40);
        bitset2.set(1);

        dataHyperedge.addEncoding(bitset2);
        dataHyperedge1.addEncoding(bitset);
        dataHyperedge2.addEncoding(bitset);
        dataHyperedge3.addEncoding(bitset1);

        System.out.println(dataHyperedge1.compareTo(dataHyperedge3));

        node.addHyperedge(dataHyperedge);
        node.addHyperedge(dataHyperedge1);
        node.addHyperedge(dataHyperedge2);
        node.addHyperedge(dataHyperedge3);

        List<DataHyperedge> dataHyperedges = node.getHyperedges();
        System.out.println(dataHyperedges);
        System.out.println(dataHyperedge1.cardinality());

        assert (dataHyperedges.get(0).getEventTime() == 1000 && dataHyperedges.get(0).getEncoding().getProperty(0).get(0) && dataHyperedges.get(0).getEncoding().getProperty(0).get(1));
        assert (dataHyperedges.get(1).getEventTime() == 1200 && dataHyperedges.get(1).getEncoding().getProperty(0).get(0) && dataHyperedges.get(1).getEncoding().getProperty(0).get(1));
        assert (dataHyperedges.get(2).getEventTime() == 1000 && dataHyperedges.get(2).getEncoding().getProperty(0).get(0));
        assert (dataHyperedges.get(3).getEventTime() == 1000 && dataHyperedges.get(3).getEncoding().getProperty(0).get(1));
    }
}
