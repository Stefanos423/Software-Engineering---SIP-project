package communicatoradditions;

import java.util.concurrent.atomic.AtomicBoolean;

import net.java.sip.communicator.gui.GuiManager;

/*
 * Periodically check if a call is still active
 */
public class CallPinger extends Thread {
	public final static long PING_TIMEOUT = 3000;
	
	private final String other;
	private final GuiManager gui;
	private final AtomicBoolean started;
	
	public CallPinger(GuiManager gui, String other) {
		this(gui, other, false);
	}
	
	public CallPinger(GuiManager gui, String other, boolean started) {
		this.gui = gui;
		this.other = other;
		this.started = new AtomicBoolean(started);
	}
	
	@Override
    public void run() {
		while (true) {
			try {
				Thread.sleep(PING_TIMEOUT);
				String ret = Tools.sendMessageString(String.format("call_id %s %s", Tools.username, other));
				if (ret == null || ret.equals("0")) {
					if (started.get()) {
						gui.emulateHangup(other);
						return;
					}
				}
				else
					started.set(true);
			} catch (InterruptedException e) {
				e.printStackTrace(System.err);
				return;
			}
		}
	}
}
