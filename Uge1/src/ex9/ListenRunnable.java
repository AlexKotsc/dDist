package ex9;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ListenRunnable implements Runnable {

	int port;
	ServerSocket server;
	DistributedTextEditor dte;

	public ListenRunnable(int port, DistributedTextEditor dte){
		this.port = port;
		this.dte = dte;
	}

	@Override
	public void run() {
		try {
			server = new ServerSocket(port);

			dte.setTitle("I'm listening on " +
							server.getInetAddress().getLocalHost() + 
							":" + 
							server.getLocalPort());
			
			while(true){
				Socket incoming = server.accept();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
