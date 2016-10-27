/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtu.project.sma.sourceenergy;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;
import java.util.StringTokenizer;
import mtu.project.db.model.Load;

/**
 *
 * @author Rafael
 */
public class AgentSourceEnergy extends Agent{
    
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
            
        addBehaviour(new RecebeRequestLoad ());
    }
    
    public ACLMessage gerarMensagem(Load load, String operacao, String alteracao){
        
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        
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
            
        msg.setContent(geraDados(load, operacao, alteracao));
                        
        return msg;
    }
    
    public String geraDados(Load load, String op, String alt){
        String operacao = op; //C = Cadastro e A = Alteração
        String alteracao = alt; //S = Sim e N = Não
        String dados = operacao+" "+load.getEquipamentoId().toString()+" "+load.getPotencia()+" "+load.getTempo()+" "+load.getFonteEnergia()+" "+alteracao;
        
        return dados;
    }
    
    public int verificaGeracaoEnergia(int fonte){
        //Retornar 0 se fonte estiver desligada, ou o numero da fonte se estiver ativa.
        return fonte;
    }
     public String verificarCapacidadeAtual(int fonte){
        //comparaGeracaoAtual();
        //atualizaBD();
        return "N";
    }
    
        public class RecebeRequestLoad extends CyclicBehaviour {//este é um comportamento ciclico
        @Override
            public void action() {
                
                MessageTemplate protocolo = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                MessageTemplate performativa = MessageTemplate.MatchPerformative(ACLMessage.REQUEST) ;
                MessageTemplate mt = MessageTemplate.and(protocolo, performativa);
                Load load = new Load();
                ACLMessage msg = myAgent.receive(mt);
                
                if (msg != null) {
                    /*A classe StringTokemizer permite que você separe ou encontre palavras (tokens) em qualquer formato. */
                    StringTokenizer st = new StringTokenizer(msg.getContent());
                    String conteudo = st.nextToken(); //pego primeiro token.

                    if(conteudo.equalsIgnoreCase("registro")){ // se for para registrar no banco de dados
                        
                        Long equipamentoId = Long.parseLong(msg.getSender().getLocalName());
                        Double potencia = Double.parseDouble(st.nextToken()); //pego o segundo token
                        int tempo = Integer.parseInt(st.nextToken()); //pego o terceiro token
                        int fonte = verificaGeracaoEnergia(Integer.valueOf(myAgent.getLocalName()));

                        load.setEquipamentoId(equipamentoId);
                        load.setFonteEnergia(fonte);
                        load.setPotencia(potencia);
                        load.setTempo(tempo);
                        load.setSchedule(null);
                        
                        myAgent.send(gerarMensagem(load,"C","N"));
                        
                    }else{
                        if(conteudo.equalsIgnoreCase("iniciar")){
                            String alteracao = verificarCapacidadeAtual(Integer.valueOf(myAgent.getLocalName()));
                            myAgent.send(gerarMensagem(load,"A",alteracao));
                            
                        }
                    }
                }else{
                    block();
                }
            }
        }
}
/*
distanciaMaxima = (Math.random()*10);
System.out.println("Central " + getLocalName ( ) + ": Aguardando alarmes..." ) ;
*/