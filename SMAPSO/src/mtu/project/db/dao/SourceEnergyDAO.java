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

    public EntityManager createEM() {
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SMAPSOBDPU");
        return emf.createEntityManager();
        
    }
    
    public void saveOrUpdate(SourceEnergy source){
        EntityManager em = createEM();
        try{
            em.getTransaction().begin();
            if(source.getSourceId() == null){
                em.persist(source);
            }else{
                if(!em.contains(source)){
                    if(em.find(SourceEnergy.class, source.getSourceId()) == null){
                        throw new Exception("Erro ao atualizar dados da carga;");
                    }
                }
                em.merge(source);
            }
            em.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }finally {
            em.close();
        }
    }
    
    public void remove(Long getSourceId){
        EntityManager em = createEM();
        try{
            SourceEnergy source = em.find(SourceEnergy.class, getSourceId);
            em.getTransaction().begin();
            em.remove(source);
            em.getTransaction().commit();
        }catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }finally {
            em.close();
        }
        
    }
    
    public SourceEnergy findById(Long getSourceId){
        EntityManager em = createEM();
        SourceEnergy source = null;
        try{
            source = em.find(SourceEnergy.class, getSourceId);
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            em.close();
        }
        
        return source;
    }
    
}
