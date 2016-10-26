/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.db.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author Rafael
 */
@Entity
@Table(name="\"Load\"")
public class Load implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private Long equipamentoId;
    private Double potencia;
    private int tempo; // total em minutos
    private int fonteEnergia;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "load")
    private List<Schedule> schedule;

    public Long getEquipamentoId() {
        return equipamentoId;
    }

    public void setEquipamentoId(Long equipamentoId) {
        this.equipamentoId = equipamentoId;
    }

    public Double getPotencia() {
        return potencia;
    }

    public void setPotencia(Double potencia) {
        this.potencia = potencia;
    }

    public int getTempo() {
        return tempo;
    }

    public void setTempo(int tempo) {
        this.tempo = tempo;
    }

    public int getFonteEnergia() {
        return fonteEnergia;
    }

    public void setFonteEnergia(int fonteEnergia) {
        this.fonteEnergia = fonteEnergia;
    }

    public List<Schedule> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<Schedule> schedule) {
        this.schedule = schedule;
    }

    

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (equipamentoId != null ? equipamentoId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Load)) {
            return false;
        }
        Load other = (Load) object;
        if ((this.equipamentoId == null && other.equipamentoId != null) || (this.equipamentoId != null && !this.equipamentoId.equals(other.equipamentoId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "mtu.project.db.model.Load[ id=" + equipamentoId + " ]";
    }
    
}