package ex9;

public class MergeTextsRunnable implements Runnable {

	DocumentEventCapturer dec;
	EventReplayer er;
	int shadowVersion, version;

	public MergeTextsRunnable(DocumentEventCapturer dec, EventReplayer er){
		this.dec = dec;
		this.er = er;
	}

	@Override
	public void run() {
		System.out.println("Merge thread started");
		while(true){
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			shadowVersion = er.getShadowVersion();
			version = er.getVersion();
			
			if(shadowVersion>version){
				System.out.println("Shadow is newer, attempting to merge shadow into local");
				er.mergeShadow();
				er.setVersion(shadowVersion);
			
			} else if (shadowVersion<version){
				System.out.println("Shadow is older, attempting to merge local into shadow");
				er.mergeLocal();
				er.setShadowVersion(version);
				
			}
		}
	}
}
