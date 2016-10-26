/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.pso.search;

import mtu.project.pso.particula.Carga;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mtu.project.pso.util.InserirParticulaBD;

/**
 *
 * @author Rafael
 */
public class ProcessarPares {
    
    Double demandaMaxima;
    static Map<Integer,List<Carga>> particulaFinal = new HashMap<>();
    
    public ProcessarPares(Double demandaMaxima){
        super();
        
        this.demandaMaxima = demandaMaxima;
    }
    
    public Map<Integer,List<Carga>> particulaFinal(){
        return particulaFinal;
    }
    
    public Par processarPar(Map<Integer,List<Carga>> particulaInicial, Par par, int print){
        
        List<Carga> parPosicaoA = new ArrayList<>(particulaInicial.get(par.getPosicaoA()));
        List<Carga> parPosicaoB = new ArrayList<>(particulaInicial.get(par.getPosicaoB()));
        
        List<Carga> auxPosicaoA = new ArrayList<>();
        List<Carga> auxPosicaoB = new ArrayList<>();
        List<Carga> auxPrioridadeA = new ArrayList<>();
        List<Carga> auxPrioridadeB = new ArrayList<>();
        
        int diferencaAB = parPosicaoA.size() - parPosicaoB.size();
        int diferencaBA = parPosicaoB.size() - parPosicaoA.size();
       
        //Armazena nas Listas auxiliares as cargas repetidas.
        for(Carga cargaA : parPosicaoA){
            for(Carga cargaB : parPosicaoB){
                if(cargaA.getEquipamentoId() == cargaB.getEquipamentoId()){
                    auxPosicaoA.add(cargaA);
                    auxPosicaoB.add(cargaB);                    
                }
            }
        }        
        //remove as cargas repetidas do Par A.
        parPosicaoA.removeAll(auxPosicaoA);
        
        //remove as cargas repetidas do Par B.
        parPosicaoB.removeAll(auxPosicaoB);
        
        //Armazena na Lista auxiliar A as cargas com prioridade.
        for(Carga cargaA : parPosicaoA){
            if(cargaA.getPrioridade() == 0){
                auxPrioridadeA.add(cargaA);
            }
        }
        //remove as cargas com prioridade do Par A.
        parPosicaoA.removeAll(auxPrioridadeA);
       
        //Armazena na Lista auxiliar B as cargas com prioridade.
        for(Carga cargaB : parPosicaoB){
            if(cargaB.getPrioridade() == 0){
                auxPrioridadeB.add(cargaB);
            }
        }
        //remove as cargas com prioridade do Par B.
        parPosicaoB.removeAll(auxPrioridadeB);
        
        //Adiciona cargas vazias para equilibrar quando for balancear
        if(diferencaAB > 0){
            for(int i = 0; i<diferencaAB; i++){
                parPosicaoB = adicionarObjetoVazio(parPosicaoB);
            }
        }else{
            if(diferencaBA > 0){
                for(int i = 0; i<diferencaBA; i++){
                parPosicaoA = adicionarObjetoVazio(parPosicaoA);
            }
            }
        }
        
        auxPosicaoA.addAll(auxPrioridadeA);
        auxPosicaoB.addAll(auxPrioridadeB);
        
        //Caso o número de cargas não seja vazia realiza as trocas para balanceamento.
        return trocarCargasEntrePar(parPosicaoA, parPosicaoB, par, somaPotencia(auxPosicaoA), somaPotencia(auxPosicaoB), auxPosicaoA, auxPosicaoB, print);
       
    }
      
