package windows;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import entitys.Donor;
import entitys.PhotoCloner;
import entitys.Recipient;

import exceptions.AuthenticationException;
import exceptions.NoPageException;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private RecipientTablePanel recipientTablPnl;
	private DonorPanel donorPnl;
	private PhotoCloner photoCloner;

	private Thread checkEnd = new Thread(new Runnable() {
		@Override
		public void run() {
			boolean fl = true;
			while (fl) {
				fl = !MainFrame.this.photoCloner.isCompleted();
				try {
					Thread.sleep(5000l);
				} catch (InterruptedException e) {
				}
			}
			JOptionPane.showMessageDialog(MainFrame.this, "Завершено!");
			MainFrame.this.setEnable(true);
		}
	});

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setTitle("Instagram Cloner");
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Object[] options = { "Да", "Нет!" };
				int n = JOptionPane.showOptionDialog(e.getWindow(), "Закрыть приложение?", "Закрытие",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (n == 0) {
					e.getWindow().setVisible(false);
					System.exit(0);
				}
			}
		});

		photoCloner = new PhotoCloner();

		ActionListener btnStartListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					Donor.donor.setLink(MainFrame.this.donorPnl.getDonorLink());
				} catch (NoPageException e1) {
					JOptionPane.showMessageDialog(MainFrame.this, "Страница не найдена!");
					return;
				} // загружаю сторінку

				if (MainFrame.this.photoCloner.getRecipientList().isEmpty()) {
					JOptionPane.showMessageDialog(MainFrame.this, "Не добавили аккаунты!");
					return;
				}

				Donor.donor.setCopyOldDescription(MainFrame.this.donorPnl.isCopyDescription());
				Donor.donor.setAppendDescription(MainFrame.this.donorPnl.getCommentPlus());
				Donor.donor.setCountPhotoCopy(MainFrame.this.donorPnl.getCountPhoto());
				MainFrame.this.setEnable(false);
				// втановлюю параметри

				Thread th = new Thread(new Runnable() {
					@Override
					public void run() {
						for (Recipient rec : photoCloner.getRecipientList()) {
							try {
								rec.auth();
							} catch (AuthenticationException e) {
								continue;
							}
							rec.startUploading();
							rec.fillerPhotoQueue();
						}
					}
				});
				th.start();
				// запускаю потоки

				checkEnd.start();
			}
		};

		setBounds(100, 100, 975, 298);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

		donorPnl = new DonorPanel();
		donorPnl.setPreferredSize(new Dimension(300, 270));
		getContentPane().add(donorPnl);

		recipientTablPnl = new RecipientTablePanel(btnStartListener, photoCloner);
		recipientTablPnl.setPreferredSize(new Dimension(500, 270));
		getContentPane().add(recipientTablPnl);
		pack();
	}

	public void setEnable(boolean b) {
		this.recipientTablPnl.setEnable(b);
		this.donorPnl.setEnable(b);
	}

	public String getRecipientWayFile() {
		return this.recipientTablPnl.getRecipientWayFile();
	}

}
