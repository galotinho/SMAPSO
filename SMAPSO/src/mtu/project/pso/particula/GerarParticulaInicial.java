/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.pso.particula;

import mtu.project.pso.Configuracao;
import mtu.project.pso.util.EscreverCargas;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Rafael
 */
public class GerarParticulaInicial implements Configuracao{
        
    /* Quantidade de intervalos de tempo (STEPs) em um dia.
     * Quantidade de minutos de um dia dividido pelo STEP definido em Configuracoes 
     */
    int INTERVALOS_TEMPO_DIA = QTD_MIN_DIA / STEP; 
    int ERRO = 100;
    List<List> particulas = new ArrayList<>();
    Map<Integer,List<Carga>> particulaInicial = new HashMap<>();
        
    public Map criarParticulaInicial(List<Carga> dados){
        
        Random gerador = new Random();
        int indice; // Indice da Lista de Cargas
        Carga dado = null;
        int quantidadeDeCargas = dados.size(); //Quantidade de cargas por intervalo de tempo.
                       
        if(quantidadeDeCargas < INTERVALOS_TEMPO_DIA){
            quantidadeDeCargas = 1;
        }else{
            quantidadeDeCargas = dados.size()/INTERVALOS_TEMPO_DIA;
        }
        
        //System.out.println(dados.size());
        
        int tentativa;        
        boolean condicao;   
        
        //System.out.println(quantidadeDeCargas);
        
        /* Estrutura de repetição responsável por percorrer os dados e alocá-los em grupos
        *  INTERVALOS_TEMPO_DIA é a quantidade grupos. 
        *  quantidadeDeCargas é a quantidade de cargas por grupo.
        *  A distribuição das cargas nos grupos é feita de maneira aleatória, verificando apenas se aquela
        *  determinada carga já não está alocada naquele grupo. Obs: Toda carga pode ser alocada em N grupos,
        *  vai depender da quantidade tempo que ela demandará.
        */
        
        for(int i = 0; i<INTERVALOS_TEMPO_DIA; i++){
            
            List<Carga> dadosLocal = new ArrayList<>();
            tentativa = 0;
            
            /* A variável ERRO é responsável por encerrar o loop caso várias tentativas de alocar uma
             * determinada carga em algum grupo sejam realizadas sem êxito.
             */
            
            for(int j = 0; tentativa<ERRO && j<quantidadeDeCargas && dados.size()>0; j++){
                 do{
                    
                     condicao = false; // Variável responsável por avaliar se a carga já pertence ao grupo.
                     indice = gerador.nextInt(dados.size()); //Gera um índice aleatório.
                     dado = dados.get(indice); //Recupera o dado referente ao índice gerado anteriormente.
                    
                     for(Carga dadosLocal1 : dadosLocal) {
                           if(dado.getEquipamentoId() == dadosLocal1.getEquipamentoId()) {
                             condicao = true;
                             tentativa++;
                           }                       
                       }
                       // Verificação da quantidade de tentativas de alocar a carga em algum grupo.
                       if(tentativa >= ERRO){ condicao = false; }
                     
                    /* Caso a carga não pertença ao grupo ou caso exceda o número
                     * de tentativas para alocar a carga em algum grupo, o do...while é finalizado.
                     */
                    }while(condicao); 
                    
                if(tentativa < ERRO){
                    dadosLocal.add(dado);
                    dados.remove(indice);
                    tentativa = 0;
                }
            }              
            particulaInicial.put( i, dadosLocal);
        }
             
        //System.out.println(dados.size());
        
        // Cargas ainda não inseridas, passarão a ser inseridas em grupos através deste método.      
        inserirCargasRestantes(dados, dado);
        //imprimirParticulaInicial();
        try{
            EscreverCargas.escreverTempoFixo(particulaInicial);
        }catch(Exception e){
            System.out.println(e);
        }
        
        return particulaInicial;
    }  
    
    /* Estrutura de repetição responsável por alocar o que sobrou das cargas do processo anterior.
     * A variável "contem" é responsável por verificar se naquele grupo aquela carga já está contida. 
     * Caso não esteja, a carga é inserida neste grupo, o processo é realizado até que não exista mais
     * cargas na lista.
     */
    public void inserirCargasRestantes(List<Carga> dados, Carga dado){
        boolean contem = false;
        
        while(!dados.isEmpty()){ //Enquanto houver cargas na lista a repetição será realizada.
           
         //Verifica se a carga pode ser inserida grupo por grupo, até encontrar um grupo que ela não faça parte.
         for(Map.Entry<Integer, List<Carga>> entry: particulaInicial.entrySet()) { 
            
            if(contem == false){
                dado = dados.get(dados.size()-1); //Retira-se uma carga que sobrou do processo anterior.
            } 
            contem = false;
            
            for(Carga c: entry.getValue()) {
                
                if(c.getEquipamentoId() == dado.getEquipamentoId()){ //Verifica se a carga já está no grupo.
                    contem = true;
                }
            }
            if(contem == false){ //Caso não esteja no grupo, a carga é inserida.
                
                List<Carga> listaAtualizada = entry.getValue();
                listaAtualizada.add(dado);
                entry.setValue(listaAtualizada);
                dados.remove(dado);
                
                if(dados.isEmpty()){ break; }
            }           
          }
        }
    }
    
    public void imprimirParticulaInicial(){
        
        int soma = 0;
        
        for(int i = 0; i<INTERVALOS_TEMPO_DIA;i++){
            
            System.out.println(i + " ----");
            soma = soma + particulaInicial.get(i).size(); // Verificar se a quantidade de cargas está correta.
            
            for(int j = 0; j < particulaInicial.get(i).size(); j++){

                    Carga c = (Carga)particulaInicial.get(i).get(j);
                    System.out.println(c.getEquipamentoId());
                    System.out.println(c.getPotencia());
                    System.out.println(c.getPrioridade());
                    System.out.println(c.getTempoId());
                    System.out.println(c.getTempo());
            }
        }
        System.out.println(soma);
    }
}
