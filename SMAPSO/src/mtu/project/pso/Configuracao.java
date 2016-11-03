/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.pso;
/**
 *
 * @author Rafael
 */
public interface Configuracao {
        
	int QUANTIDADE = 4; //Quantidade de equipamentos (cargas).
        int STEP = 15; //Intervalo de tempo em minutos da leitura do total de potência em uso.
        int QTD_MIN_DIA = 1440; // Quantidade de minutos em um dia.
        double PORCENTAGEM = 0.05; //Intervalo superior e inferior da potência total média.
        int ENXAME = 30; // Quantidade de partículas.
        int ITERACOES = 1000; //Quantidade de iterações
        int VARIAVEL = ITERACOES/10; //Usado para calcular a probabilidade das velocidades.
        String CaminhoDoArquivo = "D:\\Perfil\\Desktop\\Imagens\\dados.dat";
}