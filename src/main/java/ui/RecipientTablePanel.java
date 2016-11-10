package ui;

import entity.PhotoCloner;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RecipientTablePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private ControlRecipientPanel controlRecipientPanel;
	private JTable table;

	/**
	 * Create the panel.
	 */
	public RecipientTablePanel(ActionListener btnStartListener,PhotoCloner photoCloner) {
		setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 2), "\u041A\u043B\u043E\u043D\u044B:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BorderLayout(0, 0));
		controlRecipientPanel = new ControlRecipientPanel(btnStartListener, photoCloner);
		add(controlRecipientPanel,BorderLayout.SOUTH);
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		table = new JTable(new TableModel(photoCloner));
		scrollPane.setViewportView(table);
		setMinimumSize(new Dimension(270, 270));
		
		table.getColumnModel().getColumn(1).setMinWidth(67);
		table.getColumnModel().getColumn(1).setMaxWidth(67);
		
		table.getColumnModel().getColumn(2).setMinWidth(100);
		table.getColumnModel().getColumn(2).setMaxWidth(100);
		
		table.getColumnModel().getColumn(3).setMinWidth(103);
		table.getColumnModel().getColumn(3).setMaxWidth(103);
		
		Timer tm = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				RecipientTablePanel.this.table.updateUI();
			}
		});
		tm.start();
	}
	
	public String getRecipientWayFile(){
		return controlRecipientPanel.getRecipientWayFile();
	}
	
	public void setEnable(boolean b){
		this.controlRecipientPanel.setEnable(b);
	}
}
