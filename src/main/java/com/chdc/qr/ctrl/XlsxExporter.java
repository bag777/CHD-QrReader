package com.chdc.qr.ctrl;

import com.chdc.qr.QRResources;
import com.chdc.qr.lib.PrefUtils;
import com.chdc.qr.lib.db.QueryManager;
import com.chdc.qr.lib.db.QueryResult;
import com.chdc.qr.mdl.ItemInfo;
import com.chdc.qr.mdl.ItemInfoPool;
import com.chdc.qr.mdl.StaticProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class XlsxExporter implements QRResources {
    private static final String[] Headers =
            { "날짜", "주", "CODE", "교번", "교인명", "배우자", "금액", "비고", };

    private static final String QuerySelect = "" +
            "SELECT I.TYPE, I.ITEM, I.PEOPLE, P.NAME, I.PRICE " +
            "FROM INCOME_INFO I, PEOPLE_INFO P " +
            "WHERE I.PEOPLE = P.CODE AND DATE=%d";

    private JFrame owner;

    public XlsxExporter(JFrame owner) {
        this.owner = owner;
    }

    public void export() {
        query();
        for (FinanceType type : FinanceType.values()) {
            createWorkbook(type);
        }
        JOptionPane.showMessageDialog(owner, "저장 완료했습니다.");
    }

    private QueryResult lists = null;
    private void query() {
        QueryManager queryManager = new QueryManager();

        try {
            lists = queryManager.executeQuery(String.format(QuerySelect, StaticProperties.getDate()));
        } catch (SQLException e) {
            e.printStackTrace();
            // Show Dialog
        }
        log.info("[Total] " + lists.size());
    }

    private void createWorkbook(FinanceType type) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet(type.getName());

            createHeaderRow(sheet);
            writeContents(sheet, type);

            String dir = PrefUtils.get(SelectedDir, ".");
            try (FileOutputStream fileOut = new FileOutputStream(
                    dir + File.separator +type.getName() + "-" + StaticProperties.getDate() + ".xlsx")) {
                wb.write(fileOut);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(owner, "엑셀 저장에 실패했습니다. - " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(owner, "엑셀 저장에 실패했습니다. - " + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(owner, "엑셀 저장에 실패했습니다. - " + e.getMessage());
        }
    }

    private void writeContents(Sheet sheet, FinanceType type) {
        int rowIndex = 1;

//        ItemInfo itemInfo;
        int itemCode;
        for (List rowInfo : lists) {
            log.info(" > " + rowInfo);
            itemCode = QueryManager.getInt(rowInfo.get(1));
//            itemInfo = ItemInfoPool.getInstance().get(type.getCode(), itemCode);

//            "SELECT I.TYPE, I.ITEM, I.PEOPLE, P.NAME, I.PRICE " +

            if (type.getCode() == QueryManager.getInt(rowInfo.get(0))) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(StaticProperties.getDate());
                row.createCell(2).setCellValue(itemCode);
                row.createCell(3).setCellValue(QueryManager.getInt(rowInfo.get(2)));
                row.createCell(4).setCellValue(QueryManager.getString(rowInfo.get(3)));
                row.createCell(6).setCellValue(QueryManager.getInt(rowInfo.get(4)));
            }
        }

        log.info("[" + type.getName() + "] : " + (rowIndex - 1));
    }

    private static Cell getCell(Sheet sheet, int row, int col) {
        Row targetRow = sheet.getRow(row);
        if (targetRow == null) {
            targetRow = sheet.createRow(row);
        }

        Cell targetCell = targetRow.getCell(col);
        if (targetCell == null) {
            targetCell = targetRow.createCell(col);
        }

        return targetCell;
    }

    private void createHeaderRow(Sheet sheet) {
        Row row = sheet.createRow(0);

        for (int i = 0; i < Headers.length; i++) {
            row.createCell(i).setCellValue(Headers[i]);
        }
    }
}
