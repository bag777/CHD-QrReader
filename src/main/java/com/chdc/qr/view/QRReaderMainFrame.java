/*
 * Created by JFormDesigner on Tue Aug 07 23:29:33 KST 2018
 */

package com.chdc.qr.view;

import com.chdc.qr.QRResources;
import com.chdc.qr.ctrl.*;
import com.chdc.qr.lib.CustomDispatcher;
import com.chdc.qr.mdl.*;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.JXDatePicker;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author unknown
 */
@Slf4j
public class QRReaderMainFrame extends JFrame implements QRResources {

    private Mode mode = Mode.Ready;

    private boolean thousandFlag = true;

    private int type;
    private int itemCode;
    private int peopleCode;

    private int lastType;
    private int lastItemCode;
    private int lastPeopleCode;

    private Webcam webcam = null;
    private WebcamPanel webcamPanel = null;

    public QRReaderMainFrame() {
        setLnF();
        initComponents();
        init();
        addListener();
        regComponents();
        try {
            initWebcam();
        } catch (InvocationTargetException e) {
            CustomDispatcher.invokeAndWait(() -> {
                JOptionPane.showMessageDialog(this, "웹캠 초기화 실패(1) : " + e.getMessage());
            });
        } catch (InterruptedException e) {
            CustomDispatcher.invokeAndWait(() -> {
                JOptionPane.showMessageDialog(this, "웹캠 초기화 실패(2) : " + e.getMessage());
            });
        }
        loadData();
    }

