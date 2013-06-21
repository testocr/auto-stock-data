package org.tloss.lessthan1dollar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class AdminControlFrame extends JFrame {
	final LessThan1Dollar dollar = new LessThan1Dollar();
	String host;

	protected void initExitMenu(JMenu menu) {
		// Exit menu
		JMenuItem menuItem;

		menuItem = new JMenuItem("Exit", KeyEvent.VK_E);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.ALT_MASK));
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AdminControlFrame.this.dispose();
			}
		});
	}

	protected void initLoginMenu(JMenu menu) {
		// Login
		JMenuItem menuItem;

		menuItem = new JMenuItem("Login", KeyEvent.VK_L);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				ActionEvent.ALT_MASK));
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				LoginForm loginForm;
				try {
					loginForm = dollar.proLogin(AdminControlFrame.this.host);
					if (!loginForm.isLogined()) {
						byte[] imageData = null;
						if (loginForm.getCaptcha() != null) {
							imageData = loginForm.getCaptcha().getData();
						}
						Login loginDlg = new Login(AdminControlFrame.this,
								imageData, loginForm, dollar,
								AdminControlFrame.this.host);
						loginDlg.setSize(400, 300);
						loginDlg.setVisible(true);
						if (loginDlg.getLoginResult()) {
							AdminControlFrame.this.setTitle("Logined as "
									+ loginDlg.getUsername());
							initLoginedMenu();

						}
					} else {
						AdminControlFrame.this.setTitle("Logined");
						initLoginedMenu();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
	}

	protected void initLoginedMenu() {
		if (mainMenu.getItemCount() == 2) {
			JMenu menu;
			menu = new JMenu("Control");
			menu.setMnemonic(KeyEvent.VK_C);
			mainMenu.addSeparator();
			mainMenu.add(menu);
			initLogoutMenu(menu);
			mainMenu.revalidate();
			menuBar.revalidate();
			// this.revalidate();
		}
	}

	protected void removeLoginedMenu() {
		if (mainMenu.getItemCount() == 4) {
			mainMenu.remove(2);
			mainMenu.remove(2);
			mainMenu.revalidate();
			menuBar.revalidate();
			// this.revalidate();
		}
	}

	protected void initLogoutMenu(JMenu menu) {
		JMenuItem menuItem;
		menuItem = new JMenuItem("Logout");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (dollar.logout()) {
					removeLoginedMenu();
				}
			}
		});
		menuItem = new JMenuItem("Import");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(AdminControlFrame.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					try {

						ImportProgressInfo info = new ImportProgressInfo(
								AdminControlFrame.this, "Import", file, dollar,
								host);
						info.startShowProgress();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

	protected void intOptionMenu() {
		JMenuItem menuItem;
		menuItem = new JMenuItem("Option");
		menuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				ConnectionOption connectionOption = new ConnectionOption(
						AdminControlFrame.this);
				connectionOption.setSize(400, 300);
				connectionOption.setVisible(true);
				dollar.setOption(connectionOption.getProxyOption());
			}
		});
		menuBar.add(menuItem);
	}

	final JMenuBar menuBar;
	final JMenu mainMenu;

	public AdminControlFrame(String title, String host) {
		super(title);
		this.host = host;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600, 400);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		mainMenu = new JMenu("File");
		mainMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(mainMenu);
		initLoginMenu(mainMenu);
		initExitMenu(mainMenu);
		intOptionMenu();
	}
}
