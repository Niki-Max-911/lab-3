package ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import entity.PhotoCloner;

public class ControlRecipientPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
	private String wayFile = "";
	private JButton btnStart;
	private JButton btnClear;
	private JButton btnAddRecipients;
	private PhotoCloner photoCloner;

	/**
	 * Create the panel.
	 */
	public ControlRecipientPanel(ActionListener btnStartListener, PhotoCloner photoCloner) {
		this.photoCloner = photoCloner;
		setLayout(new FlowLayout(FlowLayout.RIGHT));

		btnStart = new JButton("\u0421\u0442\u0430\u0440\u0442");
		btnStart.addActionListener(btnStartListener);
		add(btnStart);

		Component horizontalStrut_1 = Box.createHorizontalStrut(5);
		add(horizontalStrut_1);

		btnClear = new JButton("\u041E\u0447\u0438\u0441\u0442\u0438\u0442\u044C");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ControlRecipientPanel.this.photoCloner.getRecipientList().clear();
			}
		});
		add(btnClear);

		Component horizontalStrut = Box.createHorizontalStrut(5);
		add(horizontalStrut);

		btnAddRecipients = new JButton(
				"\u0414\u043E\u0431\u0430\u0432\u0438\u0442\u044C \u043A\u043B\u043E\u043D\u043E\u0432");
		btnAddRecipients.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (fc.showOpenDialog(ControlRecipientPanel.this) == JFileChooser.APPROVE_OPTION) {
					ControlRecipientPanel.this.wayFile = fc.getSelectedFile().getAbsolutePath();
				} // �������� ����
					// ControlRecipientPanel.this.wayFile =
					// "d:/workspace/InstagramCloner/Accounts.txt";

				Thread th = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							ControlRecipientPanel.this.photoCloner.parseFile(ControlRecipientPanel.this.wayFile);
						} catch (FileNotFoundException e) {
							JOptionPane.showMessageDialog(ControlRecipientPanel.this, "Файл не найден!");
						}
					}
				});// ���� �������� �����
				th.start();
			}
		});// ����������� ������
		add(btnAddRecipients);

	}

	public String getRecipientWayFile() {
		return wayFile;
	}

	public void setEnable(boolean b) {
		this.btnAddRecipients.setEnabled(b);
		this.btnClear.setEnabled(b);
		this.btnStart.setEnabled(b);
	}
}
