package org.erachain.at;

public interface ATAPI {


    // range 0x0100..0x01ff

    /**
     * sets @addr to A1 (0x0100)
     */
    public long get_A1(ATMachineState state);

    /**
     * sets @addr to A2 (0x0101)
     */
    public long get_A2(ATMachineState state);

    /**
     * sets @addr to A3 (0x0102)
     */
    public long get_A3(ATMachineState state);

    /**
     * sets @addr to A4 (0x0103)
     */
    public long get_A4(ATMachineState state);

    /**
     * sets @addr to B1 (0x0104)
     */
    public long get_B1(ATMachineState state);

    /**
     * sets @addr to B2 (0x0105)
     */
    public long get_B2(ATMachineState state);

    /**
     * sets @addr to B3 (0x0106)
     */
    public long get_B3(ATMachineState state);

    /**
     * sets @addr to B4 (0x0107)
     */
    public long get_B4(ATMachineState state);

    /**
     * sets A1 to @addr (0x0110)
     */
    public void set_A1(long val, ATMachineState state);

    /**
     * sets A2 to @addr (0x0111)
     */
    public void set_A2(long val, ATMachineState state);

    /**
     * sets A3 to @addr (0x0112)
     */
    public void set_A3(long val, ATMachineState state);

    /**
     * sets A4 to @addr (0x0113)
     */
    public void set_A4(long val, ATMachineState state);

    /**
     * sets A1 from @addr1 and A2 from @addr2 (0x0114)
     */
    public void set_A1_A2(long val1, long val2, ATMachineState state);

    /**
     * sets A3 from @addr1 and A4 from @addr2 ((0x0115)
     */
    public void set_A3_A4(long val1, long val2, ATMachineState state);

    /**
     * sets B1 from @addr (0x0116)
     */
    public void set_B1(long val, ATMachineState state);

    /**
     * sets B2 from @addr (0x0117)
     */
    public void set_B2(long val, ATMachineState state);

    /**
     * sets B3 from @addr (0x0118)
     */
    public void set_B3(long val, ATMachineState state);

    /**
     * sets B4 @addr (0x0119)
     */
    public void set_B4(long val, ATMachineState state);

    /**
     * sets B1 from @addr1 and B2 from @addr2 (0x011a)
     */
    public void set_B1_B2(long val1, long val2, ATMachineState state);

    /**
     * sets B3 from @addr3 and @addr4 to B4 (0x011b)
     */
    public void set_B3_B4(long val3, long val4, ATMachineState state);

    /**
     * sets A to zero (A being A1...4)
     */
    public void clear_A(ATMachineState state);

    /**
     * sets B to zero (B being B1...4)
     */
    public void clear_B(ATMachineState state);

    /**
     * gets A from B
     */
    public void copy_A_From_B(ATMachineState state);

    /**
     * gets B from A
     */
    public void copy_B_From_A(ATMachineState state);

    /**
     * bool is A is zero
     */
    public long check_A_Is_Zero(ATMachineState state);

    /**
     * bool is B is zero
     */
    public long check_B_Is_Zero(ATMachineState state);


    public long check_A_equals_B(ATMachineState state);

    /**
     * swap the values of A and B
     */
    public void swap_A_and_B(ATMachineState state);

    // template: these 8 math ops are intended for a future implementaion so no need to support them

    /**
     * adds A to B (result in B)
     */
    public void add_A_to_B(ATMachineState state);

    /**
     * add B to A (result in A)
     */
    public void add_B_to_A(ATMachineState state);

    /**
     * subs A from B (result in B)
     */
    public void sub_A_from_B(ATMachineState state);

    /**
     * subs B from A (result in A)
     */
    public void sub_B_from_A(ATMachineState state);

    /**
     * multiplies A by B (result in B)
     */
    public void mul_A_by_B(ATMachineState state);

    /**
     * multiplies B by A (result in A)
     */
    public void mul_B_by_A(ATMachineState state);

    /**
     * divides A by B (result in B) *can cause a divide by zero error which would stop the machine
     */
    public void div_A_by_B(ATMachineState state);

    /**
     * divides B by A (result in A) *can cause a divide by zero error which would stop the machine
     */
    public void div_B_by_A(ATMachineState state);

    /**
     * ors A by B (result in A)
     */
    public void or_A_with_B(ATMachineState state);

    /**
     * ors B by A (result in B)
     */
    public void or_B_with_A(ATMachineState state);

    /**
     * ands A by B (result in A)
     */
    public void and_A_with_B(ATMachineState state);

    /**
     * ands B by A (result in B)
     */
    public void and_B_with_A(ATMachineState state);

    /**
     * xors A by B (result in A)
     */
    public void xor_A_with_B(ATMachineState state);

    /**
     * xors B by A (result in B)
     */
    public void xor_B_with_A(ATMachineState state);

    // end template
    // end range 0x0100..0x01ff


