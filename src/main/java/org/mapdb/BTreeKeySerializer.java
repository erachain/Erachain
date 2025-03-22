package org.mapdb;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Comparator;

/**
 * Custom serializer for BTreeMap keys which enables [Delta encoding](https://en.wikipedia.org/wiki/Delta_encoding).
 *
 * Keys in BTree Nodes are sorted, this enables number of tricks to save disk space.
 * For example for numbers we may store only difference between subsequent numbers, for string we can only take suffix, etc...
 *
 * @param <K> type of key
 */
public abstract class BTreeKeySerializer<K>{

    /**
     * Serialize keys from single BTree Node.
     *
     * @param out output stream where to put ata
     * @param start where data start in array. Before this index all keys are null
     * @param end where data ends in array (exclusive). From this index all keys are null
     * @param keys array of keys for single BTree Node
     *
     * @throws IOException
     */
    public abstract void serialize(DataOutput out, int start, int end, Object[] keys) throws IOException;

    /**
     * Deserializes keys for single BTree Node. To
     *
     * @param in input stream to read data from
     * @param start where data start in array. Before this index all keys are null
     * @param end where data ends in array (exclusive). From this index all keys are null
     * @param size size of array which should be returned
     * @return array of keys for single BTree Node
     *
     * @throws IOException
     */
    public abstract Object[] deserialize(DataInput in, int start, int end, int size) throws IOException;



    /**
     * Some key serializers  may only work with they own comparators.
     * For example delta-packing stores increments between numbers and requires ascending order.
     * So Key Serializer may provide its own comparator.
     *
     * @return comparator which must be used with this method. `null` if comparator is not strictly required.
     */
    public abstract Comparator<K> getComparator();

    public static final BTreeKeySerializer BASIC = new BasicKeySerializer(Serializer.BASIC);

    /**
     * Basic Key Serializer which just writes data without applying any compression.
     * Is used by default if no other Key Serializer is specified.
     */
    public static final class BasicKeySerializer extends BTreeKeySerializer<Object> implements Serializable {

        private static final long serialVersionUID = 1654710710946309279L;

        protected final Serializer defaultSerializer;

        public BasicKeySerializer(Serializer defaultSerializer) {
            this.defaultSerializer = defaultSerializer;
        }

        /** used for deserialization*/
        protected BasicKeySerializer(SerializerBase serializerBase, DataInput is, SerializerBase.FastArrayList<Object> objectStack) throws IOException {
            objectStack.add(this);
            defaultSerializer = (Serializer) serializerBase.deserialize(is,objectStack);
        }

        @Override
        public void serialize(DataOutput out, int start, int end, Object[] keys) throws IOException {
            for(int i = start;i<end;i++){
                defaultSerializer.serialize(out,keys[i]);
            }
        }

        @Override
        public Object[] deserialize(DataInput in, int start, int end, int size) throws IOException{
            Object[] ret = new Object[size];
            for(int i=start; i<end; i++){
                ret[i] = defaultSerializer.deserialize(in,-1);
            }
            return ret;
        }

        @Override
        public Comparator<Object> getComparator() {
            return null;
        }
    }


    /**
     * Applies delta packing on {@code java.lang.Long}. All keys must be non negative.
     * Difference between consequential numbers is also packed itself, so for small diffs it takes only single byte per
     * number.
     */
    public static final  BTreeKeySerializer<Long> ZERO_OR_POSITIVE_LONG = new BTreeKeySerializer<Long>() {
        @Override
        public void serialize(DataOutput out, int start, int end, Object[] keys) throws IOException {
            if(start>=end) return;
            long prev = (Long)keys[start];
            DataOutput2.packLong(out,prev);
            for(int i=start+1;i<end;i++){
                long curr = (Long)keys[i];
                DataOutput2.packLong(out, curr-prev);
                prev = curr;
            }
        }

        @Override
        public Object[] deserialize(DataInput in, int start, int end, int size) throws IOException {
            Object[] ret = new Long[size];
            long prev = 0 ;
            for(int i = start; i<end; i++){
                ret[i] = prev = prev + DataInput2.unpackLong(in);
            }
            return ret;
        }

        @Override
        public Comparator<Long> getComparator() {
            return BTreeMap.COMPARABLE_COMPARATOR;
        }
    };

    /**
     * Applies delta packing on {@code java.lang.Integer}. All keys must be non negative.
     * Difference between consequential numbers is also packed itself, so for small diffs it takes only single byte per
     * number.
     */
    public static final  BTreeKeySerializer<Integer> ZERO_OR_POSITIVE_INT = new BTreeKeySerializer<Integer>() {
        @Override
        public void serialize(DataOutput out, int start, int end, Object[] keys) throws IOException {
            if(start>=end) return;
            int prev = (Integer)keys[start];
            DataOutput2.packLong(out,prev);
            for(int i=start+1;i<end;i++){
                int curr = (Integer)keys[i];
                DataOutput2.packInt(out, curr-prev);
                prev = curr;
            }
        }

        @Override
        public Object[] deserialize(DataInput in, int start, int end, int size) throws IOException {
            Object[] ret = new Integer[size];
            int prev = 0 ;
            for(int i = start; i<end; i++){
                ret[i] = prev = prev + DataInput2.unpackInt(in);
            }
            return ret;
        }

        @Override
        public Comparator<Integer> getComparator() {
            return BTreeMap.COMPARABLE_COMPARATOR;
        }

    };


