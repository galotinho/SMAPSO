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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 *
 * @author Rafael
 */
@Entity
public class SourceEnergy implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private Long sourceId;
    private int typeSource;
    private String nameSource;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "sourceEnergy")
    private List<SourceSchedule> sourceSchedule;
    
    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public int getTypeSource() {
        return typeSource;
    }

    public void setTypeSource(int typeSource) {
        this.typeSource = typeSource;
    }

    public String getNameSource() {
        return nameSource;
    }

    public void setNameSource(String nameSource) {
        this.nameSource = nameSource;
    }

    public List<SourceSchedule> getSourceSchedule() {
        return sourceSchedule;
    }

    public void setSourceSchedule(List<SourceSchedule> sourceSchedule) {
        this.sourceSchedule = sourceSchedule;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (sourceId != null ? sourceId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SourceEnergy)) {
            return false;
        }
        SourceEnergy other = (SourceEnergy) object;
        if ((this.sourceId == null && other.sourceId != null) || (this.sourceId != null && !this.sourceId.equals(other.sourceId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "multiagent.pso.model.SourceEnergy[ id=" + sourceId + " ]";
    }
    
}
