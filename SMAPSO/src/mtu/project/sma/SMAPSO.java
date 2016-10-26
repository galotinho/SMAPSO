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
                                "-container-name", "Loads",
 				"1:mtu.project.sma.load.AgenteLoad(354.98, 27, 1);"
                              + "2:mtu.project.sma.load.AgenteLoad(178.18, 45, 1);"
                              + "3:mtu.project.sma.load.AgenteLoad(427.46, 51, 0)"};		
        Boot.main(parametros);		
		
        String[] novoContainer = { "-local-host", "127.0.0.1", "-container", 
				   "-container-name", "Coordenador",				
				   "Central:mtu.project.sma.central.AgenteCentral(\"iniciar\")"};
	Boot.main(novoContainer);
        
        	
    }
    
}
