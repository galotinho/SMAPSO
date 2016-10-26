/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.pso.particula;

/**
 *
 * @author Rafael
 */
public class Carga {
    
    private int prioridade;
    private Double potencia;
    private int tempo; // total em minutos
    private int tempoId;
    private Integer equipamentoId;
    
    public Carga(){
        super();
    }
    
    public Carga(int prioridade, Double potencia, int tempo, int tempoId, int equipamentoId){
        super();
        
        this.prioridade = prioridade;
        this.potencia = potencia;
        this.tempo = tempo;
        this.tempoId = tempoId;
        this.equipamentoId = equipamentoId;
    }

    /**
     * @return the prioridade
     */
    public int getPrioridade() {
        return prioridade;
    }

    /**
     * @param prioridade the prioridade to set
     */
    public void setPrioridade(int prioridade) {
        this.prioridade = prioridade;
    }

    /**
     * @return the potencia
     */
    public Double getPotencia() {
        return potencia;
    }

    /**
     * @param potencia the potencia to set
     */
    public void setPotencia(Double potencia) {
        this.potencia = potencia;
    }

    /**
     * @return the tempo
     */
    public int getTempo() {
        return tempo;
    }

    /**
     * @param tempo the tempo to set
     */
    public void setTempo(int tempo) {
        this.tempo = tempo;
    }

    /**
     * @return the tempoId
     */
    public int getTempoId() {
        return tempoId;
    }

    /**
     * @param tempoId the tempoId to set
     */
    public void setTempoId(int tempoId) {
        this.tempoId = tempoId;
    }

    /**
     * @return the equipamentoId
     */
    public int getEquipamentoId() {
        return equipamentoId;
    }

    /**
     * @param equipamentoId the equipamentoId to set
     */
    public void setEquipamentoId(int equipamentoId) {
        this.equipamentoId = equipamentoId;
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
        if (!(object instanceof Carga)) {
            return false;
        }
        Carga other = (Carga) object;
        if ((this.equipamentoId == null && other.equipamentoId != null) || (this.equipamentoId != null && !this.equipamentoId.equals(other.equipamentoId))) {
            return false;
        }
        return true;
    }
    
}