    /**
     * Applies delta packing on {@code java.lang.String}. This serializer splits consequent strings
     * to two parts: shared prefix and different suffix. Only suffix is than stored.
     */
    public static final  BTreeKeySerializer<String> STRING = new BTreeKeySerializer<String>() {

        private final Charset UTF8_CHARSET = Charset.forName("UTF8");

        @Override
        public void serialize(DataOutput out, int start, int end, Object[] keys) throws IOException {
            byte[] previous = null;
            for (int i = start; i < end; i++) {
                byte[] b = ((String) keys[i]).getBytes(UTF8_CHARSET);
                leadingValuePackWrite(out, b, previous, 0);
                previous = b;
            }
        }

        @Override
        public Object[] deserialize(DataInput in, int start, int end, int size) throws IOException {
            Object[] ret = new Object[size];
            byte[] previous = null;
            for (int i = start; i < end; i++) {
                byte[] b = leadingValuePackRead(in, previous, 0);
                if (b == null) continue;
                ret[i] = new String(b,UTF8_CHARSET);
                previous = b;
            }
            return ret;
        }

        @Override
        public Comparator<String> getComparator() {
            return BTreeMap.COMPARABLE_COMPARATOR;
        }

    };

    /**
     * Read previously written data from {@code leadingValuePackWrite()} method.
     *
     * author: Kevin Day
     */
    public static byte[] leadingValuePackRead(DataInput in, byte[] previous, int ignoreLeadingCount) throws IOException {
        int len = DataInput2.unpackInt(in) - 1;  // 0 indicates null
        if (len == -1)
            return null;

        int actualCommon = DataInput2.unpackInt(in);

        byte[] buf = new byte[len];

        if (previous == null) {
            actualCommon = 0;
        }


        if (actualCommon > 0) {
            in.readFully(buf, 0, ignoreLeadingCount);
            System.arraycopy(previous, ignoreLeadingCount, buf, ignoreLeadingCount, actualCommon - ignoreLeadingCount);
        }
        in.readFully(buf, actualCommon, len - actualCommon);
        return buf;
    }

    /**
     * This method is used for delta compression for keys.
     * Writes the contents of buf to the DataOutput out, with special encoding if
     * there are common leading bytes in the previous group stored by this compressor.
     *
     * author: Kevin Day
     */
    public static void leadingValuePackWrite(DataOutput out, byte[] buf, byte[] previous, int ignoreLeadingCount) throws IOException {
        if (buf == null) {
            DataOutput2.packInt(out, 0);
            return;
        }

        int actualCommon = ignoreLeadingCount;

        if (previous != null) {
            int maxCommon = buf.length > previous.length ? previous.length : buf.length;

            if (maxCommon > Short.MAX_VALUE) maxCommon = Short.MAX_VALUE;

            for (; actualCommon < maxCommon; actualCommon++) {
                if (buf[actualCommon] != previous[actualCommon])
                    break;
            }
        }


        // there are enough common bytes to justify compression
        DataOutput2.packInt(out, buf.length + 1);// store as +1, 0 indicates null
        DataOutput2.packInt(out, actualCommon);
        out.write(buf, 0, ignoreLeadingCount);
        out.write(buf, actualCommon, buf.length - actualCommon);

    }

    /**
     * Tuple2 Serializer which uses Default Serializer from DB and expect values to implement {@code Comparable} interface.
     */
    public static final Tuple2KeySerializer TUPLE2 = new Tuple2KeySerializer(null, null, null);

    /**
     * Applies delta compression on array of tuple. First tuple value may be shared between consequentive tuples, so only
     * first occurrence is serialized. An example:
     *
     * <pre>
     *     Value            Serialized as
     *     -------------------------
     *     Tuple(1, 1)       1, 1
     *     Tuple(1, 2)          2
     *     Tuple(1, 3)          3
     *     Tuple(1, 4)          4
     * </pre>
     *
     * @param <A> first tuple value
     * @param <B> second tuple value
     */
    public final  static class Tuple2KeySerializer<A,B> extends  BTreeKeySerializer<Fun.Tuple2<A,B>> implements Serializable {

        private static final long serialVersionUID = 2183804367032891772L;
        protected final Comparator<A> aComparator;
        protected final Serializer<A> aSerializer;
        protected final Serializer<B> bSerializer;

        /**
         * Construct new Tuple2 Key Serializer. You may pass null for some value,
         * In that case 'default' value will be used, Comparable comparator and Default Serializer from DB.
         *
         * @param aComparator comparator used for first tuple value
         * @param aSerializer serializer used for first tuple value
         * @param bSerializer serializer used for second tuple value
         */
        public Tuple2KeySerializer(Comparator<A> aComparator,Serializer<A> aSerializer, Serializer<B> bSerializer){
            this.aComparator = aComparator;
            this.aSerializer = aSerializer;
            this.bSerializer = bSerializer;
        }

        /** used for deserialization, `extra` is to avoid argument collision */
        Tuple2KeySerializer(SerializerBase serializerBase, DataInput is, SerializerBase.FastArrayList<Object> objectStack, int extra) throws IOException {
            objectStack.add(this);
            aComparator = (Comparator<A>) serializerBase.deserialize(is,objectStack);
            aSerializer = (Serializer<A>) serializerBase.deserialize(is,objectStack);
            bSerializer = (Serializer<B>) serializerBase.deserialize(is,objectStack);
        }

