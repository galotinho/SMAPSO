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
import mtu.project.db.model.SourceEnergy;
import mtu.project.db.model.SourceSchedule;

/**
 *
 * @author Rafael
 */
public class SourceScheduleDAO {
    private static SourceScheduleDAO instance;
    protected static EntityManager entityManager;

    public static SourceScheduleDAO getInstance(){
        if (instance == null){
            instance = new SourceScheduleDAO();
        }
        return instance;
    }

    private SourceScheduleDAO() {
        entityManager = createEM();
    }
    
    public EntityManager createEM() {
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SMAPSOBDPU");
        
        if(entityManager == null){
            entityManager = emf.createEntityManager();
        }
        return entityManager;
    }
    
    public void save(SourceSchedule schedule){
        try{
            entityManager.getTransaction().begin();
            entityManager.persist(schedule);
            entityManager.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }
    }
    
    public void update(SourceSchedule schedule){
        try{
           entityManager.getTransaction().begin();
           if(!entityManager.contains(schedule)){
                if(entityManager.find(SourceEnergy.class, schedule.getId()) == null){
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
            SourceSchedule schedule = entityManager.find(SourceSchedule.class, id);
            entityManager.getTransaction().begin();
            entityManager.remove(schedule);
            entityManager.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }       
    }
    
    public void removeAll(SourceEnergy source){
        
        try{
            entityManager.getTransaction().begin();
            Query query = entityManager.createQuery(
                            "DELETE FROM SourceSchedule s WHERE s.sourceEnergy.sourceId = :p");
            query.setParameter("p", (long)source.getSourceId()).executeUpdate();
            
            entityManager.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }       
    }
    
    public SourceSchedule findByEquipamentoId(Long id){
        SourceSchedule schedule = null;
        try{
            entityManager.getTransaction().begin();
            schedule = entityManager.find(SourceSchedule.class, id);
            entityManager.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
        }      
        return schedule;
    }
}
