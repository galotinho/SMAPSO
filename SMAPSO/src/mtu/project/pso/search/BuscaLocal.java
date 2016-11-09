/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.pso.search;


import mtu.project.pso.particula.Carga;
import mtu.project.pso.util.Metricas;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Rafael
 */
public class BuscaLocal {
    
    List<Double> potenciaPorGrupo;
        
    public BuscaLocal(){
        super();
    }
        
    public List<Par> combinarParesAleatorios(Map<Integer, List<Carga>> particulaInicial,
                                    List<Double> demandaMaxima, List<Double> demandaMinima){
        
        List<Integer> colunaBinaria = new ArrayList<>();
        List<Integer> posicaoMaior = new ArrayList<>();
        List<Integer> posicaoMenor = new ArrayList<>();
        List<Integer> posicaoIntervalo = new ArrayList<>(); 
        
        potenciaPorGrupo = Metricas.calcularPotenciaTotalPorGrupo(particulaInicial);
        
        int j = 0;
        for (int i = 1; i < potenciaPorGrupo.size();i++) {
            if(potenciaPorGrupo.get(i) >= demandaMaxima.get(i)){
                colunaBinaria.add(j, 1);
                posicaoMaior.add(j);
            }else{
                if(potenciaPorGrupo.get(j) <= demandaMinima.get(i)){
                    colunaBinaria.add(j, -1); 
                    posicaoMenor.add(j);
                }else{
                     colunaBinaria.add(j, 0); 
                     posicaoIntervalo.add(j);
                }       
            }
            j++;
        }
        
        return combinarPares(posicaoMaior, posicaoMenor, posicaoIntervalo, colunaBinaria);
    }
    
    public void calcularQuantidadeBinaria(List<Integer> colunaBinaria){
        
        int maior = 0, menor = 0, intervalo = 0;
        
        for(Integer colunaBinaria1 : colunaBinaria){
            if(colunaBinaria1 == 1){
                maior++;
            }else{
                if(colunaBinaria1 == -1){
                    menor++;
                }else{
                    intervalo++;
                }
            }
        }
        System.out.println(maior+" - "+menor+" - "+intervalo);
    }
    
    public List<Par> combinarPares(List<Integer> posicaoMaior, List<Integer> posicaoMenor, 
                                        List<Integer> posicaoIntervalo, List<Integer> colunaBinaria){
        
        int tamanho = colunaBinaria.size();
        List<Par> parDeTrocas = new ArrayList<>();
        Par par;
        Random gerador = new Random();
        Integer posicaoA, posicaoB; 
        int indiceA, indiceB;
               
        for(int i = 0; i < tamanho/2; i++){
            
            posicaoA = -1; posicaoB = -1;
            
            if(posicaoMaior.size()>0 && posicaoMenor.size()>0){
                
                 indiceA = gerador.nextInt(posicaoMaior.size());
                 indiceB = gerador.nextInt(posicaoMenor.size());
                 posicaoA = posicaoMaior.get(indiceA);
                 posicaoB = posicaoMenor.get(indiceB);
                 posicaoMaior.remove(posicaoA);
                 posicaoMenor.remove(posicaoB);
                                  
            }else{
                if(posicaoMaior.size()>0 && posicaoIntervalo.size()>0){
                    
                    indiceA = gerador.nextInt(posicaoMaior.size());
                    indiceB = gerador.nextInt(posicaoIntervalo.size());
                    posicaoA = posicaoMaior.get(indiceA);
                    posicaoB = posicaoIntervalo.get(indiceB);
                    posicaoMaior.remove(posicaoA);
                    posicaoIntervalo.remove(posicaoB);
                    
                }else{
                    if(posicaoMenor.size()>0 && posicaoIntervalo.size()>0){
                        
                        indiceA = gerador.nextInt(posicaoMenor.size());
                        indiceB = gerador.nextInt(posicaoIntervalo.size());
                        posicaoA = posicaoMenor.get(indiceA);
                        posicaoB = posicaoIntervalo.get(indiceB);
                        posicaoMenor.remove(posicaoA);
                        posicaoIntervalo.remove(posicaoB);
                        
                    }else{
                        if(posicaoIntervalo.size()>2){
                            do{
                                indiceA = gerador.nextInt(posicaoIntervalo.size());
                                indiceB = gerador.nextInt(posicaoIntervalo.size());
                            }while(indiceA == indiceB);
                            
                            posicaoA = posicaoIntervalo.get(indiceA);
                            posicaoB = posicaoIntervalo.get(indiceB);
                            posicaoIntervalo.remove(posicaoA);
                            posicaoIntervalo.remove(posicaoB);
                            
                        }else{
                            if(posicaoIntervalo.size() == 2){
                                posicaoA = posicaoIntervalo.get(1);
                                posicaoB = posicaoIntervalo.get(0);
                                posicaoIntervalo.remove(posicaoA);
                                posicaoIntervalo.remove(posicaoB);
                            }
                        }
                    }
                }
            }
            
            
            if(posicaoA == -1 || posicaoB == -1){
                System.out.println("Erro na execução da Troca de Pares: Programa Encerrado!");
                System.exit(2);
            }
            
            posicaoA =  posicaoA + 1;
            posicaoB = posicaoB + 1;
            
            par = new Par(posicaoA, posicaoB, potenciaPorGrupo.get(posicaoA), potenciaPorGrupo.get(posicaoB));
            parDeTrocas.add(par);
            
        }
        
        return parDeTrocas;
    }
    
}
