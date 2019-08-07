package com.chdc.qr.ctrl;

import com.chdc.qr.QRResources;
import com.chdc.qr.mdl.LabelInfo;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LabelListXlsxReader implements QRResources {
    private final File file;

    private enum ReadColumn {
        Type("1단계명칭"),
        Statement("전표번호"),
        Code("CODE"),
        Id("ID"),
        CodedName("이름");

        final String headerName;
        int pos;

        ReadColumn(String headerName) {
            this.headerName = headerName;
        }

        public String getHeaderName() {
            return headerName;
        }

        public int getPos() {
            return pos;
        }

        public void setPos(int pos) {
            this.pos = pos;
        }
    }

    public LabelListXlsxReader(File file) {
        this.file = file;
    }

    public List<LabelInfo> read() throws IOException, InvalidFormatException {
        Workbook workbook = WorkbookFactory.create(this.file);
        Sheet sheet = workbook.getSheetAt(0);
        Header header = sheet.getHeader();

        // 년도, 전표번호, CODE, 날짜, 전체항목, 분류항목, ID, 이름, 금액, 배우자, 비고, 1단계명칭, 2단계명칭, 3단계명칭, 4단계명칭
        int rowsCount = sheet.getLastRowNum();

        if (rowsCount > 0) {
            List<LabelInfo> labelInfos = new ArrayList<>();

            markColumnIndexes(sheet.getRow(0));
            for (int i = 1; i <= rowsCount; i++) {
                Row row = sheet.getRow(i);

                Cell cell = row.getCell(ReadColumn.Statement.getPos());
                if(cell == null
                        || cell.getCellTypeEnum() == CellType.BLANK
                        || cell.getCellTypeEnum() != CellType.NUMERIC
                        || cell.getNumericCellValue() != 0.0f) // 0인 것들만을 대상으로 합니다.
                    continue;

                cell = row.getCell(ReadColumn.Type.getPos());
                if(cell == null
                        || cell.toString().trim().equals("")
                        || cell.getStringCellValue().trim().equals("")
                        || cell.getCellTypeEnum() == CellType.BLANK)
                    continue;

                String type = cell.toString().trim();

                cell = row.getCell(ReadColumn.Code.getPos());
                if(cell == null
                        || cell.toString().trim().equals("")
                        || cell.getCellTypeEnum() == CellType.BLANK)
                    continue;

                int code = (int) Double.parseDouble(cell.toString().trim());

                cell = row.getCell(ReadColumn.Id.getPos());
                if(cell == null
                        || cell.toString().trim().equals("")
                        || cell.getCellTypeEnum() == CellType.BLANK)
                    continue;

                int id = (int) Double.parseDouble(cell.toString().trim());

                cell = row.getCell(ReadColumn.CodedName.getPos());
                if(cell == null
                        || cell.toString().trim().equals("")
                        || cell.getStringCellValue().trim().equals("")
                        || cell.getCellTypeEnum() == CellType.BLANK)
                    continue;

                String codedName = cell.toString().trim();

                LabelInfo labelInfo = new LabelInfo(
                        FinanceType.getByName(type).getCode(), code, id, codedName);

                if (labelInfo.isLabelTarget()) {
                    getList(code).add(labelInfo);
                }
            }

            for (int code : hashLabelInfoList.keySet()) {
                labelInfos.addAll(hashLabelInfoList.get(code));
            }

            return labelInfos;
        }
        return null;
    }

    private HashMap<Integer, List<LabelInfo>> hashLabelInfoList = new HashMap<>();
    private List<LabelInfo> getList(int code) {
        if (hashLabelInfoList.containsKey(code)) {
            return hashLabelInfoList.get(code);
        } else {
            List<LabelInfo> labelInfoList = new ArrayList<>();
            hashLabelInfoList.put(code, labelInfoList);

            return labelInfoList;
        }
    }

    private void markColumnIndexes(Row headerRow) {
        HashMap<String, ReadColumn> hashColumn = new HashMap<>();
        for (ReadColumn col : ReadColumn.values()) {
            hashColumn.put(col.getHeaderName(), col);
        }

        int colCount = headerRow.getLastCellNum();
        for (int i = 0; i < colCount; i++) {
            Cell cell = headerRow.getCell(i);
            String name = "";
            switch (cell.getCellTypeEnum()) {
                case STRING:
                    name = cell.getStringCellValue().trim();
                    break;
            }

            if (hashColumn.containsKey(name)) {
                hashColumn.get(name).setPos(i);
            }
        }
    }
}
