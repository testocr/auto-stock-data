package org.tloss.lessthan1dollar;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class RerangeIIndexDialog extends JDialog {
	LessThan1Dollar dollar;

	public RerangeIIndexDialog(String title, JFrame frame,
			LessThan1Dollar dollar, String host, String nid) throws Exception {
		super(frame, title, true);
		this.dollar = dollar;

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		Vector rowData = dollar.selectTopTransaction(host, nid);
		Vector columnNames = new Vector();
		String[] tmp = { "tid", "nid","uid", "idx", "buy_date", "trace_id",
				"ref_date","point" };
		for (int i = 0; i < tmp.length; i++) {
			columnNames.add(tmp[i]);
		}

		JTable table = new JTable(rowData, columnNames);
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);

	}
	
}
