package uge1;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;


public class IncomingSocket implements Runnable {

	ChordNameServiceImpl i;
	Socket s;
	
	public IncomingSocket(ChordNameServiceImpl i, Socket s){
		this.i = i;
		this.s = s;
	}
	
	@Override
	public void run() {
		
		String fromClient;
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
			PrintWriter writer = new PrintWriter(s.getOutputStream(),true);


			while((fromClient=reader.readLine())!=null){
				System.out.println("Client@" + s.getLocalPort() + " says: " + fromClient);
				//Appropriate conditionals and writer prints.
				switch(fromClient){
				case "joinGroup":
					writer.println(i.keyOfName(i.pred()));
				case "lookup":
					InetSocketAddress temp = i.lookup(Integer.parseInt(reader.readLine()));
					writer.println(temp.getAddress());
					writer.println(Integer.toString(temp.getPort()));

				}
				//writer.println("I say: Screw you!");

			}

			reader.close();
			writer.close();
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
