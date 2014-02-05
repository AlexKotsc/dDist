import java.math.BigInteger;
import java.rmi.*;

public interface Calculator extends Remote {

	public void assign(String var, BigInteger val) 
		throws RemoteException;

	public void add(String left, String right, String res) 
		throws RemoteException;

	public void mult(String left, String right, String res) 
		throws RemoteException;

	public BigInteger read(String var) 
		throws RemoteException;
	
}
