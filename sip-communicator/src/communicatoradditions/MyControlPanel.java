package communicatoradditions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class MyControlPanel extends JFrame {
	private static final int DEFAULT_WIDTH = 600;
	private static final int DEFAULT_HEIGHT = 600;
	
	private final String username;
	private final JTabbedPane tabs;
	private final boolean canReset;
	
	public MyControlPanel(String username) {
		this(username, true);
	}
	public MyControlPanel(String username, boolean canReset) {
		super(username + "'s Control Panel!");
		this.username = username;
		this.canReset = canReset;

		tabs = new JTabbedPane();
		tabs.addTab("My info", null, new InfoPanel(), "Contains client's general information");
		tabs.addTab("Billing", null, new BillingPanel(), "Contains Billing info and call log");
		tabs.addTab("Blocking", null, new BlockingPanel(), "Allows you to manage blocking and unblocking of users");
		this.add(tabs);
		
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
	}
	
	public static void start(String username) {
		new MyControlPanel(username);
	}
	
	public static void main(String[] args) {
		// main provided for easy testing
		new MyControlPanel("dummy", false);
	}

	private class InfoPanel extends JPanel implements ChangeListener {
		private final JLabel idL;
		private final JLabel userL;
		private final JLabel balanceL;
		private final JLabel rateL;
		private final JTextField forwardTF;
		private final JButton saveB;
		private final JButton resetB;
		
		public InfoPanel() {
			setLayout(new BorderLayout());
			
			JPanel boxPanel = new JPanel();
			boxPanel.setLayout(new GridLayout(0, 2));
			
			JLabel label = new JLabel("User Id: ");
			boxPanel.add(label);
			idL = new JLabel();
			boxPanel.add(idL);
			
			label = new JLabel("Username: ");
			boxPanel.add(label);
			userL = new JLabel();
			boxPanel.add(userL);
			
			label = new JLabel("Balance: ");
			boxPanel.add(label);
			balanceL = new JLabel();
			boxPanel.add(balanceL);
			
			label = new JLabel("Billing Policy: ");
			boxPanel.add(label);
			rateL = new JLabel();
			boxPanel.add(rateL);
			
			label = new JLabel("Forward To: ");
			boxPanel.add(label);
			forwardTF = new JTextField();
			forwardTF.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent arg0) {
					saveB.setEnabled(true);
					resetB.setEnabled(true);
				}
				@Override
				public void insertUpdate(DocumentEvent arg0) {
					changedUpdate(arg0);
				}
				@Override
				public void removeUpdate(DocumentEvent arg0) {
					changedUpdate(arg0);
				}});
			boxPanel.add(forwardTF);
			add(boxPanel, BorderLayout.NORTH);
			
			boxPanel = new JPanel();
			boxPanel.setLayout(new GridLayout(0, 2));
			
			saveB = new JButton("Save");
			saveB.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					String forwardee = forwardTF.getText().trim();
					if (username.equals(forwardee)) {
						JOptionPane.showMessageDialog(MyControlPanel.this, 
	        					"You can't forward calls to yourself, lol!",
	        					"Forwarding Failed",
	        					JOptionPane.ERROR_MESSAGE);
					}
					else if (forwardee.equals("")) {
						Tools.sendMessageVoid("stop_forward " + username);
						reset();
					}
					else if (!Tools.sendMessageString(String.format("forward %s %s", username, forwardee)).equals("ok")) {
						JOptionPane.showMessageDialog(MyControlPanel.this, 
	        					"The username you provided is probably invalid!",
	        					"Forwarding Failed",
	        					JOptionPane.ERROR_MESSAGE);
					} else reset();
				}});
			boxPanel.add(saveB);
			
			resetB = new JButton("Reset");
			resetB.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					reset();
				}});
			boxPanel.add(resetB);
			add(boxPanel, BorderLayout.SOUTH);
			
			tabs.addChangeListener(this);
		}

		private void reset() {
			if (canReset) {
				idL.setText(Tools.sendMessageString("get_user_id " + username));
				userL.setText(username);
				balanceL.setText(Tools.sendMessageString("get_balance " + username));
				rateL.setText(Tools.rateToName(Integer.parseInt(Tools.sendMessageString("get_rate " + username))));
				forwardTF.setText(Tools.sendMessageString("get_forward " + username));
				if (forwardTF.getText().equals("<nobody>"))
					forwardTF.setText("");
				
				saveB.setEnabled(false);
				resetB.setEnabled(false);
			}
		}
		
		@Override
		public void stateChanged(ChangeEvent arg0) {
			if (tabs.getSelectedComponent() == this) {
				reset();
			}
		}
	}
	
	private class NotEditableTableModel extends DefaultTableModel {
		@Override
		public boolean isCellEditable(int row,int col) {
			return false;
		}
	}
	
	private class BillingPanel extends JPanel implements ChangeListener {
		private final JTable table;
		private NotEditableTableModel myModel;
		private final String[] Titles = {"Call Id", "Caller", "Callee", "Start Time", "End Time", "Duration (s)", "Cost"};
		
		public BillingPanel() {
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			
			table = new JTable(myModel);
			add(table.getTableHeader());
			add(table);
			
			tabs.addChangeListener(this);
		}

		private void reset() {
			if (canReset) {
				myModel = new NotEditableTableModel();
				for (int i = 0; i < Titles.length; i++) 
					myModel.addColumn(Titles[i]);
				
				Socket socket = Tools.sendMessage("log " + username);
				try {
					DataInputStream in = new DataInputStream(socket.getInputStream());
					while (true) {
						String[] parts = in.readUTF().split(" ");
						String callId = parts[0];
						String caller = parts[3];
						String callee = parts[4];
						Long startTimeL = Long.parseLong(parts[5]);
						Long endTimeL = Long.parseLong(parts[6]);
						Timestamp startTime = new Timestamp(startTimeL);
						Timestamp endTime = new Timestamp(endTimeL);
						String charge = caller.equals(username) ? parts[7] : "0";
						
						String[] row = {callId, caller, callee, startTime.toString(), endTime.toString(),
								Long.toString((endTimeL - startTimeL) / 1000), charge};
						
						myModel.addRow(row);
					}
				} catch (IOException e) {}
				finally {
					Tools.safeClose(socket);
					table.setModel(myModel);
				}
			}
		}
		
		@Override
		public void stateChanged(ChangeEvent arg0) {
			if (tabs.getSelectedComponent() == this) {
				reset();
			}
		}
	}
	
	private class BlockingPanel extends JPanel implements ChangeListener {
		private ArrayList<BlockingLine> lines;
		private final JPanel centerP;
		
		public BlockingPanel() {
			setLayout(new BorderLayout(0, 8));
			JLabel label = new JLabel("Currently Blocked Users", SwingConstants.CENTER);
			add(label, BorderLayout.PAGE_START);
			
			centerP = new JPanel();
			centerP.setLayout(new BoxLayout(centerP, BoxLayout.PAGE_AXIS));
			add(centerP, BorderLayout.CENTER);
			
			JPanel botP = new JPanel();
			botP.setLayout(new BorderLayout());
			
			JButton button = new JButton("New");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					String blocked = JOptionPane.showInputDialog(MyControlPanel.this, 
							"Please enter the username of the user you want to block:",
							"Add New Blocked User", JOptionPane.QUESTION_MESSAGE);
					
					if (blocked != null) {
						blocked = blocked.trim();
						if (username.equals(blocked)) {
							JOptionPane.showMessageDialog(MyControlPanel.this, 
		        					"You can't block yourself, lol!",
		        					"Blocking Failed",
		        					JOptionPane.ERROR_MESSAGE);
						}
						else if (blocked.equals("")) {
							// Do Nothing
						}
						else if (!Tools.sendMessageString(String.format("block %s %s", username, blocked)).equals("ok")) {
							JOptionPane.showMessageDialog(MyControlPanel.this, 
		        					"The username you provided is invalid or the user is already blocked!",
		        					"Blocking Failed",
		        					JOptionPane.ERROR_MESSAGE);
						} else reset();
					}
				}});
			botP.add(button, BorderLayout.LINE_START);
			
			button = new JButton("Clear all");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					Tools.sendMessageVoid("unblock_all " + username);
					reset();
				}});
			botP.add(button, BorderLayout.LINE_END);
			add(botP, BorderLayout.PAGE_END);
			tabs.addChangeListener(this);
		}

		private void reset() {
			if (canReset) {
				if (lines != null)
					for (BlockingLine line: lines)
						centerP.remove(line);
				
				lines = new ArrayList<>();
				
				Socket socket = Tools.sendMessage("blocked_users " + username);
				try {
					DataInputStream in = new DataInputStream(socket.getInputStream());
					while (true) {
						new BlockingLine(in.readUTF());
					}
				} catch (IOException e) {}
				finally {
					Tools.safeClose(socket);
					repaint();
				}
			}
		}
		
		public void add(BlockingLine comp) {
			centerP.add(comp);
			lines.add(comp);
		}
		
		public void remove(BlockingLine comp) {
			centerP.remove(comp);
			lines.remove(comp);
			repaint();
		}
		
		@Override
		public void stateChanged(ChangeEvent arg0) {
			if (tabs.getSelectedComponent() == this) {
				reset();
			}
		}
		
		private class BlockingLine extends JPanel implements ActionListener {
			private final String blocked;
			
			public BlockingLine(String blocked) {
				this.setLayout(new BorderLayout());
				this.setMaximumSize(new Dimension(200, 20));
				this.blocked = blocked;
				
				JLabel nameL = new JLabel(blocked);
				add(nameL, BorderLayout.LINE_START);
				
				JButton deleteB = new JButton("X");
				deleteB.addActionListener(this);
				add(deleteB, BorderLayout.LINE_END);
				
				BlockingPanel.this.add(this);
			}
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Tools.sendMessageVoid(String.format("unblock %s %s", username, blocked));
				BlockingPanel.this.remove(this);
			}
		}
	}
}
