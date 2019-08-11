package com.chdc.qr.ctrl;

import com.chdc.qr.QRResources;
import com.chdc.qr.lib.db.QueryManager;
import com.chdc.qr.lib.db.QueryResult;
import com.chdc.qr.mdl.LabelInfo;
import com.chdc.qr.mdl.StaticProperties;
import com.chdc.qr.lib.PrefUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LabelListXlsGenerator implements QRResources {

    private final String QuerySelectCustomPeople = "" +
            "SELECT NAME, SECTION, DUTY FROM CUSTOM_PEOPLE_INFO WHERE CODE=%d;";

    private QueryManager qm = new QueryManager();

    private enum WriteColumn {
        Type,
        Duty,
        Section,
        Name,
        Qr;
    }

    private JFrame owner;

    private String path = "";

    public LabelListXlsGenerator(JFrame owner) {
        this.owner = owner;
    }

    public void generate() {
        // 대상 파일들 선택
        File[] files = selectFiles();
        // 선택 파일들로 부터 대상 정보 read
        List<LabelInfo> labelInfos = readAndFilter(files);
        // 출력 대상 write
        writeXlsx(labelInfos);
    }

    private File[] selectFiles() {
        JFileChooser chooser = new JFileChooser(new File(PrefUtils.get(SelectedDir, ".")));

        FileNameExtensionFilter filter = new FileNameExtensionFilter("xlsx 파일","xlsx");
        chooser.setFileFilter(filter);
        chooser.setMultiSelectionEnabled(true);

        int ret=chooser.showOpenDialog(null);
        if(ret!=JFileChooser.APPROVE_OPTION){
            JOptionPane.showMessageDialog(null, "파일을 선택하지 않았습니다.","경고",JOptionPane.WARNING_MESSAGE);
            return null;
        }

        File[] files = chooser.getSelectedFiles();

        PrefUtils.set(SelectedDir, files[0].getParent());

        return files;
    }

    private List<LabelInfo> readAndFilter(File[] files) {
        List<LabelInfo> labelInfos = new ArrayList<>();

        for (File file : files) {
            this.path = file.getParent();
            LabelListXlsxReader reader = new LabelListXlsxReader(file);
            try {
                labelInfos.addAll(reader.read());
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(owner, "파일 열기 실패. - " + file.getName() + " > " + e.getMessage());
            } catch (InvalidFormatException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(owner, "파일 열기 실패. - " + file.getName() + " > " + e.getMessage());
            }
        }

        return labelInfos;
    }

    private void writeXlsx(List<LabelInfo> labelInfos) {
        createWorkbook(labelInfos);
    }

    private void createWorkbook(List<LabelInfo> labelInfos) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Data");

            createHeaderRow(sheet);
            writeContents(sheet, labelInfos);

            try (FileOutputStream fileOut = new FileOutputStream(
                    this.path + "\\LabelInfo" + "-" + StaticProperties.getDate() + "-label.xlsx")) {
                wb.write(fileOut);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(owner, "실패했습니다. - " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(owner, "실패했습니다. - " + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(owner, "실패했습니다. - " + e.getMessage());
        }
        JOptionPane.showMessageDialog(owner, "저장 완료");
    }

    private void writeContents(Sheet sheet, List<LabelInfo> labelInfos) {
        int rowIndex = 1;

        for (LabelInfo labelInfo : labelInfos) {
            List customPeople = getCustomPeople(labelInfo.getCode());

            Row row = sheet.createRow(rowIndex++);

            int col = 0;
            if (customPeople == null) {
                row.createCell(col++).setCellValue(labelInfo.getType());
                row.createCell(col++).setCellValue(labelInfo.getDuty());
                row.createCell(col++).setCellValue(labelInfo.getSection());
                row.createCell(col++).setCellValue(labelInfo.getName());
                row.createCell(col++).setCellValue(labelInfo.getQrCode());
            } else {
                row.createCell(col++).setCellValue(labelInfo.getType());
                row.createCell(col++).setCellValue(customPeople.get(2) + "");
                row.createCell(col++).setCellValue(customPeople.get(1) + "");
                row.createCell(col++).setCellValue(customPeople.get(0) + "");
                row.createCell(col++).setCellValue(labelInfo.getQrCode());
            }
        }
    }

    private List getCustomPeople(int code) {
        String query = String.format(QuerySelectCustomPeople, code);

        QueryResult results = null;
        try {
            results = qm.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (results.isEmpty()) return null;
        else return results.get(0);
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

        int pos = 0;
        for (WriteColumn column : WriteColumn.values()) {
            row.createCell(pos++).setCellValue(column.name());
        }
    }
}
