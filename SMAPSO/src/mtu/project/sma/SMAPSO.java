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
        String[] parametros = { "-gui", "-local-host", "127.0.0.1", "-name", "Factory1",
                                "-container-name", "Loads",
 				"L1:smapso.FIPARequestAgentsLoads();"
                              + "L2:smapso.FIPARequestAgentsLoads();"
                              + "L3:smapso.FIPARequestAgentsLoads()"};		
        Boot.main(parametros);		
		
        String[] novoContainer = { "-local-host", "127.0.0.1", "-container", 
				   "-container-name", "Manager Main",				
				   "Manager:smapso.AgentCentral(L1,L2,L3)"};
	Boot.main(novoContainer);
        
        	
    }
    
}