    private void loadData() {
        Date today = new Date(Calendar.getInstance().getTimeInMillis());
        int date = Integer.parseInt(dateFormat.format(today));
        StaticProperties.setDate(date);
        loadIncomeData(date);
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYYMMdd");

    private void loadIncomeData(int date) {
        try {
            IncomeListHandler.getInstance().loadIncomeByDate();
        } catch (Exception e1) {
            e1.printStackTrace();
            CustomDispatcher.invokeAndWait(() -> {
                JOptionPane.showMessageDialog(QRReaderMainFrame.this,
                        e1.getMessage());
            });
        }
    }

    private void addListener() {
        this.picker.addActionListener(e -> {
            int date = Integer.parseInt(dateFormat.format(picker.getDate()));
            StaticProperties.setDate(date);
            loadIncomeData(date);
        });
        this.ftfPrice.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    // 아직 입력 준비가 되어 있지 않을때는 동작을 막아줍니다.
                    if (mode == Mode.Ready) return;

                    int price = getPrice();
                    if (price > 0) {
                        switch(mode) {
                            case Insert:
                                try {
                                    IncomeListHandler.getInstance().addIncome(type, itemCode, peopleCode, lbName.getText(), price);
                                } catch (Exception e1) {
                                    JOptionPane.showMessageDialog(QRReaderMainFrame.this,
                                            e1.getMessage());
                                    return;
                                }
                                break;
                            case Update:
                                if (!updateIncome()) {
                                    return;
                                }
                        }
                        lastType = type;
                        lastItemCode = itemCode;
                        lastPeopleCode = peopleCode;
                    }
                    clearInfo();
                    camPane.requestFocus();
                    mode = Mode.Ready;
                }
                if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                    mode = Mode.Ready;
                    lastType = -1;
                    lastItemCode = -1;
                    lastPeopleCode = -1;
                    clearInfo();
                    setEditButtons(false);
                }
            }
        });

        this.ftfPrice.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                SwingUtilities.invokeLater(() -> ftfPrice.selectAll());
            }
        });

        this.tbIncome.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int selRow = tbIncome.getSelectedRow();

                if (selRow < 0) return;

                int ord = (Integer) tbIncome.getValueAt(tbIncome.getSelectedRow(), 0);
                setPastInfo(ord);
            }
        });
    }

    private int selectedOdr = -1;

    private void setPastInfo(int ord) {
        IncomeRecord incomeRecord = null;
        try {
            incomeRecord = IncomeListHandler.getInstance().getIncomeRecord(ord);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(QRReaderMainFrame.this,
                    "DB에서 선택한 정보를 찾지 못했습니다." +
                            "\n(" + e.getMessage() + ")");
            selectedOdr = -1;
            return;
        }

        mode = Mode.Update;
        this.selectedOdr = ord;

        setEditButtons(true);

        type = incomeRecord.getType();
        itemCode = incomeRecord.getItemCode();
        peopleCode = incomeRecord.getPeopleCode();

        ItemInfo itemInfo = ItemInfoPool.getInstance().get(type, itemCode);
        PeopleInfo peopleInfo = PeopleInfo.getPeopleInfo(peopleCode);

        lbItem12.setText("▣ " + itemInfo.getCategory());
        lbItem3.setText("▷ " + itemInfo.getName());
        lbPeopleInfo1.setText(String.format("%d %s(%d) %s",
                peopleInfo.getCode(),
                peopleInfo.getGender(),
                peopleInfo.getAge(),
                peopleInfo.getDuty()));
        lbPeopleInfo2.setText(String.format("%s %s %s",
                peopleInfo.getArea(),
                peopleInfo.getSection(),
                peopleInfo.getContact()));
        lbName.setText(peopleInfo.getName());

        if (ckbThousand.isSelected()) {
            if (incomeRecord.getPrice() % 1000 != 0) {
                ckbThousand.setSelected(false);
                tfThousand.setEnabled(false);
                tfThousand.setText("");
                ftfPrice.setText(incomeRecord.getPrice() + "");
            } else {
                ftfPrice.setText(incomeRecord.getPrice() / 1000 + "");
            }
        } else {
            tfThousand.setText("");
            ftfPrice.setText(incomeRecord.getPrice() + "");
        }

        ftfPrice.requestFocus();
    }

    private void setEditButtons(boolean enabled) {
        btEdit.setEnabled(enabled);
        btDelete.setEnabled(enabled);
        btCancel.setEnabled(enabled);
    }

    private int getPrice() {
        String priceStr = ftfPrice.getText();
        priceStr = priceStr.replaceAll(",", "");

        if (priceStr.isEmpty()) {
            CustomDispatcher.invokeAndWait(() -> {
                JOptionPane.showMessageDialog(this,
                        "금액을 입력해 주세요.");
            });
            return -1;
        }

        int price = Integer.parseInt(priceStr);
        if (thousandFlag) {
            price *= 1000;
        }

        return price;
    }

    private void regComponents() {
        IncomeListHandler.getInstance().setIncomeListTable(this.tbIncome);
        SummaryListHandler.getInstance().setSummaryTable(this.tbSummary);
        SummaryListHandler.getInstance().setTfTotalCount(this.tfTotalCount);
        SummaryListHandler.getInstance().setTfTotalPrice(this.tfTotalPrice);
    }

    private void setLnF() {
        SwingUtilities.invokeLater(() -> SubstanceLookAndFeel.setSkin(Skin.BusinessBlackSteelSkin.getSkin()));
    }

    private JXDatePicker picker = new JXDatePicker(Locale.KOREA);
    private void init() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 날짜 선택
        picker.setDate(Calendar.getInstance().getTime());
        picker.setFormats(new SimpleDateFormat("yyyy년 MM월 dd일")); //$NON-NLS-1$

        datePane.removeAll();
        datePane.add(picker, BorderLayout.CENTER);
        picker.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        // 금액 입력 필드 설정
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(0);
//        NumberFormatter nff = new NumberFormatter(numberFormat);

        NumberFormatter nff = new NumberFormatter(numberFormat) {
            // we have to allow the empty string, the call chain is
            //      DefaultFormatter
            //              DefaultDocumentFilter.remove
            //              replace
            //              canReplace
            //              isValidEdit
            //              stringToValue
            public Object stringToValue(String string)
                    throws ParseException {
                if (string == null || string.length() == 0) {
                    return null;
                }
                return super.stringToValue(string);
            }
        };

        nff.setAllowsInvalid(false);
