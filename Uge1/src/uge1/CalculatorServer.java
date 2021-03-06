package uge1;
import java.math.BigInteger;
import java.rmi.server.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.util.HashMap;

public class CalculatorServer extends UnicastRemoteObject implements Calculator {
	
	public static final int portNumber = 40499;
	
	public CalculatorServer() throws RemoteException {
	    super();
	    valuation = new HashMap<String,BigInteger>();
	}
	
	private HashMap<String,BigInteger> valuation;

	private BigInteger getValue(String var) {
		BigInteger val = valuation.get(var);
		if (val==null) { 
			val = BigInteger.ZERO; 
		}
		return val;
	}

	private void setValue(String var, BigInteger val) {
		valuation.put(var, val);
	}

	public void assign(String var, BigInteger val) throws RemoteException {
		setValue(var,val);
	}

	public BigInteger read(String var) throws RemoteException {
		return getValue(var);
	}

	public void add(String left, String right, String res) throws RemoteException {
		setValue(res,getValue(left).add(getValue(right)));
	}
	
	public void mult(String left, String right, String res) throws RemoteException {
		setValue(res,getValue(left).multiply(getValue(right)));
	}

	public static void main(String[] args) throws Exception {
		
		Runtime.getRuntime().exec("rmiregistry " + portNumber);
		CalculatorServer calc = new CalculatorServer();

		/* Make <calc> accessible at the RMI Registry running on port <portNumber> on 
		 * the local machine, under the name "dDistCalculator". */
		Naming.rebind("//localhost:" + portNumber + "/dDistCalculator", calc); 
	}
}
