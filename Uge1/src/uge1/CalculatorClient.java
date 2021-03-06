package uge1;
import java.math.BigInteger;
import java.rmi.Naming;

public class CalculatorClient {

	public static void main(String[] args) throws Exception {

		String host = "localhost";
		if (args.length > 0) { host = args[0]; }

		BigInteger inc = BigInteger.ONE;
		if (args.length > 1) { inc = new BigInteger(args[1]); }
		
		/* Access the remote object made accessible on the machine <host> on port <portNumber>,
		 * under the name "dDistCalculator". */
		Calculator calc = (Calculator)Naming.lookup("//" + host + ":" + CalculatorServer.portNumber + "/dDistCalculator"); 
	
		calc.assign("x", inc);  // x = inc
		calc.add("x", "y", "y");          // y = y + x
		System.out.println("y = " + calc.read("y"));
	}
}