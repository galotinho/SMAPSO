/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.pso.particula;

import mtu.project.pso.Configuracao;
import mtu.project.pso.util.EscreverCargas;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mtu.project.db.dao.LoadDAO;
import mtu.project.db.model.Load;

/**
 *
 * @author Rafael
 */
public class GerarCargas implements Configuracao{
    
    List<Carga> dados = new ArrayList<>();
    Random gerador = new Random();
        
    public GerarCargas(){
        super();
    }
        
    public List<Carga> gerarListaDeCargas(){
        
        List<Carga> cargaArquivo = new ArrayList<>();
        Carga carga, cargaArquivoEscrita;
        List<Load> loads = LoadDAO.getInstance().listAllLoads();
        Load load;
        int equipamentoId;
         /* Aloca o equipamento em X STEPs de tempo, dependendo do tempo 
         * total que o equipamento deverá funcionar.*/
        for(int i = 0; i<loads.size(); i++){
            
            load = loads.get(i);
            equipamentoId = Integer.valueOf(String.valueOf((long)load.getEquipamentoId()));
            
            carga = new Carga(-1, load.getPotencia(), load.getTempo(), 0, equipamentoId);
            
            cargaArquivoEscrita = carga;
            cargaArquivoEscrita.setTempo(load.getTempo());
            cargaArquivo.add(cargaArquivoEscrita);
                                    
            Carga c = new Carga();
             
            for(int j = 1; j <= carga.getTempo(); j++){
                if(carga.getPrioridade() == -1){                    
                    c.setPrioridade(gerador.nextInt(4));                    
                }
                c.setPotencia(carga.getPotencia());
                c.setTempo(carga.getTempo());
                c.setTempoId(j);
                c.setEquipamentoId(equipamentoId);
                
                dados.add(c);
            }
        }
        try{
            EscreverCargas.escreverArquivo(cargaArquivo, dados);
            
        }catch(Exception e){
            System.out.println(e);
        }
        return dados;
    }
      
    public void imprimirListaDeCargas(){
        
        gerarListaDeCargas();
        
        for (Carga carga : dados) {
            System.out.print(carga.getPrioridade());
            System.out.print(" "+carga.getPotencia());
            System.out.print(" "+carga.getTempo());
            System.out.print(" "+carga.getTempoId()+"\n\n");
        }
    }
}