        @Override
        public void serialize(DataOutput out, int start, int end, Object[] keys) throws IOException {
            int acount=0;
            for(int i=start;i<end;i++){
                Fun.Tuple2<A,B> t = (Fun.Tuple2<A, B>) keys[i];
                if(acount==0){
                    //write new A
                    aSerializer.serialize(out,t.a);
                    //count how many A are following
                    acount=1;
                    while(i+acount<end && aComparator.compare(t.a, ((Fun.Tuple2<A, B>) keys[i+acount]).a)==0){
                        acount++;
                    }
                    DataOutput2.packInt(out,acount);
                }
                bSerializer.serialize(out,t.b);

                acount--;
            }
        }

        @Override
        public Object[] deserialize(DataInput in, int start, int end, int size) throws IOException {
            Object[] ret = new Object[size];
            A a = null;
            int acount = 0;

            for(int i=start;i<end;i++){
                if(acount==0){
                    //read new A
                    a = aSerializer.deserialize(in,-1);
                    acount = DataInput2.unpackInt(in);
                }
                B b = bSerializer.deserialize(in,-1);
                ret[i]= Fun.t2(a,b);
                acount--;
            }
            assert(acount==0);

            return ret;
        }

        @Override
        public Comparator<Fun.Tuple2<A,B>> getComparator() {
            return BTreeMap.COMPARABLE_COMPARATOR;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Tuple2KeySerializer t = (Tuple2KeySerializer) o;

            return
                    Fun.eq(aComparator, t.aComparator) &&
                    Fun.eq(aSerializer, t.aSerializer) &&
                    Fun.eq(bSerializer, t.bSerializer);
        }

        @Override
        public int hashCode() {
            int result = aComparator != null ? aComparator.hashCode() : 0;
            result = 31 * result + (aSerializer != null ? aSerializer.hashCode() : 0);
            result = 31 * result + (bSerializer != null ? bSerializer.hashCode() : 0);
            return result;
        }
    }

    /**
     * Tuple3 Serializer which uses Default Serializer from DB and expect values to implement {@code Comparable} interface.
     */
    public static final Tuple3KeySerializer TUPLE3 = new Tuple3KeySerializer(null, null, null, null, null);

    /**
     * Applies delta compression on array of tuple. First and second tuple value may be shared between consequentive tuples, so only
     * first occurrence is serialized. An example:
     *
     * <pre>
     *     Value            Serialized as
     *     ----------------------------
     *     Tuple(1, 2, 1)       1, 2, 1
     *     Tuple(1, 2, 2)             2
     *     Tuple(1, 3, 3)          3, 3
     *     Tuple(1, 3, 4)             4
     * </pre>
     *
     * @param <A> first tuple value
     * @param <B> second tuple value
     * @param <C> third tuple value
     */
    public static class Tuple3KeySerializer<A,B,C> extends  BTreeKeySerializer<Fun.Tuple3<A,B,C>> implements Serializable {

        private static final long serialVersionUID = 2932442956138713885L;
        protected final Comparator<A> aComparator;
        protected final Comparator<B> bComparator;
        protected final Serializer<A> aSerializer;
        protected final Serializer<B> bSerializer;
        protected final Serializer<C> cSerializer;

        /**
         * Construct new Tuple3 Key Serializer. You may pass null for some value,
         * In that case 'default' value will be used, Comparable comparator and Default Serializer from DB.
         *
         * @param aComparator comparator used for first tuple value
         * @param bComparator comparator used for second tuple value
         * @param aSerializer serializer used for first tuple value
         * @param bSerializer serializer used for second tuple value
         * @param cSerializer serializer used for third tuple value
         */
        public Tuple3KeySerializer(Comparator<A> aComparator, Comparator<B> bComparator,  Serializer<A> aSerializer,
                                   Serializer<B> bSerializer, Serializer<C> cSerializer){
            this.aComparator = aComparator;
            this.bComparator = bComparator;
            this.aSerializer = aSerializer;
            this.bSerializer = bSerializer;
            this.cSerializer = cSerializer;
        }

        /** used for deserialization */
        Tuple3KeySerializer(SerializerBase serializerBase, DataInput is, SerializerBase.FastArrayList<Object> objectStack) throws IOException {
            objectStack.add(this);
            aComparator = (Comparator<A>) serializerBase.deserialize(is,objectStack);
            bComparator = (Comparator<B>) serializerBase.deserialize(is,objectStack);
            aSerializer = (Serializer<A>) serializerBase.deserialize(is,objectStack);
            bSerializer = (Serializer<B>) serializerBase.deserialize(is,objectStack);
            cSerializer = (Serializer<C>) serializerBase.deserialize(is,objectStack);
        }


        @Override
        public void serialize(DataOutput out, int start, int end, Object[] keys) throws IOException {
            int acount=0;
            int bcount=0;
            for(int i=start;i<end;i++){
                Fun.Tuple3<A,B,C> t = (Fun.Tuple3<A, B,C>) keys[i];
                if(acount==0){
                    //write new A
                    aSerializer.serialize(out,t.a);
                    //count how many A are following
                    acount=1;
                    while(i+acount<end && aComparator.compare(t.a, ((Fun.Tuple3<A, B, C>) keys[i+acount]).a)==0){
                        acount++;
                    }
                    DataOutput2.packInt(out,acount);
                }
                if(bcount==0){
                    //write new B
                    bSerializer.serialize(out,t.b);
                    //count how many B are following
                    bcount=1;
                    while(i+bcount<end && bComparator.compare(t.b, ((Fun.Tuple3<A, B,C>) keys[i+bcount]).b)==0){
                        bcount++;
                    }
                    DataOutput2.packInt(out,bcount);
                }


                cSerializer.serialize(out,t.c);

                acount--;
                bcount--;
            }


        }

