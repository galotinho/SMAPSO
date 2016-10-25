/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.sma;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import java.util.StringTokenizer;
/**
 *
 * @author Rafael
 */
public class Participant extends AchieveREResponder{
    
    double DISTANCIA_MAX;
    
    public Participant(Agent a, MessageTemplate mt, double distanciaMaxima){
     //Define agente e protocolo de comunicação.
        super(a, mt);
        this.DISTANCIA_MAX = distanciaMaxima;
    }
    
/* Método que aguarda uma mensagem REQUEST, definida com o uso do objeto mt, utilizado no construtor
desta classe. O retorno deste método é uma mensagem que é enviada automaticamente para o iniciador. */
   
    @Override
    protected ACLMessage prepareResponse (ACLMessage request) throws NotUnderstoodException, 
                                                                     RefuseException {

        System.out.println( "Central " + super.myAgent.getLocalName ( ) + ": Recebemos uma chamada de " + 
                request.getSender().getName() + " dizendo que observou um incêndio.");

        /*A classe StringTokemizer permite que você separe ou encontre palavras (tokens) em qualquer formato. */
        StringTokenizer st = new StringTokenizer(request.getContent());
        String conteudo = st.nextToken(); //pego primeiro token.
        
        if(conteudo.equalsIgnoreCase("fogo")){ // se for fogo.
            //st.nextToken(); //pulo o segundo token.
            int distancia = Integer.parseInt(st.nextToken()); //capturo a distância.
            
            if(distancia < DISTANCIA_MAX){
                System.out.println("Central " + super.myAgent.getLocalName ( ) + ": Saimos correndo!" );
                ACLMessage agree = request.createReply();
                
                agree.setPerformative(ACLMessage.AGREE);
            
                return agree; //envia mensagem AGREE.
            }else{
                //Fogo está longe. Envia Mensagem Refuse com o motivo.
                System.out.println("Central " + super.myAgent.getLocalName()+": Fogo está longe demais. Não podemos "
                                    + "atender a solicitação." );
                throw new RefuseException ("Fogo está muito longe.");
            }
            // envia mensagem NOT UNDERSTOOD
        }else{
            throw new NotUnderstoodException ( "Central de Bombeiros não entendeu sua mensagem." );
        }
    }
    
    //Prepara resultado final, caso tenha aceitado.
    @Override
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{
        if(Math.random() > 0.2){
            System.out.println("Central " + super.myAgent.getLocalName() + ": Voltamos após apagar o fogo.");
            ACLMessage inform = request.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            return inform; // envia mensagem INFORM.
        }else{
            System.out.println("Central " + super.myAgent.getLocalName ( ) + ": Ficamos sem água.");
            throw new FailureException("Ficamos sem água." );
        }
    }
}