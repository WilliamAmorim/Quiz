/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.william.jogoquiz.controller;

import br.com.william.jogoquiz.sql.Sql;
import br.com.william.jogoquiz.util.Util;
import br.com.william.jogoquiz.view.Inicio;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author willi
 */
public class FXML_perguntaController implements Initializable {
    @FXML
    private ImageView imagem_background;
    
    @FXML
    private Pane panel_game;

    @FXML
    private JFXButton BT_c;

    @FXML
    private JFXButton BT_a;

    @FXML
    private JFXButton BT_b;

    @FXML
    private JFXButton BT_d;

    @FXML
    private Label label_enunciado;

    @FXML
    private Label label_pontos;        

    @FXML
    private Label label_tema;        
    
    @FXML
    private Label label_nPerguntas;

    @FXML
    void BT_a(ActionEvent event) {           
        responder("A");
    }

    @FXML
    void BT_b(ActionEvent event) {
        responder("B");
    }

    @FXML
    void BT_c(ActionEvent event) {
        responder("C");
    }

    @FXML
    void BT_d(ActionEvent event) {
        responder("D");
    }
    boolean responder = true;
    public void responder(String resposta){
        if(responder){
            verificarResposta(resposta,true);
            enviarMensagem(servidor,"resposta,"+meusPontos);
            label_pontos.setText(meusPontos+"");
        }
        responder = false;
    }

    @FXML
    private TextField txt_codigoJogo;

    @FXML
    private JFXButton txt_entrarJogo;
    
    @FXML
    private Label label_nome;

    @FXML
    private JFXSpinner progress_entraJogo;

    @FXML
    private Label label_conectado;
    @FXML
    private Label label_conectado1;

    @FXML
    void BT_entrarJogo(ActionEvent event) {
        label_nome.setText(Util.nome_log());
        new Thread(){
            public void run(){
                if(!label_conectado.isVisible()){            
                    conectar();
                }    
            }
        }.start();
    }

    @FXML
    void BT_voltar(ActionEvent event){
        label_nome.setText(Util.nome_log());
        pacoteEscolhido = null;
        a = true;
        Nperguntas = 0;
        perguntas.clear();
        pontos = 0;
        resposta = null;
        p = 0;
        meusPontos = 0;
        progress_entraJogo.setVisible(false);
        label_conectado.setVisible(false);
        label_conectado1.setVisible(false);
        panel_game.setVisible(false);
        panel_ranking.setVisible(false);
        txt_codigoJogo.setText("");
        
        //if(servidor.toString()){
        fecharConexao();
        //}
        Inicio abrir = new Inicio();
        abrir.abrirScene("inicioAluno");  
                 
    }
    public void fecharConexao(){
        try{
            servidor.close();
        }catch(Exception ex){
            
        }
    }
     @FXML
    private Pane panel_ranking;

    @FXML
    private Label label_2Lugar;

    @FXML
    private Label label_1Lugar;

    @FXML
    private Label label_3Lugar;
    
    @FXML
    private Label label_p1;

    @FXML
    private Label label_p2;

    @FXML
    private Label label_p3;
    private void cadastrarResultado(String professor){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        
        Sql cadastrar = new Sql();
        
        ArrayList values = new ArrayList();
        values.add(Util.nome_log());
        values.add(professor);
        values.add(pacoteEscolhido);
        values.add(dateFormat.format(date));
        values.add(meusPontos);
        values.add(label_tema.getText());
        cadastrar.executeQuery("INSERT INTO `desempenho`(`aluno`, `professor`, `codigo_pacote`, `data`, `pontuacao`, `assunto`) VALUES (?,?,?,?,?,?)", values);
        
        
    }
    public void finalizarGame(){
        Platform.runLater(()->panel_game.setVisible(false));
        Platform.runLater(()->panel_ranking.setVisible(true));
        System.out.println("Jogo Finalizado");
        //enviar resultado do aluno
    }
    
