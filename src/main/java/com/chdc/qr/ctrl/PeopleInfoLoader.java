package com.chdc.qr.ctrl;

import com.chdc.qr.lib.db.QueryManager;
import com.chdc.qr.lib.db.QueryResult;
import com.chdc.qr.mdl.PeopleInfo;
import lombok.extern.slf4j.Slf4j;
import org.h2.command.dml.Update;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PeopleInfoLoader {
    private QueryManager queryManager = new QueryManager();
    private final static String InsertQuery = "" +
            "INSERT INTO PEOPLE_INFO (CODE, NAME, GENDER, AREA, SECTION, DUTY, CONTACT, AGE) " +
            "VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', %d)";

    private final static String UpdateQuery = "" +
            "UPDATE PEOPLE_INFO " +
            "SET NAME='%s', GENDER='%s', AREA='%s', SECTION='%s', DUTY='%s', CONTACT='%s', AGE=%d " +
            "WHERE CODE=%d";

    private final static String SelectQuery = "" +
            "SELECT CODE, NAME, GENDER, AREA, SECTION, DUTY, CONTACT, AGE FROM PEOPLE_INFO WHERE CODE=%d";

    public int[] load(String text) throws Exception {
        List<String> infos = split(text);

        // Column Index 생성
        parsingColIndexes(infos.remove(0));

//        deleteOldDate();

        int insCount = 0;
        int chgCount = 0;
        for (String info : infos) {
            // parsing one PeopleInfo
            PeopleInfo peopleInfo = getPeopleInfo(info);
            if (peopleInfo != null) {
                PeopleInfo oldInfo = null;
                try {
                    oldInfo = selectPeopleInfo(peopleInfo.getCode());
                } catch (SQLException ex) {
                    oldInfo = null;
                }

                if (oldInfo == null) {
                    try {
                        insert(peopleInfo);
                        insCount++;
                    } catch (SQLException ex) {
                        log.error("Insert fail - " + ex.getMessage() + "\n > " + peopleInfo);
                    }
                } else {
                    if (oldInfo.equals(peopleInfo)) {
                        // Do nothing...
                    } else {
                        try {
                            update(peopleInfo);
                            chgCount++;
                        } catch (SQLException ex) {
                            log.error("Update fail - " + ex.getMessage() + "\n > " + peopleInfo);
                        }
                    }
                }

            }
        }
        log.info("[PeopleInfo] Insert : " + insCount + ", Update : " + chgCount);

        return new int[]{insCount, chgCount};
    }

    private void deleteOldDate() throws SQLException {
        int count = queryManager.executeUpdate("DELETE FROM PEOPLE_INFO");
        log.info("[PeopleInfo] Delete : " + count);
    }

    private final static String[] ColumnOrders = {
            "ID",
            "이름",
            "성별",
            "교구",
            "목장",
            "세부직분",
            "핸드폰",
            "나이"
    };

    java.util.List<Integer> colIndexes = null;

    private void parsingColIndexes(String header) {
        colIndexes = new ArrayList<>();

        String[] colNames = header.split("\t");

        for (int i = 0; i < ColumnOrders.length; i++) {
            boolean find = false;
            for (int col = 0; col < colNames.length; col++) {
                if (colNames[col].equalsIgnoreCase(ColumnOrders[i])) {
                    colIndexes.add(col);
                    find = true;
                    break;
                }
            }
            if (!find) {
                colIndexes.add(-1);
            }
        }
    }

    /**
     * 일부 정보 가운데 \n이 포함된 경우 이를 제거한 list를 반환
     *
     * @return
     */
    private java.util.List<String> split(String text) {
        final java.util.List<String> composedList = new ArrayList<>();

        String[] infos = text.split("\n");

        String before = "";
        for (String info : infos) {
            if (info.trim().isEmpty()) continue;

            if (info.startsWith("\t")) {
                before += info;
                composedList.set(composedList.size() - 1, before);
            } else {
                before = info;
                composedList.add(before);
            }
        }

        return composedList;
    }

    private boolean insert(String line) throws Exception {
        String[] cols = line.split("\t");

        for (int i = 0; i < cols.length; i++) {
            System.err.println(" : " + i + " - " + cols[i]);
        }
        int pos = 0;
//        (CODE, NAME, GENDER, AREA, SECTION, DUTY, CONTACT, AGE)
        queryManager.executeUpdate(String.format(InsertQuery,
                cols[this.colIndexes.get(pos++)],
                cols[this.colIndexes.get(pos++)],
                this.colIndexes.get(pos) == -1 ? "" : cols[this.colIndexes.get(pos++)],
                this.colIndexes.get(pos) == -1 ? "" : cols[this.colIndexes.get(pos++)],
                this.colIndexes.get(pos) == -1 ? "" : cols[this.colIndexes.get(pos++)],
                this.colIndexes.get(pos) == -1 ? "" : cols[this.colIndexes.get(pos++)],
                this.colIndexes.get(pos) == -1 ? "" : cols[this.colIndexes.get(pos++)],
                cols[this.colIndexes.get(pos)].trim().isEmpty() ? "0" : cols[this.colIndexes.get(pos++)]));

        return true;
    }

    public PeopleInfo getPeopleInfo(String line) {
        String[] cols = line.split("\t");
//        (CODE, NAME, GENDER, AREA, SECTION, DUTY, CONTACT, AGE)
        int pos = 0;
        PeopleInfo peopleInfo = new PeopleInfo(
                Integer.parseInt(cols[this.colIndexes.get(pos++)]),
                cols[this.colIndexes.get(pos++)],
                this.colIndexes.get(pos) == -1 ? "" : cols[this.colIndexes.get(pos++)],
                this.colIndexes.get(pos) == -1 ? "" : cols[this.colIndexes.get(pos++)],
                this.colIndexes.get(pos) == -1 ? "" : cols[this.colIndexes.get(pos++)],
                this.colIndexes.get(pos) == -1 ? "" : cols[this.colIndexes.get(pos++)],
                this.colIndexes.get(pos) == -1 ? "" : cols[this.colIndexes.get(pos++)],
                Integer.parseInt(cols[this.colIndexes.get(pos)].trim().isEmpty() ? "0" : cols[this.colIndexes.get(pos++)])
        );

        return peopleInfo;
    }

    public PeopleInfo selectPeopleInfo(int code) throws SQLException {
        // CODE, NAME, GENDER, AREA, SECTION, DUTY, CONTACT, AGE
        QueryResult result = queryManager.executeQuery(String.format(SelectQuery, code));

        if (result.isEmpty()) throw new SQLException("None");

        List row = result.get(0);

        int pos = 0;
        PeopleInfo peopleInfo = new PeopleInfo(
                Integer.parseInt(row.get(pos++) + ""),
                row.get(pos++) + "",
                row.get(pos++) + "",
                row.get(pos++) + "",
                row.get(pos++) + "",
                row.get(pos++) + "",
                row.get(pos++) + "",
                Integer.parseInt(row.get(pos++) + "")
        );

        return peopleInfo;
    }

    public void insert(PeopleInfo peopleInfo) throws SQLException {
        queryManager.executeUpdate(String.format(InsertQuery,
                peopleInfo.getCode(),
                peopleInfo.getName(),
                peopleInfo.getGender(),
                peopleInfo.getArea(),
                peopleInfo.getSection(),
                peopleInfo.getDuty(),
                peopleInfo.getContact(),
                peopleInfo.getAge()
        ));
    }

    public void update(PeopleInfo peopleInfo) throws SQLException {
        queryManager.executeUpdate(String.format(UpdateQuery,
                peopleInfo.getName(),
                peopleInfo.getGender(),
                peopleInfo.getArea(),
                peopleInfo.getSection(),
                peopleInfo.getDuty(),
                peopleInfo.getContact(),
                peopleInfo.getAge(),
                peopleInfo.getCode()));
    }

    private String removeSection(String section) {
        return section;
    }
}