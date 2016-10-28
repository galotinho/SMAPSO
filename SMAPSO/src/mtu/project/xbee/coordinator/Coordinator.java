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
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.ZigBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBeeMessage;

public class Coordinator {
    
        public XBeeNetwork inicializarRedeZigbee(ZigBeeDevice myDevice){
            
            XBeeNetwork network = null;
            try{
                network = myDevice.getNetwork();
                System.out.println("\nLocal Device XBee: " + myDevice.getNodeID());
                System.out.println("\nScanning the network, please wait...");

            //    network.addRemoteDevice(network.discoverDevice("RASPBERRY1"));
            //    network.addRemoteDevice(network.discoverDevice("RASPBERRY2"));
            //    network.addRemoteDevice(network.discoverDevice("END_DEVICE1"));
            //    network.addRemoteDevice(network.discoverDevice("END_DEVICE2"));
            //    network.addRemoteDevice(network.discoverDevice("END_DEVICE3")); 
                network.addRemoteDevice(network.discoverDevice("END_DEVICE4"));
            //    network.addRemoteDevice(network.discoverDevice("END_DEVICE5"));

                System.out.println("Devices found:");
                System.out.println(network.getDevices().size());

                for(RemoteXBeeDevice remote : network.getDevices()){
                    System.out.println(" - " + remote.getNodeID());
                }
            }catch(XBeeException e){
                e.printStackTrace();
                myDevice.close();
                System.exit(1);
            }
            
            return network;
        }
        
	/* @return myDevice
	 */
        public ZigBeeDevice inicializarConexaoRedeZigbee(String porta, int baud_rate){
            
            ZigBeeDevice myDevice = new ZigBeeDevice(porta, baud_rate);
		
            try{
                myDevice.open();
            }catch(XBeeException e){
                e.printStackTrace();
                myDevice.close();
                System.exit(1);
            }
            
            return myDevice;
        }
        
        public void enviarRequisicao(ZigBeeDevice myDevice, XBeeNetwork network, String destinatario, String mensagem){
            
            RemoteXBeeDevice remote = network.getDevice(destinatario);
            try{
                if(remote != null){
                    myDevice.sendData(remote, mensagem.getBytes());
                    XBeeMessage resposta = myDevice.readDataFrom(remote);
                    System.out.println(resposta.getDataString());
                }else{
                    System.err.println("Could not find the module " + destinatario + " in the network.");
                }
            }catch(XBeeException e) {
                if(!e.getMessage().equals("There was a timeout while executing the requested operation.")){
                   System.err.println("Error transmitting message: " + e.getMessage());
                }
            }
        }
        
	public static void main(String[] args) {
            
            Coordinator coord = new Coordinator();            
            ZigBeeDevice myDevice = coord.inicializarConexaoRedeZigbee("COM3", 9600);            
            XBeeNetwork network = coord.inicializarRedeZigbee(myDevice);
            coord.enviarRequisicao(myDevice, network, "END_DEVICE4", "desliga");
            
	} 
}
