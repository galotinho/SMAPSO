/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.sma.sourceenergy;

import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
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
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import java.util.StringTokenizer;
import mtu.project.db.model.Load;

/**
 *
 * @author Rafael
 */
public class AgentSourceEnergy extends Agent{
   
Load load = new Load();
Boolean resposta = false;

@Override
    protected void setup( ){
        
         //Registrando o Agente Source Energy no DF (Páginas Amarelas)
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName());
        sd.setType("Agente SE");
        
        dfd.addServices(sd);
        
        try{
            DFService.register(this, dfd);
        }catch(FIPAException e){
            e.printStackTrace();
        }
        
        //Meu agente conversa sob o protocolo FIPA REQUEST.
        MessageTemplate protocolo = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        MessageTemplate performativa = MessageTemplate.MatchPerformative(ACLMessage.REQUEST) ;
        MessageTemplate padrao = MessageTemplate.and(protocolo, performativa);
        
        FSMBehaviour comportamento = new FSMBehaviour();
        comportamento.registerState(new CapturaRequestLoad (this, padrao), "Step1");
        comportamento.registerState(new EnviaRequestCentral (this, geraMensagem()), "Step2");

        comportamento.registerDefaultTransition("Step1", "Step2");
        comportamento.registerDefaultTransition("Step2", "Step1");

        addBehaviour(comportamento);
    }
    
    public ACLMessage geraMensagem(){
        
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        DFAgentDescription pesquisarAgenteCentral = new DFAgentDescription();
        ServiceDescription sdAgentesCentral = new ServiceDescription();
        sdAgentesCentral.setType("Agente Central");
        pesquisarAgenteCentral.addServices(sdAgentesCentral);
        
            try{
                //Pesquisando pelo Agente Coordenador.
                DFAgentDescription[] agenteCentral = DFService.search(this, pesquisarAgenteCentral);
                for(int i = 0; i<agenteCentral.length; i++){
                    msg.addReceiver(agenteCentral[i].getName());
                }
            }catch(FIPAException e){
                e.printStackTrace();
            }
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            msg.setContent(geraDados());
            
            return msg;
    }
    
    public String geraDados(){
        String operacao = "C"; //C = Cadastro e A = Alteração
        String alteracao = "N"; //S = Sim e N = Não
        return operacao+" "+load.getEquipamentoId().toString()+" "+load.getPotencia()+" "+load.getTempo()+" "+load.getFonteEnergia()+" "+alteracao;
    }
    
    public int verificaGeracaoEnergia(int fonte){
        //Retornar 0 se fonte estiver desligada, ou o numero da fonte se estiver ativa.
        return fonte;
    }
     public void verificarCapacidadeAtual(int fonte){
        //comparaGeracaoAtual();
        //atualizaBD();
        
    }
    
        public class CapturaRequestLoad extends AchieveREResponder{

            public CapturaRequestLoad(Agent a, MessageTemplate mt){
             //Define agente e protocolo de comunicação.
                super(a, mt);
            }

            /* Método que aguarda uma mensagem REQUEST, definida com o uso do objeto mt, utilizado no construtor
            desta classe. O retorno deste método é uma mensagem que é enviada automaticamente para o iniciador. */

            @Override
            protected ACLMessage prepareResponse (ACLMessage request) throws NotUnderstoodException, RefuseException {
                
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                /*A classe StringTokemizer permite que você separe ou encontre palavras (tokens) em qualquer formato. */
                StringTokenizer st = new StringTokenizer(request.getContent());
                String conteudo = st.nextToken(); //pego primeiro token.

                if(conteudo.equalsIgnoreCase("registro")){ // se for para registrar no banco de dados
                    //pego o segundo token
                    Double potencia = Double.parseDouble(st.nextToken()); 
                    //pego o terceiro token
                    int tempo = Integer.parseInt(st.nextToken()); 
                    Long equipamentoId = Long.parseLong(request.getSender().getName());
                    
                    load.setEquipamentoId(equipamentoId);
                    int fonte = verificaGeracaoEnergia(Integer.valueOf(myAgent.getName()));
                    load.setFonteEnergia(fonte);
                    load.setPotencia(potencia);
                    load.setTempo(tempo);
                    load.setSchedule(null);

                    if(tempo > 0 && potencia > 0 && equipamentoId != 0){
                        return agree; //envia mensagem AGREE.
                    }else{
                        //Envia Mensagem Refuse.
                        throw new RefuseException ("Não foi possível realizar sua solicitação!");
                    }
                    // envia mensagem NOT UNDERSTOOD
                }else{
                    if(conteudo.equalsIgnoreCase("iniciar")){
                        verificarCapacidadeAtual(Integer.valueOf(myAgent.getName()));
                        return agree; //envia mensagem AGREE.
                    }else{
                        throw new NotUnderstoodException ( "O Agente Source Energy não entendeu sua solicitação." );
                
                    }
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
    
        public class EnviaRequestCentral extends AchieveREInitiator{

        // envia a mensagem request para o receptor (Agente Central).
        public EnviaRequestCentral(Agent a, ACLMessage msg){
            super(a, msg); // parâmetros = agente que esta enviando e mensagem a ser enviada.
        }

        //Os metodos a seguir tratam a resposta do Agente Central.

        //Se o Agente Load está ativado e funcionando corretamente, istoé, envia uma mensagem AGREE.
        @Override
        protected void handleAgree (ACLMessage agree){
            System.out.println("Agente Central " + agree.getSender().getName() +
                               " informa que recebeu a solicitação e irá analisar.");
        }

        //Se o participante se negar, enviando uma mensagem REFUSE.
        @Override
        protected void handleRefuse (ACLMessage refuse){
            System.out.println("Agente Central " + refuse.getSender().getName() +
                               " responde que os novos dados recebidos estão incompletos e não cadastrará.");
        }

        //Se o participante não entendeu, enviando uma mensagem NOT UNDERSTOOD.
        @Override
        protected void handleNotUnderstood (ACLMessage notUnderstood){
            System.out.println("Agente Central " +  notUnderstood.getSender().getName() +
                           " informa que a solicitação realizada é inválida.") ;
        }

        //Se houve uma falha na execução do pedido.
        @Override
        protected void handleFailure (ACLMessage failure){
            //Verifica inicialmente se foi um erro nas páginas brancas.
            if(failure.getSender().equals(super.myAgent.getAMS())){
                System.out.println("Agente Central não existe!");
            }
            /* O conteúdo de uma mensagem envolvida neste protocolo é automaticamente colocado entre 
               parenteses. Com o metodo substring() podemos ler apenas o que esta dentro deles.*/
            else{
                System.out.println("Falha no Agente Central " + failure.getSender().getName() +
                ": " + failure.getContent().substring(0, failure.getContent().length()) ) ;
            }
        }

        //Ao finalizar o protocolo, o participante envia uma mensagem inform.
        @Override
        protected void handleInform (ACLMessage inform ){
            System.out.println("Agente Central " + inform.getSender().getName ( ) + 
                               " informa que processou as informações corretamente." ) ;
        }
    }
}
/*
distanciaMaxima = (Math.random()*10);
System.out.println("Central " + getLocalName ( ) + ": Aguardando alarmes..." ) ;
*/