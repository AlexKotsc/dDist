package ex9;

import java.io.Serializable;

/**
 * 
 * @author Jesper Buus Nielsen
 *
 */

//MyTextEvent now implements Serializable so we can send it through ObjectIn/OutputStreams.
public class MyTextEvent extends MyEvent {
	
	int stringLength;
	
	MyTextEvent(int offset) {
		this.offset = offset;
	}
	private int offset;
	int getOffset() { return offset; }
	public int getStringLength() {
		return stringLength;
	}
	public void setStringLength(int stringLength) {
		this.stringLength = stringLength;
	}
	
	
}
