package encoding;

import com.eventhypergraph.encoding.PPBitset;
import org.junit.Test;

public class PPBitesetTest {
    public void preparation() {
        PPBitset bitset1 = new PPBitset(20);
        PPBitset bitset2 = new PPBitset(20);
        PPBitset bitset3 = new PPBitset(21);

        bitset1.set(2, true);
        bitset1.set(20,true);

        bitset2.set(2, true);
        bitset2.set(3, true);
        bitset2.set(10,true);

        bitset3.set(2, true);
        bitset3.set(3, true);
        bitset3.set(11,true);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkSet() {
        PPBitset bitset = new PPBitset(64);
        assert bitset.length() == 64;

        bitset.set(2, true);
        bitset.set(63, true);
        bitset.set(64, true);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkGet() {
        PPBitset bitset = new PPBitset(64);

        bitset.set(2, true);
        bitset.set(63, true);
        assert bitset.get(2) == true;
        assert bitset.get(28) == false;
        assert bitset.get(63) == true;
        bitset.get(64);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkOr() {
        PPBitset bitset1 = new PPBitset(20);
        PPBitset bitset2 = new PPBitset(20);
        PPBitset bitset3 = new PPBitset(21);

        bitset1.set(2, true);
        bitset1.set(10,true);

        bitset2.set(2, true);
        bitset2.set(3, true);
        bitset2.set(10,true);

        PPBitset bitset4 = bitset1.or(bitset1);
        assert bitset4.equals(bitset1);

        PPBitset bitset5 = bitset1.or(bitset2);
        assert bitset5.equals(bitset2);

        bitset1.or(bitset3);
    }


    @Test(expected = IllegalArgumentException.class)
    public void checkSubset() {
        PPBitset bitset1 = new PPBitset(20);
        PPBitset bitset2 = new PPBitset(20);
        PPBitset bitset3 = new PPBitset(20);
        PPBitset bitset4 = new PPBitset(21);

        bitset1.set(2, true);
        bitset1.set(10,true);

        bitset2.set(2, true);
        bitset2.set(3, true);
        bitset2.set(10,true);
        assert bitset1.isBitwiseSubset(bitset2) == true;

        bitset3.set(2, true);
        bitset3.set(3, true);
        bitset3.set(11,true);
        assert bitset1.isBitwiseSubset(bitset3) == false;

        bitset4.set(2, true);
        bitset4.set(3, true);
        bitset4.set(10,true);
        assert bitset1.isBitwiseSubset(bitset4) == true;
    }

    @Test
    public void checkEquals() {
        PPBitset bitset1 = new PPBitset(20);
        PPBitset bitset2 = new PPBitset(20);
        PPBitset bitset3 = new PPBitset(20);
        PPBitset bitset4 = new PPBitset(21);

        bitset1.set(2, true);
        bitset1.set(10,true);

        bitset2.set(2, true);
        bitset2.set(3, true);
        bitset2.set(10,true);

        bitset3.set(2, true);
        bitset3.set(10,true);

        bitset4.set(2, true);
        bitset4.set(10,true);

        assert bitset1.equals(bitset1) == true;
        assert bitset1.equals(bitset2) == false;
        assert bitset1.equals(bitset3) == true;
        assert bitset1.equals(bitset4) == false;
    }

    @Test
    public void checkNextbit() {
        PPBitset bitset = new PPBitset(64);

        bitset.set(2, true);
        bitset.set(3,false);
        bitset.set(62,true);
        bitset.set(63, true);

        assert (bitset.nextSetBit(4) == 62);
        assert (bitset.nextSetBit(62) == 62); // 本身是true会返回本身
        assert (bitset.nextSetBit(63) == 63);
        assert (bitset.nextSetBit(64) == -1);

        assert (bitset.nextClearBit(2) == 3);
        assert (bitset.nextClearBit(3) == 3);// 本身是false会返回本身
        assert (bitset.nextClearBit(63) == 64);
        assert (bitset.nextClearBit(64) == -1);
        assert (bitset.nextClearBit(65) == -1);

        bitset.clear();
        System.out.println(bitset.toString());
    }

    @Test
    public void checkBit() {
        PPBitset bitset = new PPBitset(40);
        PPBitset bitset1 = new PPBitset(40);
        bitset.set(2);
        bitset1.set(0);

        System.out.println(bitset.nextSetBit(0));
        System.out.println(bitset.compareTo(bitset1));
    }
}
