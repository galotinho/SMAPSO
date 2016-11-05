/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.db.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import mtu.project.db.model.SourceEnergy;

/**
 *
 * @author Rafael
 */
public class SourceEnergyDAO {

    private static SourceEnergyDAO instance;
    protected static EntityManager entityManager;

    public static SourceEnergyDAO getInstance(){
        if (instance == null){
            instance = new SourceEnergyDAO();
        }
        return instance;
    }

    private SourceEnergyDAO() {
        entityManager = createEM();
    }
    
    public EntityManager createEM() {
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SMAPSOBDPU");
        
        if(entityManager == null){
            entityManager = emf.createEntityManager();
        }
        
        return entityManager;
        
    }
    
    public void save(SourceEnergy energy){
        try{
            entityManager.getTransaction().begin();
            entityManager.persist(energy);
            entityManager.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }
    }
    
    public void update(SourceEnergy energy){
        try{
           entityManager.getTransaction().begin();
           if(!entityManager.contains(energy)){
                if(entityManager.find(SourceEnergy.class, energy.getSourceId()) == null){
                    throw new Exception("Erro ao atualizar dados da carga;");
                }
           }
           entityManager.merge(energy);
           entityManager.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }
    }
    
    public void remove(Long sourceId){
        try{
            SourceEnergy energy = entityManager.find(SourceEnergy.class, sourceId);
            entityManager.getTransaction().begin();
            entityManager.remove(energy);
            entityManager.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }       
    }
    
    public SourceEnergy findBySourceId(Long sourceId){
        SourceEnergy carga = null;
        try{
            entityManager.getTransaction().begin();
            carga = entityManager.find(SourceEnergy.class, sourceId);
            entityManager.getTransaction().begin();
        }catch (Exception e) {
            e.printStackTrace();
        }       
       
        return carga;
    }
}
