/*
 *  Copyright (c) 2012 Jan Kotek
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.mapdb;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Wraps {@link ByteBuffer} and provides {@link DataInput}
 *
 * @author Jan Kotek
 */
public final class DataInput2 extends InputStream implements DataInput {

    public ByteBuffer buf;
    public int pos;

    public DataInput2(final ByteBuffer buf, final int pos) {
        this.buf = buf;
        this.pos = pos;
    }

    public DataInput2(byte[] b) {
        this(ByteBuffer.wrap(b),0);
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        ByteBuffer clone = buf.duplicate();
        clone.position(pos);
        pos+=len;
        clone.get(b,off,len);
    }

    @Override
    public int skipBytes(final int n) throws IOException {
        pos +=n;
        return n;
    }

    @Override
    public boolean readBoolean() throws IOException {
        return buf.get(pos++) ==1;
    }

    @Override
    public byte readByte() throws IOException {
        return buf.get(pos++);
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return buf.get(pos++)& 0xff;
    }

    @Override
    public short readShort() throws IOException {
        final short ret = buf.getShort(pos);
        pos+=2;
        return ret;
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return (( (buf.get(pos++) & 0xff) << 8) |
                ( (buf.get(pos++) & 0xff)));
    }

    @Override
    public char readChar() throws IOException {
        return (char) readInt();
    }

    @Override
    public int readInt() throws IOException {
        final int ret = buf.getInt(pos);
        pos+=4;
        return ret;
    }

    @Override
    public long readLong() throws IOException {
        final long ret = buf.getLong(pos);
        pos+=8;
        return ret;
    }

    @Override
    public float readFloat() throws IOException {
        final float ret = buf.getFloat(pos);
        pos+=4;
        return ret;
    }

    @Override
    public double readDouble() throws IOException {
        final double ret = buf.getDouble(pos);
        pos+=8;
        return ret;
    }

    @Override
    public String readLine() throws IOException {
        return readUTF();
    }

    @Override
    public String readUTF() throws IOException {
        final int size = DataInput2.unpackInt(this);
        return SerializerBase.deserializeString(this, size);
    }

    @Override
    public int read() throws IOException {
        return readUnsignedByte();
    }



    /* unpackInt and unpackLong originally come from Kryo framework and were written by Nathan Sweet.
     * It was modified to fit MapDB purposes.
     * It is relicensed from BSD to Apache 2 with his permission:
     *
     * Date: 27.5.2014 12:44
     *
     *   Hi Jan,
     *
     *   I'm fine with you putting code from the Kryo under Apache 2.0, as long as you keep the copyright and author. :)
     *
     *   Cheers!
     *   -Nate
     *
     * -----------------------------
     *
     *  Copyright (c) 2012 Nathan Sweet
     *
     *  Licensed under the Apache License, Version 2.0 (the "License");
     *  you may not use this file except in compliance with the License.
     *  You may obtain a copy of the License at
     *
     *    http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */

    /**
     * Unpack positive int value from the input stream.
     *
     * This method originally comes from Kryo Framework, author Nathan Sweet.
     * It was modified to fit MapDB needs.
     *
     * @param is The input stream.
     * @return The long value.
     * @throws IOException
     */
    static public int unpackInt(DataInput is) throws IOException {
        //TODO unrolled version?
        for (int offset = 0, result = 0; offset < 32; offset += 7) {
            int b = is.readUnsignedByte();
            result |= (b & 0x7F) << offset;
            if ((b & 0x80) == 0) {
                return result;
            }
        }
        throw new AssertionError("Malformed int.");
    }


    /**
     * Unpack positive long value from the input stream.
     *
     * This method originally comes from Kryo Framework, author Nathan Sweet.
     * It was modified to fit MapDB needs.
     *
     * @param in The input stream.
     * @return The long value.
     * @throws IOException
     */
    static public long unpackLong(DataInput in) throws IOException {
        //TODO unrolled version?
        long result = 0;
        for (int offset = 0; offset < 64; offset += 7) {
            long b = in.readUnsignedByte();
            result |= (b & 0x7F) << offset;
            if ((b & 0x80) == 0) {
                return result;
            }
        }
        throw new AssertionError("Malformed long.");
    }

}
