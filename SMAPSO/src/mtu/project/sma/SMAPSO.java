/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.sma;

import jade.Boot;

/**
 *
 * @author Rafael
 */
public class SMAPSO {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        /* Argumentos
           Agente Load: Potência, Instantes de Tempo, ID da Fonte de Energia (0 Se não tiver), Tempo de Sleep(Sincronização), Dispositivo, Porta, Rate
           Agente SE: Nome da Fonte, Tipo da Fonte (1-Fotovoltaica, 2- Wind Power), Dispositivo, Porta, Rate
        */
        //String porta = "/dev/ttyUSB0";
        String porta = "COM3";
        /*
        String[] parametros = { "-name", "Factory",
                                "-container-name","Router2", 
                                "3:mtu.project.sma.load.AgentLoad(178.18, 45, 0, 12000, END_DEVICE3, "+porta+", 9600);"
                              + "4:mtu.project.sma.load.AgentLoad(238.18, 55, 1, 24000, END_DEVICE4, "+porta+", 9600);"
                              
                              };		
        Boot.main(parametros);	
        */
       
        /*
        String[] parametros = { "-host", "141.219.123.44", "-container",
                                "-container-name","Router1", 
                                "1:mtu.project.sma.sourceenergy.AgentSourceEnergy(Fotovoltaica1, 1, END_DEVICE1, "+porta+", 9600);"
                              + "5:mtu.project.sma.load.AgentLoad(256.36, 80, 1, 36000, END_DEVICE5, "+porta+", 9600);"
                              
                              };		
        Boot.main(parametros);	
        */
        
        
        String[] parametros = { "-gui", "-host", "141.219.123.44", "-container",
                                "-container-name","Coordinator", 
                                "Central:mtu.project.sma.central.AgentCentral(iniciar);"
                              + "2:mtu.project.sma.load.AgentLoad(47.18, 78, 0, 48000, END_DEVICE2, "+porta+", 9600);"
                              
                              };    
        Boot.main(parametros);	
        
	 
    }
    
}
