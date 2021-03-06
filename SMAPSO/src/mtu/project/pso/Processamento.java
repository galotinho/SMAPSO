/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.pso;

import mtu.project.pso.particula.GerarParticulaInicial;
import mtu.project.pso.util.Metricas;
import mtu.project.pso.search.ProcessarPares;
import mtu.project.pso.search.Par;
import mtu.project.pso.particula.Carga;
import mtu.project.pso.particula.GerarCargas;
import mtu.project.pso.search.BuscaLocal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import mtu.project.db.dao.ScheduleDAO;
import mtu.project.db.dao.SourceScheduleDAO;
import mtu.project.db.model.Schedule;
import mtu.project.pso.util.InserirParticulaBD;

/**
 *
 * @author Rafael
 */
public class Processamento implements Configuracao{
    
    Map<Integer,List<Par>> enxame = new HashMap<>();
    List<Par> melhorGlobal = new ArrayList<>();
    int VELOCIDADE;
    List<Carga> loads;
    Double potenciaMediaUnitaria, potenciaMediaGeral;
    List<Double> demandaMaxima;
    List<Double> demandaMinima;
       
    public void execute(){
               
        List<Schedule> schedules = ScheduleDAO.getInstance().listAllSchedules();
        Map particulaInicial;
        List<Carga> dados;
        if(schedules == null || schedules.isEmpty()){
            //new GerarCargas().imprimirListaDeCargas();
            dados = new GerarCargas().gerarListaDeCargas();
            loads = new ArrayList<>(dados);            
            particulaInicial = new GerarParticulaInicial().criarParticulaInicial(dados);
            new InserirParticulaBD().inserirParticula(particulaInicial, loads);
        }else{
            dados = new GerarCargas().gerarListaDeCargasExistentes();
            loads = new ArrayList<>(dados);            
            particulaInicial = new GerarParticulaInicial().criarParticulaInicialExistente(dados);
        }
        
        potenciaMediaUnitaria = Metricas.potenciaMediaUnitaria(dados);
        potenciaMediaGeral = (potenciaMediaUnitaria*dados.size())/(QTD_MIN_DIA/STEP); 
        demandaMaxima = new ArrayList<>();
        demandaMinima = new ArrayList<>();
        
        gerarDemandaPorTempo();
             
        //Exibir soma de potência dos grupos da particula inicial.
        int id = 0;
        for(Double grupo : (List<Double>)Metricas.calcularPotenciaTotalPorGrupo(particulaInicial)){
            System.out.println(grupo+" "+id);
            id++;
        }
                
        BuscaLocal buscaLocal = new BuscaLocal();
        int iteracoes = 0;
        
                
        while((iteracoes < ITERACOES-1) && 
                ((Metricas.avaliadorGeral(melhorGlobal, demandaMaxima, demandaMinima) != 0) || 
                                                                    melhorGlobal.isEmpty())){
            List<Par> pares;
            List<Par> parDeBusca;
            
            for(int i = 0; i < ENXAME; i++){
                
                VELOCIDADE = probabilidadeCaminho(iteracoes);
                pares = new ArrayList<>(buscaLocal.combinarParesAleatorios(particulaInicial, demandaMaxima, demandaMinima));
        
                if(VELOCIDADE == 0){
                      processamento(particulaInicial, i, pares); 
                }else{
                    if(enxame.size() == ENXAME && VELOCIDADE == 1){
                        parDeBusca = new ArrayList<>(enxame.get(i));
                        percorrerCaminho(parDeBusca, pares, particulaInicial, i); 
                    }else{
                        if(!melhorGlobal.isEmpty() && VELOCIDADE == 2){
                            parDeBusca = new ArrayList<>(melhorGlobal);
                            percorrerCaminho(parDeBusca, pares, particulaInicial, i);
                            
                        }
                    }
                }
            }
            iteracoes++;
        }
        System.out.println("Fim da Busca");
        System.out.println(Metricas.avaliadorGeral(melhorGlobal, demandaMaxima, demandaMinima));
                
        exibirMelhorGlobal();
        processarResultado(particulaInicial);
                
    }
    
