/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.sma.central;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
// para implementar o protocolo request importamos a seguinte classe:
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import java.util.StringTokenizer;
import mtu.project.db.dao.ScheduleDAO;
import mtu.project.db.model.Load;

public class AgentCentral extends Agent {

    private int CONTADOR;
    
    @Override
    protected void setup( ){
        
        //Registrando o Agente Central no DF (Páginas Amarelas)
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName());
        sd.setType("Agente Central");
        
        dfd.addServices(sd);
        
        try{
            DFService.register(this, dfd);
        }catch(FIPAException e){
            e.printStackTrace();
        }
        
        Object[ ] args = getArguments( );
        if ( args != null && args.length > 0){
            DFAgentDescription pesquisarAgentesLoad = new DFAgentDescription();
            ServiceDescription sdAgentesLoad = new ServiceDescription();
            sdAgentesLoad.setType("Agente Load");
            pesquisarAgentesLoad.addServices(sdAgentesLoad);
            
            //montando a mensagem inicial a ser enviada aos Agentes Loads
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            try{
                //Pesquisando pelos Agentes Load ativos.
                DFAgentDescription[] agentesLoad = DFService.search(this, pesquisarAgentesLoad);
                for(int i = 0; i<agentesLoad.length; i++){
                    msg.addReceiver(agentesLoad[i].getName());
                }
            }catch(FIPAException e){
                e.printStackTrace();
            }
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            msg.setContent((String) args[0]);
            
            SequentialBehaviour comportamento = new SequentialBehaviour ( this ){
                @Override
                public int onEnd(){
                  doDelete();
                  return 0 ;
                }
            };
            /*A classe IniciarAgentsLoad (abaixo) extende a classe AchieveREInitiator, 
            ela atua como o iniciador do protoloco. Seu metodo construtor envia automaticamente 
            a mensagem que esta no objeto msg*/
            comportamento.addSubBehaviour(new WakerBehaviour(this, 3000){
                @Override
                protected void onWake ( ){
                    System.out.println("Agentes Load serão registrados!");
                }
            }); 
            comportamento.addSubBehaviour(new IniciarAgentsLoad(this, msg));
            comportamento.addSubBehaviour(new RecebeRequest());
            addBehaviour(comportamento);
            
            
        }else{
            System.out.println("Especifique o procedimento inicial a ser realizado." ) ;
        }
    }
    
    public void inserirBancoDados(Load load){
       // LoadDAO.getInstance().save(load);
        System.out.println("Carga inserida no Banco de Dados." );
    }
    
    public void zerarSchedule(Load load){
        Integer id = Integer.valueOf(load.getEquipamentoId().toString());
        ScheduleDAO.getInstance().removeAll(id);
        System.out.println("Schedule atualizado no Banco de Dados." );
    }
    
    
    public class IniciarAgentsLoad extends AchieveREInitiator{
        
        // envia a mensagem request para os receptores (Agentes Load) que foram especificados no objeto msg.
        public IniciarAgentsLoad(Agent a, ACLMessage msg){
            super(a, msg); // parâmetros = agente que esta enviando e mensagem a ser enviada.
        }

        //Os metodos a seguir tratam a resposta do Agente Load.

        //Se o Agente Load está ativado e funcionando corretamente, istoé, envia uma mensagem AGREE.
        @Override
        protected void handleAgree (ACLMessage agree){
            System.out.println("Dados do Agente Load " + agree.getSender().getName() +
                               " enviados para registro.");
        }

        //Se o participante se negar, enviando uma mensagem REFUSE.
        @Override
        protected void handleRefuse (ACLMessage refuse){
            System.out.println("Dados referente a carga do Agente Load " + refuse.getSender().getName() +
                               " estão inconsistentes.");
        }

        //Se o participante não entendeu, enviando uma mensagem NOT UNDERSTOOD.
        @Override
        protected void handleNotUnderstood (ACLMessage notUnderstood){
            System.out.println("Agente Load " +  notUnderstood.getSender().getName() +
                           " por algum motivo não entendeu a solicitação.") ;
        }

        //Se houve uma falha na execução do pedido.
        @Override
        protected void handleFailure (ACLMessage failure){
            //Verifica inicialmente se foi um erro nas páginas brancas.
            if(failure.getSender().equals(super.myAgent.getAMS())){
                System.out.println("Agente Load não existe!");
            }
            /* O conteúdo de uma mensagem envolvida neste protocolo é automaticamente colocado entre 
               parenteses. Com o metodo substring() podemos ler apenas o que esta dentro deles.*/
            else{
                System.out.println("Falha no Agente Load " + failure.getSender().getName() +
                ": " + failure.getContent().substring(0, failure.getContent().length()) ) ;
            }
        }

        //Ao finalizar o protocolo, o participante envia uma mensagem inform.
        @Override
        protected void handleInform (ACLMessage inform ){
            System.out.println("Agente Load " + inform.getSender().getName() + 
                               " informa que o registro está sendo processado!" ) ;
        }
    }
    
    private class RecebeRequest extends CyclicBehaviour {//este é um comportamento ciclico
        @Override
        public void action() {
            MessageTemplate protocolo = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            MessageTemplate performativa = MessageTemplate.MatchPerformative(ACLMessage.REQUEST) ;
            MessageTemplate mt = MessageTemplate.and(protocolo, performativa);
            
            ACLMessage msg = myAgent.receive(mt);
            
            if (msg != null) {
                // processar a mensagem recebida
                int tipo = 0;
                ACLMessage resposta = msg.createReply();
                //System.out.println(msg.getContent());
                try{
                    //Verificando se é um Agente Load (tipo = 1).
                    DFAgentDescription pesquisarAgentesLoad = new DFAgentDescription();
                    ServiceDescription sdAgentesLoad = new ServiceDescription();
                    sdAgentesLoad.setType("Agente Load");
                    pesquisarAgentesLoad.addServices(sdAgentesLoad);
                    DFAgentDescription[] agentesLoad = DFService.search(myAgent, pesquisarAgentesLoad);
                    
                    for(int i = 0; i<agentesLoad.length; i++){
                        if(agentesLoad[i].getName().getName().equals(msg.getSender().getName())){
                           tipo = 1;                            
                        }
                    }
                    //Verificando se é um Agente SE (tipo = 2).
                    DFAgentDescription pesquisarAgentesSE = new DFAgentDescription();
                    ServiceDescription sdAgentesSE = new ServiceDescription();
                    sdAgentesSE.setType("Agente SE");
                    pesquisarAgentesSE.addServices(sdAgentesSE);
                    DFAgentDescription[] agentesSE = DFService.search(myAgent, pesquisarAgentesSE);
                    
                    for(int i = 0; i<agentesSE.length; i++){
                        if(agentesSE[i].getName().getName().equals(msg.getSender().getName())){
                            tipo = 2;                            
                        }
                    }
                }catch(FIPAException e){
                    e.printStackTrace();
                }
                
                if(tipo == 1){//Agente Load
                    StringTokenizer st = new StringTokenizer(msg.getContent());
                    String situacao = st.nextToken();
                    String equipamentoId = st.nextToken(); 
                    String potencia = st.nextToken(); 
                    String tempo = st.nextToken(); 
                    String fonteEnergia = st.nextToken(); 
                    
                    Load carga = new Load();
                    carga.setEquipamentoId(Long.valueOf(equipamentoId));
                    carga.setPotencia(Double.valueOf(potencia));
                    carga.setTempo(Integer.valueOf(tempo));
                    carga.setFonteEnergia(Integer.valueOf(fonteEnergia));
                    carga.setSchedule(null);
                    
                    if(situacao.equals("R")){// Registrar carga
                        inserirBancoDados(carga);
                    }else{
                        if(situacao.equals("F")){ // Carga com falha
                            zerarSchedule(carga);
                        }
                    }
                }else{
                    if(tipo == 2){                                       
                        StringTokenizer st = new StringTokenizer(msg.getContent());
                        String operacao = st.nextToken();
                        String equipamentoId = st.nextToken(); 
                        String potencia = st.nextToken(); 
                        String tempo = st.nextToken(); 
                        String fonteEnergia = st.nextToken(); 
                        String alteracao = st.nextToken(); 
                        
                        Load carga = new Load();
                        carga.setEquipamentoId(Long.valueOf(equipamentoId));
                        carga.setPotencia(Double.valueOf(potencia));
                        carga.setTempo(Integer.valueOf(tempo));
                        carga.setFonteEnergia(Integer.valueOf(fonteEnergia));
                        carga.setSchedule(null);

                        if(!operacao.equals("C") && !operacao.equals("A")){ //Realizar cadastro de carga ou alteração
                            System.out.println("Agente Central não entendeu solicitação do Agente SE "+msg.getSender().getName());
                        }else{   
                            System.out.println("Agente Central concorda em realizar a solicitação do Agente SE "+msg.getSender().getName());
                            if(operacao.equals("C")){ // Realizar cadastro de carga
                                try{
                                    if(equipamentoId.equals("") || potencia.equals("") || tempo.equals("")){
                                        System.out.println("Dados para registro da Carga "+equipamentoId+" incompletos! Solicitação está sendo recusada!");
                                    }else{
                                        inserirBancoDados(carga);
                                    }
                                }catch(Exception e){
                                    System.out.println("Ocorreu uma falha com o Banco de Dados no cadastro da Carga "+equipamentoId);
                                }
                            }else{
                                if(alteracao.equals("S")){ // Se alteraçao S(sim), realizar alteração.
                                    try{
                                        CONTADOR++;
                                        if(CONTADOR < 3){
                                            System.out.println("Dados para alteração de alocação da Carga "+equipamentoId+" registrados! Mas ainda não será processada!");
                                        }else{
                                            CONTADOR = 0;
                                            //executar algoritmo
                                            System.out.println("Dados para alteração de alocação da Carga "+equipamentoId+" registrados! Será processada agora!");
                                        }
                                    }catch(Exception e){
                                        resposta.setContent("Ocorreu uma na execução do algoritmo!");
                                    }
                                }                       
                            }
                        }
                    }else{
                        block();
                    }
                }
            }
        }//fim do action
    }//fim da classe
}