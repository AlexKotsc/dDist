import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class SocketTest {
	public static void main(String[] args){
		
		
		try {
			System.out.println("Creating ServerSocket");
			ServerSocket server = new ServerSocket(40001);
			System.out.println("Creating ServerSocket.accept()");
			Socket conn = server.accept();
			System.out.println("Creating Test Socket " + server.getInetAddress() + ":40001");
			Socket test = new Socket(server.getInetAddress(), 40001);
			System.out.println("Creating PrintWriter");
			PrintWriter printer = new PrintWriter(test.getOutputStream());
			System.out.println("Creating BufferedReader");
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			System.out.println("Entering while");
			while(true){
				if(reader.readLine()!=null){
					System.out.println(reader.readLine());
				} else {
				}
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}


try {
	
	local = new ServerSocket(port);

	System.out.println(myKey + ": ServerSocket running on port " + port);

	known = new Socket(_getMyName().getAddress(),port);
	localSocket = local.accept();
	input = new BufferedReader(new InputStreamReader(localSocket.getInputStream()));
	if(joining){
		known = new Socket(connectedAt.getAddress(),connectedAt.getPort());
	}
	Thread writer = new Thread(new OutputWriter(known,port));
	writer.start();
	
	
	while(true){
		
		if(input.readLine()!=null){
			System.out.println(port + ":" +  input.readLine());
		}
		
	}
			

} catch (IOException e) {
	e.printStackTrace();
} 
