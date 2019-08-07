package com.chdc.qr.mdl;

import com.chdc.qr.QRResources;

public class LabelInfo implements QRResources {
    private ItemInfo itemInfo;
    private PeopleInfo peopleInfo;
    private final String codedName;

    private String section = "";

    public LabelInfo(int type, int itemCode, int peopleCode, String codedName) {
        this.peopleInfo = PeopleInfo.getPeopleInfo(peopleCode);
        if (this.peopleInfo != null) {
            this.itemInfo = ItemInfoPool.getInstance().get(type, itemCode);
        }
        this.codedName = codedName;
    }

    public ItemInfo getItemInfo() {
        return itemInfo;
    }

    public PeopleInfo getPeopleInfo() {
        return peopleInfo;
    }

    public String getType() {
        if (this.itemInfo == null) return null;

        return this.itemInfo.getName();
    }

    public int getCode() {
        if (itemInfo == null || peopleInfo == null) return 0;

        return peopleInfo.getCode();
    }

    public String getSection() {
        if (itemInfo == null || peopleInfo == null) return "";

        if (this.peopleInfo.getArea().equals("청년국")) {
            return "청년국";
        }
        if (this.peopleInfo.getArea().equals("교역자")) {
            return this.peopleInfo.getDuty();
        }
        if (this.peopleInfo.getSection().isEmpty()
                && this.peopleInfo.getDuty().isEmpty()) {
            return "성도";
        }
//        if (this.peopleInfo.getDuty().equals("성도")) {
//            return "성도";
//        }
        return this.peopleInfo.getSection();
    }

    public String getDuty() {
        if (itemInfo == null || peopleInfo == null) return null;

        if (this.peopleInfo.getArea().equals("청년국")
                || this.peopleInfo.getArea().equals("교역자")) {
            return "";
        } else if (this.peopleInfo.getDuty().equals("성도")
                && this.peopleInfo.getSection().equals("")) {
            return "";
        } else if (this.peopleInfo.getDuty().equals("서리집사")) {
            return "집사";
        } else {
            return this.peopleInfo.getDuty();
        }
    }

    public String getName() {
        if (itemInfo == null || peopleInfo == null) return null;

        return this.codedName;
    }

    public String getQrCode() {
        if (itemInfo == null || peopleInfo == null) return null;

        return String.format("%d,%d,%d,%s",
                itemInfo.getType(),itemInfo.getCode(),
                peopleInfo.getCode(), this.codedName);
    }

    public boolean isLabelTarget() {
        return this.itemInfo.isLabelTarget();
    }

    @Override
    public String toString() {
        if (itemInfo == null || peopleInfo == null) return null;

        return String.format("%s - %s - %s - %s",
                this.itemInfo.getName(),
                this.peopleInfo.getSection(),
                getName(),
                getQrCode());
    }
}
