import java.util.Collections;
import java.util.Arrays;

// You will implement a direct-mapped, write-back cache.

class Block {
  short slot;
  int valid;
  int tag;
  short[] data = new short[16];

  public Block(short slotNum) {
    slot = slotNum;
    Arrays.fill(data, (short) 0);
  }

  public void set(int cacheTag, short[] cacheData) {
    tag = cacheTag;
    valid = 1;
    setData(cacheData);
  }

  private void setData(short[] cacheData) {
    for (short i = 0; i < cacheData.length; i++) {
      data[i] = cacheData[i];
    }
  }

  private String dataToString() {
    String array = "";
    for (short i = 0; i < data.length; i++) {
      array += Integer.toHexString(data[i]);
      if (i != data.length - 1) {
        array += ", ";
      }
    }
    return array;
  }

  public String toString() {
    return Integer.toHexString(slot) + "    " + valid + "     " + tag + "       " + dataToString();
  }
}

class Cache {
  static short[] mainMemory = new short[2048];
  static Block[] blocks = new Block[16];

  static {
    for (short i = 0; i < blocks.length; i++) {
      blocks[i] = new Block(i);
    }
  }

  static {
    for (short i = 0, j = 0; i < mainMemory.length - 1; i++, j++) {
      if (j == (0xFF + 1)) {
        j = 0;
      }
      mainMemory[i] = j;
    }
  }

  private static Block get(int slot) {
    return blocks[slot];
  }

/*
  -----------------------------------
  |   Tag   | Slot # | Block Offset |
  -----------------------------------
  |  4 bits | 4 bits |   4 bits     |
  -----------------------------------
*/

  public static void read(int address) {
    boolean cacheHit = false;

    int blockOffset = address & 0xF;
    int blockBeginAddress = address & 0xFFF0;
    int tag = address >>> 8;
    int slotNum = (address & 0xF0) >>> 4;

    if (cacheHit) {

    } else {
      Block block = get(slotNum);
      short[] cacheData = Arrays.copyOfRange(mainMemory, blockBeginAddress, blockBeginAddress + 16);
      block.set(tag, cacheData);
      System.out.println("address: " + Integer.toHexString(address) + " offset: " + Integer.toHexString(blockOffset) + " begin address: " + Integer.toHexString(blockBeginAddress) + " tag: " + Integer.toHexString(tag) + " slotnum: " + Integer.toHexString(slotNum));
    }
  }

  public static void write(short address, short data) {

  }

  public static void displayCache() {
    System.out.println("Slot Valid Tag     Data");
    for (short i = 0; i < blocks.length; i++) {
      System.out.println(blocks[i].toString());
    }
  }

  public static void main(String[] args) {
    // System.out.println(mainMemory[0]);
    // System.out.println(mainMemory[0xFF]);
    // System.out.println(mainMemory[0x100]);
    read(0x7ae);
    displayCache();
  }
}
