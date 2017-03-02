package communicatoradditions;

import javax.swing.JOptionPane;

public class Pinger extends Thread {
	public final static long PING_TIMEOUT = 2000;
	public final static int MAX_RETRIES = 2;
	private int retries = 0;
	
	@Override
    public void run() {
		while (true) {
			try {
				Thread.sleep(PING_TIMEOUT);
				if (Tools.sendMessageString("ping " + Tools.username) == null) {
					if (retries++ == MAX_RETRIES) {
						JOptionPane.showMessageDialog(null, 
			        			"Lost connection to the server.\n" +
			        			"The client will now terminate.",
								"Goodbye",
								JOptionPane.ERROR_MESSAGE);
						System.exit(1);
					}
				} else retries = 0;
			} catch (InterruptedException e) {
				e.printStackTrace(System.err);
				return;
			}
		}
	}
}
