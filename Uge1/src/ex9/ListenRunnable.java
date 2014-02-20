package ex9;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/* Runnable that creates a ServerSocket and waits for new connections. 
 * The connections are sent to the EventReplayer */
public class ListenRunnable implements Runnable {

	int port;
	ServerSocket server;
	DistributedTextEditor dte;
	Socket incoming;
	ObjectInputStream input;
	boolean notInterrupted;

	public ListenRunnable(int port, DistributedTextEditor dte){
		this.port = port;
		this.dte = dte;
	}

	@Override
	public void run() {
		try {
			server = new ServerSocket(port);
			server.setReuseAddress(true);

			dte.setTitle("Listening on " +
					server.getInetAddress().getLocalHost() + ":" +
					server.getLocalPort());
			
			while(true){
				
				incoming = server.accept();
				System.out.println("SERVER - START Incoming connection");
				dte.setConnection(incoming);
				dte.setTitle("Connected - " + incoming.getInetAddress().getHostAddress() + ":" + incoming.getPort());
				dte.clearFields();
				dte.Disconnect.setEnabled(true);
				dte.Listen.setEnabled(false);
				dte.Connect.setEnabled(false);
				while(!incoming.isClosed()){

				}
				
				System.out.println("SERVER - END Incoming connection");
				dte.setTitle("Listening on " +
						server.getInetAddress().getLocalHost() + ":" +
						server.getLocalPort());
				dte.clearFields();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
