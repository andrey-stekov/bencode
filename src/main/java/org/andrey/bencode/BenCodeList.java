package org.andrey.bencode;

import java.util.ArrayList;

public class BenCodeList extends ArrayList<BenCodeElement> implements BenCodeElement {
    @Override
    public BenCodeValueType getType() {
        return BenCodeValueType.LIST;
    }
}