    public void gerarDemandaPorTempo(){
        int intervalos = QTD_MIN_DIA / STEP; 
        
        demandaMaxima.add(0, 0.0);
        demandaMinima.add(0, 0.0);
        
        for(int i = 1; i <= intervalos; i++){
            Double demanda = SourceScheduleDAO.getInstance().sumByTime(i);
            demandaMaxima.add(i, potenciaMediaGeral+(potenciaMediaGeral*PORCENTAGEM)+demanda);
            demandaMinima.add(i, potenciaMediaGeral-(potenciaMediaGeral*PORCENTAGEM)+demanda);
        }       
              
    }
    
    public void processarResultado(Map particulaInicial){
        //removo schedules antigos
        new InserirParticulaBD().removeSchedules(loads);
        ProcessarPares par;
                
        for(Par p : melhorGlobal){
            par = new ProcessarPares(demandaMaxima);
            //exibo e insino no BD schedules atualizados
            par.processarPar(particulaInicial, p, 1);
            System.out.println("------------");
        }
    }
          
    public void exibirMelhorGlobal(){
        for(Par p : melhorGlobal){
            
            System.out.println(p.getPosicaoA()+" "+p.getDiferencaA());
            System.out.println(p.getPosicaoB()+" "+p.getDiferencaB());
        }
    }
    
    public void percorrerCaminho(List<Par> parML, List<Par> parBL, Map particulaInicial, 
                                                   int i){
        
        Par pA = null, pB = null;
        int pa = -1, pb = -1;
        
        for(Par pML : parML){
            for(Par pBL : parBL){
                if((pML.getPosicaoA() == pBL.getPosicaoA()) || (pML.getPosicaoA() == pBL.getPosicaoB())){
                    pA = pBL;
                }
                 if((pML.getPosicaoB() == pBL.getPosicaoA()) || (pML.getPosicaoB() == pBL.getPosicaoB())){
                    pB = pBL;
                }
            }
            
            if(pA != null){
                if((pML.getPosicaoA() == pA.getPosicaoA()) || (pML.getPosicaoB() == pA.getPosicaoA())){
                    pa = pA.getPosicaoB();
                }else{
                    pa = pA.getPosicaoA(); 
                }
            }
            
            if(pB != null){
                if((pML.getPosicaoA() == pB.getPosicaoA()) || (pML.getPosicaoB() == pB.getPosicaoA())){
                    pb = pB.getPosicaoB();
                }else{
                    pb = pB.getPosicaoA(); 
                }
            }
            
            if(pa != pb){
                
                Par p = new Par(pa, pb, 0.0, 0.0);
            
                parBL.remove(pA);
                parBL.remove(pB);

                parBL.add(pML);
                parBL.add(p);
                
                processamento(particulaInicial, i, parBL);
            }
            
            if(pa == -1 || pb == -1){
                System.out.println("Erro na execução da Busca: Programa Encerrado!");
                System.exit(1);
            }
            
        }
    }
        
    public void processamento(Map particulaInicial, int i, List<Par> pares){
        
        int metrica = 0;
               
        List<Par> paresAtualizados = new ArrayList<>();
             
        for(Par p : pares){
            
            ProcessarPares par = new ProcessarPares(demandaMaxima); 
            
            paresAtualizados.add(par.processarPar(particulaInicial, p, 0));

            if((p.getDiferencaA()>demandaMaxima.get(p.getPosicaoA())) || (p.getDiferencaA()<demandaMinima.get(p.getPosicaoA())) ||
                    (p.getDiferencaB()>demandaMaxima.get(p.getPosicaoB())) || (p.getDiferencaB()<demandaMinima.get(p.getPosicaoB()))){
                metrica++;
            }
        }

        if(enxame.size() < ENXAME){
            enxame.put(i, pares);
        }else{

            if(Metricas.avaliadorGeral(enxame.get(i), demandaMaxima, demandaMinima) > metrica){

                enxame.replace(i, enxame.get(i), pares);
            }
        }
        paresAtualizados.clear();
        
        for(Map.Entry<Integer, List<Par>> particula: enxame.entrySet()) { 
                
            if(melhorGlobal.isEmpty()){
                melhorGlobal.addAll(particula.getValue());
            }

            if(Metricas.avaliadorGeral(melhorGlobal, demandaMaxima, demandaMinima) > 
                    Metricas.avaliadorGeral(particula.getValue(), demandaMaxima, demandaMinima)){

                melhorGlobal.clear();
                melhorGlobal.addAll(particula.getValue());

            }
        }
    }
    
