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
import mtu.project.db.dao.LoadDAO;
import mtu.project.db.model.Load;
import mtu.project.db.model.Schedule;
import mtu.project.xbee.ConexaoXBee;

/**
 *
 * @author Rafael
 */
public class AgentLoad extends Agent{

int acionarDispositivo = 0;
int ativarComportamento = 0;
int reiniciarComportamento = 0;
String dispositivo;
String porta;
int rate;

@Override
    protected void setup( ){
        Load load = new Load();
        long sleep = 0;
         //Registrando o Agente Central no DF (Páginas Amarelas)
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName());
        sd.setType("Agente Load");
        
        dfd.addServices(sd);
        
        try{
            //Argumentos recebidos na inicialiazação do Agente: Potência, Tempo, ID da fonte de energia, Tempo em milissegundos para sincronização (sleep).
            Object[ ] args = getArguments();
            if( args != null && args.length > 0){
                load.setEquipamentoId(Long.valueOf(getLocalName()));                
                load.setPotencia(Double.valueOf((String)args[0])); //Argumento 1
                load.setTempo(Integer.valueOf((String)args[1])); //Argumento 2
                load.setSchedule(null);  
                load.setFonteEnergia(Integer.valueOf((String)args[2])); //Argumento 3
                sleep = Long.valueOf((String)args[3]); //Argumento 4
                dispositivo = (String)args[4];//Argumento 5
                porta = (String)args[5]; //Argumento 6
                rate = Integer.valueOf((String)args[6]); //Argumento 7
            }
            DFService.register(this, dfd);
        }catch(FIPAException e){
            e.printStackTrace();
        }
        
        //Meu Agente Load conversa sob o protocolo FIPA REQUEST com o Agente Central.
        MessageTemplate protocolo = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        MessageTemplate performativa = MessageTemplate.MatchPerformative(ACLMessage.REQUEST) ;
        MessageTemplate padrao = MessageTemplate.and(protocolo, performativa);
        
        //Comportamento para receber a mensagem inicial do Agente Central.
        addBehaviour(new CapturaRequestCentral (this, padrao, load));
        
