/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.db.dao;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import mtu.project.db.model.Load;

/**
 *
 * @author Rafael
 */
public class LoadDAO {

    private static LoadDAO instance;
    protected static EntityManager entityManager;

    public static LoadDAO getInstance(){
        if (instance == null){
            instance = new LoadDAO();
        }
        return instance;
    }

    private LoadDAO() {
        entityManager = createEM();
    }
    
    public EntityManager createEM() {
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SMAPSOBDPU");
        
        if(entityManager == null){
            entityManager = emf.createEntityManager();
        }
        
        return entityManager;
        
    }
    
    public void save(Load carga){
        try{
            entityManager.getTransaction().begin();
            entityManager.persist(carga);
            entityManager.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }
    }
    
    public void update(Load carga){
        try{
           entityManager.getTransaction().begin();
           if(!entityManager.contains(carga)){
                if(entityManager.find(Load.class, carga.getEquipamentoId()) == null){
                    throw new Exception("Erro ao atualizar dados da carga;");
                }
           }
           entityManager.merge(carga);
           entityManager.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }
    }
    
    public void saveWithFindById(Load load){
        Load carga = null;
        try{
            carga = entityManager.find(Load.class, load.getEquipamentoId());
            if(carga == null){
                entityManager.getTransaction().begin();
                entityManager.persist(load);
                entityManager.getTransaction().commit();
            }
        }catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }       
    }
    
    public void remove(Long equipamentoId){
        try{
            Load carga = entityManager.find(Load.class, equipamentoId);
            entityManager.getTransaction().begin();
            entityManager.remove(carga);
            entityManager.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }       
    }
    
    public List<Load> listAllLoads() {
        Query query = entityManager.createQuery("SELECT l FROM Load l", Load.class);
 
        return query.getResultList();
    }
    
    public void removeAll(){
        
        try{
            entityManager.getTransaction().begin();
            Query query = entityManager.createNativeQuery("DELETE FROM multiagentsystem.load");
            query.executeUpdate();                      
            entityManager.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }       
    }
    
    
    public Load findByEquipamentoId(Long equipamentoId){
        Load carga = null;
        try{
            entityManager.getTransaction().begin();
            carga = entityManager.find(Load.class, equipamentoId);
            entityManager.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
        }       
       
        return carga;
    }
    
    
              
}