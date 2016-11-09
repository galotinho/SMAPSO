/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.pso.particula;

import mtu.project.pso.Configuracao;
import mtu.project.pso.util.EscreverCargas;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;
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
        
        System.out.println(loads.size());
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
      
    public List<Carga> gerarListaDeCargasExistentes(){
        
        Date horaAtual = new Date();
        Locale locale = new Locale("pt","BR");
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH mm", locale);
        String currentTime = sdf.format(horaAtual);
        //O dia é dividido em 96 instantes de 15 minutos cada. Então utiliza-se um método para verificar em qual instante de tempo do dia a hora atual corresponde.
        StringTokenizer st = new StringTokenizer(currentTime);
        String hora = st.nextToken();
        String minuto = st.nextToken();
        int tempo = conversorTempo(hora, minuto); //método que converte a hora atual em um dos 96 instantes de tempo.
                
        Carga carga;
        List<Load> loads = LoadDAO.getInstance().listAllLoads();
        Load load;
        int equipamentoId;
     
        /* Aloca o equipamento em X STEPs de tempo, dependendo do tempo
         * total que o equipamento deverá funcionar.*/
        for (Load load1 : loads) {
            
            load = load1;
            equipamentoId = Integer.valueOf(String.valueOf((long)load.getEquipamentoId()));
            carga = new Carga(-1, load.getPotencia(), load.getTempo(), 0, equipamentoId);
            
            for(int j = 0; j < carga.getTempo(); j++){
                Carga c = new Carga();
                if(load.getSchedule().get(j).getTempo() >= tempo){
                    c.setPrioridade(load.getSchedule().get(j).getPrioridade());
                }else{
                    c.setPrioridade(0);
                }                
                c.setPotencia(carga.getPotencia());
                c.setTempo(carga.getTempo());
                c.setTempoId(load.getSchedule().get(j).getTempo());
                c.setEquipamentoId(equipamentoId);
                
                dados.add(c);
            }
        }     
        imprimirListaDeCargas();
        return dados;
    }
    
    public int conversorTempo(String hora, String minuto){
        
        int h = Integer.valueOf(hora)*4;
        int m = Integer.valueOf(minuto);
        
        if(m>=0 && m<15){
            m = 1;
        }else{
            if(m>=15 && m<30){
                m = 2;
            }else{
                if(m>=30 && m<45){
                    m = 3;
                }else{
                    m = 4;
                }
            }
        }
        return h+m;
    }
    
    public void imprimirListaDeCargas(){
        
                
        for (Carga carga : dados) {
            System.out.print(carga.getPrioridade());
            System.out.print(" "+carga.getPotencia());
            System.out.print(" "+carga.getTempo());
            System.out.print(" "+carga.getEquipamentoId());
            System.out.print(" "+carga.getTempoId()+"\n\n");
        }
        System.out.println(dados.size());
    }
    
    
}
