package com.chdc.qr.view;

import com.chdc.qr.lib.db.QueryManager;
import com.chdc.qr.lib.db.QueryResult;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomPeopleInfoDialog extends JDialog {
    private final String QuerySelectPeople = "" +
            "SELECT CODE, NAME, GENDER, AREA, SECTION, DUTY, CONTACT, AGE " +
            "FROM PEOPLE_INFO WHERE NAME LIKE '%%%s%%' ORDER BY NAME";

    private final String QuerySelectCustomPeople = "" +
            "SELECT NAME, SECTION, DUTY " +
            "FROM CUSTOM_PEOPLE_INFO WHERE CODE=%d";

    private final String QueryInsertCustomPeople = "" +
            "INSERT INTO CUSTOM_PEOPLE_INFO (CODE, NAME, SECTION, DUTY) VALUES " +
            "(%d, '%s', '%s', '%s')";

    private final String QueryDeleteCustomPeople = "" +
            "DELETE FROM CUSTOM_PEOPLE_INFO WHERE CODE=%d";

    private JPanel contentPane;
    private JButton btSave;
    private JButton btClose;
    private JTextField tfName;
    private JButton btSearch;
    private JTable tbList;
    private JTextField tfCustName;
    private JTextField tfCustSection;
    private JTextField tfCustDuty;
    private JLabel lbCode;
    private JLabel lbName;
    private JLabel lbGender;
    private JLabel lbArea;
    private JLabel lbSection;
    private JLabel lbDuty;
    private JLabel lbContact;
    private JLabel lbAge;

    private QueryManager qm = new QueryManager();

    private int selectedCode = 0;

    public CustomPeopleInfoDialog(Frame owner) {
        super(owner, "인명 정보 가공");
        setContentPane(contentPane);
        setModal(true);
//        getRootPane().setDefaultButton(btSave);

        btSave.addActionListener(e -> {
//                onOK();
            saveAndClear();
        });

        btClose.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
//        contentPane.registerKeyboardAction(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                onCancel();
//            }
//        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        tfName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    searchPeople();
                }
            }
        });
        btSearch.addActionListener(e -> {
            searchPeople();
        });
        tbList.getSelectionModel().addListSelectionListener(e -> {
            if (tbList.getRowCount() == 0) return;

            int selRow = e.getLastIndex();

            List row = new ArrayList();

            for (int c = 0; c < tbList.getColumnCount(); c++) {
                row.add(tbList.getValueAt(selRow, c));
            }

            setSelectedPeopleInfo(row);
            tfCustName.requestFocus();
        });
        tfCustName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tfCustName.selectAll();
            }
        });
        tfCustSection.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tfCustSection.selectAll();
            }
        });
        tfCustDuty.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tfCustDuty.selectAll();
            }
        });
        tfCustName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    tfCustName.setText(lbName.getText());
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    clear();
                }
            }
        });
        tfCustSection.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    tfCustSection.setText(lbSection.getText());
                }
            }
        });
        tfCustDuty.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    tfCustDuty.setText(lbDuty.getText());
                }
            }
        });
    }

    private void searchPeople() {
        String name = tfName.getText().trim();
        if (name.isEmpty()) {
            tfName.setText("");
            tfName.requestFocus();
            return;
        }

        String query = String.format(QuerySelectPeople, name);

        QueryResult results = null;
        try {
            results = qm.executeQuery(query);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "정보를 조회 할 수 없습니다.\n" + e.getMessage(),
                    "조회오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setResultOnTable(results);
    }

    private final String[] Headers = { "번호", "이름", "성별", "교구", "목장", "직책", "연락처", "나이" };
    private void setResultOnTable(List<List> results) {
        String[][] data = new String[results.size()][Headers.length];
        int r = 0, c = 0;
        for (List row : results) {
            c = 0;
            for (Object val : row) {
                data[r][c] = val + "";
                c++;
            }
            r++;
        }

        if (tbList.getModel() != null) {
            ((DefaultTableModel) tbList.getModel()).setRowCount(0);
        }
        DefaultTableModel tableModel = new DefaultTableModel(data, Headers);
        tbList.setModel(tableModel);
        tbList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbList.setRowSelectionInterval(0, 0);
    }

    private void setSelectedPeopleInfo(List row) {
        int col = 0;
        this.selectedCode = Integer.parseInt(row.get(col++) + "");

        lbCode.setText(this.selectedCode + "");
        lbName.setText(row.get(col++) + "");
        lbGender.setText(row.get(col++) + "");
        lbArea.setText(row.get(col++) + "");
        lbSection.setText(row.get(col++) + "");
        lbDuty.setText(row.get(col++) + "");
        lbContact.setText(row.get(col++) + "");
        lbAge.setText(row.get(col++) + "");

        List customPeopleInfo = getCustomPeopleInfo(this.selectedCode);
        if (customPeopleInfo == null) {
            tfCustName.setText("");
            tfCustSection.setText("");
            tfCustDuty.setText("");
        } else {
            int pos = 0;
            tfCustName.setText(customPeopleInfo.get(pos++) + "");
            tfCustSection.setText(customPeopleInfo.get(pos++) + "");
            tfCustDuty.setText(customPeopleInfo.get(pos++) + "");
        }
    }

    private List getCustomPeopleInfo(int code) {
        String query = String.format(QuerySelectCustomPeople, code);

        QueryResult results = null;
        try {
            results = qm.executeQuery(query);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "변경 정보를 조회 할 수 없습니다.\n" + e.getMessage(),
                    "변경정보 조회실패", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void saveAndClear() {
        if (this.selectedCode == 0) return;

        String query = String.format(QueryDeleteCustomPeople, this.selectedCode);
        try {
            qm.executeUpdate(query);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "이전 변경정보를 삭제할 수 없습니다.\n" + e.getMessage(),
                    "이전 변경정보 삭제실패", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String custName = tfCustName.getText().trim();
        String custSection = tfCustSection.getText().trim();
        String custDuty = tfCustDuty.getText().trim();

        if (custName.isEmpty()
                && custSection.isEmpty()
                && custDuty.isEmpty()) {
            // Do not insert(clear)
        } else {
            query = String.format(QueryInsertCustomPeople,
                    this.selectedCode, custName, custSection, custDuty);
            try {
                qm.executeUpdate(query);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "변경정보를 저장할 수 없습니다.\n" + e.getMessage(),
                        "변경정보 저장실패", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        clear();
    }

    private void clear() {
        tfName.setText("");

        ((DefaultTableModel) tbList.getModel()).setRowCount(0);

        lbCode.setText("-");
        lbName.setText("-");
        lbArea.setText("-");
        lbSection.setText("-");
        lbDuty.setText("-");
        lbContact.setText("-");
        lbAge.setText("-");

        tfCustName.setText("");
        tfCustSection.setText("");
        tfCustDuty.setText("");

        selectedCode = 0;

        tfName.requestFocus();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        CustomPeopleInfoDialog dialog = new CustomPeopleInfoDialog(null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
