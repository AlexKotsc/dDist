package ex9;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/*Runnable that creates a new connection to a ServerSocket.
 * The connection is passed on to an EventReplayer */
public class ConnectRunnable implements Runnable {

	InetSocketAddress addr;
	DistributedTextEditor dte;
	Socket conn;

	public ConnectRunnable(InetSocketAddress addr, DistributedTextEditor dte){
		this.addr = addr;
		this.dte = dte;
	}

	@Override
	public void run() {
		try {

			conn = new Socket(addr.getAddress(),addr.getPort());
			System.out.println("CLIENT - START Outgoing connection");
			dte.setConnection(conn);
			dte.setTitle("Connected to " + addr.getAddress() + ":" + addr.getPort());
			dte.clearFields();	
			dte.Connect.setEnabled(false);
			dte.Listen.setEnabled(false);
			dte.Disconnect.setEnabled(true);
			while(!conn.isClosed()){

			}
			dte.Connect.setEnabled(true);
			dte.Listen.setEnabled(true);
			dte.Disconnect.setEnabled(false);
			System.out.println("CLIENT - END Outgoing connection");
			dte.clearFields();
			dte.setTitle("Not connected");

			return;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
