import java.util.Arrays;
import java.util.Collections;

/*
  -----------------------------------
  |   Tag   | Slot # | Block Offset |
  -----------------------------------
  |  28 bits | 4 bits |    4 bits   |
  -----------------------------------
*/

class Address {
  public int raw;
  public int offset;
  public int beginAddress;
  public int tag;
  public int slot;

  public Address(int address) {
    this.raw = address;
    this.offset = address & 0xF;
    this.beginAddress = address & 0xFFF0;
    this.tag = address >>> 8;
    this.slot = (address & 0xF0) >>> 4;
  }
}

class Block {
  short slot;
  int dirty;
  int valid;
  int tag;
  short[] data = new short[16];

  public Block(short slotNum) {
    this.slot = slotNum;
    Arrays.fill(this.data, (short) 0);
  }

  public void loadMemory(Address address, short[] cacheData) {
    this.tag = address.tag;
    this.valid = 1;
    for (short i = 0; i < cacheData.length; i++) {
      this.data[i] = cacheData[i];
    }
  }

  public void update(int offset, short data) {
    this.data[offset] = data;
  }

  public String dataToString() {
    String array = "";
    for (short i = 0; i < this.data.length; i++) {
      if (Integer.toHexString(this.data[i]).length() != 2) {
        array += " " + Integer.toHexString(this.data[i]);
      } else {
        array += Integer.toHexString(this.data[i]);
      }
      if (i != this.data.length - 1) {
        array += ", ";
      }
    }
    return array;
  }

  public String toString() {
    return Integer.toHexString(this.slot) + "    " + this.valid + "     " + this.tag + "       " + this.dataToString();
  }
}

class Cache {
  protected static short[] mainMemory = new short[2048];
  protected static Block[] blocks = new Block[16];

  public static boolean test = false;

  static {
    for (short i = 0; i < blocks.length; i++) {
      blocks[i] = new Block(i);
    }
  }

  static {
    for (short i = 0, j = 0; i < mainMemory.length; i++, j++) {
      if (j == (0xFF + 1)) {
        j = 0;
      }
      mainMemory[i] = j;
    }
  }

  private static String mainMemoryToString(int beginAddress, int endAddress) {
    short[] array = Arrays.copyOfRange(mainMemory, beginAddress, endAddress);
    String output = "";
    for (int i = 0; i < array.length; i++) {
      output += Integer.toHexString(array[i]);
      if (i != array.length - 1) {
        output += ", ";
      }
    }
    return output;
  }

  protected static Block getBlock(int slot) {
    return blocks[slot];
  }

  protected static short[] getRow(int beginAddress) {
    return Arrays.copyOfRange(mainMemory, beginAddress, beginAddress + 16);
  }

  protected static boolean isCacheHit(Address address) {
    Block block = getBlock(address.slot);
    if (block.valid == 1 && block.tag == address.tag) {
      return true;
    }
    return false;
  }

  protected static boolean isDirty(Address address) {
    Block block = getBlock(address.slot);
    if (block.dirty == 1) {
      return true;
    }
    return false;
  }

  private static void writeBack(Address address) {
    Block block = getBlock(address.slot);
    Address writeBackAddress = new Address((block.tag << 8) + (block.slot << 4));
    for (short i = 0; i < block.data.length; i++) {
      if (mainMemory[writeBackAddress.beginAddress + i] != block.data[i]) {
        // System.out.println("\nUpdating address " + Integer.toHexString(address.beginAddress + i) + " new val: " + Integer.toHexString(block.data[i]) + ", old val: " + Integer.toHexString(mainMemory[address.beginAddress + i]));
        mainMemory[writeBackAddress.beginAddress + i] = block.data[i];
      }
    }
  }

  private static Address parseAddress(int rawAddress) {
    Address address = new Address(rawAddress);
    if (!test) {
      System.out.println(Integer.toHexString(address.raw));
    }
    // System.out.println("address: " + Integer.toHexString(address.raw) + " offset: " + Integer.toHexString(address.offset) + " begin address: " + Integer.toHexString(address.beginAddress) + " tag: " + Integer.toHexString(address.tag) + " slotnum: " + Integer.toHexString(address.slot));
    return address;
  }

  public static String read(int addressRaw) {
    Address address = parseAddress(addressRaw);
    Block block = getBlock(address.slot);
    String storedVal;
    String message;
    if (isCacheHit(address)) {
      storedVal = Integer.toHexString(block.data[address.offset]);
      message = "At that byte there is the value " + storedVal + " (Cache Hit)";
    } else {
      if (isDirty(address)) { // write cache to memory before it is bumped out
        writeBack(address);
      }
      block.loadMemory(address, getRow(address.beginAddress));
      storedVal = Integer.toHexString(mainMemory[address.raw]);
      message = "At that byte there is the value " + storedVal + " (Cache Miss)";
    }
    if (!test) {
      System.out.println(message);
    }
    return message;
  }

  public static String write(int addressRaw, short data) {
    Address address = parseAddress(addressRaw);
    if (!test) {
      System.out.println(data);
    }
    Block block = getBlock(address.slot);
    String message;
    block.dirty = 1;
    if (isCacheHit(address)) {
      block.update(address.offset, data);
      message = "Value " + data + " has been written to address " + Integer.toHexString(address.raw) + " (Cache Hit)";
    } else {
      if (isDirty(address)) {
        writeBack(address);
      }
      block.loadMemory(address, getRow(address.beginAddress));
      block.update(address.offset, data);
      message = "Value " + data + " has been written to address " + Integer.toHexString(address.raw) + " (Cache Miss)";
    }
    if (!test) {
      System.out.println(message);
    }
    return message;
  }

  public static void displayCache() {
    System.out.println("Slot Valid Tag     Data");
    for (short i = 0; i < blocks.length; i++) {
      System.out.println(blocks[i].toString());
    }
  }
}
