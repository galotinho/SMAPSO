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

/**
 *
 * @author Rafael
 */
public class GerarCargas implements Configuracao{
    
    List<Carga> dados = new ArrayList<>();
    Random gerador = new Random();
    static int equipamentoId; //Identificador da Load
    
    public GerarCargas(){
        super();
    }
    //Método para gerar uma carga com Prioridade, Potência e Tempos de Uso aleatórios.
    public Carga criarCarga(){
        
        Double potencia = gerador.nextDouble()*100;
        int tempo = gerador.nextInt(QTD_MIN_DIA+1);
        
        while(tempo == 0){
            tempo = gerador.nextInt(QTD_MIN_DIA+1);
        }
        
        Carga carga = new Carga(-1, potencia, tempo, 0, equipamentoId++ );
        return carga;
    }
    
    public List<Carga> gerarListaDeCargas(){
        
        List<Carga> cargaArquivo = new ArrayList<>();
        Carga carga, cargaArquivoEscrita;
        /* Aloca o equipamento em X STEPs de tempo, dependendo do tempo 
         * total que o equipamento deverá funcionar.*/
        for(int i = 0; i<QUANTIDADE; i++){
            
            carga = criarCarga();            
            int step = carga.getTempo();
            
            if(step < STEP){
               step = 1;
            }else{               
                if(step % STEP == 0){
                   step = carga.getTempo()/STEP;
                }else{                
                   step = (carga.getTempo()/STEP)+1;
                }
            }
            
            cargaArquivoEscrita = carga;
            cargaArquivoEscrita.setTempo(step);
            cargaArquivo.add(cargaArquivoEscrita);
            
            Carga c = new Carga();
             
            for(int j = 1; j <= step; j++){
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
