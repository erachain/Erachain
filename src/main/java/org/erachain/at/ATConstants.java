package org.erachain.at;


import java.util.HashMap;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ATConstants {


    //platform based
    public final static int AT_ID_SIZE = 25;
    public final static int NAME_MAX_LENGTH = 200;
    public final static int DESC_MAX_LENGTH = 2000;
    public static final int TYPE_MAX_LENGTH = 200;
    public static final int TAGS_MAX_LENGTH = 200;
    public static final int CREATION_BYTES_MAX_LENGTH = 100000;
    public static final int STATE_STORE_DISTANCE = 500;
    private final static NavigableMap<Integer, Short> AT_VERSION = new TreeMap<>();
    private final static HashMap<Short, Long> MIN_FEE = new HashMap<>();
    private final static HashMap<Short, Long> STEP_FEE = new HashMap<>();
    private final static HashMap<Short, Long> MAX_STEPS = new HashMap<>();
    private final static HashMap<Short, Long> API_STEP_MULTIPLIER = new HashMap<>();
    private final static HashMap<Short, Long> COST_PER_PAGE = new HashMap<>();
    private final static HashMap<Short, Long> MAX_WAIT_FOR_NUM_OF_BLOCKS = new HashMap<>();
    private final static HashMap<Short, Long> MAX_SLEEP_BETWEEN_BLOCKS = new HashMap<>();
    private final static HashMap<Short, Long> PAGE_SIZE = new HashMap<>();
    private final static HashMap<Short, Long> MAX_MACHINE_CODE_PAGES = new HashMap<>();
    private final static HashMap<Short, Long> MAX_MACHINE_DATA_PAGES = new HashMap<>();
    private final static HashMap<Short, Long> MAX_MACHINE_USER_STACK_PAGES = new HashMap<>();
    private final static HashMap<Short, Long> MAX_MACHINE_CALL_STACK_PAGES = new HashMap<>();
    private final static HashMap<Short, Long> BLOCKS_FOR_TICKET = new HashMap<>();
    private final static HashMap<Short, Integer> MAX_PAYLOAD_FOR_BLOCK = new HashMap<>();
    private final static HashMap<Short, Long> AVERAGE_BLOCK_MINUTES = new HashMap<>();
    private final static ATConstants instance = new ATConstants();


    private ATConstants() {

        //version 1
        AT_VERSION.put(0, (short) 1); //block 0 version 1
        // icreator version 3 - get ERROR - not start a generating blocks
        /// error - AT_VERSION.put( 2 , (short)3 ); //block 1 version 3

        //constants for AT version 1
        MIN_FEE.put((short) 1, 1L);
        STEP_FEE.put((short) 1, 10000000L);
        MAX_STEPS.put((short) 1, 500L);
        API_STEP_MULTIPLIER.put((short) 1, 10L);


        COST_PER_PAGE.put((short) 1, 1000000000L);

        MAX_WAIT_FOR_NUM_OF_BLOCKS.put((short) 1, 31536000L);
        MAX_SLEEP_BETWEEN_BLOCKS.put((short) 1, 31536000L);

        PAGE_SIZE.put((short) 1, 256L);

        MAX_MACHINE_CODE_PAGES.put((short) 1, 20L);
        MAX_MACHINE_DATA_PAGES.put((short) 1, 20L);
        MAX_MACHINE_USER_STACK_PAGES.put((short) 1, 20L);
        MAX_MACHINE_CALL_STACK_PAGES.put((short) 1, 20L);

        BLOCKS_FOR_TICKET.put((short) 1, 11L); //for testing 2 -> normally 1440
        MAX_PAYLOAD_FOR_BLOCK.put((short) 1, (800 * 24)); //use at max half size of the block.
        AVERAGE_BLOCK_MINUTES.put((short) 1, 4L);
        // end of AT version 1
    }

    public static ATConstants getInstance() {
        return instance;
    }

    public short AT_VERSION(int blockHeight) {

        if (AT_VERSION.floorEntry(blockHeight) == null) return 1;
        return AT_VERSION.floorEntry(blockHeight).getValue();
    }

    public long STEP_FEE(int height) {
        return STEP_FEE.get(AT_VERSION(height));
    }

    public long MAX_STEPS(int height) {
        return MAX_STEPS.get(AT_VERSION(height));
    }

    public long API_STEP_MULTIPLIER(int height) {
        return API_STEP_MULTIPLIER.get(AT_VERSION(height));
    }

    public long COST_PER_PAGE(int height) {
        return COST_PER_PAGE.get(AT_VERSION(height));
    }

    public long get_MAX_WAIT_FOR_NUM_OF_BLOCKS(int height) {
        return MAX_WAIT_FOR_NUM_OF_BLOCKS.get(AT_VERSION(height));
    }

    public long MAX_SLEEP_BETWEEN_BLOCKS(int height) {
        return MAX_SLEEP_BETWEEN_BLOCKS.get(AT_VERSION(height));
    }

    public long PAGE_SIZE(int height) {
        return PAGE_SIZE.get(AT_VERSION(height));
    }

    public long MAX_MACHINE_CODE_PAGES(int height) {
        return MAX_MACHINE_CODE_PAGES.get(AT_VERSION(height));
    }

    public long MAX_MACHINE_DATA_PAGES(int height) {
        return MAX_MACHINE_DATA_PAGES.get(AT_VERSION(height));
    }

    public long MAX_MACHINE_USER_STACK_PAGES(int height) {
        return MAX_MACHINE_USER_STACK_PAGES.get(AT_VERSION(height));
    }

    public long MAX_MACHINE_CALL_STACK_PAGES(int height) {
        return MAX_MACHINE_CALL_STACK_PAGES.get(AT_VERSION(height));
    }

    public long BLOCKS_FOR_TICKET(int height) {
        return BLOCKS_FOR_TICKET.get(AT_VERSION(height));
    }

    public int MAX_PAYLOAD_FOR_BLOCK(int height) {
        //if (MAX_PAYLOAD_FOR_BLOCK.isEmpty()) return 0;
        return MAX_PAYLOAD_FOR_BLOCK.get(AT_VERSION(height));
    }

    public long AVERAGE_BLOCK_MINUTES(int height) {
        return AVERAGE_BLOCK_MINUTES.get(AT_VERSION(height));
    }


}