    public int probabilidadeCaminho(int iteracoes){
       
        Random gerador = new Random();
        int target = gerador.nextInt(100);
        int velocidade = 0;
        int bL, mL, mG;
        
        if(iteracoes >= 0 && iteracoes < VARIAVEL){
            velocidade = 0;
        }
        
        if(iteracoes >= VARIAVEL && iteracoes < VARIAVEL*2 ){
            
            bL = 80;
            mL = 15;
            mG = 5;
            
            if(target<bL){
                velocidade = 0;
            }else{
                if(target>bL && target <bL+mL){
                    velocidade = 1;
                }else{
                    velocidade = 2;
                }
            }
           
        }
        
        if(iteracoes>=VARIAVEL*2 && iteracoes<VARIAVEL*3 ){
            
            bL = 60;
            mL = 30;
            mG = 10;
            
            if(target<bL){
                velocidade = 0;
            }else{
                if(target>bL && target <bL+mL){
                    velocidade = 1;
                }else{
                    velocidade = 2;
                }
            }
            
        }
        
        if(iteracoes>=VARIAVEL*3 && iteracoes<VARIAVEL*4 ){
            
            bL = 40;
            mL = 40;
            mG = 20;
            
            if(target<bL){
                velocidade = 0;
            }else{
                if(target>bL && target <bL+mL){
                    velocidade = 1;
                }else{
                    velocidade = 2;
                }
            }
            
        }
        
        if(iteracoes>=VARIAVEL*4 && iteracoes<VARIAVEL*5 ){
            
            bL = 20;
            mL = 60;
            mG = 20;
            
            if(target<bL){
                velocidade = 0;
            }else{
                if(target>bL && target <bL+mL){
                    velocidade = 1;
                }else{
                    velocidade = 2;
                }
            }
            
        }
        
        if(iteracoes>=VARIAVEL*5 && iteracoes<VARIAVEL*6 ){
            
            bL = 20;
            mL = 50;
            mG = 30;
            
            if(target<bL){
                velocidade = 0;
            }else{
                if(target>bL && target <bL+mL){
                    velocidade = 1;
                }else{
                    velocidade = 2;
                }
            }
            
        }
        
        if(iteracoes>=VARIAVEL*6 && iteracoes<VARIAVEL*7 ){
            
            bL = 20;
            mL = 40;
            mG = 40;
            
            if(target<bL){
                velocidade = 0;
            }else{
                if(target>bL && target <bL+mL){
                    velocidade = 1;
                }else{
                    velocidade = 2;
                }
            }
            
        }
        
        if(iteracoes>=VARIAVEL*7 && iteracoes<VARIAVEL*8 ){
            
            bL = 10;
            mL = 30;
            mG = 60;
            
            if(target<bL){
                velocidade = 0;
            }else{
                if(target>bL && target <bL+mL){
                    velocidade = 1;
                }else{
                    velocidade = 2;
                }
            }
            
        }
        
        if(iteracoes>=VARIAVEL*8 && iteracoes<VARIAVEL*9 ){
            
            bL = 10;
            mL = 20;
            mG = 70;
            
            if(target<bL){
                velocidade = 0;
            }else{
                if(target>bL && target <bL+mL){
                    velocidade = 1;
                }else{
                    velocidade = 2;
                }
            }
        }
        
        if(iteracoes>=VARIAVEL*9 && iteracoes<ITERACOES ){
            
            bL = 10;
            mL = 10;
            mG = 80;
            
            if(target<bL){
                velocidade = 0;
            }else{
                if(target>bL && target <bL+mL){
                    velocidade = 1;
                }else{
                    velocidade = 2;
                }
            }
            
        }
        return velocidade;
        
    }
    
}
