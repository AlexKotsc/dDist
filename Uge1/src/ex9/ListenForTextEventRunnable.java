package ex9;

/* Runnable that waits for events in the document. 
 * After receiving a document calls back to the EventReplayer.
 * Passes on the detected Event. */
public class ListenForTextEventRunnable implements Runnable {

	DocumentEventCapturer dec;
	EventReplayer er;

	public ListenForTextEventRunnable(DocumentEventCapturer dec, EventReplayer er){
		this.dec = dec;
		this.er = er;
	}

	@Override
	public void run() {
		MyTextEvent mte;

		while(true){
			try {
				mte = dec.take();
				er.receive(mte);					
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}