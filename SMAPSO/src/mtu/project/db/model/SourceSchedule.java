/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.db.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author Rafael
 */
@Entity
public class SourceSchedule implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private int tempo;
    private Double potenciaPrevista;
    private Double potenciaReal;
    @ManyToOne
    private SourceEnergy sourceEnergy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getTempo() {
        return tempo;
    }

    public void setTempo(int tempo) {
        this.tempo = tempo;
    }

    public Double getPotenciaPrevista() {
        return potenciaPrevista;
    }

    public void setPotenciaPrevista(Double potenciaPrevista) {
        this.potenciaPrevista = potenciaPrevista;
    }

    public Double getPotenciaReal() {
        return potenciaReal;
    }

    public void setPotenciaReal(Double potenciaReal) {
        this.potenciaReal = potenciaReal;
    }

    public SourceEnergy getSourceEnergy() {
        return sourceEnergy;
    }

    public void setSourceEnergy(SourceEnergy sourceEnergy) {
        this.sourceEnergy = sourceEnergy;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SourceSchedule)) {
            return false;
        }
        SourceSchedule other = (SourceSchedule) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "multiagent.pso.model.SourceSchedule[ id=" + id + " ]";
    }
    
}
