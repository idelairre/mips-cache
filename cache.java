import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

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
  static short[] mainMemory = new short[2048];
  static Block[] blocks = new Block[16];

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

  private static int decodeHex(String input) {
    if (!input.contains("0x")) {
      input = "0x" + input;
    }
    return Integer.decode(input);
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

  private static Block getBlock(int slot) {
    return blocks[slot];
  }

  private static short[] getRow(int beginAddress) {
    return Arrays.copyOfRange(mainMemory, beginAddress, beginAddress + 16);
  }

  private static void runTests() {
    Block block = getBlock(0xa);
    block.loadMemory(new Address(0x7a0), getRow(0x7a0));
    read(parseAddress(0x7ae));
    read(parseAddress(0x2e));
    read(parseAddress(0x2f));
    read(parseAddress(0x3d5));
    displayCache();
  }

  private static void run() {
    read(parseAddress(0x5));
    read(parseAddress(0x6));
    read(parseAddress(0x7));
    read(parseAddress(0x14c));
    read(parseAddress(0x14d));
    read(parseAddress(0x14e));
    read(parseAddress(0x14f));
    read(parseAddress(0x150));
    read(parseAddress(0x151));
    read(parseAddress(0x3a6));
    read(parseAddress(0x4c3));
    displayCache();
    write(parseAddress(0x14c), (short) 99);
    write(parseAddress(0x63b), (short) 7);
    read(parseAddress(0x582));
    read(parseAddress(0x348));
    read(parseAddress(0x3F));
    displayCache();
    read(parseAddress(0x14b));
    read(parseAddress(0x14c));
    read(parseAddress(0x63f));
    read(parseAddress(0x83));
    displayCache();
  }

  private static boolean isCacheHit(Address address) {
    Block block = getBlock(address.slot);
    if (block.valid == 1 && block.tag == address.tag) {
      return true;
    }
    return false;
  }

  private static boolean isDirty(Address address) {
    Block block = getBlock(address.slot);
    // System.out.println("Conflict: " + (block.dirty == 1));
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
    System.out.println(Integer.toHexString(address.raw));
    // System.out.println("address: " + Integer.toHexString(address.raw) + " offset: " + Integer.toHexString(address.offset) + " begin address: " + Integer.toHexString(address.beginAddress) + " tag: " + Integer.toHexString(address.tag) + " slotnum: " + Integer.toHexString(address.slot));
    return address;
  }

  public static void read(Address address) {
    Block block = getBlock(address.slot);
    if (isCacheHit(address)) {
      String storedVal = Integer.toHexString(block.data[address.offset]);
      System.out.println("At that byte there is the value " + storedVal + " (Cache Hit)");
    } else {
      if (isDirty(address)) { // write cache to memory before it is bumped out
        writeBack(address);
      }
      block.loadMemory(address, getRow(address.beginAddress));
      String storedVal = Integer.toHexString(mainMemory[address.raw]);
      System.out.println("At that byte there is the value " + storedVal + " (Cache Miss)");
    }
  }

  public static void write(Address address, short data) {
    System.out.println(data);
    Block block = getBlock(address.slot);
    block.dirty = 1;
    if (isCacheHit(address)) {
      block.update(address.offset, data);
      System.out.println("Value " + data + " has been written to address " + Integer.toHexString(address.raw) + " (Cache Hit)");
    } else {
      if (isDirty(address)) {
        writeBack(address);
      }
      block.loadMemory(address, getRow(address.beginAddress));
      block.update(address.offset, data);
      System.out.println("Value " + data + " has been written to address " + Integer.toHexString(address.raw) + " (Cache Miss)");
    }
  }

  public static void prompt() {
    Scanner scanner = new Scanner(System.in);
    System.out.println("(R)ead, (W)rite, or (D)isplay Cache?");
    String option = scanner.next();
    if (option.equals("R")) {
      String input = scanner.next();
      int address = Integer.decode(input);
      read(parseAddress(address));
    } else if (option.equals("W")) {
      int address = decodeHex(scanner.next());
      int data = scanner.nextInt();
      write(parseAddress(address), (short) data);
    } else if (option.equals("D")) {
      displayCache();
    } else {
      System.out.println("Invalid input");
    }
    prompt();
  }

  public static void displayCache() {
    System.out.println("Slot Valid Tag     Data");
    for (short i = 0; i < blocks.length; i++) {
      System.out.println(blocks[i].toString());
    }
  }

  public static void main(String[] args) {
    // prompt();
    run();
  }
}
