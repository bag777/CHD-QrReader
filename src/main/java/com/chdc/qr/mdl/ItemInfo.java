package com.chdc.qr.mdl;

import com.chdc.qr.QRResources;

public class ItemInfo implements QRResources {
    private final int code;
    private final int type;
    private final String category;
    private final String name;

    public ItemInfo(int type, int code, String category, String name) {
        this.type = type;
        this.code = code;
        this.category = category;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public int getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public boolean isLabelTarget() {
        return FinanceType.getByCode(this.type).isTarget(this.code);
    }
}