        @Override
        public Object[] deserialize(DataInput in, int start, int end, int size) throws IOException {
            Object[] ret = new Object[size];
            A a = null;
            int acount = 0;
            B b = null;
            int bcount = 0;

            for(int i=start;i<end;i++){
                if(acount==0){
                    //read new A
                    a = aSerializer.deserialize(in,-1);
                    acount = DataInput2.unpackInt(in);
                }
                if(bcount==0){
                    //read new B
                    b = bSerializer.deserialize(in,-1);
                    bcount = DataInput2.unpackInt(in);
                }
                C c = cSerializer.deserialize(in,-1);
                ret[i]= Fun.t3(a, b, c);
                acount--;
                bcount--;
            }
            assert(acount==0);
            assert(bcount==0);

            return ret;
        }

        @Override
        public Comparator<Fun.Tuple3<A,B,C>> getComparator() {
            return BTreeMap.COMPARABLE_COMPARATOR;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Tuple3KeySerializer t = (Tuple3KeySerializer) o;

            return
                    Fun.eq(aComparator, t.aComparator) &&
                    Fun.eq(bComparator, t.bComparator) &&
                    Fun.eq(aSerializer, t.aSerializer) &&
                    Fun.eq(bSerializer, t.bSerializer) &&
                    Fun.eq(cSerializer, t.cSerializer);
        }

        @Override
        public int hashCode() {
            int result = aComparator != null ? aComparator.hashCode() : 0;
            result = 31 * result + (bComparator != null ? bComparator.hashCode() : 0);
            result = 31 * result + (aSerializer != null ? aSerializer.hashCode() : 0);
            result = 31 * result + (bSerializer != null ? bSerializer.hashCode() : 0);
            result = 31 * result + (cSerializer != null ? cSerializer.hashCode() : 0);
            return result;
        }
    }

    /**
     * Tuple4 Serializer which uses Default Serializer from DB and expect values to implement {@code Comparable} interface.
     */
    public static final Tuple4KeySerializer TUPLE4 = new Tuple4KeySerializer(null, null, null, null, null, null, null);


    /**
     * Applies delta compression on array of tuple. First, second and third tuple value may be shared between consequential tuples,
     * so only first occurrence is serialized. An example:
     *
     * <pre>
     *     Value                Serialized as
     *     ----------------------------------
     *     Tuple(1, 2, 1, 1)       1, 2, 1, 1
     *     Tuple(1, 2, 1, 2)                2
     *     Tuple(1, 3, 3, 3)          3, 3, 3
     *     Tuple(1, 3, 4, 4)             4, 4
     * </pre>
     *
     * @param <A> first tuple value
     * @param <B> second tuple value
     * @param <C> third tuple value
     */
    public static class Tuple4KeySerializer<A,B,C,D> extends  BTreeKeySerializer<Fun.Tuple4<A,B,C,D>> implements Serializable {

        private static final long serialVersionUID = -1835761249723528530L;
        protected final Comparator<A> aComparator;
        protected final Comparator<B> bComparator;
        protected final Comparator<C> cComparator;
        protected final Serializer<A> aSerializer;
        protected final Serializer<B> bSerializer;
        protected final Serializer<C> cSerializer;
        protected final Serializer<D> dSerializer;

        /**
         * Construct new Tuple4 Key Serializer. You may pass null for some value,
         * In that case 'default' value will be used, Comparable comparator and Default Serializer from DB.
         *
         * @param aComparator comparator used for first tuple value
         * @param bComparator comparator used for second tuple value
         * @param cComparator comparator used for third tuple value*
         * @param aSerializer serializer used for first tuple value
         * @param bSerializer serializer used for second tuple value
         * @param cSerializer serializer used for third tuple value
         * @param dSerializer serializer used for fourth tuple value
         */
        public Tuple4KeySerializer(Comparator<A> aComparator, Comparator<B> bComparator, Comparator<C> cComparator,
                                   Serializer<A> aSerializer, Serializer<B> bSerializer, Serializer<C> cSerializer, Serializer<D> dSerializer){
            this.aComparator = aComparator;
            this.bComparator = bComparator;
            this.cComparator = cComparator;
            this.aSerializer = aSerializer;
            this.bSerializer = bSerializer;
            this.cSerializer = cSerializer;
            this.dSerializer = dSerializer;
        }

        /** used for deserialization */
        Tuple4KeySerializer(SerializerBase serializerBase, DataInput is, SerializerBase.FastArrayList<Object> objectStack) throws IOException {
            objectStack.add(this);
            aComparator = (Comparator<A>) serializerBase.deserialize(is,objectStack);
            bComparator = (Comparator<B>) serializerBase.deserialize(is,objectStack);
            cComparator = (Comparator<C>) serializerBase.deserialize(is,objectStack);
            aSerializer = (Serializer<A>) serializerBase.deserialize(is,objectStack);
            bSerializer = (Serializer<B>) serializerBase.deserialize(is,objectStack);
            cSerializer = (Serializer<C>) serializerBase.deserialize(is,objectStack);
            dSerializer = (Serializer<D>) serializerBase.deserialize(is,objectStack);
        }


