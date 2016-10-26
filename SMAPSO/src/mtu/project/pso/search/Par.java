/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.pso.search;

/**
 *
 * @author Rafael
 */
public class Par {
    
    private int posicaoA;
    private int posicaoB;
    private Double diferencaA;
    private Double diferencaB;
    
    public Par(){
        super();
    }
    
    public Par(int posicaoA, int posicaoB, Double diferencaA, Double diferencaB){
        super();
        
        this.posicaoA = posicaoA;
        this.posicaoB = posicaoB;
        this.diferencaA = diferencaA;
        this.diferencaB = diferencaB;
    }

    /**
     * @return the posicaoA
     */
    public int getPosicaoA() {
        return posicaoA;
    }

    /**
     * @param posicaoA the posicaoA to set
     */
    public void setPosicaoA(int posicaoA) {
        this.posicaoA = posicaoA;
    }

    /**
     * @return the posicaoB
     */
    public int getPosicaoB() {
        return posicaoB;
    }

    /**
     * @param posicaoB the posicaoB to set
     */
    public void setPosicaoB(int posicaoB) {
        this.posicaoB = posicaoB;
    }

    /**
     * @return the diferencaA
     */
    public Double getDiferencaA() {
        return diferencaA;
    }

    /**
     * @param diferencaA the diferencaA to set
     */
    public void setDiferencaA(Double diferencaA) {
        this.diferencaA = diferencaA;
    }

    /**
     * @return the diferencaB
     */
    public Double getDiferencaB() {
        return diferencaB;
    }

    /**
     * @param diferencaB the diferencaB to set
     */
    public void setDiferencaB(Double diferencaB) {
        this.diferencaB = diferencaB;
    }
    
    
}

