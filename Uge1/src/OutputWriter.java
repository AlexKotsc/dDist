import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


public class OutputWriter implements Runnable {

	Socket s;
	int port;
	PrintWriter p;

	public OutputWriter(Socket s, int port){
		this.s = s;
		this.port = port;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			p = new PrintWriter(s.getOutputStream());
			int i = 0;
			while(true){
				if(i>10000){
					i = 0;
					p.println("Test@" + port);
				}
				
				i++;
			
			}
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}