        @Override
        public void serialize(DataOutput out, int start, int end, Object[] keys) throws IOException {
            int acount=0;
            int bcount=0;
            int ccount=0;
            for(int i=start;i<end;i++){
                Fun.Tuple4<A,B,C,D> t = (Fun.Tuple4<A, B,C,D>) keys[i];
                if(acount==0){
                    //write new A
                    aSerializer.serialize(out,t.a);
                    //count how many A are following
                    acount=1;
                    while(i+acount<end && aComparator.compare(t.a, ((Fun.Tuple4<A, B, C,D>) keys[i+acount]).a)==0){
                        acount++;
                    }
                    DataOutput2.packInt(out,acount);
                }
                if(bcount==0){
                    //write new B
                    bSerializer.serialize(out,t.b);
                    //count how many B are following
                    bcount=1;
                    while(i+bcount<end && bComparator.compare(t.b, ((Fun.Tuple4<A, B,C,D>) keys[i+bcount]).b)==0){
                        bcount++;
                    }
                    DataOutput2.packInt(out,bcount);
                }
                if(ccount==0){
                    //write new C
                    cSerializer.serialize(out,t.c);
                    //count how many C are following
                    ccount=1;
                    while(i+ccount<end && cComparator.compare(t.c, ((Fun.Tuple4<A, B,C,D>) keys[i+ccount]).c)==0){
                        ccount++;
                    }
                    DataOutput2.packInt(out,ccount);
                }


                dSerializer.serialize(out,t.d);

                acount--;
                bcount--;
                ccount--;
            }
        }

        @Override
        public Object[] deserialize(DataInput in, int start, int end, int size) throws IOException {
            Object[] ret = new Object[size];
            A a = null;
            int acount = 0;
            B b = null;
            int bcount = 0;
            C c = null;
            int ccount = 0;


            for(int i=start;i<end;i++){
                if(acount==0){
                    //read new A
                    a = aSerializer.deserialize(in,-1);
                    acount = DataInput2.unpackInt(in);
                }
                if(bcount==0){
                    //read new B
                    b = bSerializer.deserialize(in,-1);
                    bcount = DataInput2.unpackInt(in);
                }
                if(ccount==0){
                    //read new C
                    c = cSerializer.deserialize(in,-1);
                    ccount = DataInput2.unpackInt(in);
                }

                D d = dSerializer.deserialize(in,-1);
                ret[i]= Fun.t4(a, b, c, d);
                acount--;
                bcount--;
                ccount--;
            }
            assert(acount==0);
            assert(bcount==0);
            assert(ccount==0);

            return ret;
        }

        @Override
        public Comparator<Fun.Tuple4<A,B,C,D>> getComparator() {
            return BTreeMap.COMPARABLE_COMPARATOR;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Tuple4KeySerializer t = (Tuple4KeySerializer) o;

            return
                    Fun.eq(aComparator, t.aComparator) &&
                    Fun.eq(bComparator, t.bComparator) &&
                    Fun.eq(cComparator, t.cComparator) &&
                    Fun.eq(aSerializer, t.aSerializer) &&
                    Fun.eq(bSerializer, t.bSerializer) &&
                    Fun.eq(cSerializer, t.cSerializer) &&
                    Fun.eq(dSerializer, t.dSerializer);
        }


        @Override
        public int hashCode() {
            int result = aComparator != null ? aComparator.hashCode() : 0;
            result = 31 * result + (bComparator != null ? bComparator.hashCode() : 0);
            result = 31 * result + (cComparator != null ? cComparator.hashCode() : 0);
            result = 31 * result + (aSerializer != null ? aSerializer.hashCode() : 0);
            result = 31 * result + (bSerializer != null ? bSerializer.hashCode() : 0);
            result = 31 * result + (cSerializer != null ? cSerializer.hashCode() : 0);
            result = 31 * result + (dSerializer != null ? dSerializer.hashCode() : 0);
            return result;
        }
    }


    /**
     * Applies delta compression on array of tuple. First, second and third tuple value may be shared between consequential tuples,
     * so only first occurrence is serialized. An example:
     *
     * <pre>
     *     Value                Serialized as
     *     ----------------------------------
     *     Tuple(1, 2, 1, 1)       1, 2, 1, 1
     *     Tuple(1, 2, 1, 2)                2
     *     Tuple(1, 3, 3, 3)          3, 3, 3
     *     Tuple(1, 3, 4, 4)             4, 4
     * </pre>
     *
     * @param <A> first tuple value
     * @param <B> second tuple value
     * @param <C> third tuple value
     */
    public static class Tuple5KeySerializer<A,B,C,D,E> extends  BTreeKeySerializer<Fun.Tuple5<A,B,C,D,E>> implements Serializable {

        private static final long serialVersionUID = 8607477718850453705L;
        protected final Comparator<A> aComparator;
        protected final Comparator<B> bComparator;
        protected final Comparator<C> cComparator;
        protected final Comparator<D> dComparator;
        protected final Serializer<A> aSerializer;
        protected final Serializer<B> bSerializer;
        protected final Serializer<C> cSerializer;
        protected final Serializer<D> dSerializer;
        protected final Serializer<E> eSerializer;

