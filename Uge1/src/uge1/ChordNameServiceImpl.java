package uge1;
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

	ServerSocket serve = null;
	InetSocketAddress tempAddr = null;

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
		pre = myName;
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
		return suc; // You might want to modify this. p� den m�de
	}

	public InetSocketAddress pred() {
		return pre; // You might want to modify this. go
	}

	public InetSocketAddress lookup(int key) {

		final int tempKey = key;

		if(key != keyOfName(myName)){

			System.out.println("Key is not this address: unknown " + key + "!= my key " + keyOfName(myName));

			String fromServer;

			try {
				Socket s = new Socket(connectedAt.getAddress(),connectedAt.getPort());

				BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
				PrintWriter writer = new PrintWriter(s.getOutputStream(),true);


				writer.println("lookup");
				writer.println(Integer.toString(tempKey));

				while((fromServer=reader.readLine())!=null){
					String tempString = reader.readLine();

					tempAddr = new InetSocketAddress(fromServer,Integer.parseInt(tempString));
					System.out.println("Server@" + s.getPort() + " says: " + tempAddr);
				}

				reader.close();
				writer.close();
				s.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

			return tempAddr;
		}


		/*
		 * The below works fine for singleton groups, but you might
		 * want to connect to the rest of the group to lookup the
		 * responsible if the group is larger.
		 */
		return myName; 
	}

	public void run() {
		System.out.println("My name is " + myName + " and my key is " + myKey);


		suc = myName;
		pre = myName;


		if(joining){

			Runnable myRun = new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					//Enter group
					try {
						synchronized(this){
							wait(10);
						}
						String fromServer;
						
						Socket conn = new Socket(connectedAt.getAddress(),connectedAt.getPort());
						
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						PrintWriter writer = new PrintWriter(conn.getOutputStream(),true);

						
						writer.println("joinGroup");
						while((fromServer=reader.readLine())!=null){
							System.out.println("Server@" + connectedAt.getPort() + " says: " + fromServer);
							pre = lookup(Integer.parseInt(fromServer));
						}
						reader.close();
						writer.close();
						conn.close();

					} catch (IOException e) {
						System.out.println("Something went wrong in the joining thread");
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};

			Thread t = new Thread(myRun);

			t.start();
		}

		initServer();

		while(true){

			Socket incoming;

			try {
				if((incoming=serve.accept())!=null){
					Thread t = new Thread(new IncomingSocket(this,incoming));
					t.start();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
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

	private void initServer(){

		try {
			serve = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}