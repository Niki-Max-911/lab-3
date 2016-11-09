package ui;

import javax.swing.table.AbstractTableModel;

import entity.PhotoCloner;

public class TableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private String[] nameColumns = new String[] { "Логин", "Загрузил", "В очереди", "Авторизация" };
	private PhotoCloner photoCloner;

	public TableModel(PhotoCloner photoCloner) {
		this.photoCloner = photoCloner;
	}

	@Override
	public int getColumnCount() {
		return nameColumns.length;
	}

	@Override
	public int getRowCount() {
		return photoCloner.getRecipientList().size();
	}

	@Override
	public Object getValueAt(int row, int col) {

		switch (col) {
		case 0: {
			return photoCloner.getRecipientList().get(row).getUserName();
		}
		case 1: {
			return photoCloner.getRecipientList().get(row).getCountUpLoadedPhoto();
		}
		case 2: {
			return photoCloner.getRecipientList().get(row).countInQueue();
		}
		case 3: {
			return photoCloner.getRecipientList().get(row).isSuccessAuth() ? "Успех" : "Провал";
		}
		}
		return "";
	}

	@Override
	public String getColumnName(int i) {
		return nameColumns[i];
	}
}
