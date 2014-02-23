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

	int lastPos;

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
			conn.shutdownInput();
			conn.shutdownOutput();
			conn.close();
		} catch (SocketException e){
			System.out.println("Socket exception");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeConnection(){
		if(!conn.isClosed()){
			try {
				connected = false;
				output.writeObject(new DisconnectEvent());
			} catch (IOException e) {
				e.printStackTrace();
			}		
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

				//Starts the listener if it isn't already running
				if(!listenThread.isAlive()){
					listenThread.start();
				}

				//If connected to other text editor, read TextEvents and display them
				if (connected) {
					try {

						MyEvent me = (MyEvent) input.readObject();

						if(me instanceof MyTextEvent) queueTextEvent((MyTextEvent) me);
						if(me instanceof DisconnectEvent) {
							System.out.println("Received a DisconnectEvent");
							connected = false;
							output.writeObject(new DisconnectReceivedEvent());
							Thread.sleep(100);
							disconnect();
						}
						if(me instanceof DisconnectReceivedEvent){
							disconnect();
						}

					} catch (EOFException e){
						System.out.println("EOF Exception");
						disconnect();
					} catch (SocketException e){
						System.out.println("Socket exception");
						e.printStackTrace();
						disconnect();

					}


				}} catch (Exception _) {
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
			mte.setStringLength(area.getText().length());
			try {
				if(mte instanceof TextInsertEvent){
					TextInsertEvent t = (TextInsertEvent) mte; 
					lastPos = t.getOffset() + t.getText().length();
				}
				if(mte instanceof TextRemoveEvent){
					TextRemoveEvent t = (TextRemoveEvent) mte;
					lastPos = t.getOffset();
				}
				output.writeObject(mte);
			} catch (SocketException e){
				disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//Handles displaying Insert and Remove variants in the JTextArea.
	public void queueTextEvent(MyTextEvent x){
		if (x instanceof TextInsertEvent) {
			final TextInsertEvent tie = (TextInsertEvent) x;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						dec.setListen(false);

						if(tie.getStringLength()!=area.getText().length()+tie.getText().length()){
							int diff = area.getText().length()+tie.getText().length()-tie.getStringLength();
							System.out.println("Diff: "  + diff);
							if(lastPos<tie.getOffset()){
								area.insert(tie.getText(), tie.getOffset()+diff);
							} else {
								area.insert(tie.getText(), tie.getOffset());
							}
						} else {
							area.insert(tie.getText(),tie.getOffset());
						}

						dec.setListen(true);
					} catch (Exception e) {
						System.err.println(e);
					}
				}
			});
		} else if (x instanceof TextRemoveEvent) {
			final TextRemoveEvent tre = (TextRemoveEvent) x;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						dec.setListen(false);
						if(tre.getStringLength()!=(area.getText().length()-tre.getLength())){
							int diff = area.getText().length()+tre.getLength()-tre.getStringLength();
							System.out.println("Diff: "  + diff);
							if(lastPos<tre.getOffset()+diff){
								area.replaceRange(null, tre.getOffset()-diff, tre.getOffset() + tre.getLength()-diff);
							}
						} else {
							area.replaceRange(null, tre.getOffset(), tre.getOffset() + tre.getLength());
						}
						dec.setListen(true);

					} catch (Exception e) {
						System.err.println(e + " at " + tre.getOffset() + " to " + (tre.getOffset()+tre.getLength()) + " when text is " + area.getText().length());
					}
				}
			});
		}	
	}

}
