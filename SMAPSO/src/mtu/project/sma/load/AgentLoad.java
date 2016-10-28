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
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import mtu.project.db.dao.LoadDAO;
import mtu.project.db.model.Load;
import mtu.project.db.model.Schedule;
import mtu.project.xbee.ConexaoXBee;

/**
 *
 * @author Rafael
 */
public class AgentLoad extends Agent{
   

@Override
    protected void setup( ){
        Load load = new Load();
         //Registrando o Agente Central no DF (Páginas Amarelas)
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName());
        sd.setType("Agente Load");
        
        dfd.addServices(sd);
        
        try{
            //Argumentos: Potencia, tempo, id da Fonte de Energia
            Object[ ] args = getArguments();
            if( args != null && args.length > 0){
                load.setEquipamentoId(Long.valueOf(getLocalName()));                
                load.setPotencia(Double.valueOf((String)args[0]));
                load.setTempo(Integer.valueOf((String)args[1]));
                load.setSchedule(null);  
                load.setFonteEnergia(Integer.valueOf((String)args[2]));
            }
            DFService.register(this, dfd);
        }catch(FIPAException e){
            e.printStackTrace();
        }
        
        //Meu agente conversa sob o protocolo FIPA REQUEST.
        MessageTemplate protocolo = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        MessageTemplate performativa = MessageTemplate.MatchPerformative(ACLMessage.REQUEST) ;
        MessageTemplate padrao = MessageTemplate.and(protocolo, performativa);
        
        addBehaviour(new CapturaRequestCentral (this, padrao, load));
        addBehaviour(new ScheduleAgentLoad (this, 5000, load));
    }
    
    public int verificaStatusDaCarga(){
        
        String resposta = ConexaoXBee.getInstance("COM3", 9600).enviarRequisicao("END_DEVICE4", "liga");
        if(resposta.equals("Ligado!")){
            return 1;
        }
        return 0;
    }
    
    public boolean verificaTemposAlocados(Load load){
            Load carga = LoadDAO.getInstance().findByEquipamentoId(load.getEquipamentoId());
            
            if(carga != null){
                if(!carga.getSchedule().isEmpty()){
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }
    }
    
    public boolean verificaEstadoAtual(Load load){
        Date horaAtual = new Date();
        Locale locale = new Locale("pt","BR");
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH mm", locale);
        String currentTime = sdf.format(horaAtual);
        
        StringTokenizer st = new StringTokenizer(currentTime);
        String hora = st.nextToken();
        String minuto = st.nextToken();
        int tempo = conversorTempo(hora, minuto);
        int acionamento = 0;
        
        Load carga = LoadDAO.getInstance().findByEquipamentoId(load.getEquipamentoId());
                
        for(Schedule s: carga.getSchedule()) {
           
            if(s.getTempo() == tempo){
                acionamento = 1;
            }
        }
        int status = verificaStatusDaCarga(); // Status 1:Ligado 0:Desligado      
        
        if(status == acionamento){
            return true;
        }
        
        return false;
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
    
    public boolean verificaAcionamentoProxCiclo(Load load){
        
        Date horaAtual = new Date();
        Locale locale = new Locale("pt","BR");
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH mm", locale);
        String currentTime = sdf.format(horaAtual);
        
        StringTokenizer st = new StringTokenizer(currentTime);
        String hora = st.nextToken();
        String minuto = st.nextToken();
        int tempo = conversorTempo(hora, minuto);
        
        if(tempo == 96){
            tempo = 0;
        }else{
            tempo++;
        }
        
        int acionamento = 0;
        
        Load carga = LoadDAO.getInstance().findByEquipamentoId(load.getEquipamentoId());
                
        for(Schedule s: carga.getSchedule()) {
           
            if(s.getTempo() == tempo){
                acionamento = 1;
            }
        }
        
        int status = verificaStatusDaCarga();
        
        if(acionamento == 1 && status != 1){
            return true;
        }
        
        return false;
        
    }
    
    public class CapturaRequestCentral extends AchieveREResponder{
            
        Load load;
        
        public CapturaRequestCentral(Agent a, MessageTemplate mt, Load load){
             //Define agente e protocolo de comunicação.
                super(a, mt);
                this.load = load;
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
        
        Load load;
        
        public ScheduleAgentLoad(Agent a, long period, Load load) {
            super(a, period);
            this.load = load;
        }

        @Override
        protected void onTick() {
            
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            String situacao;
            
            if(verificaTemposAlocados(load)){
                if(verificaEstadoAtual(load)){
                    if(verificaAcionamentoProxCiclo(load)){
                       msg.addReceiver(super.myAgent.getAID(Integer.toString(load.getFonteEnergia())));
                       msg.setContent("iniciar");
                       myAgent.send(msg);
                    }
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
                    //Carga com Falha
                    situacao = "F";
                    msg.setContent(situacao+" "+load.getEquipamentoId()+" "+load.getPotencia()+" "+load.getTempo()+" "+load.getFonteEnergia());
                    myAgent.send(msg);
                }
            }else{
                if(load.getFonteEnergia() != 0){
                    System.out.println(verificaStatusDaCarga());
                    msg.addReceiver(super.myAgent.getAID(Integer.toString(load.getFonteEnergia())));
                    msg.setContent("registro "+load.getPotencia()+" "+load.getTempo());
                    myAgent.send(msg);
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
                            //System.out.println(agenteCentral[i].getName().getName());
                        }
                    } catch (FIPAException ex) {
                        Logger.getLogger(AgentLoad.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //Registrar Carga
                    situacao = "R";
                    msg.setContent(situacao+" "+load.getEquipamentoId()+" "+load.getPotencia()+" "+load.getTempo()+" 0");
                   // System.out.println(msg.getContent());
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