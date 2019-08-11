package com.chdc.qr.lib.db;

import org.apache.commons.dbcp2.BasicDataSource;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QueryManager {
    public int executeUpdate(String query) throws SQLException {
        BasicDataSource bds = DataSource.getInstance().getBds();
        try (Connection connection = bds.getConnection();
             Statement statement = connection.createStatement()) {
            return statement.executeUpdate(query);
        }
    }

    public QueryResult executeQuery(String query) throws SQLException {
        BasicDataSource bds = DataSource.getInstance().getBds();

        try (Connection connection = bds.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            return getResults(resultSet);
        }
    }

    private static QueryResult getResults(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int colCount = metaData.getColumnCount();

        List<String> headerList = new ArrayList<>();

        for (int i = 1; i <= colCount; i++) {
            headerList.add(metaData.getColumnName(i));
        }

        QueryResult result = new QueryResult();

        result.setHeader(headerList);

        ArrayList record;
        while (resultSet.next()) {
            record = new ArrayList();

            for (int i = 1; i <= colCount; i++) {
                record.add(resultSet.getObject(i));
            }
            result.add(record);
        }

        return result;
    }

    public static int getInt(Object obj) {
        if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).intValue();
        } else {
            try {
                return Integer.parseInt(obj + "");
            } catch (NumberFormatException ex) {
                return -1;
            }
        }
    }

    public static String getString(Object obj) {
        return obj + "";
    }
}
