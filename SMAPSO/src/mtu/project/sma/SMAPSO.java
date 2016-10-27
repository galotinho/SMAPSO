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
        // TODO code application logic here
        String[] parametros = { "-gui", "-local-host", "127.0.0.1", "-name", "Factory",
                                "-container-name", "Loads-SourceEnergy",
 				//"1:mtu.project.sma.load.AgentLoad(354.98, 27, 4);"
                                "2:mtu.project.sma.load.AgentLoad(178.18, 45, 4);"
                              + "4:mtu.project.sma.sourceenergy.AgentSourceEnergy();"
                              //+ "3:mtu.project.sma.load.AgentLoad(427.46, 51, 0)"
                              };		
        Boot.main(parametros);		
		
        String[] novoContainer = { "-local-host", "127.0.0.1", "-container", 
				   "-container-name", "Coordenador",				
				   "Central:mtu.project.sma.central.AgentCentral(iniciar)"};
	Boot.main(novoContainer);
        
        	
    }
    
}
