package com.chdc.qr.mdl;

public class IncomeRecord {
    private int ord;
    private int type;
    private int itemCode;
    private int peopleCode;
    private int price;

    public IncomeRecord(int ord, int type, int itemCode, int peopleCode, int price) {
        this.ord = ord;
        this.type = type;
        this.itemCode = itemCode;
        this.peopleCode = peopleCode;
        this.price = price;
    }

    public int getOrd() {
        return ord;
    }

    public int getType() {
        return type;
    }

    public int getItemCode() {
        return itemCode;
    }

    public int getPeopleCode() {
        return peopleCode;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
