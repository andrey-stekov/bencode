package org.andrey.bencode;

import java.nio.charset.Charset;

public class BenCodeValue implements BenCodeElement {
    public static final Charset CHARSET = Charset.forName("UTF-8");
    private final BenCodeValueType type;
    private final Object value;

    public BenCodeValue(BenCodeValueType type, Object value) {
        this.type = type;
        this.value = value;
    }

    private void checkType(BenCodeValueType expected) {
        if (!type.equals(expected)) {
            throw new IllegalStateException(expected + " type required but " + type + " found");
        }
    }

    public int intVal() {
        checkType(BenCodeValueType.INTEGER);
        return (Integer) value;
    }

    public String strVal() {
        checkType(BenCodeValueType.BYTE_ARRAY);
        return new String((byte[]) value, CHARSET);
    }

    public byte[] byteArrVal() {
        checkType(BenCodeValueType.BYTE_ARRAY);
        return (byte[]) value;
    }

    public static BenCodeValue newInt(int i) {
        return new BenCodeValue(BenCodeValueType.INTEGER, i);
    }

    public static BenCodeValue newStr(String s) {
        return new BenCodeValue(BenCodeValueType.BYTE_ARRAY, s.getBytes(CHARSET));
    }

    public static BenCodeValue newBytes(byte[] bytes) {
        return new BenCodeValue(BenCodeValueType.BYTE_ARRAY, bytes);
    }

    @Override
    public BenCodeValueType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "BenCodeValue{" +
                "type=" + type +
                ", value=" + (BenCodeValueType.BYTE_ARRAY.equals(type) ? strVal() : intVal()) +
                '}';
    }
}
