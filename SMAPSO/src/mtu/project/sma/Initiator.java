/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.sma;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 *
 * @author Rafael
 */
public class Initiator extends AchieveREInitiator{


// envia a mensagem request para os receptores (Agentes Load) que foram especificados no objeto msg.
    public Initiator(Agent a, ACLMessage msg){
        super(a, msg); // parâmetros = agente que esta enviando e mensagem a ser enviada.
    }

//Os metodos a seguir tratam a resposta do Agente Load.

    //Se o Agente Load está ativado e funcionando corretamente, istoé, envia uma mensagem AGREE.
    @Override
    protected void handleAgree (ACLMessage agree){
        System.out.println("Central de bombeiros " + agree.getSender().getName() +
                           " informa que saiu para apagar o fogo.");
    }

    //Se o participante se negar, enviando uma mensagem REFUSE.
    @Override
    protected void handleRefuse (ACLMessage refuse){
        System.out.println("Central de bombeiros " + refuse.getSender().getName() +
                           " responde que o fogo está muito longe e não poderá apagá-lo.");
    }

    //Se o participante não entendeu, enviando uma mensagem NOT UNDERSTOOD.
    @Override
    protected void handleNotUnderstood (ACLMessage notUnderstood){
        System.out.println("Central de bombeiros " +  notUnderstood.getSender().getName() +
                       " por algum motivo não entendeu a solicitação.") ;
    }

    //Se houve uma falha na execução do pedido.
    @Override
    protected void handleFailure (ACLMessage failure){
        //Verifica inicialmente se foi um erro nas páginas brancas.
        if(failure.getSender().equals(super.myAgent.getAMS())){
            System.out.println("Alguma Central de Bombeiros não existe!");
        }
        /* O conteúdo de uma mensagem envolvida neste protocolo é automaticamente colocado entre 
           parenteses. Com o metodo substring() podemos ler apenas o que esta dentro deles.*/
        else{
            System.out.println("Falha na central de bombeiros " + failure.getSender().getName() +
            ": " + failure.getContent().substring(0, failure.getContent().length()) ) ;
        }
    }

    //Ao finalizar o protocolo, o participante envia uma mensagem inform.
    @Override
    protected void handleInform (ACLMessage inform ){
        System.out.println("Central de bombeiros " + inform.getSender().getName ( ) + 
                           " informa que apagou o fogo." ) ;
    }
}
