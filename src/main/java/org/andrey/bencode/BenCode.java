package org.andrey.bencode;

import java.io.*;
import java.util.Map;

public class BenCode {
    public static final char START_INT  = 'i';
    public static final char START_DICT = 'd';
    public static final char START_LIST = 'l';
    public static final char END_MARKER = 'e';
    public static final char MINUS = '-';
    public static final char COLON = ':';

    public static void write(OutputStream os, BenCodeElement el) throws IOException {
        switch (el.getType()) {
            case INTEGER:
                writeInt(os, (BenCodeValue) el);
                break;
            case BYTE_ARRAY:
                writeByteArr(os, ((BenCodeValue) el).byteArrVal());
                break;
            case DICTIONARY:
                writeDictionary(os, (BenCodeDictionary) el);
                break;
            case LIST:
                writeList(os, (BenCodeList) el);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private static void writeInt(OutputStream os, BenCodeValue val) throws IOException {
        os.write(START_INT);
        os.write(Integer.toString(val.intVal()).getBytes());
        os.write(END_MARKER);
    }

    private static void writeByteArr(OutputStream os, byte[] bytes) throws IOException {
        os.write(Integer.toString(bytes.length).getBytes());
        os.write(COLON);
        os.write(bytes);
    }

    private static void writeDictionary(OutputStream os, BenCodeDictionary dictionary) throws IOException {
        os.write(START_DICT);
        for (Map.Entry<String, BenCodeElement> entry : dictionary.entrySet()) {
            writeByteArr(os, entry.getKey().getBytes());
            write(os, entry.getValue());
        }
        os.write(END_MARKER);
    }

    private static void writeList(OutputStream os, BenCodeList list) throws IOException {
        os.write(START_LIST);
        for (BenCodeElement el : list) {
            write(os, el);
        }
        os.write(END_MARKER);
    }

    public static BenCodeElement read(InputStream is) throws IOException {
        PushbackInputStream pushBackIS = new PushbackInputStream(is);
        return read(pushBackIS);
    }

    public static BenCodeElement read(PushbackInputStream pushBackIS) throws IOException {
        char ch = peekCharacter(pushBackIS);

        if (Character.isDigit(ch)) {
            return readByteArr(pushBackIS);
        } else if (ch == START_INT) {
            return readInteger(pushBackIS);
        } else if (ch == START_LIST) {
            return readList(pushBackIS);
        } else if (ch == START_DICT) {
            return readDictionary(pushBackIS);
        }

        throw new IllegalStateException("Unexpected character: \'" + ch + "\'");
    }

    private static BenCodeElement readList(PushbackInputStream pushBackIS) throws IOException {
        BenCodeList list = new BenCodeList();

        ensureCharacter(START_LIST, pushBackIS);
        while (peekCharacter(pushBackIS) != END_MARKER) {
            list.add(read(pushBackIS));
        }
        ensureCharacter(END_MARKER, pushBackIS);

        return list;
    }

    private static BenCodeElement readDictionary(PushbackInputStream pushBackIS) throws IOException {
        BenCodeDictionary dictionary = new BenCodeDictionary();

        ensureCharacter(START_DICT, pushBackIS);
        while (peekCharacter(pushBackIS) != END_MARKER) {
            BenCodeValue key = readByteArr(pushBackIS);
            dictionary.put(key.strVal(), read(pushBackIS));
        }
        ensureCharacter(END_MARKER, pushBackIS);

        return dictionary;
    }

    private static BenCodeElement readInteger(PushbackInputStream pushBackIS) throws IOException {
        int val, sign = 1;
        char ch;

        ensureCharacter(START_INT, pushBackIS);
        ch = peekCharacter(pushBackIS);

        if (ch == MINUS) {
            sign = -1;
            ensureCharacter(MINUS, pushBackIS);
        }
        val = readUnsigned(pushBackIS);
        ensureCharacter(END_MARKER, pushBackIS);

        return BenCodeValue.newInt(sign * val);
    }

    private static BenCodeValue readByteArr(PushbackInputStream pushBackIS) throws IOException {
        int size = readUnsigned(pushBackIS);
        ensureCharacter(COLON, pushBackIS);
        byte[] bytes = new byte[size];
        if (pushBackIS.read(bytes) < size) {
            throw new IllegalStateException("Unexpected end of stream");
        }
        return BenCodeValue.newBytes(bytes);
    }

    private static int readUnsigned(PushbackInputStream pushBackIS) throws IOException {
        StringBuilder builder = new StringBuilder();

        int intch;
        while ((intch = pushBackIS.read()) != -1) {
            char ch = (char) intch;
            if (!Character.isDigit(ch)) {
                pushBackIS.unread(intch);
                break;
            }
            builder.append(ch);
        }

        return Integer.parseInt(builder.toString());
    }

    private static char peekCharacter(PushbackInputStream pushBackIS) throws IOException {
        int intch;
        if ((intch = pushBackIS.read()) != -1) {
            pushBackIS.unread(intch);
            return  (char) intch;
        }

        throw new IllegalStateException("Unexpected end of stream");
    }

    private static void ensureCharacter(char expected, PushbackInputStream pushBackIS) throws IOException {
        int intch;
        if ((intch = pushBackIS.read()) != -1) {
            char ch = (char) intch;

            if (ch != expected) {
                throw new IllegalStateException("\'" + expected + "\' expect but \'" + ch + "\' found");
            }
        } else {
            throw new IllegalStateException("Unexpected end of stream");
        }
    }
}