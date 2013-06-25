package org.tloss.lessthan1dollar;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ConnectionOption extends JDialog {
	ProxyOption option = new ProxyOption();
	JCheckBox box;
	JTextField text1, text2,text3;

	public ConnectionOption(JFrame frame) {
		super(frame, "Option FORM", true);
		JPanel panel;
		JLabel label1, label2, label3,label4;

		JButton SUBMIT;
		label4 = new JLabel();
		label4.setText("Host:");
		text3 = new JTextField(30);
		label1 = new JLabel();
		label1.setText("Proxy:");
		text1 = new JTextField(30);

		label2 = new JLabel();
		label2.setText("Port:");
		label3 = new JLabel();
		label3.setText("Proxy:");
		text2 = new JTextField(30);
		box = new JCheckBox();
		box.setSelected(true);

		SUBMIT = new JButton("Apply");

		panel = new JPanel(new GridLayout(5, 2));
		panel.add(label4);
		panel.add(text3);
		panel.add(box);
		panel.add(label3);
		panel.add(label1);
		panel.add(text1);
		panel.add(label2);
		panel.add(text2);
		panel.add(SUBMIT);
		text1.setText("172.16.203.5");
		text2.setText("8080");
		text3.setText("http://localhost/drupal6");
		add(panel, BorderLayout.CENTER);
		setTitle("Option FORM");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		SUBMIT.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				option.setUserProxy(box.isSelected());
				option.setProxy(text1.getText());
				option.setPort(Integer.parseInt(text2.getText()));
				option.setHost(text3.getText());
				dispose();
			}
		});
	}

	public ProxyOption getProxyOption() {
		return option;
	}
}
