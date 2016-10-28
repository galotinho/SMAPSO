/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.db.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import mtu.project.db.model.Load;
import mtu.project.db.model.Schedule;
import mtu.project.pso.particula.Carga;
/**
 *
 * @author Rafael
 */
public class ScheduleDAO {
    private static ScheduleDAO instance;
    protected static EntityManager entityManager;

    public static ScheduleDAO getInstance(){
        if (instance == null){
            instance = new ScheduleDAO();
        }
        return instance;
    }

    private ScheduleDAO() {
        entityManager = createEM();
    }
    
    public EntityManager createEM() {
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SMAPSOBDPU");
        
        if(entityManager == null){
            entityManager = emf.createEntityManager();
        }
        return entityManager;
    }
    
    public void save(Schedule schedule){
        try{
            entityManager.getTransaction().begin();
            entityManager.persist(schedule);
            entityManager.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }
    }
    
    public void update(Schedule schedule){
        try{
           entityManager.getTransaction().begin();
           if(!entityManager.contains(schedule)){
                if(entityManager.find(Load.class, schedule.getId()) == null){
                    throw new Exception("Erro ao atualizar dados do schedule;");
                }
           }
           entityManager.merge(schedule);
           entityManager.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }
    }
    
    public void remove(Long id){
        try{
            Schedule schedule = entityManager.find(Schedule.class, id);
            entityManager.getTransaction().begin();
            entityManager.remove(schedule);
            entityManager.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }       
    }
    
    public void removeAll(int equipamentoId){
        
        try{
            entityManager.getTransaction().begin();
            Query query = entityManager.createQuery(
                            "DELETE FROM Schedule s WHERE s.load.equipamentoId = :p");
            query.setParameter("p", (long)equipamentoId).executeUpdate();
            
            entityManager.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }       
    }
    
    public Schedule findByScheduleId(Long id){
        Schedule schedule = null;
        try{
            schedule = entityManager.find(Schedule.class, id);
        }catch (Exception e) {
            e.printStackTrace();
        }      
        return schedule;
    }
}