//        nff.setAllowsInvalid(true);
        DefaultFormatterFactory factory = new DefaultFormatterFactory(nff);
        ftfPrice.setFormatterFactory(factory);

        clearInfo();
    }

    private void clearInfo() {
        selectedOdr = -1;
        lbItem12.setText("▣ -");
        lbItem3.setText("▷ -");
        lbPeopleInfo1.setText(" ");
        lbPeopleInfo2.setText(" ");
        lbName.setText("-");
//        ftfPrice.setText("");
    }

    private Mode setIncomeBase(int type, int itemCode, int peopleCode) {
        ItemInfo itemInfo = ItemInfoPool.getInstance().get(type, itemCode);
        PeopleInfo peopleInfo = PeopleInfo.getPeopleInfo(peopleCode);

        if (itemInfo == null) {
            CustomDispatcher.invokeAndWait(() -> {
                JOptionPane.showMessageDialog(this,
                        "알 수 없는 헌금항목입니다.(" + type + "," + itemCode + ")");
            });
            return Mode.Ready;
        }

        if (peopleInfo == null) {
            CustomDispatcher.invokeAndWait(() -> {
                JOptionPane.showMessageDialog(this,
                        "해당 성도의 정보를 찾을 수 없습니다.(" + peopleCode + ")");
            });
            return Mode.Ready;
        }

        this.type = type;
        this.itemCode = itemCode;
        this.peopleCode = peopleCode;

        lbItem12.setText("▣ " + itemInfo.getCategory());
        lbItem3.setText("▷ " + itemInfo.getName());
        lbPeopleInfo1.setText(String.format("%d %s(%d) %s",
                peopleInfo.getCode(),
                peopleInfo.getGender(),
                peopleInfo.getAge(),
                peopleInfo.getDuty()));
        lbPeopleInfo2.setText(String.format("%s %s %s",
                peopleInfo.getArea(),
                peopleInfo.getSection(),
                peopleInfo.getContact()));
        lbName.setText(peopleInfo.getName());
        btCancel.setEnabled(true);
        ftfPrice.requestFocus();

        try {
            if (!IncomeListHandler.getInstance().checkExist(type, itemCode, peopleCode)) {
                int code = JOptionPane.showConfirmDialog(this,
                        "이미 입력 되어 있습니다.\n" +
                                "중복 입력 하시겠습니까?",
                        "중복 입력 허용",
                        JOptionPane.OK_CANCEL_OPTION);
                if (code != JOptionPane.OK_OPTION) {
                    clearInfo();
                    return Mode.Ready;
                }
            }
        } catch (Exception e) {
            CustomDispatcher.invokeAndWait(() -> {
                JOptionPane.showMessageDialog(this,
                        "중복 입력을 확인할 수 없습니다.\n" + e.getMessage());
            });
            return Mode.Ready;
        }

        return Mode.Insert;
    }

    private void initWebcam() throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(() -> {
            Dimension size = Resolution.getSize();

            webcam = Webcam.getDefault();
            webcam.setCustomViewSizes(new Dimension[] { size });
            webcam.setViewSize(size);
            webcam.open();

            webcamPanel = new WebcamPanel(webcam);
            webcamPanel.setFPSDisplayed(true);
            webcamPanel.setImageSizeDisplayed(true);

//            final JHFlipFilter rotate = new JHFlipFilter(JHFlipFilter.FLIP_90CCW);
            final AdvancedFlipFilter rotate = new AdvancedFlipFilter(AdvancedFlipFilter.FLIP_90CCW);

            final WebcamPanel.Painter painter = webcamPanel.new DefaultPainter() {

                @Override
                public void paintImage(WebcamPanel owner, BufferedImage image, Graphics2D g2) {
                    image = rotate.filter(image, null);
                    super.paintImage(owner, image, g2);
                }
            };

            webcamPanel.setPainter(painter);

            // set cam pane size
            camPane.add(webcamPanel, BorderLayout.CENTER);
        });

        showWebcam();
    }

    private void showWebcam() {
        new Thread(() -> {
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mode != Mode.Ready) continue;

                Result result = null;
                BufferedImage image = null;

                if (webcam.isOpen()) {
                    if ((image = webcam.getImage()) == null) {
                        continue;
                    }

                    LuminanceSource souce = new BufferedImageLuminanceSource(image);
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(souce));

                    try {
                        result = new MultiFormatReader().decode(bitmap);
                    } catch (NotFoundException e) {
                        // fail thry, it means ther is no QR code in image
                    }
                }

                if (result != null) {
                    String[] elems = result.getText().split("[,]");
                    int type = Integer.parseInt(elems[0]);
                    int item, people;

                    // type 구분 필드 추가전 QR 인식을 위함
                    if (type > 2) {
                        item = type;
                        type = 2; // 선교계정
                        people = Integer.parseInt(elems[1]);
                    } else {
                        item = Integer.parseInt(elems[1]);
                        people = Integer.parseInt(elems[2]);
                    }

                    // Temporary
                    // 급하게 만드느랴 코드를 고려하지 않고 만듬(비전헌금)
                    if (item == 999999) {
                        type = 2;
                        item = 100101;
                    }

                    // 직전 입력 완료된 정보를 다시 읽게되면 무시한다.
                    if (lastType == type && lastItemCode == item && lastPeopleCode == people) continue;

                    mode = setIncomeBase(type, item, people);
                }
            } while (true);
        }).start();
    }

    private void ckbThousandItemStateChanged(ItemEvent e) {
        int state = e.getStateChange();
        if (state == ItemEvent.SELECTED) {
            thousandFlag = true;
            tfThousand.setText(",000");
        } else {
            thousandFlag = false;
            tfThousand.setText("");
        }
        ftfPrice.requestFocus();
    }

    private void btReloadPeoplesActionPerformed(ActionEvent e) {
        PeopleLoadDialog dialog = new PeopleLoadDialog(this);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void btLabelInfoActionPerformed(ActionEvent e) {
        LabelListXlsGenerator writer = new LabelListXlsGenerator(this);
        writer.generate();
    }

    private void btEditActionPerformed(ActionEvent e) {
        updateIncome();
    }

    private boolean updateIncome() {
        try {
            IncomeListHandler.getInstance().updateIncome(this.selectedOdr, getPrice());
        } catch (Exception e1) {
            JOptionPane.showMessageDialog(this, "변경정보 적용에 실패 했습니다. - " + e1.getMessage());
            return false;
        }
        clearInfo();

        this.mode = Mode.Update;

        mode = Mode.Ready;
        selectedOdr = -1;
        lastType = -1;
        lastItemCode = -1;
        lastPeopleCode = -1;
        setEditButtons(false);

        return true;
    }

    private void btDeleteActionPerformed(ActionEvent e) {
        deleteIncome();
    }

    private void deleteIncome() {
        try {
            IncomeListHandler.getInstance().deleteIncome(this.selectedOdr);
        } catch (Exception e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(this, e1.getMessage());
        }
        clearInfo();

        this.mode = Mode.Delete;

        mode = Mode.Ready;
        lastType = -1;
        lastItemCode = -1;
        lastPeopleCode = -1;
        btEdit.setEnabled(false);
        btDelete.setEnabled(false);
        btCancel.setEnabled(false);
    }

    private void btCancelActionPerformed(ActionEvent e) {
        clearInfo();

        this.mode = Mode.Ready;

        mode = Mode.Ready;
        lastType = -1;
        lastItemCode = -1;
        lastPeopleCode = -1;

        setEditButtons(false);
    }

    private void btExportXlsxActionPerformed(ActionEvent e) {
        XlsxExporter exporter = new XlsxExporter(this);
        exporter.export();
    }

    private void thisComponentResized(ComponentEvent e) {
        Dimension size = Resolution.getSize();
        final int oHeight = camPane.getHeight();
        final int oWidth = camPane.getWidth();

//        log.info(" resized >>>> " + oWidth + ", " + oHeight);
        final int width = size.height * oHeight / size.width;

        if (oWidth != width) {
            SwingUtilities.invokeLater(() -> {
                camPane.setPreferredSize(new Dimension(width, oHeight));
//                camPane.setSize(width, oHeight);
                //            revalidate();
                //            repaint();
                validate();
//                log.info(" > resize : " + width + ", " + oHeight);
            });
        }
    }

	private void btCustomActionPerformed(ActionEvent e) {
        CustomPeopleInfoDialog dialog = new CustomPeopleInfoDialog(this);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
	}

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		camPane = new JPanel();
		ctxPane = new JPanel();
		panel2 = new JPanel();
		datePane = new JPanel();
		lbDate = new JLabel();
		panel4 = new JPanel();
		panel5 = new JPanel();
		lbItem12 = new JLabel();
		lbItem3 = new JLabel();
		lbPeopleInfo1 = new JLabel();
		lbPeopleInfo2 = new JLabel();
		label7 = new JLabel();
		lbName = new JLabel();
		label8 = new JLabel();
		ftfPrice = new JFormattedTextField();
		tfThousand = new JTextField();
		ckbThousand = new JCheckBox();
		panel3 = new JPanel();
		btEdit = new JButton();
		btDelete = new JButton();
		btCancel = new JButton();
		panel10 = new JPanel();
		scrollPane2 = new JScrollPane();
		tbSummary = new JTable();
		panel9 = new JPanel();
		label13 = new JLabel();
		tfTotalCount = new JTextField();
		label14 = new JLabel();
		tfTotalPrice = new JTextField();
		panel11 = new JPanel();
		btReloadPeoples = new JButton();
		btCustom = new JButton();
		btExportXlsx = new JButton();
		btLabelInfo = new JButton();
		summaryPane = new JPanel();
		scrollPane1 = new JScrollPane();
		tbIncome = new JTable();

		//======== this ========
		setTitle("\ucc3d\ud6c8\ub300\uad50\ud68c QR\ucf54\ub4dc \uc785\ub825");
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				thisComponentResized(e);
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== camPane ========
		{
			camPane.setPreferredSize(new Dimension(350, 700));
			camPane.setLayout(new BorderLayout());
		}
		contentPane.add(camPane, BorderLayout.LINE_START);

		//======== ctxPane ========
		{
			ctxPane.setLayout(new BorderLayout());

			//======== panel2 ========
			{
				panel2.setPreferredSize(new Dimension(400, 330));
				panel2.setLayout(new BorderLayout());

				//======== datePane ========
				{
					datePane.setBorder(new TitledBorder(null, "\ub0a0\uc9dc", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("\ub9d1\uc740 \uace0\ub515", Font.BOLD, 12)));
					datePane.setLayout(new BorderLayout());

					//---- lbDate ----
					lbDate.setText("2018\ub144 8\uc6d4 12\uc77c");
					lbDate.setFont(new Font("\ub9d1\uc740 \uace0\ub515", Font.BOLD, 20));
					lbDate.setHorizontalAlignment(SwingConstants.LEFT);
					datePane.add(lbDate, BorderLayout.WEST);
				}
				panel2.add(datePane, BorderLayout.NORTH);

				//======== panel4 ========
				{
					panel4.setLayout(new BorderLayout());

					//======== panel5 ========
					{
						panel5.setBorder(new TitledBorder(null, "\ub0b4\uc5ed", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
							new Font("\ub9d1\uc740 \uace0\ub515", Font.BOLD, 12)));
						panel5.setLayout(new FormLayout(
							"10dlu, $lcgap, default, $ugap, default:grow, $lcgap, 0dlu, default, $lcgap, default",
							"2*(default, $lgap), 2*(default), 3*($lgap, default)"));

						//---- lbItem12 ----
						lbItem12.setText(" \u25a3 \uc77c\ubc18\uacc4\uc815 \ud2b9\ubcc4\ud5cc\uae08");
						lbItem12.setFont(new Font("\ub9d1\uc740 \uace0\ub515", Font.BOLD, 20));
						panel5.add(lbItem12, CC.xywh(1, 1, 10, 1));

						//---- lbItem3 ----
						lbItem3.setText("\u25b7 \uc120\uad50\ud5cc\uae08");
						lbItem3.setFont(new Font("\ub9d1\uc740 \uace0\ub515", Font.BOLD, 22));
						panel5.add(lbItem3, CC.xywh(3, 3, 8, 1));

						//---- lbPeopleInfo1 ----
						lbPeopleInfo1.setText("2889 \ub0a8(43) \uc11c\ub9ac\uc9d1\uc0ac");
						panel5.add(lbPeopleInfo1, CC.xywh(5, 5, 6, 1));

						//---- lbPeopleInfo2 ----
						lbPeopleInfo2.setText("3\uc9c0\uad6c 326\uad6c\uc5ed 010-4554-3854");
						panel5.add(lbPeopleInfo2, CC.xywh(5, 6, 6, 1));

						//---- label7 ----
						label7.setText("\uc774\ub984");
						label7.setFont(new Font("\ub9d1\uc740 \uace0\ub515", Font.PLAIN, 14));
						panel5.add(label7, CC.xy(3, 8));

						//---- lbName ----
						lbName.setText("\uc6d0\uc900\ud76c");
						lbName.setFont(new Font("\ub9d1\uc740 \uace0\ub515", Font.BOLD, 20));
						panel5.add(lbName, CC.xywh(5, 8, 6, 1));

						//---- label8 ----
						label8.setText("\uae08\uc561");
						label8.setFont(new Font("\ub9d1\uc740 \uace0\ub515", Font.PLAIN, 14));
						panel5.add(label8, CC.xy(3, 10));

						//---- ftfPrice ----
						ftfPrice.setFont(new Font("\ub9d1\uc740 \uace0\ub515", Font.PLAIN, 20));
						ftfPrice.setHorizontalAlignment(SwingConstants.RIGHT);
						ftfPrice.setText("123456");
						panel5.add(ftfPrice, CC.xy(5, 10));

						//---- tfThousand ----
						tfThousand.setMinimumSize(new Dimension(53, 37));
						tfThousand.setPreferredSize(new Dimension(53, 37));
						tfThousand.setFont(new Font("\ub9d1\uc740 \uace0\ub515", Font.PLAIN, 20));
						tfThousand.setEditable(false);
						tfThousand.setText(",000");
						panel5.add(tfThousand, CC.xy(8, 10, CC.FILL, CC.FILL));

						//---- ckbThousand ----
						ckbThousand.setText("\ucc9c\ub2e8\uc704");
						ckbThousand.setHorizontalAlignment(SwingConstants.TRAILING);
						ckbThousand.setSelected(true);
						ckbThousand.addItemListener(e -> ckbThousandItemStateChanged(e));
						panel5.add(ckbThousand, CC.xy(10, 10));

						//======== panel3 ========
						{
							panel3.setLayout(new FlowLayout(FlowLayout.TRAILING));

							//---- btEdit ----
							btEdit.setText("\uc218\uc815");
							btEdit.setEnabled(false);
							btEdit.addActionListener(e -> btEditActionPerformed(e));
							panel3.add(btEdit);

							//---- btDelete ----
							btDelete.setText("\uc0ad\uc81c");
							btDelete.setEnabled(false);
							btDelete.addActionListener(e -> btDeleteActionPerformed(e));
							panel3.add(btDelete);

							//---- btCancel ----
							btCancel.setText("\ucde8\uc18c");
							btCancel.setEnabled(false);
							btCancel.addActionListener(e -> btCancelActionPerformed(e));
							panel3.add(btCancel);
						}
						panel5.add(panel3, CC.xywh(1, 12, 10, 1));
					}
					panel4.add(panel5, BorderLayout.NORTH);

					//======== panel10 ========
					{
						panel10.setBorder(new TitledBorder(null, "\uc694\uc57d", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
							new Font("\ub9d1\uc740 \uace0\ub515", Font.BOLD, 12)));
						panel10.setLayout(new BorderLayout());

						//======== scrollPane2 ========
						{
							scrollPane2.setPreferredSize(new Dimension(300, 50));
							scrollPane2.setMinimumSize(new Dimension(300, 50));
							scrollPane2.setMaximumSize(new Dimension(300, 50));

							//---- tbSummary ----
							tbSummary.setModel(new DefaultTableModel(
								new Object[][] {
									{"", null, null},
								},
								new String[] {
									"\ud56d\ubaa9", "\uac74\uc218", "\uae08\uc561"
								}
							) {
								Class<?>[] columnTypes = new Class<?>[] {
									String.class, Integer.class, Integer.class
								};
								@Override
								public Class<?> getColumnClass(int columnIndex) {
									return columnTypes[columnIndex];
								}
							});
							scrollPane2.setViewportView(tbSummary);
						}
						panel10.add(scrollPane2, BorderLayout.CENTER);

						//======== panel9 ========
						{
							panel9.setLayout(new FormLayout(
								"2*(default, $lcgap), 40dlu, $ugap, default, $lcgap, default:grow",
								"default"));

							//---- label13 ----
							label13.setText("\ucd1d \uac74\uc218");
							label13.setFont(new Font("\ub9d1\uc740 \uace0\ub515", Font.PLAIN, 14));
							panel9.add(label13, CC.xy(3, 1));

							//---- tfTotalCount ----
							tfTotalCount.setFont(new Font("\ub9d1\uc740 \uace0\ub515", Font.PLAIN, 20));
							tfTotalCount.setHorizontalAlignment(SwingConstants.TRAILING);
							tfTotalCount.setText("0");
							tfTotalCount.setEditable(false);
							panel9.add(tfTotalCount, CC.xy(5, 1));

							//---- label14 ----
							label14.setText("\ucd1d \uae08\uc561");
							label14.setFont(new Font("\ub9d1\uc740 \uace0\ub515", Font.PLAIN, 14));
							panel9.add(label14, CC.xy(7, 1));

							//---- tfTotalPrice ----
							tfTotalPrice.setFont(new Font("\ub9d1\uc740 \uace0\ub515", Font.PLAIN, 20));
							tfTotalPrice.setHorizontalAlignment(SwingConstants.TRAILING);
							tfTotalPrice.setText("0");
							tfTotalPrice.setEditable(false);
							panel9.add(tfTotalPrice, CC.xy(9, 1));
						}
						panel10.add(panel9, BorderLayout.SOUTH);
					}
					panel4.add(panel10, BorderLayout.CENTER);
				}
				panel2.add(panel4, BorderLayout.CENTER);

				//======== panel11 ========
				{
					panel11.setBorder(new TitledBorder(null, "\uc9c0\uc6d0", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("\ub9d1\uc740 \uace0\ub515", Font.BOLD, 12)));
					panel11.setLayout(new GridLayout(0, 2, 10, 5));

					//---- btReloadPeoples ----
					btReloadPeoples.setText("\uc131\ub3c4 \uc778\uba85 \uac31\uc2e0");
					btReloadPeoples.addActionListener(e -> btReloadPeoplesActionPerformed(e));
					panel11.add(btReloadPeoples);

					//---- btCustom ----
					btCustom.setText("\uc778\uba85 \uc815\ubcf4 \uac00\uacf5");
					btCustom.addActionListener(e -> btCustomActionPerformed(e));
					panel11.add(btCustom);

					//---- btExportXlsx ----
					btExportXlsx.setText("Excel \uc800\uc7a5");
					btExportXlsx.addActionListener(e -> btExportXlsxActionPerformed(e));
					panel11.add(btExportXlsx);

					//---- btLabelInfo ----
					btLabelInfo.setText("\ub77c\ubca8\ucd9c\ub825\uc815\ubcf4");
					btLabelInfo.addActionListener(e -> btLabelInfoActionPerformed(e));
					panel11.add(btLabelInfo);
				}
				panel2.add(panel11, BorderLayout.SOUTH);
			}
			ctxPane.add(panel2, BorderLayout.WEST);

			//======== summaryPane ========
			{
				summaryPane.setLayout(new BorderLayout());

				//======== scrollPane1 ========
				{
					scrollPane1.setBorder(new TitledBorder(null, "\uae30\ub85d", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("\ub9d1\uc740 \uace0\ub515", Font.BOLD, 12)));

					//---- tbIncome ----
					tbIncome.setModel(new DefaultTableModel(
						new Object[][] {
						},
						new String[] {
							"No.", "\ub0b4\uc5ed", "\uc131\uba85", "\uae08\uc561"
						}
					) {
						Class<?>[] columnTypes = new Class<?>[] {
							Integer.class, String.class, String.class, Integer.class
						};
						boolean[] columnEditable = new boolean[] {
							false, false, false, false
						};
						@Override
						public Class<?> getColumnClass(int columnIndex) {
							return columnTypes[columnIndex];
						}
						@Override
						public boolean isCellEditable(int rowIndex, int columnIndex) {
							return columnEditable[columnIndex];
						}
					});
					{
						TableColumnModel cm = tbIncome.getColumnModel();
						cm.getColumn(0).setPreferredWidth(30);
					}
					tbIncome.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					scrollPane1.setViewportView(tbIncome);
				}
				summaryPane.add(scrollPane1, BorderLayout.CENTER);
			}
			ctxPane.add(summaryPane, BorderLayout.CENTER);
		}
		contentPane.add(ctxPane, BorderLayout.CENTER);
		setSize(1190, 780);
		setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel camPane;
	private JPanel ctxPane;
	private JPanel panel2;
	private JPanel datePane;
	private JLabel lbDate;
	private JPanel panel4;
	private JPanel panel5;
	private JLabel lbItem12;
	private JLabel lbItem3;
	private JLabel lbPeopleInfo1;
	private JLabel lbPeopleInfo2;
	private JLabel label7;
	private JLabel lbName;
	private JLabel label8;
	private JFormattedTextField ftfPrice;
	private JTextField tfThousand;
	private JCheckBox ckbThousand;
	private JPanel panel3;
	private JButton btEdit;
	private JButton btDelete;
	private JButton btCancel;
	private JPanel panel10;
	private JScrollPane scrollPane2;
	private JTable tbSummary;
	private JPanel panel9;
	private JLabel label13;
	private JTextField tfTotalCount;
	private JLabel label14;
	private JTextField tfTotalPrice;
	private JPanel panel11;
	private JButton btReloadPeoples;
	private JButton btCustom;
	private JButton btExportXlsx;
	private JButton btLabelInfo;
	private JPanel summaryPane;
	private JScrollPane scrollPane1;
	private JTable tbIncome;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
