/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.xbee;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.ZigBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBeeMessage;

/**
 *
 * @author rferreir
 */
public class ConexaoXBee {
    private static ConexaoXBee instance;
    protected static ZigBeeDevice myDevice;            
    protected static XBeeNetwork network;
    
    public static ConexaoXBee getInstance(String port, int baud_rate){
        if (instance == null){
            instance = new ConexaoXBee(port, baud_rate);
        }
        return instance;
    }

    private ConexaoXBee(String port, int baud_rate) {
        myDevice = inicializarConexaoRedeZigbee(port, baud_rate);
        network = inicializarRedeZigbee();
    }
    
    public ZigBeeDevice inicializarConexaoRedeZigbee(String port, int baud_rate){
        if(myDevice == null){
            myDevice = new ZigBeeDevice(port, baud_rate);
            try{
                myDevice.open();
            }catch(XBeeException e){
                e.printStackTrace();
                myDevice.close();
                System.out.println("Conexao nao foi estabelecida!");
            }
        }    
        return myDevice;
    }
    
    public XBeeNetwork inicializarRedeZigbee(){
      
        if(network == null && myDevice != null){
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
                System.out.println("Alguns dispostivos nao foram encontrados e a rede nao pode ser estabelecida!");
            }
        }
        
        return network;
    }
    
    public String enviarRequisicao(String destinatario, String mensagem){
            
        XBeeMessage resposta = null;
        RemoteXBeeDevice remote = network.getDevice(destinatario);
        try{
            if(remote != null){
                myDevice.sendData(remote, mensagem.getBytes());
                resposta = myDevice.readDataFrom(remote);
                System.out.println(resposta.getDataString());
            }else{
                System.err.println("Could not find the module " + destinatario + " in the network.");
            }
        }catch(XBeeException e) {
            if(!e.getMessage().equals("There was a timeout while executing the requested operation.")){
               System.err.println("Error transmitting message: " + e.getMessage());
            }
        }
        return resposta.getDataString();
    }
    
}
