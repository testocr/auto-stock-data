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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ImportProgressInfo extends JDialog {
	JLabel label1, label2;
	JLabel label3, label4;
	JPanel panel;
	int success = 0;
	int error = 0;
	File dataFile;
	LessThan1Dollar dollar;
	String section;
	public ImportProgressInfo(JFrame frame, String title, File file,
			LessThan1Dollar dollar) {
		super(frame, title, true);
		this.dollar = dollar;
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
		String[] sections = new String[] { "Mo", "Tu", "We", "Th", "Fr" };
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < sections.length; i++) {
			map.put(sections[i], String.valueOf(i + 2));
		}
		String section = (String) JOptionPane.showInputDialog(
				ImportProgressInfo.this, "What Section?", "Sections",
				JOptionPane.QUESTION_MESSAGE, null, sections,
				sections[0]);
		this.section =  section;
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
			Calendar calendar = Calendar.getInstance();
			InputStream fis;
			BufferedReader br;
			String line;
			fis = new FileInputStream(dataFile);
			br = new BufferedReader(new InputStreamReader(fis,
					Charset.forName("UTF-16LE")));
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
					if ("0AAFBA2762A623".equals(transaction.getTrace())) {
						System.out.println(transaction.getTime());
					}
					StringBuffer dateString = new StringBuffer();
					calendar.setTime(transaction.getTime());
					dateString.append(calendar.get(Calendar.YEAR)).append(",");
					dateString.append(calendar.get(Calendar.MONTH)+1).append(",");
					dateString.append(calendar.get(Calendar.DAY_OF_MONTH))
							.append(",");
					dateString.append(calendar.get(Calendar.HOUR_OF_DAY))
							.append(",");
					dateString.append(calendar.get(Calendar.MINUTE))
							.append(",");
					dateString.append(calendar.get(Calendar.SECOND));
					if(dateString.toString().equals("2012,5,7,18,56,3")){
						System.out.println();
					}
					if (newLine == null) {
						if (Constants.SUCCESS == dollar.importData(
								transaction.getTrace(), dateString.toString(),
								"1",section)) {
							updateSuccess(1);
						} else {
							updateError(1);
						}

					} else {
						if (Constants.SUCCESS == dollar.importData(
								transaction.getTrace(), dateString.toString(),
								"0",section)) {
							updateSuccess(1);
						} else {
							updateError(1);
						}
					}

					System.out.println(dateString.toString());
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
