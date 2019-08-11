package com.chdc.qr.ctrl;

import com.chdc.qr.QRResources;
import com.chdc.qr.lib.db.QueryManager;
import com.chdc.qr.lib.db.QueryResult;
import com.chdc.qr.mdl.IncomeRecord;
import com.chdc.qr.mdl.ItemInfoPool;
import com.chdc.qr.mdl.PeopleInfo;
import com.chdc.qr.mdl.StaticProperties;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Vector;

public class IncomeListHandler implements QRResources {
    private static final class Creator {
        private static final IncomeListHandler inst = new IncomeListHandler();
    }

    private static final String QuerySelectAll = "" +
            "SELECT ODR, TYPE, ITEM, PEOPLE, PRICE FROM INCOME_INFO WHERE DATE=%d ORDER BY ODR";
    private static final String QuerySelectEach = "" +
            "SELECT ODR, TYPE, ITEM, PEOPLE, PRICE FROM INCOME_INFO WHERE DATE=%d AND ODR=%d";
    private static final String QuerySelect4CheckExist = "" +
            "SELECT ODR, TYPE, ITEM, PEOPLE, PRICE FROM INCOME_INFO WHERE DATE=%d AND TYPE=%d AND ITEM=%d AND PEOPLE=%d";

    private static final String QueryInsert = "" +
            "INSERT INTO INCOME_INFO (DATE, ODR, TYPE, ITEM, PEOPLE, PRICE) VALUES (%d, %d, %d, %d, %d, %d);";

    private static final String QueryUpdate = "" +
            "UPDATE INCOME_INFO SET PRICE=%d WHERE DATE=%d AND ODR=%d";

    private static final String QueryDelete = "" +
            "DELETE FROM INCOME_INFO WHERE DATE=%d AND ODR=%d";

    private static final QueryManager queryManager = new QueryManager();

    private JTable incomeListTable = null;

    private IncomeListHandler() {
    }

    public static IncomeListHandler getInstance() {
        return Creator.inst;
    }

    public JTable getIncomeListTable() {
        return incomeListTable;
    }

    public void setIncomeListTable(JTable incomeListTable) {
        this.incomeListTable = incomeListTable;
    }

    private static NumberFormat numberFormat = NumberFormat.getNumberInstance();
    public void addIncome(int type, int itemCode, int peopleCode, String peopleName, int price) throws Exception {
        int odr = StaticProperties.getNextOdr();

        try {
            queryManager.executeUpdate(String.format(QueryInsert,
                    StaticProperties.getDate(),
                    odr, type, itemCode, peopleCode, price));
        } catch (SQLException e) {
            throw new Exception("DB에 추가하지 못했습니다. - " + e.getMessage());
        }

        DefaultTableModel model = (DefaultTableModel) incomeListTable.getModel();

        Vector row = new Vector();

        row.add(model.getRowCount() + 1);
        row.add(ItemInfoPool.getInstance().get(type, itemCode).getName());
        row.add(peopleName);
        row.add(numberFormat.format(price));

        model.insertRow(0, row);

        SummaryListHandler.getInstance().updateSummary();
    }

    public void updateIncome(int odr, int price) throws Exception {
        try {
            queryManager.executeUpdate(String.format(QueryUpdate,
                    price, StaticProperties.getDate(), odr));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("금액 변경에 실패했습니다. - " + e.getMessage());
        }

        DefaultTableModel model = (DefaultTableModel) incomeListTable.getModel();

        int rowOdr;
        for (int i = 0; i < model.getRowCount(); i++) {
            rowOdr = (Integer) model.getValueAt(i, 0);
            if (rowOdr == odr) {
                model.setValueAt(numberFormat.format(price), i, 3);
                break;
            }
        }

        SummaryListHandler.getInstance().updateSummary();
    }

    public void deleteIncome(int odr) throws Exception {
        try {
            queryManager.executeUpdate(String.format(QueryDelete,
                    StaticProperties.getDate(), odr));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("정보 삭제에 실패했습니다. - " + e.getMessage());
        }

        DefaultTableModel model = (DefaultTableModel) incomeListTable.getModel();

        int rowOdr;
        for (int i = 0; i < model.getRowCount(); i++) {
            rowOdr = (Integer) model.getValueAt(i, 0);
            if (rowOdr == odr) {
                model.removeRow(i);
                break;
            }
        }

        StaticProperties.setOdr(getMaxOrder());

        SummaryListHandler.getInstance().updateSummary();
    }

    public void loadIncomeByDate() throws Exception {
        StaticProperties.setOdr(0);

        // clear incomeListTable
        DefaultTableModel model = (DefaultTableModel) incomeListTable.getModel();
        model.setRowCount(0);

        // load from DB
        QueryResult lists;
        try {
            lists = queryManager.executeQuery(String.format(QuerySelectAll,
                    StaticProperties.getDate()));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("정보를 조회할 수 없습니다. - " + e.getMessage());
        }

        // add to incomeListTable
        int odr = 0;
        Vector row;
        for (List record : lists) {
            row = new Vector();

            odr =queryManager.getInt(record.get(0));
            row.add(odr);
            row.add(ItemInfoPool.getInstance().get(
                    queryManager.getInt(record.get(1)),
                    queryManager.getInt(record.get(2))
            ).getName());
            row.add(PeopleInfo.getPeopleInfo(queryManager.getInt(record.get(3))).getName());
            row.add(numberFormat.format(queryManager.getInt(record.get(4))));

            model.insertRow(0, row);
        }

        StaticProperties.setOdr(odr);

        SummaryListHandler.getInstance().updateSummary();
    }

    public boolean checkExist(int type, int itemCode, int peopleCode) throws Exception {
        QueryResult lists = null;
        try {
            lists = queryManager.executeQuery(String.format(QuerySelect4CheckExist,
                    StaticProperties.getDate(), type, itemCode, peopleCode));
        } catch (SQLException e) {
            throw new Exception("수입 내역 조회 실패 - " + e.getMessage());
        }

        return lists.isEmpty();
    }

    public IncomeRecord getIncomeRecord(int ord) throws Exception {
        QueryResult lists = null;
        try {
            lists = queryManager.executeQuery(String.format(QuerySelectEach, StaticProperties.getDate(), ord)) ;
        } catch (SQLException e) {
            throw new Exception("수입 내역 조회 실패 - " + e.getMessage());
        }

        if (lists.isEmpty()) {
            throw new Exception("해당 정보가 DB에 없습니다.(????)");
        }

        List record = lists.get(0);

        return new IncomeRecord(ord,
                queryManager.getInt(record.get(1)),
                queryManager.getInt(record.get(2)),
                queryManager.getInt(record.get(3)),
                queryManager.getInt(record.get(4)));
    }

    public int getMaxOrder() {
        DefaultTableModel model = (DefaultTableModel) incomeListTable.getModel();

        if (model.getRowCount() > 0) {
            return (Integer) model.getValueAt(0,0);
        } else {
            return 0;
        }
    }
}