        /**
         * Construct new Tuple4 Key Serializer. You may pass null for some value,
         * In that case 'default' value will be used, Comparable comparator and Default Serializer from DB.
         *
         */
        public Tuple5KeySerializer(Comparator<A> aComparator, Comparator<B> bComparator, Comparator<C> cComparator, Comparator<D> dComparator,
                                   Serializer<A> aSerializer, Serializer<B> bSerializer, Serializer<C> cSerializer, Serializer<D> dSerializer, Serializer<E> eSerializer){
            this.aComparator = aComparator;
            this.bComparator = bComparator;
            this.cComparator = cComparator;
            this.dComparator = dComparator;
            this.aSerializer = aSerializer;
            this.bSerializer = bSerializer;
            this.cSerializer = cSerializer;
            this.dSerializer = dSerializer;
            this.eSerializer = eSerializer;
        }

        /** used for deserialization */
        Tuple5KeySerializer(SerializerBase serializerBase, DataInput is, SerializerBase.FastArrayList<Object> objectStack) throws IOException {
            objectStack.add(this);
            aComparator = (Comparator<A>) serializerBase.deserialize(is,objectStack);
            bComparator = (Comparator<B>) serializerBase.deserialize(is,objectStack);
            cComparator = (Comparator<C>) serializerBase.deserialize(is,objectStack);
            dComparator = (Comparator<D>) serializerBase.deserialize(is, objectStack);
            aSerializer = (Serializer<A>) serializerBase.deserialize(is,objectStack);
            bSerializer = (Serializer<B>) serializerBase.deserialize(is,objectStack);
            cSerializer = (Serializer<C>) serializerBase.deserialize(is,objectStack);
            dSerializer = (Serializer<D>) serializerBase.deserialize(is,objectStack);
            eSerializer = (Serializer<E>) serializerBase.deserialize(is,objectStack);
        }


        @Override
        public void serialize(DataOutput out, int start, int end, Object[] keys) throws IOException {
            int acount=0;
            int bcount=0;
            int ccount=0;
            int dcount=0;
            for(int i=start;i<end;i++){
                Fun.Tuple5<A,B,C,D,E> t = (Fun.Tuple5<A, B,C,D,E>) keys[i];
                if(acount==0){
                    //write new A
                    aSerializer.serialize(out,t.a);
                    //count how many A are following
                    acount=1;
                    while(i+acount<end && aComparator.compare(t.a, ((Fun.Tuple5<A, B, C,D, E>) keys[i+acount]).a)==0){
                        acount++;
                    }
                    DataOutput2.packInt(out,acount);
                }
                if(bcount==0){
                    //write new B
                    bSerializer.serialize(out,t.b);
                    //count how many B are following
                    bcount=1;
                    while(i+bcount<end && bComparator.compare(t.b, ((Fun.Tuple5<A, B,C,D, E>) keys[i+bcount]).b)==0){
                        bcount++;
                    }
                    DataOutput2.packInt(out,bcount);
                }
                if(ccount==0){
                    //write new C
                    cSerializer.serialize(out,t.c);
                    //count how many C are following
                    ccount=1;
                    while(i+ccount<end && cComparator.compare(t.c, ((Fun.Tuple5<A, B,C,D, E>) keys[i+ccount]).c)==0){
                        ccount++;
                    }
                    DataOutput2.packInt(out,ccount);
                }

                if(dcount==0){
                    //write new D
                    dSerializer.serialize(out,t.d);
                    //count how many D are following
                    dcount=1;
                    while(i+dcount<end && dComparator.compare(t.d, ((Fun.Tuple5<A, B,C,D,E>) keys[i+dcount]).d)==0){
                        dcount++;
                    }
                    DataOutput2.packInt(out,dcount);
                }


                eSerializer.serialize(out,t.e);

                acount--;
                bcount--;
                ccount--;
                dcount--;
            }
        }

        @Override
        public Object[] deserialize(DataInput in, int start, int end, int size) throws IOException {
            Object[] ret = new Object[size];
            A a = null;
            int acount = 0;
            B b = null;
            int bcount = 0;
            C c = null;
            int ccount = 0;
            D d = null;
            int dcount = 0;

            for(int i=start;i<end;i++){
                if(acount==0){
                    //read new A
                    a = aSerializer.deserialize(in,-1);
                    acount = DataInput2.unpackInt(in);
                }
                if(bcount==0){
                    //read new B
                    b = bSerializer.deserialize(in,-1);
                    bcount = DataInput2.unpackInt(in);
                }
                if(ccount==0){
                    //read new C
                    c = cSerializer.deserialize(in,-1);
                    ccount = DataInput2.unpackInt(in);
                }
                if(dcount==0){
                    //read new D
                    d = dSerializer.deserialize(in,-1);
                    dcount = DataInput2.unpackInt(in);
                }


                E e = eSerializer.deserialize(in,-1);
                ret[i]= Fun.t5(a, b, c, d, e);
                acount--;
                bcount--;
                ccount--;
                dcount--;
            }
            assert(acount==0);
            assert(bcount==0);
            assert(ccount==0);
            assert(dcount==0);

            return ret;
        }

