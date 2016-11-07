package windows;

import java.awt.Color;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class DonorPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTextField txtDonorLink;
	private JTextArea txtCommentPlus;
	private JSpinner spnrCountPhotoToLoad;
	private JCheckBox chbxOldescription;

	/**
	 * Create the panel.
	 */
	public DonorPanel() {

		setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 2),
				"\u0414\u043E\u043D\u043E\u0440 \u0444\u043E\u0442\u043E\u0433\u0440\u0430\u0444\u0438\u0439:",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		setLayout(null);

		txtDonorLink = new JTextField();
		txtDonorLink.setText("https://instagram.com/roman_liuk");
		txtDonorLink.setBounds(23, 42, 258, 20);
		add(txtDonorLink);
		txtDonorLink.setColumns(10);

		JLabel lbl1 = new JLabel("C\u0441\u044B\u043B\u043A\u0430:");
		lbl1.setBounds(23, 25, 144, 14);
		add(lbl1);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(23, 144, 258, 68);
		add(scrollPane);

		txtCommentPlus = new JTextArea();
		scrollPane.setViewportView(txtCommentPlus);

		JLabel lbl4 = new JLabel("\u0414\u043E\u043F. \u043E\u043F\u0438\u0441\u0430\u043D\u0438\u0435:");
		scrollPane.setColumnHeaderView(lbl4);

		spnrCountPhotoToLoad = new JSpinner();
		spnrCountPhotoToLoad.setModel(new SpinnerNumberModel(0, 0, 1000, 10));
		spnrCountPhotoToLoad.setBounds(79, 73, 46, 20);
		add(spnrCountPhotoToLoad);

		JLabel lbl2 = new JLabel("\u041A\u043E\u043B-\u0432\u043E:\r\n");
		lbl2.setBounds(23, 76, 46, 14);
		add(lbl2);

		JLabel lbl3 = new JLabel("(0 - \u0437\u0430\u0433\u0440\u0443\u0436\u0430\u0435\u0442 \u0432\u0441\u0435)");
		lbl3.setBounds(135, 76, 117, 14);
		add(lbl3);

		chbxOldescription = new JCheckBox(
				"\u043A\u043E\u043F\u0438\u0440\u043E\u0432\u0430\u0442\u044C \u0441\u0442\u0430\u0440\u043E\u0435 \u043E\u043F\u0438\u0441\u0430\u043D\u0438\u0435");
		chbxOldescription.setSelected(true);
		chbxOldescription.setBounds(23, 114, 199, 23);
		add(chbxOldescription);
	}

	public void setEnable(boolean b) {
		this.txtDonorLink.setEditable(b);
		this.spnrCountPhotoToLoad.setEnabled(b);
		this.chbxOldescription.setEnabled(b);
		this.txtCommentPlus.setEditable(b);
	}

	public String getDonorLink() {
		return this.txtDonorLink.getText();
	}

	public String getCommentPlus() {
		return this.txtCommentPlus.getText();
	}

	public boolean isCopyDescription() {
		return this.chbxOldescription.isSelected();
	}

	public int getCountPhoto() {
		return (int) this.spnrCountPhotoToLoad.getValue();
	}
}
