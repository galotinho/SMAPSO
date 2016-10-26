/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.pso.util;

import static mtu.project.pso.Configuracao.*;
import mtu.project.pso.particula.Carga;
import mtu.project.pso.util.Metricas;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

 
public class EscreverCargas {
 
    
    
    public static void escreverArquivo(List<Carga> cargaArquivo, List<Carga> dados) throws IOException {

      
      Double potenciaMediaUnitaria = Metricas.potenciaMediaUnitaria(dados);
      Double potenciaMediaGeral = (potenciaMediaUnitaria*dados.size())/(QTD_MIN_DIA/STEP); 
      
      Locale locale = Locale.US;  
      NumberFormat df = new DecimalFormat("0.###").getNumberInstance(locale); 
      
      FileWriter arq = new FileWriter(CaminhoDoArquivo);
      PrintWriter gravarArq = new PrintWriter(arq);

      gravarArq.printf("M = "+QUANTIDADE+";%n");
      gravarArq.printf("N = "+QTD_MIN_DIA/STEP+";%n");
      gravarArq.printf("K = "+df.format(potenciaMediaGeral+(potenciaMediaGeral*PORCENTAGEM))+";%n%n");
      gravarArq.printf("P = [");
      
      
      for (int i = 0; i < cargaArquivo.size(); i++) {
        gravarArq.printf(df.format((Double)cargaArquivo.get(i).getPotencia())+"%n");
      }
      gravarArq.printf("];%n%n");
      gravarArq.printf("D = [");

      for (int i = 0; i < cargaArquivo.size(); i++) {
        gravarArq.printf(String.valueOf(cargaArquivo.get(i).getTempo()+"%n"));
      }
      gravarArq.printf("];%n%n");
      arq.close();

      System.out.printf("\nArquivo de cargas foi gravado com sucesso em "+CaminhoDoArquivo+".\n");
  }
  
  public static void escreverTempoFixo(Map<Integer,List<Carga>> particula) throws IOException{
      
      String arq = CaminhoDoArquivo;
      int cont = 0, avaliador = 0;
      List<Integer> tempo = new ArrayList<>();
      List<Integer> potencia = new ArrayList<>();
      
      if(new File(arq).exists()) {
          BufferedWriter gravarArq = new BufferedWriter(new FileWriter(arq, true));   
          gravarArq.write("T = [ ");
          gravarArq.newLine();
          
          for(Map.Entry<Integer, List<Carga>> entry: particula.entrySet()) { 
            for(Carga c: entry.getValue()) {
               if(c.getPrioridade() == 0){
                   tempo.add(entry.getKey());
                   potencia.add(c.getEquipamentoId());
                   //System.out.println(entry.getKey()+" "+c.getEquipamentoId());
                }
            }
          }
          
          for(int i = 1; i<=QUANTIDADE; i++){
              gravarArq.write("[ ");
              for(int j = 0; j < QTD_MIN_DIA/STEP; j++){
                  cont++;
                  for(int k = 0; k < tempo.size(); k++){
                      if((tempo.get(k) == j) && (potencia.get(k) == i)){
                          avaliador = 1;
                      }
                  }
                  if(avaliador == 1){
                      gravarArq.write("1 ");
                      avaliador = 0;
                  }else{
                      gravarArq.write("0 ");
                  }
                  if(cont == ((QTD_MIN_DIA/STEP)/3) && ((j+1)/cont)!=3){
                    gravarArq.newLine();
                    gravarArq.write("  ");
                    cont = 0;
                  }
              }
              cont = 0;
              gravarArq.write("] ");
              gravarArq.newLine();
          }
          gravarArq.write("];");
          gravarArq.newLine();
          gravarArq.close();
      }else{
          System.out.println("Arquivo não encontrado!");
      }
      
  }
}