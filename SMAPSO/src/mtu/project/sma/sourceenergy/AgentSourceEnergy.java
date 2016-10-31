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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import mtu.project.db.dao.SourceEnergyDAO;
import mtu.project.db.model.Load;
import mtu.project.db.model.SourceEnergy;
import mtu.project.db.model.SourceSchedule;
import mtu.project.xbee.ConexaoXBee;

/**
 *
 * @author Rafael
 */
public class AgentSourceEnergy extends Agent{
    
@Override
    protected void setup( ){
        SourceEnergy source = new SourceEnergy();
         //Registrando o Agente Source Energy no DF (Páginas Amarelas)
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName());
        sd.setType("Agente SE");
        
        dfd.addServices(sd);
        
        try{
            Object[ ] args = getArguments();
            if( args != null && args.length > 0){
                source.setSourceId(Long.valueOf(getLocalName()));
                source.setNameSource((String)args[0]);
                source.setTypeSource(Integer.valueOf((String)args[1]));
                source.setSourceSchedule(null);
                
            }
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
    
    public Double verificaGeracaoEnergia(int fonte) throws InterruptedException, ExecutionException{
        Double resultado;
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        List<Callable<String>> lst = new ArrayList<>();
        lst.add(new ConexaoXBee("END_DEVICE4", "liga", "COM12", 9600));
        
        List<Future<String>> tasks = executorService.invokeAll(lst);
        if(tasks.get(0).get().equals("Ligado!")){
            resultado = 1.0;
        }else{
            resultado = 0.0;
        }
        executorService.shutdown();
        return resultado;
    }
    
     public String verificarCapacidadeAtual(int fonte, Double geracao){
        Date dataAtual = new Date();
        Locale locale = new Locale("pt","BR");
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH mm", locale);
        String currentTime = sdf.format(dataAtual);
        
        StringTokenizer st = new StringTokenizer(currentTime);
        String hora = st.nextToken();
        String minuto = st.nextToken();
        int tempo = conversorTempo(hora, minuto);
        Double geracaoPrevista = 0.0;
        
        SourceEnergy source = SourceEnergyDAO.getInstance().findBySourceId(Long.valueOf((long)fonte));
                
        for(SourceSchedule s: source.getSourceSchedule()) {
           
            if(s.getDataAtual().compareTo(dataAtual) == 0){
                if(s.getTempo() == tempo){
                    geracaoPrevista = s.getPotenciaPrevista();
                }
            }
        }
        if(geracao == -1){
            return "S";
        }else{
            if((geracao*1.2) < geracaoPrevista){
                return "S";
            }else{
                return "N";
            }
        }
        
    }
     
     public int conversorTempo(String hora, String minuto){
        
        int h = Integer.valueOf(hora)*4;
        int m = Integer.valueOf(minuto);
        
        if(m>=0 && m<15){
            m = 1;
        }else{
            if(m>=15 && m<30){
                m = 2;
            }else{
                if(m>=30 && m<45){
                    m = 3;
                }else{
                    m = 4;
                }
            }
        }
        return h+m;
    }
    
        public class RecebeRequestLoad extends CyclicBehaviour {//este é um comportamento ciclico
        @Override
            public void action() {
                
                MessageTemplate protocolo = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                MessageTemplate performativa = MessageTemplate.MatchPerformative(ACLMessage.REQUEST) ;
                MessageTemplate mt = MessageTemplate.and(protocolo, performativa);
                Load load = new Load();
                Double geracao = 0.0;
                ACLMessage msg = myAgent.receive(mt);
                
                if (msg != null) {
                    /*A classe StringTokemizer permite que você separe ou encontre palavras (tokens) em qualquer formato. */
                    StringTokenizer st = new StringTokenizer(msg.getContent());
                    String conteudo = st.nextToken(); //pego primeiro token.

                    if(conteudo.equalsIgnoreCase("registro")){ // se for para registrar no banco de dados
                        
                        Long equipamentoId = Long.parseLong(msg.getSender().getLocalName());
                        Double potencia = Double.parseDouble(st.nextToken()); //pego o segundo token
                        int tempo = Integer.parseInt(st.nextToken()); //pego o terceiro token
                        try {
                            geracao = verificaGeracaoEnergia(Integer.valueOf(myAgent.getLocalName()));
                        } catch (InterruptedException | ExecutionException ex) {
                            Logger.getLogger(AgentSourceEnergy.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        load.setEquipamentoId(equipamentoId);

                        if(geracao != -1){
                            load.setFonteEnergia(Integer.valueOf(myAgent.getLocalName()));
                        }else{
                            load.setFonteEnergia(0);
                        }
                        
                        load.setPotencia(potencia);
                        load.setTempo(tempo);
                        load.setSchedule(null);
                        
                        myAgent.send(gerarMensagem(load,"C","N"));
                        
                    }else{
                        if(conteudo.equalsIgnoreCase("iniciar")){
                            try {
                                geracao = verificaGeracaoEnergia(Integer.valueOf(myAgent.getLocalName()));
                            } catch (InterruptedException | ExecutionException ex) {
                                Logger.getLogger(AgentSourceEnergy.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            String alteracao = verificarCapacidadeAtual(Integer.valueOf(myAgent.getLocalName()), geracao);
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