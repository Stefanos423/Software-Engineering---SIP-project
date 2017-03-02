package communicatoradditions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import net.java.sip.communicator.common.Utils;

public class Tools {
	private static InetAddress serverAddress; 
	private static int serverPort;
	private static boolean hasInit;
	public static String username;
	
	public static boolean init() {
		if (hasInit) return true;
		
		serverPort = Integer.valueOf(Utils.getProperty("ProxyAdditions.SERVER_PORT"));
		try {
			serverAddress = InetAddress.getByName(Utils.getProperty("ProxyAdditions.SERVER"));
		} catch (UnknownHostException e) {
			e.printStackTrace(System.err);
			return hasInit = false;
		}
		return hasInit = true;
	}
	
	public static void safeClose(AutoCloseable... objs) {
		for (AutoCloseable obj: objs) {
			try {
				if (obj != null)
					obj.close();
			} catch (Exception e) {}
		}
	}
	
	public static Socket sendMessage(String message) {
		init();
		Socket socket = null;
		
		try {
			socket = new Socket(serverAddress, serverPort);
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF(message);
		} catch (IOException e) {}
		return socket;
	}
	
	public static void sendMessageVoid(String message) {
		safeClose(sendMessage(message));
	}
	
	public static String sendMessageString(String message) {
		Socket socket = sendMessage(message);
		if (socket == null) return null;
		try {
			DataInputStream in = new DataInputStream(socket.getInputStream());
			String ret = in.readUTF();
			safeClose(socket);
			return ret;
		} catch (IOException e) {
			safeClose(socket);
			return null;
		}
	}
	
	public static String rateToName(int rate) {
		switch (rate) {
		case 1:
			return "Premium";
		case 2:
			return "Bounded";
		case 3:
			return "Tzapa";
		default: // if the rate somehow becomes an invalid value
			// you should be charged normally!!
			return "Normal";
		}
	}
}
