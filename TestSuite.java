import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import java.util.Scanner;
import org.junit.Test;

public class TestSuite {

  public static void run() {
    Cache.read(0x5);
    Cache.read(0x6);
    Cache.read(0x7);
    Cache.read(0x14c);
    Cache.read(0x14d);
    Cache.read(0x14e);
    Cache.read(0x14f);
    Cache.read(0x150);
    Cache.read(0x151);
    Cache.read(0x3a6);
    Cache.read(0x4c3);
    Cache.displayCache();
    Cache.write((0x14c), (short) 99);
    Cache.write((0x63b), (short) 7);
    Cache.read(0x582);
    Cache.read(0x348);
    Cache.read(0x3F);
    Cache.displayCache();
    Cache.read(0x14b);
    Cache.read(0x14c);
    Cache.read(0x63f);
    Cache.read(0x83);
    Cache.displayCache();
  }

  private static boolean testHit(int address) {
    String message = Cache.read(address);
    return message.contains("Cache Hit");
  }

  private static boolean testWrite(int address, short data) {
    String message = Cache.write(address, data);
    return message.contains("Cache Hit");
  }

  @Test
  public void testHits() {
    assertFalse("failure - should be false", testHit(0x5));
    assertTrue("failure - should be true", testHit(0x6));
    assertTrue("failure - should be true", testHit(0x7));
    assertFalse("failure - should be false", testHit(0x14c));
    assertTrue("failure - should be true", testHit(0x14d));
    assertTrue("failure - should be true", testHit(0x14d));
    assertTrue("failure - should be true", testHit(0x14e));
    assertTrue("failure - should be true", testHit(0x14f));
    assertFalse("failure - should be false", testHit(0x150));
    assertTrue("failure - should be true", testHit(0x151));
    assertFalse("failure - should be false", testHit(0x3a6));
    assertFalse("failure - should be false", testHit(0x4c3));
    assertTrue("failure - should be true", testWrite(0x14c, (short) 99));
    assertFalse("failure - should be false", testWrite(0x63b, (short) 7));
    assertFalse("failure - should be false", testHit(0x582));
    assertFalse("failure - should be false", testHit(0x348));
    assertFalse("failure - should be false", testHit(0x3F));
    assertFalse("failure - should be false", testHit(0x14b));
    assertTrue("failure - should be true", testHit(0x14c));
    assertFalse("failure - should false", testHit(0x63f));
    assertFalse("failure - should false", testHit(0x83));
  }

  @Test
  public void testWriteback() {
    assertSame(Cache.mainMemory[0x14c], (short) 99);
    assertSame(Cache.mainMemory[0x63b], (short) 7);
  }
}