        @Override
        public Comparator<Fun.Tuple5<A,B,C,D,E>> getComparator() {
            return BTreeMap.COMPARABLE_COMPARATOR;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Tuple5KeySerializer t = (Tuple5KeySerializer) o;

            return
                    Fun.eq(aComparator, t.aComparator) &&
                    Fun.eq(bComparator, t.bComparator) &&
                    Fun.eq(cComparator, t.cComparator) &&
                    Fun.eq(dComparator, t.dComparator) &&
                    Fun.eq(aSerializer, t.aSerializer) &&
                    Fun.eq(bSerializer, t.bSerializer) &&
                    Fun.eq(cSerializer, t.cSerializer) &&
                    Fun.eq(dSerializer, t.dSerializer) &&
                    Fun.eq(eSerializer, t.eSerializer);
        }

        @Override
        public int hashCode() {
            int result = aComparator != null ? aComparator.hashCode() : 0;
            result = 31 * result + (bComparator != null ? bComparator.hashCode() : 0);
            result = 31 * result + (cComparator != null ? cComparator.hashCode() : 0);
            result = 31 * result + (dComparator != null ? dComparator.hashCode() : 0);
            result = 31 * result + (aSerializer != null ? aSerializer.hashCode() : 0);
            result = 31 * result + (bSerializer != null ? bSerializer.hashCode() : 0);
            result = 31 * result + (cSerializer != null ? cSerializer.hashCode() : 0);
            result = 31 * result + (dSerializer != null ? dSerializer.hashCode() : 0);
            result = 31 * result + (eSerializer != null ? eSerializer.hashCode() : 0);
            return result;
        }
    }

    /**
     * Applies delta compression on array of tuple. First, second and third tuple value may be shared between consequential tuples,
     * so only first occurrence is serialized. An example:
     *
     * <pre>
     *     Value                Serialized as
     *     ----------------------------------
     *     Tuple(1, 2, 1, 1)       1, 2, 1, 1
     *     Tuple(1, 2, 1, 2)                2
     *     Tuple(1, 3, 3, 3)          3, 3, 3
     *     Tuple(1, 3, 4, 4)             4, 4
     * </pre>
     *
     * @param <A> first tuple value
     * @param <B> second tuple value
     * @param <C> third tuple value
     */
    public static class Tuple6KeySerializer<A,B,C,D,E,F> extends  BTreeKeySerializer<Fun.Tuple6<A,B,C,D,E,F>> implements Serializable {

        private static final long serialVersionUID = 3666600849149868404L;
        protected final Comparator<A> aComparator;
        protected final Comparator<B> bComparator;
        protected final Comparator<C> cComparator;
        protected final Comparator<D> dComparator;
        protected final Comparator<E> eComparator;
        protected final Serializer<A> aSerializer;
        protected final Serializer<B> bSerializer;
        protected final Serializer<C> cSerializer;
        protected final Serializer<D> dSerializer;
        protected final Serializer<E> eSerializer;
        protected final Serializer<F> fSerializer;

        /**
         * Construct new Tuple4 Key Serializer. You may pass null for some value,
         * In that case 'default' value will be used, Comparable comparator and Default Serializer from DB.
         *
         */
        public Tuple6KeySerializer(Comparator<A> aComparator, Comparator<B> bComparator, Comparator<C> cComparator, Comparator<D> dComparator,Comparator<E> eComparator,
                                   Serializer<A> aSerializer, Serializer<B> bSerializer, Serializer<C> cSerializer, Serializer<D> dSerializer, Serializer<E> eSerializer,Serializer<F> fSerializer){
            this.aComparator = aComparator;
            this.bComparator = bComparator;
            this.cComparator = cComparator;
            this.dComparator = dComparator;
            this.eComparator = eComparator;
            this.aSerializer = aSerializer;
            this.bSerializer = bSerializer;
            this.cSerializer = cSerializer;
            this.dSerializer = dSerializer;
            this.eSerializer = eSerializer;
            this.fSerializer = fSerializer;
        }

        /** used for deserialization */
        Tuple6KeySerializer(SerializerBase serializerBase, DataInput is, SerializerBase.FastArrayList<Object> objectStack) throws IOException {
            objectStack.add(this);
            aComparator = (Comparator<A>) serializerBase.deserialize(is,objectStack);
            bComparator = (Comparator<B>) serializerBase.deserialize(is,objectStack);
            cComparator = (Comparator<C>) serializerBase.deserialize(is,objectStack);
            dComparator = (Comparator<D>) serializerBase.deserialize(is,objectStack);
            eComparator = (Comparator<E>) serializerBase.deserialize(is,objectStack);
            aSerializer = (Serializer<A>) serializerBase.deserialize(is,objectStack);
            bSerializer = (Serializer<B>) serializerBase.deserialize(is,objectStack);
            cSerializer = (Serializer<C>) serializerBase.deserialize(is,objectStack);
            dSerializer = (Serializer<D>) serializerBase.deserialize(is,objectStack);
            eSerializer = (Serializer<E>) serializerBase.deserialize(is,objectStack);
            fSerializer = (Serializer<F>) serializerBase.deserialize(is,objectStack);
        }


