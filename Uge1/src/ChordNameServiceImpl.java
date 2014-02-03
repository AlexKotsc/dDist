import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChordNameServiceImpl extends Thread implements ChordNameService  {

	private boolean joining;
	private int port;
	protected InetSocketAddress myName;
	protected int myKey;
	private InetSocketAddress suc;
	private InetSocketAddress pre;
	private InetSocketAddress connectedAt;

	//myvars
	boolean leaving = false;

	public int keyOfName(InetSocketAddress name)  {
		int tmp = name.hashCode()*1073741651 % 2147483647;
		if (tmp < 0) { tmp = -tmp; }
		return tmp;
	}

	public InetSocketAddress getChordName()  {
		return myName;
	}

	/**
	 * Computes the name of this peer by resolving the local host name
	 * and adding the current portname.
	 */
	protected InetSocketAddress _getMyName() {
		try {
			InetAddress localhost = InetAddress.getLocalHost();
			InetSocketAddress name = new InetSocketAddress(localhost, port);
			return name;
		} catch (UnknownHostException e) {
			System.err.println("Cannot resolve the Internet address of the local host.");
			System.err.println(e);
		}
		return null;
	}

	public void createGroup(int port) {
		joining = false;
		this.port = port;
		myName = _getMyName();
		myKey = keyOfName(myName);
		start();
	}

	public void joinGroup(InetSocketAddress knownPeer, int port)  {
		joining = true;
		this.port = port;
		connectedAt = knownPeer;
		myName = _getMyName();
		myKey = keyOfName(myName);
		start();
	}

	public void leaveGroup() {
		// More code needed here!
		System.out.println(port + ": leaving group");
		leaving = true;
	}

	public InetSocketAddress succ() {
		return suc; // You might want to modify this.
	}

	public InetSocketAddress pred() {
		return pre; // You might want to modify this.
	}

	public InetSocketAddress lookup(int key) {
		/*
		 * The below works fine for singleton groups, but you might
		 * want to connect to the rest of the group to lookup the
		 * responsible if the group is larger.
		 */
		return myName; 
	}

	public void run() {
		System.out.println("My name is " + myName + " and my key is " + myKey);

		if(joining){

			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					//Enter group
					try {
						synchronized(this){
							wait(1000);
						}
						String fromServer;
						//System.out.println(connectedAt.getAddress() + ":" + connectedAt.getPort());
						Socket conn = new Socket(connectedAt.getAddress(),connectedAt.getPort());
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						PrintWriter writer = new PrintWriter(conn.getOutputStream(),true);

						writer.println("joinGroup");
						while((fromServer=reader.readLine())!=null){
							System.out.println("Server@" + connectedAt.getPort() + " says: " + fromServer);
						}

						conn.close();

					} catch (IOException e) {
						System.out.println("Something went wrong in the joining thread");
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

			t.start();
//			try {
//				t.join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}

		ServerSocket serve = null;

		try {
			serve = new ServerSocket(port);
			System.out.println("ServerSocket running on " + port);
		} catch (IOException e) {
			System.out.println("Couldn't open ServerSocket");
			e.printStackTrace();
		}

		while(true){
			try {
				if(serve != null){

					String fromClient;

					Socket incoming = serve.accept();

					BufferedReader reader = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
					PrintWriter writer = new PrintWriter(incoming.getOutputStream(),true);


					while((fromClient=reader.readLine())!=null){
						System.out.println("Client@" + incoming.getLocalPort() + " says: " + fromClient);
						//Appropriate conditionals and writer prints.
						switch(fromClient){
						case "joinGroup":
							writer.println("Client wants to join a group eh?");
							writer.println("Can I print more than once?");
						}
						//writer.println("I say: Screw you!");

					}


					incoming.close();
				}

				//				if(leaving){
				//					try {
				//						serve.close();
				//					} catch (IOException e) {
				//						System.out.println("Couldn't close ServerSocket");
				//						e.printStackTrace();
				//					}
				//				}

			} catch (IOException e) {
				System.out.println("Something went wrong in while loop");
				e.printStackTrace();
			}
		}






		/*
		 * If joining we should now enter the existing group and
		 * should at some point register this peer on its port if not
		 * already done and start listening for incoming connection
		 * from other peers who want to enter or leave the
		 * group. After leaveGroup() was called, the run() method
		 * should return so that the threat running it might
		 * terminate.
		 */
	}

	public static void main(String[] args) {
		ChordNameService peer1 = new ChordNameServiceImpl();
		ChordNameService peer2 = new ChordNameServiceImpl();
		ChordNameService peer3 = new ChordNameServiceImpl();

		peer1.createGroup(40001);
		peer2.joinGroup(peer1.getChordName(),40002);
		peer3.joinGroup(peer2.getChordName(),40003);

		peer1.leaveGroup();
		peer3.leaveGroup();
		peer2.leaveGroup();
	}

}