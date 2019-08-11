package com.chdc.qr.view;

import com.chdc.qr.QRResources;
import com.chdc.qr.lib.db.QueryManager;
import com.chdc.qr.lib.db.QueryResult;
import com.chdc.qr.mdl.ItemInfo;
import com.chdc.qr.mdl.ItemInfoPool;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class BatchItemFilterDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel filterListPane;

    public BatchItemFilterDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        custom();
    }

    private void custom() {
        this.filterListPane.setLayout(new GridLayout(0, 1, 5, 5));
    }

    private static final String Query_FilterList = "SELECT TYPE, ITEM FROM INCOME_INFO GROUP BY TYPE, ITEM ORDER BY TYPE, ITEM";
    private void getList() {
        QueryManager qm = new QueryManager();

        QueryResult lists = null;
        try {
            lists = qm.executeQuery(Query_FilterList);
        } catch (SQLException e) {
            log.error("Filter list query fail - " + Query_FilterList, e);
            return;
        }

        for (List row : lists) {
            int type = Integer.parseInt(row.get(0) + "");
            int code = Integer.parseInt(row.get(0) + "");
            ItemInfo itemInfo = ItemInfoPool.getInstance().get(type, code);
            if (itemInfo != null) {
                String info = String.format("%s > %s",
                        QRResources.FinanceType.getByCode(type).getName(),
                        itemInfo.getName()
                );
//                this.filterListPane.add(new)
            }
        }
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        BatchItemFilterDialog dialog = new BatchItemFilterDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
