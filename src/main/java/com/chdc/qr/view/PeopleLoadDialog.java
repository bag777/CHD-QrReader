/*
 * Created by JFormDesigner on Thu Aug 09 23:31:47 KST 2018
 */

package com.chdc.qr.view;

import com.chdc.qr.ctrl.PeopleInfoLoader;
import com.chdc.qr.lib.CustomDispatcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author 준희 원
 */
public class PeopleLoadDialog extends JDialog {
    public PeopleLoadDialog(Frame owner) {
        super(owner);
        initComponents();
    }

    public PeopleLoadDialog(Dialog owner) {
        super(owner);
        initComponents();
    }

    private void btCloseActionPerformed(ActionEvent e) {
        dispose();
    }

    private void btLoadActionPerformed(ActionEvent e) {
        try {
            int[] count = load();
//            JOptionPane.showMessageDialog(this, count + "명의 정보를 가져왔습니다.");
            JOptionPane.showMessageDialog(this, "* 추가 : " + count[0]
                    + "\n* 변경 : " + count[1]);
            dispose();
        } catch (Exception e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(this, "정보 가져오기 실패 - " + e1.getMessage());
        }
    }

    private int[] load() throws Exception {
        return new PeopleInfoLoader().load(taInfo.getText());
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        scrollPane1 = new JScrollPane();
        taInfo = new JTextArea();
        panel2 = new JPanel();
        btLoad = new JButton();
        btClose = new JButton();

        //======== this ========
        setTitle("\uc131\ub3c4 \uc778\uba85\ubd80 \uac31\uc2e0");
        setMinimumSize(new Dimension(800, 600));
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== scrollPane1 ========
        {
            scrollPane1.setViewportView(taInfo);
        }
        contentPane.add(scrollPane1, BorderLayout.CENTER);

        //======== panel2 ========
        {
            panel2.setLayout(new FlowLayout(FlowLayout.TRAILING));

            //---- btLoad ----
            btLoad.setText("\uc801\uc6a9");
            btLoad.addActionListener(e -> btLoadActionPerformed(e));
            panel2.add(btLoad);

            //---- btClose ----
            btClose.setText("\ub2eb\uae30");
            btClose.addActionListener(e -> btCloseActionPerformed(e));
            panel2.add(btClose);
        }
        contentPane.add(panel2, BorderLayout.PAGE_END);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JScrollPane scrollPane1;
    private JTextArea taInfo;
    private JPanel panel2;
    private JButton btLoad;
    private JButton btClose;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
