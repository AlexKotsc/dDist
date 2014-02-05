package ex9;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

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
			
			while(conn.isConnected()){
				dte.setTitle("Connected to " + addr.getAddress() + ":" + addr.getPort());
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
