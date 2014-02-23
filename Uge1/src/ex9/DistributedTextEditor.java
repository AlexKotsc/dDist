package ex9;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultEditorKit;

public class DistributedTextEditor extends JFrame {

	private JTextArea area1 = new JTextArea(20,120);     
	private JTextField ipaddress = new JTextField("IP address here");     
	private JTextField portNumber = new JTextField("Port number here");     

	private EventReplayer er;
	private Thread ert; 

	private JFileChooser dialog = 
			new JFileChooser(System.getProperty("user.dir"));

	private String currentFile = "Untitled";
	private boolean changed = false;
	private boolean connected = false;
	private DocumentEventCapturer dec = new DocumentEventCapturer();

	//Additions for exercise 9

	//9.1 - Implement server socket listener when menu item listen is pressed.
	Thread listenThread;
	ListenRunnable listenRunner;

	//9.2 - Implement connect when menu item Connect is pressed
	Thread connectThread;
	ConnectRunnable connectRunner;

	//For regexp
	String portRegexp = "(5\\d{4}+|[1-4]\\d{4}+|\\d{1,4}+)";

	public DistributedTextEditor() {
		area1.setFont(new Font("Monospaced",Font.PLAIN,12));
		((AbstractDocument)area1.getDocument()).setDocumentFilter(dec);

		Container content = getContentPane();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

		JScrollPane scroll1 = 
				new JScrollPane(area1, 
						JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
						JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		content.add(scroll1,BorderLayout.CENTER);

		content.add(ipaddress,BorderLayout.CENTER);	
		content.add(portNumber,BorderLayout.CENTER);	

		JMenuBar JMB = new JMenuBar();
		setJMenuBar(JMB);
		JMenu file = new JMenu("File");
		JMenu edit = new JMenu("Edit");
		JMB.add(file); 
		JMB.add(edit);

		file.add(Listen);
		file.add(Connect);
		file.add(Disconnect);
		file.addSeparator();
		file.add(Save);
		file.add(SaveAs);
		file.add(Quit);

		edit.add(Copy);
		edit.add(Paste);
		edit.getItem(0).setText("Copy");
		edit.getItem(1).setText("Paste");

		Save.setEnabled(false);
		SaveAs.setEnabled(false);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		area1.addKeyListener(k1);
		setTitle("Not connected");
		setVisible(true);
		area1.setText("");


		Disconnect.setEnabled(false);

		er = new EventReplayer(dec, area1);
		ert = new Thread(er);
		ert.start();

		ipaddress.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				if(ipaddress.getText().equals("IP address here")){
					ipaddress.setText("");

				}
			}});

		portNumber.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				if(portNumber.getText().equals("Port number here")){
					portNumber.setText("");
				}
			}

			@Override
			public void focusLost(FocusEvent e) {

			}	
		});

	}

	private KeyListener k1 = new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			changed = true;
			Save.setEnabled(true);
			SaveAs.setEnabled(true);
		}
	};

	Action Listen = new AbstractAction("Listen") {
		public void actionPerformed(ActionEvent e) {
			saveOld();
			area1.setText("");

			int tempPort;

			String portName = portNumber.getText();
			try{
				if(portMatcher(portName)){
					System.out.println("Valid port");
					tempPort = Integer.parseInt(portName);
				} else {
					area1.setText("Invalid port. Try again");
					portNumber.requestFocus();
					return;
				}
			} catch (NumberFormatException ex){
				area1.setText("Invalid port. Try again");
				portNumber.requestFocus();
				return;
			}


			//Creates and starts thread with a ServerSocket, ready for incoming connections.
			listenRunner = new ListenRunnable(tempPort, DistributedTextEditor.this);
			listenThread = new Thread(listenRunner);
			listenThread.start();

			changed = false;
			Save.setEnabled(false);
			SaveAs.setEnabled(false);
		}
	};

	Action Connect = new AbstractAction("Connect") {
		public void actionPerformed(ActionEvent e) {
			saveOld();

			//Clear both fields, ready for the new connection
			clearFields();

			String hostName = ipaddress.getText();
			String portName = portNumber.getText();

			//Sets the title to a temporary connecting to with the address and port of the server



			//We get the address and port of our server from the text fields and create a new thread.
			int tempPort;
			InetSocketAddress tempAddress;

			tempAddress = new InetSocketAddress("localhost", 40001);


			if(portMatcher(portName)){
				System.out.println("Valid port");
			} else {
				area1.setText("Invalid port. Try again.");
				portNumber.requestFocus();
				return;
			}

			if(ipMatcher(hostName)){
				System.out.println("Valid IP");

			} else {
				area1.setText("Invalid IP address. Try again.");
				ipaddress.requestFocus();
				return;
			}

			setTitle("Connecting to " + hostName + ":" + portName + "...");

			tempAddress = new InetSocketAddress(ipaddress.getText(), Integer.parseInt(portName));

			connectRunner = new ConnectRunnable(tempAddress, DistributedTextEditor.this);
			connectThread = new Thread(connectRunner);
			connectThread.start();

			changed = false;
			Save.setEnabled(false);
			SaveAs.setEnabled(false);
		}
	};

	//Utility - Delivers a connection from our threads to the Event Replayer.
	public void setConnection(Socket s){
		er.setConnection(s);
		Disconnect.setEnabled(true);
		Listen.setEnabled(false);
		Connect.setEnabled(false);
	}

	//Utility - Disconnects the socket in the Event Replayer and clears the fields.
	public void disconnectER(){
		clearFields();
		er.closeConnection();
	}

	//Utility - Clears the text fields
	public void clearFields(){
		area1.setText("");
	}

	Action Disconnect = new AbstractAction("Disconnect") {
		public void actionPerformed(ActionEvent e) {	
			disconnectER();
		}
	};

	Action Save = new AbstractAction("Save") {
		public void actionPerformed(ActionEvent e) {
			if(!currentFile.equals("Untitled"))
				saveFile(currentFile);
			else
				saveFileAs();
		}
	};

	Action SaveAs = new AbstractAction("Save as...") {
		public void actionPerformed(ActionEvent e) {
			saveFileAs();
		}
	};

	Action Quit = new AbstractAction("Quit") {
		public void actionPerformed(ActionEvent e) {
			saveOld();
			System.exit(0);
		}
	};

	ActionMap m = area1.getActionMap();

	Action Copy = m.get(DefaultEditorKit.copyAction);
	Action Paste = m.get(DefaultEditorKit.pasteAction);

	private void saveFileAs() {
		if(dialog.showSaveDialog(null)==JFileChooser.APPROVE_OPTION)
			saveFile(dialog.getSelectedFile().getAbsolutePath());
	}

	private void saveOld() {
		if(changed) {
			if(JOptionPane.showConfirmDialog(this, "Would you like to save "+ currentFile +" ?","Save",JOptionPane.YES_NO_OPTION)== JOptionPane.YES_OPTION)
				saveFile(currentFile);
		}
	}

	private void saveFile(String fileName) {
		try {
			FileWriter w = new FileWriter(fileName);
			area1.write(w);
			w.close();
			currentFile = fileName;
			changed = false;
			Save.setEnabled(false);
		}
		catch(IOException e) {
		}
	}

	public static void main(String[] arg) {
		new DistributedTextEditor();
	}     

	public boolean portMatcher(String port){
		return Pattern.matches(portRegexp, port);
	}

	public boolean ipMatcher(String host){

		InetAddress a;

		try {
			a = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			return false;
		}

		return true;
	}

}
