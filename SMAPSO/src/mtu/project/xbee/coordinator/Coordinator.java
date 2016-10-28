/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.xbee.coordinator;

/**
 *
 * @author Rafael
 */
import java.util.Scanner;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.models.XBeeMessage;

/* 
 *   - Unicast: NODE_IDENTIFIER: message
 *   - Broadcast: ALL: message
*/

public class Coordinator {
	
	/* Constants */
	
	// TODO Replace with the port where your module is connected to.
	private static final String PORT = "COM12";
	// TODO Replace with the baud rate of your module.
	private static final int BAUD_RATE = 9600;
	
	private static final Scanner s = new Scanner(System.in);
	
	private static DataReceiveListener listener = new DataReceiveListener();
	
	/**
	 * Application main method.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		System.out.println("+----------------------------------------+");
		System.out.println("|             Transmit Data              |");
		System.out.println("+----------------------------------------+\n");
		
		XBeeDevice myDevice = new XBeeDevice(PORT, BAUD_RATE);
		
		try {
			myDevice.open();
			
			XBeeNetwork network = myDevice.getNetwork();
                        System.out.println("\nLocal XBee: " + myDevice.getNodeID());
			System.out.println("\nScanning the network, please wait...");
                                       
                        
                        
                        network.addRemoteDevice(network.discoverDevice("RASPBERRY1"));
                        network.addRemoteDevice(network.discoverDevice("RASPBERRY2"));
                        network.addRemoteDevice(network.discoverDevice("END_DEVICE1"));
                        network.addRemoteDevice(network.discoverDevice("END_DEVICE2"));
                        network.addRemoteDevice(network.discoverDevice("END_DEVICE3"));
                        network.addRemoteDevice(network.discoverDevice("END_DEVICE4"));
                        network.addRemoteDevice(network.discoverDevice("END_DEVICE5"));
                        
			System.out.println("Devices found:");
                        System.out.println(network.getDevices().size());
			for (RemoteXBeeDevice remote : network.getDevices()) {
				System.out.println(" - " + remote.getNodeID());
			}
			
			System.out.println("\nType your messages here:\n");
			
			myDevice.addDataListener(listener);
			
			while (true) {
				try {
					String[] data = parseData(s.nextLine());
					
					if (data[0].toLowerCase().equals("all")) {
						myDevice.sendBroadcastData(data[1].getBytes());
					} else {
						RemoteXBeeDevice remote = network.getDevice(data[0]);
						if (remote != null) {
							myDevice.sendData(remote, data[1].getBytes());
						} else {
							System.err.println("Could not find the module " + data[0] + " in the network.");
						}
					}
				} catch (IndexOutOfBoundsException e) {
					System.err.println("Error parsing the text. Follow the format <NODE_IDENTIFIER|ALL: message>");
				} catch (XBeeException e) {
                                    if(!e.getMessage().toString().equals("There was a timeout while executing the requested operation.")){
                                        System.err.println("Error transmitting message: " + e.getMessage());
                                    }
				}
			}
			
		} catch (XBeeException e) {
			e.printStackTrace();
			myDevice.close();
			System.exit(1);
		} finally {
			myDevice.close();
			System.exit(0);
		}
	}
	
	/**
	 * Parses the given text to obtain the destination node identifier and the 
	 * message.
	 * 
	 * @param text Text in the format <NODE_IDENTIFIER|ALL: message>
	 * @return String array that contains the node identifier of the remote
	 *         device and the message to send.
	 */
	private static String[] parseData(String text) throws IndexOutOfBoundsException {
		String[] s = new String[2];
		s[0] = text.substring(0, text.indexOf(":"));
		s[1] = text.substring(text.indexOf(":") + 2);
                return s;
	}
	
	/**
	 * Sleeps the thread for the given milliseconds.
	 * 
	 * @param millis Time to sleep the thread in milliseconds.
	 */
	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {}
	}
	
	/**
	 * Class to manage the received data.
	 */
	private static class DataReceiveListener implements IDataReceiveListener {
		@Override
		public void dataReceived(XBeeMessage xbeeMessage) {
			System.out.println("------------------------------------------------------------------");
			System.out.println("> " + xbeeMessage.getDevice().getNodeID() +
					(xbeeMessage.isBroadcast() ? " (broadcast)" : "") +
					": " + new String(xbeeMessage.getData()));
			System.out.println("------------------------------------------------------------------");
		}
	}        
}
