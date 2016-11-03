/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.pso.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mtu.project.db.dao.LoadDAO;
import mtu.project.db.dao.ScheduleDAO;
import mtu.project.db.model.Load;
import mtu.project.db.model.Schedule;
import mtu.project.pso.particula.Carga;

/**
 *
 * @author Rafael
 */
public class InserirParticulaBD {
    
    public void inserirParticula(Map<Integer,List<Carga>> particula, List<Carga> dados){
        
        Set<Carga> set = new HashSet<>(dados);
        List<Carga> cargas = new ArrayList<>(set);
                       
        for(Carga carga : cargas){
             
            Load load = new Load();
            load.setEquipamentoId((long)carga.getEquipamentoId());
            load.setPotencia(carga.getPotencia());
            load.setTempo(carga.getTempo());
            List<Schedule> listaTempo = new ArrayList<>();
            
            for(Map.Entry<Integer, List<Carga>> entry: particula.entrySet()) { 
               for(Carga c: entry.getValue()) {
                    if(c.getEquipamentoId() == carga.getEquipamentoId()){
                        Schedule tempo = new Schedule();
                        tempo.setPrioridade(c.getPrioridade());
                        tempo.setTempo(entry.getKey());
                        tempo.setLoad(load);
                        listaTempo.add(tempo);
                    }
                }
            }
            load.setSchedule(listaTempo);
            LoadDAO.getInstance().update(load);
        }
    }
    
    public void removeSchedules(List<Carga> loads){
        Set<Carga> set = new HashSet<>(loads);
        List<Carga> cargas = new ArrayList<>(set);
        
            for(Carga c: cargas) {
                ScheduleDAO.getInstance().removeAll(c.getEquipamentoId());
            }
    }
    
    public void atualizarScheduleLoad(List<Carga> cargas, int schedule){
        
        Load load = null;
        for(Carga c: cargas) {
                 load = LoadDAO.getInstance().findByEquipamentoId((long)c.getEquipamentoId());
                                                   
                 List<Schedule> ls = new ArrayList<>();
                 Schedule s = new Schedule();
                 s.setPrioridade(c.getPrioridade());
                 s.setTempo(schedule);
                 s.setLoad(load);
                 ls.add(s);
                 
                 load.setSchedule(ls);
                 
                 LoadDAO.getInstance().update(load);
            }
    }
}