    float meusPontos;
    float pontos;
    String resposta;
    int p = 0;
    public void proximaPerguntas(){
            if(p == Nperguntas){
                //Finaliza game              
                //finalizarGame();
            }else{
            String a = perguntas.get(p).toString();
            String b = a.replace("[", "").replace("]", "");
            String[] tokens = b.split(",");
            Platform.runLater(()->label_enunciado.setText(tokens[0].trim()));
            Platform.runLater(()->BT_a.setText(tokens[1].trim()));
            Platform.runLater(()->BT_b.setText(tokens[2].trim()));
            Platform.runLater(()->BT_c.setText(tokens[3].trim()));
            Platform.runLater(()->BT_d.setText(tokens[4].trim()));
            Platform.runLater(()->label_nPerguntas.setText(p+"/"+Nperguntas));
            pontos = Float.parseFloat(tokens[6].trim());
            resposta = tokens[5].trim();

            verificarResposta("D",false);
            responder = true;
            }
        p++;
    }
       
    ArrayList perguntas = new ArrayList();
    private int Nperguntas = 0;
    private String disciplina;
    public void pegarPacote(String pacoteEscolhido) {
        Sql novo = new Sql();
        ArrayList values = new ArrayList();
        ArrayList r = new ArrayList();
        values.add(pacoteEscolhido);
        String[] retur = {"id_pergunta", "pontos", "resposta", "enunciado", "a", "b", "c", "d"};
        r = novo.executeQuery("SELECT * FROM `pergunta` WHERE codigo_pacote = ?", values, retur);        
        for (int i = 0; i <= r.size() - 1; i++) {
            ArrayList pergunta = new ArrayList();
            String a = r.get(i).toString();
            String b = a.replace("[", "").replace("]", "");
            String[] tokens = b.split(",");
            pergunta.add(tokens[3]);
            pergunta.add(tokens[4]);
            pergunta.add(tokens[5]);
            pergunta.add(tokens[6]);
            pergunta.add(tokens[7]);
            pergunta.add(tokens[2]);
            pergunta.add(tokens[1]);
            pergunta.add(tokens[0]);
            perguntas.add(pergunta);
            Nperguntas++;
        }
        r.clear();
        String[] returno = {"disciplina","assunto"};
        r = novo.executeQuery("SELECT `disciplina`, `assunto` FROM `pacote_pergunta` WHERE codigo_pacote = ?", values, returno);
        for (int i = 0; i <= r.size() - 1; i++) {            
            String a = r.get(i).toString();
            String b = a.replace("[", "").replace("]", "");
            String[] tokens = b.split(",");
            disciplina = tokens[0].trim();
            label_tema.setText(tokens[1].trim());            
        }
    }
    
    public void verificarResposta(String resposta,boolean  v){
        String style = "-fx-background-radius:22; ";
        if(v){
            if(this.resposta.equals(resposta)){
                meusPontos +=  pontos;
                style += "-fx-background-color:#00FF7F;";
            }else{
                style += "-fx-background-color:#EE3B3B;";
            }
            switch(resposta){
                    case "A":BT_a.setStyle(style);break;
                    case "B":BT_b.setStyle(style);break;
                    case "C":BT_c.setStyle(style);break;
                    case "D":BT_d.setStyle(style);break;                    
            }                              
        }else{
            style += "-fx-background-color:white;";
            BT_a.setStyle(style);
            BT_b.setStyle(style);
            BT_c.setStyle(style);
            BT_d.setStyle(style);             
        }
    }
   
   //Servidor*******************************************************************
    Socket servidor;
    
