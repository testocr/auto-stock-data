package org.tloss.lessthan1dollar;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class Login extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3355010558038736119L;
	JButton SUBMIT;
	JButton reloadForm;
	JPanel panel;
	JLabel label1, label2, label3;
	JPasswordField text2;
	JTextField text1, text3;
	LessThan1Dollar dollar;
	LoginForm form;
	String host;
	boolean successs = false;
	String username;

	public String getUsername() {
		return username;
	}

	public boolean getLoginResult() {
		return successs;
	}

	Login(Frame frame, byte[] imagedata, LoginForm form,
			LessThan1Dollar dollar, String host) {
		super(frame, "Login", true);
		this.form = form;
		this.dollar = dollar;
		this.host = host;
		label1 = new JLabel();
		label1.setText("Username:");
		text1 = new JTextField(15);

		label2 = new JLabel();
		label2.setText("Password:");
		text2 = new JPasswordField(15);

		SUBMIT = new JButton("Login");
		reloadForm = new JButton("Reload");

		panel = new JPanel(new GridLayout(4, 2));
		panel.add(label1);
		panel.add(text1);
		panel.add(label2);
		panel.add(text2);
		ImageIcon image = new ImageIcon(imagedata);
		label3 = new JLabel(" ", image, JLabel.CENTER);
		panel.add(label3);
		text3 = new JTextField(15);
		panel.add(text3);
		panel.add(SUBMIT);
		panel.add(reloadForm);
		SUBMIT.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				LoginResult loginResult = null;
				try {
					username = text1.getText();
					loginResult = Login.this.dollar.login(text1.getText(),
							new String(text2.getPassword()), false,
							text3.getText(), Login.this.host,
							Login.this.form.getInput());
					successs = loginResult.isResult();
					if (successs)
						dispose();
				} catch (Exception e) {

					e.printStackTrace();
				}
				if (!successs) {
					if (loginResult != null) {
						Login.this.form = loginResult.getForm();
						if (loginResult.getForm().getCaptcha() != null) {
							ImageIcon image = new ImageIcon(loginResult
									.getForm().getCaptcha().getData());
							label3.setIcon(image);
						}
						JOptionPane.showMessageDialog(Login.this,
								"Fail to login.", "Login",
								JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(Login.this,
								"Please click reload form.", "Login",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		});
		reloadForm.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				try {
					Login.this.form = Login.this.dollar
							.proLogin(Login.this.host);
					if (Login.this.form != null) {
						ImageIcon image = new ImageIcon(Login.this.form
								.getCaptcha().getData());
						label3.setIcon(image);
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(Login.this,
							"Please click reload form.", "Login",
							JOptionPane.INFORMATION_MESSAGE);
					e.printStackTrace();
				}

			}
		});
		add(panel, BorderLayout.CENTER);
		setTitle("LOGIN FORM");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

}
