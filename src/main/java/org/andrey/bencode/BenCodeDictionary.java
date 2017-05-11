package org.andrey.bencode;

import java.util.TreeMap;

public class BenCodeDictionary extends TreeMap<String, BenCodeElement> implements BenCodeElement {
    @Override
    public BenCodeValueType getType() {
        return BenCodeValueType.DICTIONARY;
    }
}