    // -----------------------
    // range 0x0200..0x02ff

    /**
     * sets @addr1 and @addr2 to the MD5 hash of A1..4
     */
    public void MD5_A_to_B(ATMachineState state);

    /**
     * bool if @addr1 and @addr2 matches the MD5 hash of A1..4
     */
    public long check_MD5_A_with_B(ATMachineState state);

    /**
     * take a RIPEMD160 hash of A1..4 and put this in B1..4
     */
    public void HASH160_A_to_B(ATMachineState state);

    /**
     * bool if RIPEMD160 hash of A1..4 matches B1..4
     */
    public long check_HASH160_A_with_B(ATMachineState state);

    /**
     * take a SHA256 hash of A1..4 abd out this in B1..4
     */
    public void SHA256_A_to_B(ATMachineState state);

    /**
     * bool if SHA256 of A1..4 matches B1..4
     */
    public long check_SHA256_A_with_B(ATMachineState state);

    // end of range 0x02..0x02ff
    // -------------------------

    // -------------------------
    // range 0x03..0x03ff

    /**
     * sets @addr to the timestamp of the current block
     */
    public long get_Block_Timestamp(ATMachineState state);

    /**
     * sets @addr to the timestamp of the AT creation block
     */
    public long get_Creation_Timestamp(ATMachineState state);


    /**
     * sets @addr to the timestamp of the previous block
     */
    public long get_Last_Block_Timestamp(ATMachineState state);

    /**
     * puts the block hash of the previous block in A
     */
    public void put_Last_Block_Hash_In_A(ATMachineState state);

    /**
     * sets A to zero/tx hash of the first tx after
     */
    public void A_to_Tx_after_Timestamp(long val, ATMachineState state);

    /**
     * bool if A is a valid tx with @addr to tx type
     * 0 -> normal tx
     * 1 -> message tx
     */
    public long get_Type_for_Tx_in_A(ATMachineState state);

    /**
     * bool if A is a valid tx with @addr to tx amount
     */
    public long get_Amount_for_Tx_in_A(ATMachineState state);

    /**
     * bool if A is a valid tx with @addr to the tx timestamp
     */
    public long get_Timestamp_for_Tx_in_A(ATMachineState state);

    /**
     * bool if A is a valid tx with @addr to the tx random id
     * random id is a 64bit signed value (always positive) and this is a blocking function
     */
    public long get_Random_Id_for_Tx_in_A(ATMachineState state);

    /**
     * bool if A is a valid tx with B to the tx message
     * if a tx is not a message tx then this will zero out the B value
     */
    public void message_from_Tx_in_A_to_B(ATMachineState state);

    /**
     * bool if A is a valid tx with B set to the tx address
     */
    public void B_to_Address_of_Tx_in_A(ATMachineState state);

    /**
     * set B to the address of the AT's creator
     */
    public void B_to_Address_of_Creator(ATMachineState state);

    // end range 0x0300..0x03ff
    // ------------------------

    // ------------------------
    // range 0x0400..0x04ff

    /**
     * sets @addr to current balance of the AT
     */
    public long get_Current_Balance(ATMachineState state);

    /**
     * sets @addr to the balance it had last had when running
     * this amount does not include any additional amounts sent to the
     * AT between "execution events"
     */
    public long get_Previous_Balance(ATMachineState state);

    /**
     * bool if B is a valid address then send it $addr amount
     * if this amount is greater than the AT's balance then it will also
     * return false
     *
     * @param key TODO
     */
    public void send_to_Address_in_B(long key, long val, ATMachineState state);

    /**
     * bool if B is a valid address then send it entire balance
     *
     * @param key TODO
     */
    public void send_All_to_Address_in_B(long key, ATMachineState state);

    /**
     * bool if B is a valid address then send it the old balance
     */
    public void send_Old_to_Address_in_B(ATMachineState state);

    /**
     * bool if B is valid address then send it A as a message
     */
    public void send_A_to_Address_in_B(ATMachineState state);

    /**
     * $addr1 is timestamp calculated from $addr2
     */
    public long add_Minutes_to_Timestamp(long val1, long val2, ATMachineState state);

    /**
     * set min amount of balance increase needed to unfreeze
     */
    public void set_Min_Activation_Amount(long val, ATMachineState state);

    // end range 0x0400.0x04ff
    // -----------------------

    /**
     * puts the gensig of the previous block in A
     */
    public void put_Last_Block_Generation_Signature_In_A(ATMachineState state);

    /**
     * take a SHA256 hash of val2 bytes starting at val1. out this in B1..4
     */
    public void SHA256_to_B(long val1, long val2, ATMachineState state);

    // range 0x0500.0x05ff
    // -------------------

    /**
     * @param val
     * @param state
     */
    public void A_to_Tx_at_Timestamp(long val, ATMachineState state);

    // end range 0x0500.0x05ff
    // -----------------------
}
