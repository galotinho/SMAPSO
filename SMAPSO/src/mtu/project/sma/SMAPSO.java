/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.sma;

import jade.Boot;
import mtu.project.db.dao.LoadDAO;
import mtu.project.db.model.Load;

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
        String porta = "COM12";
       /*
        String[] parametros = { "-gui", "-local-host", "127.0.0.1", "-name", "Factory",
                                "-container-name", "Load3",
 				"3:mtu.project.sma.load.AgentLoad(354.98, 27, 0, 1, END_DEVICE4, "+porta+", 9600);"
                              };		
        Boot.main(parametros);
       */ 
       /*
        String[] parametros = { "-gui", "-local-host", "127.0.0.1", "-container", 
                                "-container-name","Load2-SE1", 
                                "2:mtu.project.sma.load.AgentLoad(178.18, 45, 1, 5000, END_DEVICE4, "+porta+", 9600);"
                              + "1:mtu.project.sma.sourceenergy.AgentSourceEnergy(Fotovoltaica1, 1, END_DEVICE4, "+porta+", 9600);"
                              
                              };		
        Boot.main(parametros);	
        
		
        String[] novoContainer = { "-local-host", "127.0.0.1", "-container",
				   "-container-name", "Coordenador",				
				   "Central:mtu.project.sma.central.AgentCentral(iniciar)"};
	Boot.main(novoContainer);
        */
        Load carga = LoadDAO.getInstance().findByEquipamentoId(2L);
        System.out.println(carga.getPotencia());
                
    }
}
