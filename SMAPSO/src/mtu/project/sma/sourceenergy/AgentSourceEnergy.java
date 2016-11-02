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
import mtu.project.db.dao.LoadDAO;
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
    
    String dispositivo;
    String porta;
    int rate;
    
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
                dispositivo = (String)args[2];
                porta = (String)args[3];
                rate = Integer.valueOf((String)args[4]);
                
            }
            DFService.register(this, dfd);
        }catch(FIPAException e){
            e.printStackTrace();
        }
        // Comportamento que recebe requisições dos Agentes Load.    
        addBehaviour(new RecebeRequestLoad ());
    }
    
    // Método que gera mensagem para o Agente Central.
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
            for (DFAgentDescription agenteCentral1 : agenteCentral) {
                msg.addReceiver(agenteCentral1.getName());
            }
            }catch(FIPAException e){
                e.printStackTrace();
            }
            
        msg.setContent(geraDados(load, operacao, alteracao));
                        
        return msg;
    }
    
    // Método que monta a string de mensagem com as informações a serem enviadas para o Agente Central.
    public String geraDados(Load load, String op, String alt){
        String operacao = op; //R = Registro e A = Alteração
        String alteracao = alt; //S = Sim e N = Não
        String dados = operacao+" "+load.getEquipamentoId().toString()+" "+load.getPotencia()+" "+load.getTempo()+" "+load.getFonteEnergia()+" "+alteracao;
        
        return dados;
    }
    
    // Verifica-se se o dispositivo está gerando energia.
    public Double verificaGeracaoEnergia() throws InterruptedException, ExecutionException{
        
        Double resultado; //variável que guarda o retorno enviado pelo dispositivo.
        String comando = "status";
        //Cria uma pool de threads e adiciona a Thread para fazer uso da porta serial.
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        List<Callable<String>> lst = new ArrayList<>();
        lst.add(new ConexaoXBee(dispositivo, comando, porta, rate));
        //Executa a Thread e espera o resultado de retorno.
        List<Future<String>> tasks = executorService.invokeAll(lst);
        // Atribui-se o valor lido no dispositivo à variável Resultado.
        resultado = Double.valueOf(tasks.get(0).get());
        
        //Finaliza a pool de Threads.
        executorService.shutdown();
        
        return resultado;
    }
    
    //Compara a capacidade gerada com a capacidade de geração prevista.
    public String verificarCapacidadeAtual(int fonte, Double geracao){
        Date dataAtual = new Date();
        Locale locale = new Locale("pt","BR");
        //Data usada para conversão em instantes de tempo (1 à 96).
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH mm", locale);
        String currentTime = sdf.format(dataAtual);
        //Data usada para comparação com a data no Banco de Dados.
        java.text.SimpleDateFormat data = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String currentData = data.format(dataAtual);
        //Extração da hora do minuto para conversão em instantes de tempo.
        StringTokenizer st = new StringTokenizer(currentTime);
        String hora = st.nextToken();
        String minuto = st.nextToken();
        int tempo = conversorTempo(hora, minuto);
        
        Double geracaoPrevista = -1.0;
        // Busca no Banco de Dados os dados de geração do gerador de energia..
        SourceEnergy source = SourceEnergyDAO.getInstance().findBySourceId(Long.valueOf((long)fonte));
        
        for(SourceSchedule s: source.getSourceSchedule()) {
            //Se a data for encontrada na comparação é realizada a busca pelo instante de tempo.
            if(s.getDataAtual().toString().equals(currentData)){
                //É realizada a busca da geração prevista para o instante de tempo atual.
                if(s.getTempo() == tempo){
                    geracaoPrevista = s.getPotenciaPrevista();
                }
            }
        }
        // Se Geração for -1 é porque o dispositivo não está funcionando, então retorna S indicando que o algoritmo de balanceamento precisa saber.
        if(geracao == -1){
            return "S";
        }else{
            // Se Geração mais 20% for menor que o previsto é porque a geração não é satisfatória, então retorna S indicando que o algoritmo de balanceamento precisa saber.
            if(((geracao*1.2) < geracaoPrevista) || geracaoPrevista == -1.0 ){
                return "S";
            }else{
                return "N";
            }
        }
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

                if(conteudo.equalsIgnoreCase("R")){ // se for para registrar no banco de dados

                    Long equipamentoId = Long.parseLong(msg.getSender().getLocalName());
                    Double potencia = Double.parseDouble(st.nextToken()); //pego o segundo token
                    int tempo = Integer.parseInt(st.nextToken()); //pego o terceiro token
                    
                    try {
                        geracao = verificaGeracaoEnergia();
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(AgentSourceEnergy.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    load.setEquipamentoId(equipamentoId);
                    load.setPotencia(potencia);
                    load.setTempo(tempo);
                    load.setSchedule(null);
                    
                    if(geracao != -1){
                        load.setFonteEnergia(Integer.valueOf(myAgent.getLocalName()));
                    }else{
                        load.setFonteEnergia(0);
                    }
                    // Envia mensagem indicando Registro no Banco de Dados esem alteração.
                    myAgent.send(gerarMensagem(load,"R","N"));

                }else{
                    if(conteudo.equalsIgnoreCase("1")){ // Caso a mensagem contenha conteúdo = 1 é porque a carga será acionada no próximo ciclo.
                        try {
                            geracao = verificaGeracaoEnergia();
                        } catch (InterruptedException | ExecutionException ex) {
                            Logger.getLogger(AgentSourceEnergy.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        String alteracao = verificarCapacidadeAtual(Integer.valueOf(myAgent.getLocalName()), geracao);
                        load = LoadDAO.getInstance().findByEquipamentoId(Long.valueOf(msg.getSender().getLocalName()));
                        myAgent.send(gerarMensagem(load,"A",alteracao));
                    }
                }
            }else{
                block();
            }
        }
    }
}