/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.sma.load;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import mtu.project.db.model.Load;

/**
 *
 * @author Rafael
 */
public class AgentLoad extends Agent{
   
int fonteEnergia;
Load load = new Load();

@Override
    protected void setup( ){
        
         //Registrando o Agente Central no DF (Páginas Amarelas)
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName());
        sd.setType("Agente Load");
        
        dfd.addServices(sd);
        
        try{
            Object[ ] args = getArguments();
            if( args != null && args.length > 0){
                load.setEquipamentoId(Long.valueOf(getName()));                
                load.setPotencia((Double)args[0]);
                load.setTempo(((int)args[1]));
                load.setSchedule(null);  
                fonteEnergia = (int)args[2];
            }
            DFService.register(this, dfd);
        }catch(FIPAException e){
            e.printStackTrace();
        }
        
        //Meu agente conversa sob o protocolo FIPA REQUEST.
        MessageTemplate protocolo = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        MessageTemplate performativa = MessageTemplate.MatchPerformative(ACLMessage.REQUEST) ;
        MessageTemplate padrao = MessageTemplate.and(protocolo, performativa);
        
        addBehaviour(new CapturaRequestCentral (this, padrao));
        addBehaviour(new ScheduleAgentLoad (this, 60000));
        
        
    }
    
    public boolean verificaTemposAlocados(){
            return true;
    }
    
    public boolean verificaEstadoAtual(){
            return true;
    }
    
    public boolean verificaAcionamentoProxCiclo(){
            return true;
    }
    
    public int consultaIdFonteEnergia(){
            return 1;
    }
    
    
    
    public class CapturaRequestCentral extends AchieveREResponder{

            public CapturaRequestCentral(Agent a, MessageTemplate mt){
             //Define agente e protocolo de comunicação.
                super(a, mt);
            }

            /* Método que aguarda uma mensagem REQUEST, definida com o uso do objeto mt, utilizado no construtor
            desta classe. O retorno deste método é uma mensagem que é enviada automaticamente para o iniciador. */

            @Override
            protected ACLMessage prepareResponse (ACLMessage request) throws NotUnderstoodException, RefuseException {

                /*A classe StringTokemizer permite que você separe ou encontre palavras (tokens) em qualquer formato. */
                StringTokenizer st = new StringTokenizer(request.getContent());
                String requisicao = st.nextToken(); //pego primeiro token.

                if(requisicao.equalsIgnoreCase("iniciar")){ // se for para registrar no banco de dados

                    if(load.getTempo() > 0 && load.getPotencia() > 0){
                        ACLMessage agree = request.createReply();
                        agree.setPerformative(ACLMessage.AGREE);

                        return agree; //envia mensagem AGREE.
                    }else{
                        //Envia Mensagem Refuse.
                        throw new RefuseException ("Não foi possível realizar sua solicitação!(Dados da carga incorretos)");
                    }
                    // envia mensagem NOT UNDERSTOOD
                }else{
                    throw new NotUnderstoodException ( "O Agente Load não entendeu sua solicitação." );
                }
            }

            //Prepara resultado final, caso tenha aceitado.
            @Override
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{

                ACLMessage inform = request.createReply();
                inform.setPerformative(ACLMessage.INFORM);

                return inform; // envia mensagem INFORM.

            }
        }
    
    public class ScheduleAgentLoad extends TickerBehaviour{

             
        public ScheduleAgentLoad(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            String situacao;
           // msg.setContent((String) args[0]);
           // msg.addReceiver(agentesLoad[i].getName());
            
            if(verificaTemposAlocados()){
                if(verificaEstadoAtual()){
                    if(verificaAcionamentoProxCiclo()){
                       msg.addReceiver(super.myAgent.getAID(Integer.toString(fonteEnergia)));
                       msg.setContent("iniciar");
                       myAgent.send(msg);
                       
                    }
                }else{
                    
                }
            }else{
                if(fonteEnergia != 0){
                    msg.addReceiver(super.myAgent.getAID(Integer.toString(fonteEnergia)));
                    msg.setContent("registro "+load.getPotencia()+" "+load.getTempo());
                    myAgent.send(msg);
                    fonteEnergia = 0;
                }else{
                    //Adicionando Agente Central como destinatário.
                    DFAgentDescription pesquisarAgenteCentral = new DFAgentDescription();
                    ServiceDescription sdAgenteCentral = new ServiceDescription();
                    sdAgenteCentral.setType("Agente Central");
                    pesquisarAgenteCentral.addServices(sdAgenteCentral);
                    DFAgentDescription[] agenteCentral;
                    try {
                        agenteCentral = DFService.search(myAgent, pesquisarAgenteCentral);
                        for(int i = 0; i<agenteCentral.length; i++){
                            msg.addReceiver(agenteCentral[i].getName());
                        }
                    } catch (FIPAException ex) {
                        Logger.getLogger(AgentLoad.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    situacao = "R";
                    msg.setContent(situacao+" "+load.getEquipamentoId()+" "+load.getPotencia()+" "+load.getTempo());
                    myAgent.send(msg);
                }
            }
        }
        
    }
}

//adicionar variavel fonte no banco de dados
/*
distanciaMaxima = (Math.random()*10);
System.out.println("Central " + getLocalName ( ) + ": Aguardando alarmes..." ) ;
*/