        @Override
        public void serialize(DataOutput out, int start, int end, Object[] keys) throws IOException {
            int acount=0;
            int bcount=0;
            int ccount=0;
            int dcount=0;
            int ecount=0;
            for(int i=start;i<end;i++){
                Fun.Tuple6<A,B,C,D,E,F> t = (Fun.Tuple6<A, B,C,D,E,F>) keys[i];
                if(acount==0){
                    //write new A
                    aSerializer.serialize(out,t.a);
                    //count how many A are following
                    acount=1;
                    while(i+acount<end && aComparator.compare(t.a, ((Fun.Tuple6<A, B, C,D, E,F>) keys[i+acount]).a)==0){
                        acount++;
                    }
                    DataOutput2.packInt(out,acount);
                }
                if(bcount==0){
                    //write new B
                    bSerializer.serialize(out,t.b);
                    //count how many B are following
                    bcount=1;
                    while(i+bcount<end && bComparator.compare(t.b, ((Fun.Tuple6<A, B,C,D, E,F>) keys[i+bcount]).b)==0){
                        bcount++;
                    }
                    DataOutput2.packInt(out,bcount);
                }
                if(ccount==0){
                    //write new C
                    cSerializer.serialize(out,t.c);
                    //count how many C are following
                    ccount=1;
                    while(i+ccount<end && cComparator.compare(t.c, ((Fun.Tuple6<A, B,C,D, E,F>) keys[i+ccount]).c)==0){
                        ccount++;
                    }
                    DataOutput2.packInt(out,ccount);
                }

                if(dcount==0){
                    //write new C
                    dSerializer.serialize(out,t.d);
                    //count how many D are following
                    dcount=1;
                    while(i+dcount<end && dComparator.compare(t.d, ((Fun.Tuple6<A, B,C,D,E,F>) keys[i+dcount]).d)==0){
                        dcount++;
                    }
                    DataOutput2.packInt(out,dcount);
                }

                if(ecount==0){
                    //write new C
                    eSerializer.serialize(out,t.e);
                    //count how many E are following
                    ecount=1;
                    while(i+ecount<end && eComparator.compare(t.e, ((Fun.Tuple6<A, B,C,D,E,F>) keys[i+ecount]).e)==0){
                        ecount++;
                    }
                    DataOutput2.packInt(out,ecount);
                }


                fSerializer.serialize(out,t.f);

                acount--;
                bcount--;
                ccount--;
                dcount--;
                ecount--;
            }
        }

        @Override
        public Object[] deserialize(DataInput in, int start, int end, int size) throws IOException {
            Object[] ret = new Object[size];
            A a = null;
            int acount = 0;
            B b = null;
            int bcount = 0;
            C c = null;
            int ccount = 0;
            D d = null;
            int dcount = 0;
            E e = null;
            int ecount = 0;


            for(int i=start;i<end;i++){
                if(acount==0){
                    //read new A
                    a = aSerializer.deserialize(in,-1);
                    acount = DataInput2.unpackInt(in);
                }
                if(bcount==0){
                    //read new B
                    b = bSerializer.deserialize(in,-1);
                    bcount = DataInput2.unpackInt(in);
                }
                if(ccount==0){
                    //read new C
                    c = cSerializer.deserialize(in,-1);
                    ccount = DataInput2.unpackInt(in);
                }
                if(dcount==0){
                    //read new D
                    d = dSerializer.deserialize(in,-1);
                    dcount = DataInput2.unpackInt(in);
                }

                if(ecount==0){
                    //read new E
                    e = eSerializer.deserialize(in,-1);
                    ecount = DataInput2.unpackInt(in);
                }


                F f = fSerializer.deserialize(in,-1);
                ret[i]= Fun.t6(a, b, c, d, e, f);
                acount--;
                bcount--;
                ccount--;
                dcount--;
                ecount--;
            }
            assert(acount==0);
            assert(bcount==0);
            assert(ccount==0);
            assert(dcount==0);
            assert(ecount==0);

            return ret;
        }

        @Override
        public Comparator<Fun.Tuple6<A,B,C,D,E,F>> getComparator() {
            return BTreeMap.COMPARABLE_COMPARATOR;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Tuple6KeySerializer t = (Tuple6KeySerializer) o;

            return
                    Fun.eq(aComparator, t.aComparator) &&
                    Fun.eq(bComparator, t.bComparator) &&
                    Fun.eq(cComparator, t.cComparator) &&
                    Fun.eq(dComparator, t.dComparator) &&
                    Fun.eq(eComparator, t.eComparator) &&
                    Fun.eq(aSerializer, t.aSerializer) &&
                    Fun.eq(bSerializer, t.bSerializer) &&
                    Fun.eq(cSerializer, t.cSerializer) &&
                    Fun.eq(dSerializer, t.dSerializer) &&
                    Fun.eq(eSerializer, t.eSerializer) &&
                    Fun.eq(fSerializer, t.fSerializer);

        }

        @Override
        public int hashCode() {
            int result = aComparator != null ? aComparator.hashCode() : 0;
            result = 31 * result + (bComparator != null ? bComparator.hashCode() : 0);
            result = 31 * result + (cComparator != null ? cComparator.hashCode() : 0);
            result = 31 * result + (dComparator != null ? dComparator.hashCode() : 0);
            result = 31 * result + (eComparator != null ? eComparator.hashCode() : 0);
            result = 31 * result + (aSerializer != null ? aSerializer.hashCode() : 0);
            result = 31 * result + (bSerializer != null ? bSerializer.hashCode() : 0);
            result = 31 * result + (cSerializer != null ? cSerializer.hashCode() : 0);
            result = 31 * result + (dSerializer != null ? dSerializer.hashCode() : 0);
            result = 31 * result + (eSerializer != null ? eSerializer.hashCode() : 0);
            result = 31 * result + (fSerializer != null ? fSerializer.hashCode() : 0);
            return result;
        }
    }


}