package proxyadditions;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.Thread;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer extends Thread {
	public static final int DEFAULT_PORT = 6000;
	public static final int DEFAULT_THREAD_POOL_SIZE = 32;
	private static SocketServer instance = null;
	
    private ServerSocket serverSocket;
    private boolean active = true;
    private final ExecutorService executor;
	
	private SocketServer() {
		executor = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
	}

	public static SocketServer get() {
		if (instance == null)
			instance = new SocketServer(); 
		return instance;
	}
	
	@Override
    public void run() {
		active = true;
        try {
	        serverSocket = new ServerSocket(6000);
	        
	        while (active) {
                try {
                    Socket clientSocket = this.serverSocket.accept();  
                    executor.execute(new Worker(clientSocket));
                }
                catch (IOException e) {
                	if (active)
	                   System.err.println("Error occured while trying to connect with a client");
                }
	        }
	        System.out.println("Socket Server shutdown");
	        executor.shutdown();
        }
        catch (IOException e) {
        	e.printStackTrace(System.err);
        }
    }
    
    void stopSocketServer() {
        this.active = false;
        try {
            this.serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}