/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.pso.util;

import mtu.project.pso.search.Par;
import mtu.project.pso.particula.Carga;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rafael
 */
public class Metricas {
    
        
    public static List calcularPotenciaTotalPorGrupo(Map<Integer, List<Carga>> particulas){
        
        List<Double> potenciaGrupo = new ArrayList<>();
        Double somaPotencia = 0.0;
        potenciaGrupo.add(0, somaPotencia);       
        for(Map.Entry<Integer, List<Carga>> entry: particulas.entrySet()) { 
            for(Carga c: entry.getValue()) {
                somaPotencia = somaPotencia + c.getPotencia();
            }
            
            potenciaGrupo.add(entry.getKey(), somaPotencia);
            somaPotencia = 0.0;
         }
        return potenciaGrupo; 
    }
    
    public static Double potenciaMediaUnitaria(List<Carga> dados){
        Double somaPotencia = 0.0;
        for(int i = 0; i<dados.size(); i++) {
                somaPotencia = somaPotencia + dados.get(i).getPotencia();
            }
        
        
        return somaPotencia/dados.size();
    }
    
    public static int avaliadorGeral(List<Par> pares, List<Double> demandaMaxima, List<Double> demandaMinima ){
        
        int metrica = 0;
        
        if(!pares.isEmpty()){
            for(Par p : pares){

            if((p.getDiferencaA()>demandaMaxima.get(p.getPosicaoA())) || (p.getDiferencaA()<demandaMinima.get(p.getPosicaoA())) ||
                    (p.getDiferencaB()>demandaMaxima.get(p.getPosicaoB())) || (p.getDiferencaB()<demandaMinima.get(p.getPosicaoB()))){
                metrica++;
            }
            }
        }       
        
        return metrica;
    }
}
