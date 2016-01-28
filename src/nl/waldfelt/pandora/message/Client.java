package nl.waldfelt.pandora.message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import org.andengine.util.debug.Debug;

import android.os.StrictMode;

public class Client {
	// ----- Socket Variables
	private static final String SERVER_HOST = "83.84.130.18"; 
	//private static final String SERVER_HOST = "192.168.178.10";
	private static final int	SERVER_PORT = 7274;
	private static final int SERVER_TIMEOUT = 3000;
	
	// ----- Client Variables
	private Socket mSocket;
	private MessageHandler mHandler; 
	private Receiver mReceiver;
	private Transmitter mTransmitter;
	private ArrayList<ServerMessage> mRequests;
	
	public Client() {
		// Allow Threads to connect to Internet? Who codes such shit?
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		mRequests = new ArrayList<ServerMessage>();
		
		// Try to connect to server
		connect();
		
		mHandler = new MessageHandler(this);
		mHandler.start();

		mReceiver = new Receiver();
		mReceiver.start();

		mTransmitter = new Transmitter();
		mTransmitter.start();
	}

	public void addRequest(ServerMessage pRequest) {
		mRequests.add(pRequest);
		mTransmitter.restart();
		
	}
	
	// Receiver Thread. Receives the command and passes it on to the MessageHandler
		private class Receiver extends Thread {
			
			private ObjectInputStream ois;
			
			@Override
			public void run() {
				try {
					ois = new ObjectInputStream(mSocket.getInputStream());
				} catch (Exception e) {
					e.printStackTrace();
				}
				while(true) {
					try {
						
						// Read Command
						Object mClientMessage = ois.readObject();
						if(mClientMessage instanceof ClientMessage) {
							mHandler.addTask( (ClientMessage) mClientMessage);
						}
						
					} catch(Exception e) {
						System.exit(0);
						return;
					}
				}
			}
			
		}
		
		// Transmitter Thread. Transmits a command and its data back to the client.
		private class Transmitter extends Thread {
			private ObjectOutputStream oos;
			private Object mMonitor;
			
			public Transmitter() {
				mMonitor = new Object();
			}
			
			public void restart() {
				synchronized(mMonitor) {
					mMonitor.notifyAll();
				}
			}
			
			@Override
			public void run() {
				try {
					oos = new ObjectOutputStream(mSocket.getOutputStream());
				} catch( Exception e ) {
					e.printStackTrace();
				}
				
				while(true) {
					try {
						
						// Send Response
						while(!mRequests.isEmpty()) {
							oos.writeObject(mRequests.remove(0));
							oos.flush();
							oos.reset();
						}
						
						synchronized(mMonitor) {
							mMonitor.wait();
						}
						
					} catch(Exception e) {
						System.exit(0);
						return;
					}
				}
			}
		}
		
		private void connect() {
			System.out.println("Trying to connect to " + SERVER_HOST + ":" + SERVER_PORT);			
			try {
				mSocket = new Socket();
				mSocket.connect( new InetSocketAddress(SERVER_HOST, SERVER_PORT), SERVER_TIMEOUT);
			} catch (Exception e) {
				Debug.e(e);
			}
		}
}
