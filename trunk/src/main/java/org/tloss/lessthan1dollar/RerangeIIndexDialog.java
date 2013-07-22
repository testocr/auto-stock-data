package org.tloss.lessthan1dollar;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class RerangeIIndexDialog extends JDialog {
	LessThan1Dollar dollar;
	Vector rowData;

	public RerangeIIndexDialog(String title, JFrame frame,
			LessThan1Dollar dollar, String nid) throws Exception {
		super(frame, title, true);
		this.dollar = dollar;

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		rowData = dollar.selectTopTransaction(nid);
		Vector columnNames = new Vector();
		String[] tmp = { "tid", "nid", "uid", "idx", "buy_date", "trace_id",
				"ref_date", "point" };
		for (int i = 0; i < tmp.length; i++) {
			columnNames.add(tmp[i]);
		}

		JTable table = new JTable(rowData, columnNames);
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);
		JButton button = new JButton("Update");
		add(button, BorderLayout.PAGE_END);
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String[] sections = new String[] { "Mo", "Tu", "We", "Th", "Fr" };
				Map<String, String> map = new HashMap<String, String>();
				for (int i = 0; i < sections.length; i++) {
					map.put(sections[i], String.valueOf(i + 2));
				}
				String section = (String) JOptionPane.showInputDialog(
						RerangeIIndexDialog.this, "What Section?", "Sections",
						JOptionPane.QUESTION_MESSAGE, null, sections,
						sections[0]);
				String uid, nid, idx, point, ref_date, rank, buy_date;

				Vector vector;
				for (int i = 0; i < rowData.size(); i++) {
					vector = (Vector) rowData.get(i);
					nid = vector.get(1).toString();
					uid = vector.get(2).toString();
					idx = vector.get(3).toString();
					point = vector.get(7).toString();
					ref_date = vector.get(6).toString();
					rank = String.valueOf(i);
					buy_date = vector.get(4).toString();
					try {
						RerangeIIndexDialog.this.dollar.updateIndex(uid, nid,
								idx, point, ref_date, rank, buy_date,map.get(section));
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				dispose();
			}
		});

	}

}
