package ex9;

import java.awt.EventQueue;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JTextArea;

/**
 * 
 * Takes the event recorded by the DocumentEventCapturer and replays them in a
 * JTextArea. The delay of 1 sec is only to make the individual steps in the
 * reply visible to humans.
 * 
 * @author Jesper Buus Nielsen
 * 
 */

// Added the possibiliy of connecting to a remote text editor and replaying the Events there.
public class EventReplayer implements Runnable {

	private DocumentEventCapturer dec;
	private JTextArea area;

	Socket conn;

	ObjectInputStream input;
	ObjectOutputStream output;

	Thread listenThread;

	boolean connected;

	public EventReplayer(DocumentEventCapturer dec, JTextArea area) {
		this.dec = dec;
		this.area = area;
		listenThread = new Thread(new ListenForTextEventRunnable(this.dec,this));
	}

	//Sets the boolean connected to false and tries to close streams and socket.
	public void disconnect(){
		connected = false;

		try { 
			System.out.println(conn.isClosed());
			conn.shutdownOutput();
			conn.close();
			System.out.println(conn.isClosed());
		} catch (SocketException e){
			System.out.println("Socket exception");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	//Sets the connection for the ER to send/receive TextEvents.
	public void setConnection(Socket s) {
		try {
			if(s==null){
				connected=false;
			} else {
				conn = s;

				output = new ObjectOutputStream(conn.getOutputStream());
				output.flush();

				input = new ObjectInputStream(conn.getInputStream());

				connected = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		boolean wasInterrupted = false;

		while (!wasInterrupted) {
			try {

				//Starts the listener if it isnt already running
				if(!listenThread.isAlive()){
					listenThread.start();
				}

				//If connected to other text editor, read TextEvents and display them
				if (connected) {
					try {
						MyTextEvent mte = (MyTextEvent) input.readObject();
						queueTextEvent(mte);
					} catch (EOFException e){
						System.out.println("EOF Exception");
						disconnect();
					} catch (SocketException e){
						System.out.println("Socket exception");
						disconnect();
					}
				}


			} catch (Exception _) {
				_.printStackTrace();
				wasInterrupted = true;
			}
		}
		System.out
		.println("I'm the thread running the EventReplayer, now I die!");
	}

	//Handles callback from the listener thread. If connected, sends TextEvent, else displays it in local text field.
	public void receive(MyTextEvent mte){
		if(connected){
			try {
				output.writeObject(mte);
			} catch (SocketException e){
				disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			queueTextEvent(mte);
		}
	}

	//Handles displaying Insert and Remove variants in the JTextArea.
	public void queueTextEvent(MyTextEvent x){
		if (x instanceof TextInsertEvent) {
			final TextInsertEvent tie = (TextInsertEvent) x;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						area.insert(tie.getText(), tie.getOffset());
					} catch (Exception e) {
						System.err.println(e);
						/*
						 * We catch all exceptions, as an uncaught
						 * exception would make the EDT unwind, which is
						 * now healthy.
						 */
					}
				}
			});
		} else if (x instanceof TextRemoveEvent) {
			final TextRemoveEvent tre = (TextRemoveEvent) x;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						area.replaceRange(null, tre.getOffset(),
								tre.getOffset() + tre.getLength());
					} catch (Exception e) {
						System.err.println(e);
						/*
						 * We catch all exceptions, as an uncaught
						 * exception would make the EDT unwind, which is
						 * now healthy.
						 */
					}
				}
			});
		}	
	}

	public void waitForOneSecond() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException _) {

		}
	}
}