        //Adiciona o comportamento responsável por realizar todo o ciclo de ações do Agente a cada 10 minutos.
        addBehaviour(new ScheduleAgentLoad (this, 300000, load, sleep));
    }
    
    // Método responsável por enviar a requisição ao dispositivo remoto e esperar sua resposta.
    public int verificaStatusDaCarga() throws InterruptedException, ExecutionException{
        
        int resultado; //variável que guarda o retorno enviado pelo dispositivo.
        
        String comando = "status";
        //Cria uma pool de threads e adiciona a Thread para fazer uso da porta serial.
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        List<Callable<String>> lst = new ArrayList<>();
        lst.add(new ConexaoXBee(dispositivo, comando, porta, rate));
        //Executa a Thread e espera o resultado de retorno.
        List<Future<String>> tasks = executorService.invokeAll(lst);
        if(tasks.get(0).get().equals("ligado")){ // Se resultado = "ligado" retorna 1, senão retorna 0.
            resultado = 1;
        }else{
            resultado = 0;
        }
        //Finaliza a pool de Threads.
        executorService.shutdown();
        
        return resultado;
    }
    
    public void acionarDesligarCarga() throws InterruptedException, ExecutionException{
        
        String comando;
        if(acionarDispositivo == 1){
            comando = "ligar";
        }else{
            comando = "desligar";
        }        
        
        //Cria uma pool de threads e adiciona a Thread para fazer uso da porta serial.
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        List<Callable<String>> lst = new ArrayList<>();
        lst.add(new ConexaoXBee(dispositivo, comando, porta, rate));
        //Executa a Thread e espera o resultado de retorno.
        executorService.invokeAll(lst);
        
        //Finaliza a pool de Threads.
        executorService.shutdown();
    }
    
    // Método responsável verificar se já tem Schedule associado à carga no qual o Agente controla.
    public boolean verificaTemposAlocados(Load load){
        //Traz do Banco de Dados todo os dados da carga.    
        Load carga = LoadDAO.getInstance().findByEquipamentoId(load.getEquipamentoId());
        //Verifica se a carga já existe no Banco de Dados.
        if(carga != null){
            return !carga.getSchedule().isEmpty(); //Verifica se já existe Schedule associado à carga.
        }else{
            return false;
        }
    }
    
    // Método responsável por verificar o estado atual da carga,ou seja, se está ligada ou desligada e se esse status condiz com o estado cadastrado no Banco de Dados.
    public boolean verificaEstadoAtual(Load load) throws InterruptedException, ExecutionException{
        //Verifica-se qual a hora atual e armazena na variável currentTime no formato "HH mm".
        Date horaAtual = new Date();
        Locale locale = new Locale("pt","BR");
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH mm", locale);
        String currentTime = sdf.format(horaAtual);
        //O dia é dividido em 96 instantes de 15 minutos cada. Então utiliza-se um método para verificar em qual instante de tempo do dia a hora atual corresponde.
        StringTokenizer st = new StringTokenizer(currentTime);
        String hora = st.nextToken();
        String minuto = st.nextToken();
        int tempo = conversorTempo(hora, minuto); //método que converte a hora atual em um dos 96 instantes de tempo.
        int acionamento = 0;
        //Busca-se os dados de schedule da carga.
        Load carga = LoadDAO.getInstance().findByEquipamentoId(load.getEquipamentoId());
        //Verifica-se se a carga deveria estar ligada ou desligada no instante de tempo atual.        
        for(Schedule s: carga.getSchedule()) {
            if(s.getTempo() == tempo){
                acionamento = 1; // Caso a carga esteja programada para estar ligada o valor da variável acionamento passa a ser 1.
            }
        }
        int status = verificaStatusDaCarga(); // Verifica status da carga no momento atual 1:Ligado 0:Desligado.      
        
        return status == acionamento;
    }
    
    // Método útil para converter a hora atual no seu instante de tempo correspondente.
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
    
    //Método responsável por verificar se a carga será acionada no próximo instante de tempo.
    public int verificaMudancaProxCiclo(Load load) throws InterruptedException, ExecutionException{
        //Verifica-se qual a hora atual e armazena na variável currentTime no formato "HH mm".
        Date horaAtual = new Date();
        Locale locale = new Locale("pt","BR");
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH mm", locale);
        String currentTime = sdf.format(horaAtual);
        //O dia é dividido em 96 instantes de 15 minutos cada. Então utiliza-se um método para verificar em qual instante de tempo do dia a hora atual corresponde.
        StringTokenizer st = new StringTokenizer(currentTime);
        String hora = st.nextToken();
        String minuto = st.nextToken();
        int tempo = conversorTempo(hora, minuto);//método que converte a hora atual em um dos 96 instantes de tempo.
        
        if(tempo == 96){//Verifica-se qual é o próximo instante de tempo e armazena na variável tempo.
            tempo = 0; //Dia finalizado.
        }else{
            tempo++;
        }
        
        if(tempo != 0){// Ou seja, dia ainda não foi finalizado.
            int acionamento = 0;
            //Busca-se os dados de schedule da carga.
            Load carga = LoadDAO.getInstance().findByEquipamentoId(load.getEquipamentoId());
            //Verifica-se se a carga deveria estar ligada ou desligada no instante de tempo atual.   
            for(Schedule s: carga.getSchedule()) {
                if(s.getTempo() == tempo){ // Caso a carga esteja programada para estar ligada o valor da variável acionamento passa a ser 1.
                    acionamento = 1;
                }
            }
            int status = verificaStatusDaCarga(); // Verifica status da carga no momento atual 1:Ligado 0:Desligado.  
            //Return: 1 para ligar a carga, 2 para desligar a carga, 0 para não fazer nada.
            if(status == 0 && acionamento == 1){ // Verifica-se se na próximo instante de tempo a carga deverá estar ligada.
                return 1;
            }else{
                if(status == 1 && acionamento == 0){ // Verifica-se se na próximo instante de tempo a carga deverá estar desligada.
                    return 2;
                }
            }
        }
        return 0;
    }
    
    // Método necessário para sincronizar o tempo em que cada Agente irá utilizar a porta com para comunicação com o dispositivo.
    public boolean sincronizar(Long tempo, long sleep){
        
        Long tempoAtual = new Date().getTime() - tempo;
                
        return tempoAtual < sleep;
    }
    
    // Comportamento para responder a mensagem de "iniciar" do Agente Central.
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
                //Autoriza a execução do Ticker Behaviour.
                ativarComportamento = 1;
                return inform; // envia mensagem INFORM.

            }
        }
    
    public class ScheduleAgentLoad extends TickerBehaviour{
        
        Load load;
        long sleep;
        
        public ScheduleAgentLoad(Agent a, long period, Load load, long sleep) {
            super(a, period);
            this.load = load;
            this.sleep = sleep;
            setFixedPeriod(true); // método que faz com que o Ticker Behavior execute o mesmo tempo em todos os Agentes.
        }

        @Override
        protected void onTick() {
            // Se a variável ativarAgente for 1, é porque o Agente Central envio mensagem para iniciar a execução.
            if(ativarComportamento == 1){
                if(reiniciarComportamento == 0){ //Ao receber a mensagem do Agente Central todos os Agentes Loads reiniciam suas execuções para começarem juntos e sincronizados.
                    reiniciarComportamento = 1;
                    restart();
                }                 
                Long tempo = new Date().getTime();
                while(sincronizar(tempo, sleep)){} // while que executa o método de verificação do tempo para realizar a sincronização.                                        
                            
                //Verifica-se se é hora de acionar a carga. Se acionar = 1 sim, se acionar = 0 não.
                if(acionarDispositivo != 0){
                    try {
                        acionarDesligarCarga();
                        acionarDispositivo = 0;
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(AgentLoad.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

                //Variável que determina o tipo de mensagem a ser enviada. R caso seja para cadastrar a Carga ou F caso ocorra alguma falha.
                //Essa situação só é enviada para Agente Central caso o cadastro seja realizado via Agente SE essa variável não é usada.
                String situacao; 

                if(verificaTemposAlocados(load)){ //Verifica se a carga já está cadastrada e se possui schedule registrado. Caso não, o cadastro da carga é acionado.
                    try {
                        if(verificaEstadoAtual(load)){ //Verifica se o status da carga é a mesma que está cadastrada no schedule da carga. Caso não, uma mensagem de falha é enviada para o Agente Central.
                            int acao = verificaMudancaProxCiclo(load);
                            if(acao != 0){ //Verifica se a carga será acionada/desligada no próximo ciclo. 
                                if(load.getFonteEnergia()!=0){//Caso sim, envia uma mensagem 1-acionar ou 2-desligar para o Agente SE se estiver ligado a um.
                                    msg.addReceiver(super.myAgent.getAID(Integer.toString(load.getFonteEnergia())));
                                    msg.setContent(String.valueOf(acao));
                                    myAgent.send(msg);
                                }
                                //Aciona/desliga a carga no próximo ciclo.   
                                acionarDispositivo = acao;
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
                                for (DFAgentDescription agenteCentral1 : agenteCentral) {
                                    msg.addReceiver(agenteCentral1.getName());
                                }
                            } catch (FIPAException ex) {
                                Logger.getLogger(AgentLoad.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            //Carga com Falha, envia mensagem de falha para o Agente Central.
                            situacao = "F";
                            msg.setContent(situacao+" "+load.getEquipamentoId()+" "+load.getPotencia()+" "+load.getTempo()+" "+load.getFonteEnergia());
                            myAgent.send(msg);
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(AgentLoad.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }else{
                    if(load.getFonteEnergia() != 0){ //Envia uma mensagem solicitando registro/atualização no banco de dados ao Agente SE.
                        msg.addReceiver(super.myAgent.getAID(Integer.toString(load.getFonteEnergia())));
                        msg.setContent("R "+load.getPotencia()+" "+load.getTempo());
                        myAgent.send(msg);
                    }else{//Envia uma mensagem solicitando cadastro ou update no banco de dados ao Agente Central.
                        //Adicionando Agente Central como destinatário.
                        DFAgentDescription pesquisarAgenteCentral = new DFAgentDescription();
                        ServiceDescription sdAgenteCentral = new ServiceDescription();
                        sdAgenteCentral.setType("Agente Central");
                        pesquisarAgenteCentral.addServices(sdAgenteCentral);
                        DFAgentDescription[] agenteCentral;
                        try {
                            agenteCentral = DFService.search(myAgent, pesquisarAgenteCentral);
                            for (DFAgentDescription agenteCentral1 : agenteCentral) {
                                msg.addReceiver(agenteCentral1.getName());
                            }
                        } catch (FIPAException ex) {
                            Logger.getLogger(AgentLoad.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        //Registrar Carga, envia mensagem de registro para o Agente Central.
                        situacao = "R";
                        msg.setContent(situacao+" "+load.getEquipamentoId()+" "+load.getPotencia()+" "+load.getTempo()+" 0");
                        myAgent.send(msg);
                    }
                }
            }
        }
    }
}