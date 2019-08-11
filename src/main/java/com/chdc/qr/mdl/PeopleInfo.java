package com.chdc.qr.mdl;

import com.chdc.qr.lib.db.QueryManager;
import com.chdc.qr.lib.db.QueryResult;

import java.sql.SQLException;
import java.util.List;

public class PeopleInfo {
    private final int code;
    private final String name;
    private final String gender;
    private final String area;
    private final String section;
    private final String duty;
    private final String contact;
    private final int age;

    public PeopleInfo(int code, String name, String gender, String area, String section,
                      String duty, String contact, int age) {
        this.code = code;
        this.name = name.trim();
        this.gender = gender.trim();
        this.area = area.trim();
        this.section = section.trim();
        this.duty = duty.trim();
        this.contact = contact.trim();
        this.age = age;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getArea() {
        return area;
    }

    public String getSection() {
        return section;
    }

    public String getDuty() {
        return duty;
    }

    public String getContact() {
        return contact;
    }

    public int getAge() {
        return age;
    }

    private static final QueryManager queryManager = new QueryManager();
    private static final String SelectQuery = "" +
            "SELECT CODE, NAME, GENDER, AREA, SECTION, DUTY, CONTACT, AGE FROM PEOPLE_INFO WHERE CODE=%d";
    public static PeopleInfo getPeopleInfo(int code) {
        QueryResult lists;
        try {
            lists = queryManager.executeQuery(String.format(SelectQuery, code));
        } catch (SQLException e) {
            return null;
        }

        if (lists.isEmpty()) return null;

        List record = lists.get(0);

        int pos = 0;
        return new PeopleInfo(
                queryManager.getInt(record.get(pos++)),
                queryManager.getString(record.get(pos++)).trim(),
                queryManager.getString(record.get(pos++)).trim(),
                queryManager.getString(record.get(pos++)).trim(),
                queryManager.getString(record.get(pos++)).trim(),
                queryManager.getString(record.get(pos++)).trim(),
                queryManager.getString(record.get(pos++)).trim(),
                queryManager.getInt(record.get(pos++)));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PeopleInfo) {
            PeopleInfo other = (PeopleInfo) obj;

            if (other.getCode() != this.code) return false;
            if (!other.getName().equals(this.name)) return false;
            if (!other.getGender().equals(this.gender)) return false;
            if (!other.getArea().equals(this.area)) return false;
            if (!other.getSection().equals(this.section)) return false;
            if (!other.getDuty().equals(this.duty)) return false;
            if (!other.getContact().equals(this.contact)) return false;
            if (other.getAge() != this.age) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("%d %s(%d) %s %s %s %s %s",
                this.code, this.name, this.age, this.gender,
                this.area, this.section, this.duty, this.contact);
    }
}
