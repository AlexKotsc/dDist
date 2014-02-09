package ex9;

import java.io.Serializable;

/**
 * 
 * @author Jesper Buus Nielsen
 *
 */

//MyTextEvent now implements Serializable so we can send it through ObjectIn/OutputStreams.
public class MyTextEvent implements Serializable{
	MyTextEvent(int offset) {
		this.offset = offset;
	}
	private int offset;
	int getOffset() { return offset; }
}