    /* Método responsável por trocar cargas para melhor equilibrar a quantidade de 
       potência total entre os pares.
     */    
    public Par trocarCargasEntrePar(List<Carga> parPosicaoA, List<Carga> parPosicaoB, Par par, 
                 Double potenciaSimetricaA, Double potenciaSimetricaB, List<Carga> auxPosicaoA, 
                 List<Carga> auxPosicaoB, int print){
        
        
        Carga objetoPosicaoA, objetoPosicaoB;
        Double somaParPosicaoA, somaParPosicaoB, demandaDifNovo, demandaDifAntigo;
        
       
        if(print == 1){
            System.out.println(potenciaSimetricaA+" "+somaPotencia(parPosicaoA));
            System.out.println(potenciaSimetricaB+" "+somaPotencia(parPosicaoB));
            par.setDiferencaA(potenciaSimetricaA + somaPotencia(parPosicaoA));
            par.setDiferencaB(potenciaSimetricaB + somaPotencia(parPosicaoB));
        }
                
        //Compara as cargas do par verificando se existe troca que melhore o equilibrio na soma de potência.
        
        if(par.getDiferencaA().equals(0.0)){
            par.setDiferencaA(potenciaSimetricaA + somaPotencia(parPosicaoA));
        }
        
        if(par.getDiferencaB().equals(0.0)){
            par.setDiferencaB(potenciaSimetricaB + somaPotencia(parPosicaoB));
        }
       
        int controle = 0;
        int posicaoA = 0, posicaoB = 0;
        
        demandaDifAntigo = (Math.abs(demandaMaxima - par.getDiferencaA())) + 
                           (Math.abs(demandaMaxima - par.getDiferencaB()));
        
        while(controle == 0){
           
           controle = 1;
           
           for(int i = 0; i < parPosicaoA.size(); i++){
            for(int j = 0; j < parPosicaoB.size(); j++){

                objetoPosicaoA = parPosicaoA.get(i);
                objetoPosicaoB = parPosicaoB.get(j);

                somaParPosicaoA = (potenciaSimetricaA + somaPotencia(parPosicaoA) + objetoPosicaoB.getPotencia()) 
                        - objetoPosicaoA.getPotencia();
                somaParPosicaoB = (potenciaSimetricaB + somaPotencia(parPosicaoB) + objetoPosicaoA.getPotencia()) 
                        - objetoPosicaoB.getPotencia();

                demandaDifNovo = (Math.abs(demandaMaxima - somaParPosicaoA)) + (Math.abs(demandaMaxima 
                        - somaParPosicaoB));

                if(demandaDifNovo < demandaDifAntigo){

                    posicaoA = i;
                    posicaoB = j;
                    demandaDifAntigo = demandaDifNovo;
                    controle = 0;
                }
              }
            }
            if(controle == 0){

                 objetoPosicaoA = parPosicaoA.get(posicaoA);
                 objetoPosicaoB = parPosicaoB.get(posicaoB);

                 parPosicaoA.add(objetoPosicaoB);
                 parPosicaoA.remove(objetoPosicaoA);

                 parPosicaoB.add(objetoPosicaoA);
                 parPosicaoB.remove(objetoPosicaoB);

                 par.setDiferencaA(potenciaSimetricaA + somaPotencia(parPosicaoA));
                 par.setDiferencaB(potenciaSimetricaB + somaPotencia(parPosicaoB));

            }
        }
        
        if(print == 1){
            
            auxPosicaoA.addAll(parPosicaoA);
            auxPosicaoB.addAll(parPosicaoB);
           
            resultadoPrintDB(auxPosicaoA, auxPosicaoB, par);
          
        }
        return par;
    }

    public void resultadoPrintDB(List<Carga> auxPosicaoA, List<Carga> auxPosicaoB, Par par){

        auxPosicaoA = removerObjetoVazio(auxPosicaoA);
        auxPosicaoB = removerObjetoVazio(auxPosicaoB);
        //Inserir schedules atualziados no banco de dados
        new InserirParticulaBD().atualizarScheduleLoad(auxPosicaoA, par.getPosicaoA());
        new InserirParticulaBD().atualizarScheduleLoad(auxPosicaoB, par.getPosicaoB());
        
        System.out.println(par.getPosicaoA());
        for(Carga c: auxPosicaoA){
           System.out.println(c.getPotencia()+" "+c.getEquipamentoId());
        }
        
        System.out.println(par.getPosicaoB());
        for(Carga c: auxPosicaoB){
            System.out.println(c.getPotencia()+" "+c.getEquipamentoId());
        }
            
    }
    
    // Soma todas as potências da Lista.
    public Double somaPotencia(List<Carga> carga){
        
        Double soma = 0.0;
        
        for(Carga carga1 : carga){
            soma = soma + carga1.getPotencia();
        }
        return soma;
    }
    
    public List adicionarObjetoVazio(List<Carga> carga){
        Carga c = new Carga(0, 0.0, 0, 0, -1);
        carga.add(carga.size(), c);
        
        return carga;
    }
    
    public List removerObjetoVazio(List<Carga> carga){
        int tamanho = carga.size();
        List<Carga> objetos = new ArrayList<>();
        for(int i = 0; i<tamanho; i++){
            
            if(carga.get(i).getEquipamentoId() == -1){
                objetos.add(carga.get(i));
            }
        }
        if(!objetos.isEmpty()){
            carga.removeAll(objetos);
        }
        return carga;
    }
    
}
