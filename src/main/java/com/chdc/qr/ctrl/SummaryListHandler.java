package com.chdc.qr.ctrl;

import com.chdc.qr.lib.db.QueryManager;
import com.chdc.qr.mdl.StaticProperties;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Vector;

public class SummaryListHandler {
    private JTable summaryTable = null;
    private JTextField tfTotalCount;
    private JTextField tfTotalPrice;

    private static final String QuerySummary = "" +
            "SELECT NAME, COUNT(PRICE), SUM(PRICE) " +
            "FROM INCOME_INFO, ITEM_INFO " +
            "WHERE DATE=%d AND INCOME_INFO.TYPE=ITEM_INFO.TYPE AND INCOME_INFO.ITEM=ITEM_INFO.CODE " +
            "GROUP BY ITEM, NAME ORDER BY ITEM";

    private static final String QuerySummaryAll = "" +
            "SELECT COUNT(ODR), SUM(PRICE) FROM INCOME_INFO WHERE DATE=%d";

    private static final QueryManager queryManager = new QueryManager();

    private static final class Creator {
        private static final SummaryListHandler inst = new SummaryListHandler();
    }

    public SummaryListHandler() {
    }

    public static SummaryListHandler getInstance() {
        return Creator.inst;
    }

    public JTable getSummaryTable() {
        return summaryTable;
    }

    public void setSummaryTable(JTable summaryTable) {
        this.summaryTable = summaryTable;
    }

    public JTextField getTfTotalCount() {
        return tfTotalCount;
    }

    public void setTfTotalCount(JTextField tfTotalCount) {
        this.tfTotalCount = tfTotalCount;
    }

    public JTextField getTfTotalPrice() {
        return tfTotalPrice;
    }

    public void setTfTotalPrice(JTextField tfTotalPrice) {
        this.tfTotalPrice = tfTotalPrice;
    }

    private static NumberFormat numberFormat = NumberFormat.getNumberInstance();
    public void updateSummary() {
        if (summaryTable == null) return;
        if (tfTotalCount == null) return;
        if (tfTotalPrice == null) return;

        // load from DB
        List<List> lists;
        try {
            lists = queryManager.executeQuery(String.format(QuerySummary,
                    StaticProperties.getDate()));
        } catch (SQLException e) {
            new Exception("요약 정보를 조회할 수 없습니다.", e).printStackTrace();
            return;
        }

        lists.remove(0);

        // clear incomeListTable
        DefaultTableModel model = (DefaultTableModel) summaryTable.getModel();
        model.setRowCount(0);

        // add to incomeListTable
        int odr = 0;
        Vector row;
        for (List record : lists) {
            row = new Vector();

            row.add(queryManager.getString(record.get(0)));
            row.add(queryManager.getInt(record.get(1)));
            row.add(numberFormat.format(queryManager.getInt(record.get(2))));

            model.insertRow(0, row);
        }

        // load from DB
        try {
            lists = queryManager.executeQuery(String.format(QuerySummaryAll,
                    StaticProperties.getDate()));
        } catch (SQLException e) {
            new Exception("전체 요약 정보를 조회할 수 없습니다.", e).printStackTrace();
            return;
        }

        lists.remove(0);

        List record = lists.get(0);
        int count = queryManager.getInt(record.get(0));
        this.tfTotalCount.setText(count + "");
        if (count == 0) {
            this.tfTotalPrice.setText("0");
        } else {
            this.tfTotalPrice.setText(numberFormat.format(queryManager.getInt(record.get(1))));
        }
    }
}