    private void conectar(){
        try {            
            progress_entraJogo.setVisible(true);            
            
            servidor = new Socket(txt_codigoJogo.getText(), 5555);
            
            //System.out.println(Util.turma());
            enviarMensagem(servidor,"iniciar,"+Util.nome_log()+"-"+Util.turma()+":");//Util.nome_log()+" "+Util.turma()
            System.out.println("CONEXÃO ESTABELECIDA!!!");  
            progress_entraJogo.setVisible(false); 
            label_conectado.setVisible(true);
            label_conectado1.setVisible(true);
            //progress_entraJogo.setProgress(100);
        } catch (IOException ex){
            progress_entraJogo.setVisible(false);  
            label_conectado.setVisible(false);
            label_conectado1.setVisible(false);
            System.err.println("----ERRO AO TENTAR SE CONECTAR----"); 
//            Alert dialogoInfo = new Alert(Alert.AlertType.ERROR);                
//                dialogoInfo.setHeaderText("Não foi possivel se conectar");
//                dialogoInfo.setContentText("Verifique o codigo do jogo");
//                dialogoInfo.showAndWait();
            
        }
        
    }
    
    private void enviarMensagem(Socket socket,String msg){
        ObjectOutputStream output;        
        try {
            output = new ObjectOutputStream(socket.getOutputStream());           
            output.writeUTF(msg);
            output.flush();//cambio do rádio amador
            System.out.println("---------------MENSAGEM ENVIADA COM SUCESSO!!!");
            receberMensagem(servidor);
        } catch (IOException ex) {
            System.err.println("----ERRO AO ENVIAR MENSAGEM----");
        }        
    }
    
    private void receberMensagem(Socket socket){
        new Thread(){
            public void run(){
                try {
                    System.out.println("RECEBENDO MENSAGEM...");                     
                    ObjectInputStream input = new ObjectInputStream(socket.getInputStream());                           
                    String m = input.readUTF();
                    tratarMensagem(m);
                    System.out.println("----MENSAGEM: "+m);
                } catch (IOException ex) {
                    System.err.println("----ERRO AO RECEBER MENSAGEM----");
                }
            }
        }.start();      
    }
    boolean a = true;
    public String pacoteEscolhido;
    private void tratarMensagem(String msg){
        String[] tokens = msg.split(",");
        if(a){
            pegarPacote(tokens[1]);
            pacoteEscolhido = tokens[1];
            panel_game.setVisible(true);     
            proximaPerguntas();
            a = false;
        }else{ 
            if(tokens[0].equals("finalizado")){
                finalizarGame();
                String[] r1 = Util.dividirResultado(tokens[1]);
                String[] r2 = Util.dividirResultado(tokens[2]);
                String[] r3 = Util.dividirResultado(tokens[3]);
                
                if(Util.verificarDesconectado(r1[0])){
                    Platform.runLater(()->label_1Lugar.setText(r1[0]));                
                }
                if(Util.verificarDesconectado(r1[0])){
                    Platform.runLater(()->label_2Lugar.setText(r2[0]));
                }
                if(Util.verificarDesconectado(r1[0])){
                    Platform.runLater(()->label_3Lugar.setText(r3[0]));
                }
                if(Util.verificarDesconectado(r1[1])){
                    Platform.runLater(()->label_p1.setText(r1[1]));
                }
                if(Util.verificarDesconectado(r1[1])){
                    Platform.runLater(()->label_p2.setText(r2[1]));
                }
                if(Util.verificarDesconectado(r1[1])){
                    Platform.runLater(()->label_p3.setText(r3[1]));
                }
                cadastrarResultado(tokens[4]);
            }
            proximaPerguntas();          
            
        }
    }
    
    //**************************************************************************
       @FXML
    void BT_fechar(MouseEvent event) {
        Inicio.fechar();
    }

    @FXML
    void BT_minimizar(MouseEvent event) {
        Inicio.minimizar();
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        label_nome.setText(Util.nome_log());
//                Image image = new Image("src/br/com/william/jogoquiz/imagens/banner.png");
            //File file = new File("src/br/com/william/jogoquiz/imagens/banner.png");
            //imagem_background.setImage(new Image(file.toURI().toString()));
            //imagem_background.setFitWidth(2000);
        
        // TODO
    }    
    
}
