package ex9;

import java.io.Serializable;

/**
 * 
 * @author Jesper Buus Nielsen
 *
 */

//MyTextEvent now implements Serializable so we can send it through ObjectIn/OutputStreams.
public class MyTextEvent implements Serializable{
	
	private int length;
	
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	MyTextEvent(int offset) {
		this.offset = offset;
	}
	private int offset;
	int getOffset() { return offset; }
	
	
}
