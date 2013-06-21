package org.tloss.lessthan1dollar;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImportProgressInfo extends JDialog {
	JLabel label1, label2;
	JLabel label3, label4;
	JPanel panel;
	int success = 0;
	int error = 0;
	File dataFile;
	LessThan1Dollar dollar;
	String host;

	public ImportProgressInfo(JFrame frame, String title, File file,
			LessThan1Dollar dollar, String host) {
		super(frame, title, true);
		this.dollar = dollar;
		this.host = host;
		dataFile = file;
		panel = new JPanel(new GridLayout(2, 2));
		label1 = new JLabel();
		label1.setText("Success:");
		label2 = new JLabel();
		label2.setText("0");
		label3 = new JLabel();
		label3.setText("Error:");
		label4 = new JLabel();
		label4.setText("0");
		panel.add(label1);
		panel.add(label2);
		panel.add(label3);
		panel.add(label4);
		add(panel, BorderLayout.CENTER);
		setSize(200, 100);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	}

	public void startShowProgress() {
		Runnable runnable = ImportProgressInfo.this.new InternalTask();
		new Thread(runnable).start();
		setVisible(true);
	}

	public void updateSuccess(int more) {
		success = success + more;
		label2.setText(String.valueOf(success));
		label2.revalidate();
	}

	public void updateError(int more) {
		error = error + more;
		label4.setText(String.valueOf(error));
		label4.revalidate();
	}

	class InternalTask implements Runnable {
		public void showProgress() throws Exception {
			Transaction transaction = new Transaction();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			InputStream fis;
			BufferedReader br;
			String line;
			fis = new FileInputStream(dataFile);
			br = new BufferedReader(new InputStreamReader(fis,
					Charset.forName("UTF-8")));
			boolean first = true;
			line = br.readLine();
			String newLine;
			while (line != null) {
				newLine = br.readLine();
				// Deal with the line
				if (!first) {
					String tmp[] = line.split("\\t");
					transaction.setTrace(tmp[0].replace("\"", ""));
					transaction.setTime(dateFormat.parse(tmp[1].replace("\"",
							"")));
					if (newLine == null) {
						if (Constants.SUCCESS == dollar.importData(host,
								transaction.getTrace(), String
										.valueOf(transaction.getTime()
												.getTime() / 1000), "1")) {
							updateSuccess(1);
						} else {
							updateError(1);
						}

					} else {
						if (Constants.SUCCESS == dollar.importData(host,
								transaction.getTrace(), String
										.valueOf(transaction.getTime()
												.getTime() / 1000), "0")) {
							updateSuccess(1);
						} else {
							updateError(1);
						}
					}

					System.out.println(String.valueOf(transaction.getTime()
							.getTime() / 1000));
				} else {
					first = false;
				}
				line = newLine;
			}
			// Done with the file
			br.close();
			br = null;
			fis = null;
			dispose();
		}

		public void run() {
			try {
				showProgress();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}
}
