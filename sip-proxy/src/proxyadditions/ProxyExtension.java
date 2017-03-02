package proxyadditions;

import java.sql.SQLException;

import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;
import javax.swing.JOptionPane;
import gov.nist.sip.proxy.Proxy;

public class ProxyExtension {
	private final DatabaseServer dbServer;
	private final SocketServer socketServer;
	private boolean started;
	
	public ProxyExtension() {
		dbServer = DatabaseServer.get();
		socketServer = SocketServer.get();
	}
	
	public void start() {
		if (started) return;
		try {
			dbServer.connect();
			
		} catch (SQLException e1) {
			JOptionPane.showMessageDialog(null, 
					"Couldn't connect to database!",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
        try {
            new Thread(socketServer).start();
        }
        catch (IllegalThreadStateException e) {
        	JOptionPane.showMessageDialog(null,
        			"Socket Server already running!",
        			"Error", JOptionPane.ERROR_MESSAGE);
        	return;
        }
        started = true;
	}
	
	public void stop() {
		if (started) {
			dbServer.disconnect();
			socketServer.stopSocketServer();
			started = false;
		}
	}
	
	public void toggle() {
		if (started) stop(); else start();
	}
}