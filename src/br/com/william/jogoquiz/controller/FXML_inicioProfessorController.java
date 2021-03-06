/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.william.jogoquiz.controller;

import br.com.william.jogoquiz.bean.DesempenhoAlunoBean;
import br.com.william.jogoquiz.bean.PacotesBean;
import br.com.william.jogoquiz.log.DiretorioLog;
import br.com.william.jogoquiz.sql.Sql;
import br.com.william.jogoquiz.util.CodigoPacote;
import br.com.william.jogoquiz.util.Data;
import br.com.william.jogoquiz.util.Util;
import br.com.william.jogoquiz.view.Inicio;
import com.jfoenix.controls.JFXSpinner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author willi
 */
public class FXML_inicioProfessorController implements Initializable {
    @FXML
    private ImageView imagem_logo;
    @FXML
    private JFXSpinner progress_criarPacote;
    //Desempenho Aluno**********************************************************
    @FXML
    private TableView<DesempenhoAlunoBean> tabela_desempenho;

    @FXML
    private TableColumn<DesempenhoAlunoBean, String> coluna_aluno;

    @FXML
    private TableColumn<DesempenhoAlunoBean, String> coluna_professor;

    @FXML
    private TableColumn<DesempenhoAlunoBean, String> coluna_assunto;

    @FXML
    private TableColumn<DesempenhoAlunoBean, String> coluna_data;

    @FXML
    private TableColumn<DesempenhoAlunoBean, String> coluna_pontuacao;

    @FXML
    private DatePicker data_inicio;

    @FXML
    private ComboBox combo_disciplina;

    @FXML
    private ComboBox combo_turma;

    @FXML
    private ComboBox combo_serie;

    @FXML
    private TextField txt_nome;

    @FXML
    private TextField txt_assunto;

    @FXML
    private DatePicker data_fim;

    private ObservableList<DesempenhoAlunoBean> conteudoTabelaDesempenho = FXCollections.observableArrayList();

    @FXML
    void BT_buscarDesempenho(ActionEvent event) {
        buscarDesempenho();
    }

    public void buscarDesempenho() {
        conteudoTabelaDesempenho.clear();
        Sql novo = new Sql();

        String[] retorno = {"aluno", "professor", "assunto", "data", "pontuacao", "pontuacao_maxima"};
        ArrayList values = new ArrayList();
        ArrayList valorRetornado = new ArrayList();
        valorRetornado = novo.executeQuery(criarQuery(), values, retorno);
        System.out.println(criarQuery());
        for (int i = 0; i <= valorRetornado.size() - 1; i++) {
            String a = valorRetornado.get(i).toString();
            String b = a.replace("[", "").replace("]", "");
            String[] tokens = b.split(",");
            //String datas = tokens[3];//new SimpleDateFormat("dd/MM/yyyy").format(tokens[2]);           
            conteudoTabelaDesempenho.add(new DesempenhoAlunoBean(tokens[5], tokens[0], tokens[1], tokens[2], Data.converterData(tokens[3]), tokens[4]));
        }
    }

    @FXML
    void BT_limparFiltro(ActionEvent event) {
        combo_disciplina.getSelectionModel().clearSelection();
        combo_turma.getSelectionModel().clearSelection();
        combo_serie.getSelectionModel().clearSelection();

        data_inicio.setValue(null);
        data_fim.setValue(null);

        txt_nome.setText("");
        txt_assunto.setText("");
        buscarDesempenho();
    }

    public String criarQuery() {
        LocalDate data;
        String query = "SELECT `desempenho`.`aluno`,`desempenho`.`professor`,`desempenho`.`assunto`,`desempenho`.`data`,`desempenho`.`pontuacao`,`pacote_pergunta`.`pontuacao_maxima` FROM `desempenho`,`aluno`,`pacote_pergunta` where `pacote_pergunta`.`codigo_pacote` = `desempenho`.`codigo_pacote` AND `desempenho`.`aluno` = `aluno`.`nome_aluno`";
        query +=  " AND `desempenho`.`professor` = '"+Util.nome_log()+"'";
        if (combo_disciplina.getValue() != null) {
            query += "AND `desempenho`.`codigo_pacote` LIKE '%" + combo_disciplina.getValue().toString().substring(0, 3) + "%'";
        }

        if (data_inicio.getValue() != null && data_fim.getValue() != null) {
            data = data_inicio.getValue();
            query += " AND `desempenho`.`data` BETWEEN '" + data + "'";
            data = data_fim.getValue();
            query += " AND '" + data + "'";

        }
        if (data_inicio.getValue() != null && data_fim.getValue() == null) {
            data = data_inicio.getValue();
            query += " AND `desempenho`.`data` = '" + data + "'";

        }
        if (combo_serie.getValue() != null) {
            query += "AND `aluno`.`serie` = '" + combo_serie.getValue() + "'";
        }
        if (combo_turma.getValue() != null) {
            query += "AND `aluno`.`turma` = '" + combo_turma.getValue() + "'";
        }

        if (!"".equals(txt_nome.getText())) {
            query += " AND `desempenho`.`aluno` = '" + txt_nome.getText() + "'";
        }
        if (!"".equals(txt_assunto.getText())) {
            query += " AND `desempenho`.`assunto` LIKE '%" + txt_assunto.getText() + "%'";
        }

        return query;

    }
    //**************************************************************************

    //Criar/Editar Pacote*******************************************************    
    @FXML
    private ComboBox combo_disciplinaPacote;

    @FXML
    private ComboBox combo_respostaPacote;

    @FXML
    private TextField txt_assuntoPacote;

    @FXML
    private TextArea txt_enunciado;

    @FXML
    private TextField txt_a;

    @FXML
    private TextField txt_b;

    @FXML
    private TextField txt_c;

    @FXML
    private TextField txt_d;

    @FXML
    private RadioButton radio_facil;

    @FXML
    private ToggleGroup dificuldadePacote;

    @FXML
    private RadioButton radio_media;

    @FXML
    private RadioButton radio_dificil;

    @FXML
    private ListView list_perguntas;

    public String id;
    public String pacoteEscolhido = null;//pacote escolhido pelo usuario para editar
    public int cont = 0;
    public int pontuacao = 0;
    ArrayList perguntas = new ArrayList();
    ArrayList perguntasExcluir = new ArrayList();

    ObservableList<String> items = FXCollections.observableArrayList();

    @FXML
    void BT_adicionarPergunta(ActionEvent event) {
        if (!txt_enunciado.equals("") && !txt_enunciado.equals("") && !txt_enunciado.equals("") && !txt_enunciado.equals("") && !txt_enunciado.equals("") && dificuldadePacote.getSelectedToggle() != null) {
            ArrayList pergunta = new ArrayList();
            String dificuldade = "";
            
            pergunta.add(txt_enunciado.getText().trim());
            pergunta.add(txt_a.getText().trim());
            pergunta.add(txt_b.getText().trim());
            pergunta.add(txt_c.getText().trim());
            pergunta.add(txt_d.getText().trim());
            
            if (combo_respostaPacote.getValue() != null) {
                pergunta.add(combo_respostaPacote.getValue());
            }
            if (radio_facil.isSelected()) {
                pergunta.add(200);
                radio_facil.setSelected(false);

                dificuldade = "Facil";
            } else if (radio_media.isSelected()) {
                pergunta.add(300);
                radio_media.setSelected(false);

                dificuldade = "Mediana";
            } else if (radio_dificil.isSelected()) {
                pergunta.add(500);
                radio_dificil.setSelected(false);
                dificuldade = "Dificil";
            } else {
                pergunta.add(200);
            }
                       

            if (list_perguntas.getSelectionModel().getSelectedIndex() > -1) {
                System.out.println("index:" + list_perguntas.getSelectionModel().getSelectedIndex());
                if (pacoteEscolhido != null) {
                    pergunta.add(id);
                }
                perguntas.set(list_perguntas.getSelectionModel().getSelectedIndex(), pergunta);
            } else {

                if (pacoteEscolhido != null) {
                    pergunta.add("0");
                } else {

                }
                cont++;
                perguntas.add(pergunta);
                items.add("Pergunta " + dificuldade);

            }

            txt_enunciado.setText("");
            txt_a.setText("");
            txt_b.setText("");
            txt_c.setText("");
            txt_d.setText("");

        }
    }

    @FXML
    void BT_concluirPacote(ActionEvent event) {
      //  new Thread(){
      //      public void run(){
            //    progress_criarPacote.setVisible(true);
                concluirPacote();
           //     progress_criarPacote.setVisible(false);
        //    }
     //   }.start();
    }
    
    public void concluirPacote(){
        if (!txt_assuntoPacote.getText().equals("") && combo_disciplinaPacote.getValue() != null && cont != 0) {
            try {                
                cadastrarPerguntas();
//                Alert dialogoInfo = new Alert(Alert.AlertType.CONFIRMATION);
//                dialogoInfo.setHeaderText("Pacote Cadastrado Com sucesso!");
//                dialogoInfo.setContentText("");
//                dialogoInfo.showAndWait();
                
                items.clear();
                panel_editar_criarPacote.setVisible(false);
                panel_pacotePergunta.setVisible(true);
                list_perguntas.setItems(items);
                
     

            } catch (Exception ex) {
               // progress_criarPacote.setVisible(false);
              System.err.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n"+ex+"\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            }

        }
    }
    @FXML
    void BT_excluirPergunta(ActionEvent event) {
        int a = list_perguntas.getSelectionModel().getSelectedIndex();
        list_perguntas.getItems().remove(a);
        cont -= 1;

        if (pacoteEscolhido != null) {

            perguntasExcluir.add(perguntas.get(a).toString());

            //System.out.println("Escluindo:"+tokens[0].trim()+"-"+tokens[1].trim()+"-"+tokens[2].trim()+"-"+tokens[3].trim()+"-"+tokens[4].trim());
            // novo.executeQuery("DELETE FROM `pergunta` WHERE codigo_pacote = ? AND enunciado = ? AND a = ? AND b = ? AND c = ? AND d = ?", values);
        }
        perguntas.remove(a);
    }

    @FXML
    void pegarPergunta(MouseEvent event) {
        int index = list_perguntas.getSelectionModel().getSelectedIndex();
        System.out.println("index pergunta:"+index);
        if (list_perguntas.getSelectionModel().getSelectedIndex() != -1) {
            String a = perguntas.get(index).toString();
            String b = a.replace("[", "").replace("]", "");

            String[] tokens = b.split(",");
            txt_enunciado.setText(tokens[0]);
            txt_a.setText(tokens[1]);
            txt_b.setText(tokens[2]);
            txt_c.setText(tokens[3]);
            txt_d.setText(tokens[4]);
            combo_respostaPacote.setValue(tokens[5]);
            switch (tokens[6]) {
                case "  200":
                    radio_facil.setSelected(true);
                    break;
                case "  300":
                    radio_media.setSelected(true);
                    break;
                case "  500":
                    radio_dificil.setSelected(true);
                    break;

            }
            if (pacoteEscolhido != null) {
                id = tokens[7];
            }
            // cont++;
        }
    }

    @FXML
    void tirarSelection(MouseEvent event) {
        if (list_perguntas.getSelectionModel().getSelectedIndex() != -1) {
            txt_enunciado.setText("");
            txt_a.setText("");
            txt_b.setText("");
            txt_c.setText("");
            txt_d.setText("");
        }
        list_perguntas.getSelectionModel().clearSelection();
    }

    public void cadastrarPerguntas() {
        String pacote = gerarPacote();
        Sql novo = new Sql();
        for (int i = 0; i <= perguntas.size() - 1; i++) {
            ArrayList values = new ArrayList();
            String a = perguntas.get(i).toString();
            String b = a.replace("[", "").replace("]", "");
            String[] tokens = b.split(",");
            values.add(pacote);
            values.add(tokens[5].trim());
            values.add(tokens[6].trim());
            pontuacao += (int) Float.parseFloat(tokens[6]);
            values.add(tokens[0].trim());
            values.add(tokens[1].trim());
            values.add(tokens[2].trim());
            values.add(tokens[3].trim());
            values.add(tokens[4].trim());
            System.out.println("Mostrando conteudo Cadastrado: " + tokens[0] + " - " + tokens[1] + " - " + tokens[2] + " - " + tokens[3] + " - " + tokens[4] + " - " + tokens[5] + " - " + tokens[6] + " - ");
            if (pacoteEscolhido != null) {
                System.err.println("p01");
                values.set(0, combo_disciplinaPacote.getValue().toString().substring(0, 3) + pacoteEscolhido.substring(3, 8));
                if (Float.parseFloat(tokens[7]) == 0) {
                    novo.executeQuery("INSERT INTO `pergunta`(`codigo_pacote`, `resposta`, `pontos`, `enunciado`, `a`, `b`, `c`, `d`) VALUES (?,?,?,?,?,?,?,?)", values);
                } else {
                    values.add((int) Float.parseFloat(tokens[7]));
                    novo.executeQuery("UPDATE `pergunta` SET `codigo_pacote`=?, `resposta`=?,`pontos`=?,`enunciado`=?,`a`=?,`b`=?,`c`=?,`d`=? WHERE id_pergunta = ?", values);
                }
                System.err.println("p02");
            } else {
                System.err.println("p03");
                novo.executeQuery("INSERT INTO `pergunta`(`codigo_pacote`, `resposta`, `pontos`, `enunciado`, `a`, `b`, `c`, `d`) VALUES (?,?,?,?,?,?,?,?)", values);                
            }            
        }
        System.out.println("Cadastrando perguntas 01");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();

        ArrayList valores = new ArrayList();
        System.out.println("Cadastrando perguntas 02");
        valores.add(pacote);
        valores.add(combo_disciplinaPacote.getValue());
        valores.add(txt_assuntoPacote.getText().trim());
        valores.add(Util.nome_log());
        valores.add(dateFormat.format(date));
        valores.add(pontuacao);
        valores.add(cont);
        System.out.println("Cadastrando perguntas 03");
        if (pacoteEscolhido != null) {
            valores.set(0, combo_disciplinaPacote.getValue().toString().substring(0, 3) + pacoteEscolhido.substring(3, 8));
            valores.add(pacoteEscolhido);
            novo.executeQuery("UPDATE `pacote_pergunta` SET `codigo_pacote`=?,`disciplina`=?,`assunto`=?,`professor`=?,`data`=?,`pontuacao_maxima`=?,`numero_questoes`=? WHERE codigo_pacote = ?", valores);
            System.out.println("Cadastrando perguntas 04");
        } else {
            novo.executeQuery("INSERT INTO `pacote_pergunta`(`codigo_pacote`, `disciplina`, `assunto`, `professor`, `data`, `pontuacao_maxima`,`numero_questoes`) VALUES (?,?,?,?,?,?,?)", valores);
            System.out.println("Cadastrando perguntas 05");
        }
        
        if(!perguntasExcluir.isEmpty()){    
            for (int i = 0; i <= perguntasExcluir.size() - 1; i++) {
                ArrayList values = new ArrayList();
                String a = perguntasExcluir.get(i).toString();
                String b = a.replace("[", "").replace("]", "");
                String[] tokens = b.split(",");
                values.add(pacoteEscolhido);
                values.add(tokens[0].trim());
                values.add(tokens[1].trim());
                values.add(tokens[2].trim());
                values.add(tokens[3].trim());
                values.add(tokens[4].trim());
                System.out.println("Escluindo:" + tokens[0].trim() + "-" + tokens[1].trim() + "-" + tokens[2].trim() + "-" + tokens[3].trim() + "-" + tokens[4].trim());
                novo.executeQuery("DELETE FROM `pergunta` WHERE codigo_pacote = ? AND enunciado = ? AND a = ? AND b = ? AND c = ? AND d = ?", values);
            }
        }
        System.out.println("Cadastrando perguntas 06");
        perguntasExcluir.clear();
    }

    public String gerarPacote() {

        while (true) {
            Sql novo = new Sql();
            Random gerador = new Random();
            String materia = combo_disciplinaPacote.getValue().toString().substring(0, 3);
            int numero = gerador.nextInt((99999 - 10000) + 1) + 10000;
            String codigo = materia + numero;
            ArrayList values = new ArrayList();
            ArrayList retur = new ArrayList();
            values.add(codigo);
            String[] retorno = {"codigo_pacote"};

            retur = novo.executeQuery("SELECT `codigo_pacote` FROM `pacote_pergunta` WHERE codigo_pacote = ?", values, retorno);
            if (retur.isEmpty()) {
                return codigo;
            }
        }
    }

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
            cont++;
            switch (tokens[1]) {
                case " 200":
                    items.add("Pergunta Facil");
                    break;
                case " 300":
                    items.add("Pergunta Mediana");
                    break;
                case " 500":
                    items.add("Pergunta Dificil");
                    break;
            }

        }
        System.out.println("numero de questoes: " + cont);
        r.clear();
        String[] retorno = {"disciplina", "assunto"};
        r = novo.executeQuery("SELECT  `disciplina`, `assunto` FROM `pacote_pergunta` WHERE codigo_pacote = ?", values, retorno);
        for (int i = 0; i <= r.size() - 1; i++) {
            String a = r.get(i).toString();
            String b = a.replace("[", "").replace("]", "");
            String[] tokens = b.split(",");
            switch (tokens[0]) {
                case "Matematica":
                    combo_disciplinaPacote.getSelectionModel().select("Matematica");
                    break;
                case "Portugues":
                    combo_disciplinaPacote.getSelectionModel().select("Portugues");
                    break;
                case "Quimica":
                    combo_disciplinaPacote.getSelectionModel().select("Quimica");
                    break;
            }
            txt_assuntoPacote.setText(tokens[1]);
        }
    }

    //**************************************************************************
    //FILTRAR PACOTES***********************************************************
    @FXML
    private TableView<PacotesBean> tabela_pacotes;

    @FXML
    private TableColumn<PacotesBean, String> coluna_disciplinaPacote;

    @FXML
    private TableColumn<PacotesBean, String> coluna_assuntoPacote;

    @FXML
    private TableColumn<PacotesBean, String> coluna_professorPacote;

    @FXML
    private TableColumn<PacotesBean, String> coluna_dataPacote;

    @FXML
    private TableColumn<PacotesBean, String> coluna_numeroQuestoesPacote;

    private ObservableList<PacotesBean> conteudoTabelaPacotes = FXCollections.observableArrayList();

    @FXML
    private TextField txt_assuntoPacotePerguntas;

    @FXML
    private CheckBox check_Pacote;

    @FXML
    private DatePicker combo_dataPacoteInicio;

    @FXML
    private DatePicker combo_dataPacoteFim;
    
    @FXML
    private Label label_nome;
    
    @FXML
    private ComboBox combo_disciplinaPacotePerguntas;
    ArrayList pacotes = new ArrayList();

    @FXML
    void BT_buscarPacotes(ActionEvent event) {
        buscarPacotes();
    }

    public void buscarPacotes() {        
        conteudoTabelaPacotes.clear();
        pacotes.clear();
        Sql novo = new Sql();
        ArrayList v = new ArrayList();
        ArrayList r2 = new ArrayList();
        String[] r = {"codigo_pacote", "disciplina", "assunto", "professor", "data", "numero_questoes"};
        System.out.println("Query 123: " + criarQueryPacote());
        r2 = novo.executeQuery(criarQueryPacote(), v, r);
        for (int i = 0; i <= r2.size() - 1; i++) {
            String a = r2.get(i).toString();
            String b = a.replace("[", "").replace("]", "");
            String[] tokens = b.split(",");
            pacotes.add(tokens[0]);
            conteudoTabelaPacotes.add(new PacotesBean(tokens[1], tokens[2], tokens[3], Data.converterData(tokens[4]), tokens[5]));
        }

    }

    public String criarQueryPacote() {
        LocalDate data;
        String query = "SELECT `codigo_pacote`,`disciplina`, `assunto`, `professor`, `data`,`numero_questoes` FROM `pacote_pergunta` WHERE 1 ";
        if (combo_disciplinaPacotePerguntas.getValue() != null) {
            query += "AND `disciplina` = '" + combo_disciplinaPacotePerguntas.getValue() + "'";
        }
        if (!"".equals(txt_assuntoPacotePerguntas.getText())) {
            query += "AND `assunto` LIKE '%" + txt_assuntoPacotePerguntas.getText() + "%'";
        }

        if (combo_dataPacoteInicio.getValue() != null && combo_dataPacoteFim.getValue() != null) {
            data = combo_dataPacoteInicio.getValue();
            query += " AND `data` BETWEEN '" + data + "'";
            data = combo_dataPacoteFim.getValue();
            query += " AND '" + data + "'";

        }
        if (combo_dataPacoteInicio.getValue() != null && combo_dataPacoteFim.getValue() == null) {
            data = combo_dataPacoteInicio.getValue();
            query += " AND `data` = '" + data + "'";

        }

        if (check_Pacote.isSelected()) {
            query += " AND `professor` = '" +Util.nome_log() + "'";
        }

        return query;
    }

    @FXML
    void BT_limparFiltroPerguntass(ActionEvent event) {
        combo_dataPacoteInicio.setValue(null);
        combo_dataPacoteFim.setValue(null);
        txt_assuntoPacotePerguntas.setText("");
        combo_disciplinaPacotePerguntas.setValue(null);
        buscarPacotes();
    }

    @FXML
    void BT_deletarPacotePerguntas(ActionEvent event) {
        int i = tabela_pacotes.getSelectionModel().getSelectedIndex();
        String a = (String) conteudoTabelaPacotes.get(i).getProfessor().getValue();
        if (a.equals(" " + Util.nome_log())) {
            ArrayList pacoteEscolhidos = new ArrayList();
            pacoteEscolhidos.add((String) pacotes.get(i));
            Sql novo = new Sql();

            novo.executeQuery("DELETE FROM `pacote_pergunta` WHERE codigo_pacote = ?", pacoteEscolhidos);

            novo.executeQuery("DELETE FROM `pergunta` WHERE codigo_pacote = ?", pacoteEscolhidos);

            buscarPacotes();
        }
    }

    //**************************************************************************
    
    //Administrado o jogo*******************************************************
    @FXML
    void BT_criarJogo(ActionEvent event) {
        int i = tabela_pacotes.getSelectionModel().getSelectedIndex();
        if (i > -1) {            
            Inicio is = new Inicio();
            CodigoPacote c = new CodigoPacote();
            c.setCodigoPacote((String) pacotes.get(i));
            System.out.println("Codigo Pacote aqui: "+(String) pacotes.get(i));
            is.abrirScene("adminGame");
            
        }
    }
    //**************************************************************************
    @FXML
    void BT_sair(ActionEvent event) throws IOException {
        DiretorioLog pegar = new DiretorioLog();
        File file = new File(pegar.getDiretoriolog());
        try {
            FileInputStream arquivo = new FileInputStream(pegar.getDiretoriolog());
            InputStreamReader in = new InputStreamReader(arquivo);
            BufferedReader br = new BufferedReader(in);
            Sql novo = new Sql();
            ArrayList values = new ArrayList();
            String a = br.readLine();
            values.add(br.readLine().substring(4));
            arquivo.close();
            novo.executeQuery("UPDATE `professor` SET `status`='0' WHERE nome_professor  = ?", values);
        
                try{
                    if (file.delete()) {}
                    Alert dialogoInfo = new Alert(Alert.AlertType.CONFIRMATION);                
                    dialogoInfo.setHeaderText("Deletando o arquivo");
                    dialogoInfo.setContentText("");
                    dialogoInfo.showAndWait();
                }catch(Exception ex){
                    Alert dialogoInfo = new Alert(Alert.AlertType.WARNING);                
                    dialogoInfo.setHeaderText("Erro ao deletar arquivo");
                    dialogoInfo.setContentText("");
                    dialogoInfo.showAndWait();
                }    
            
            System.exit(0);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(FXML_inicioAlunoController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

//    public String nome_professor() {
//        FileInputStream arquivo = null;
//        
//        try {
//            arquivo = new FileInputStream("D:\\Projetos-git\\java\\Quiz\\src\\br\\com\\william\\jogoquiz\\log\\log.txt");
//            InputStreamReader in = new InputStreamReader(arquivo);
//            BufferedReader br = new BufferedReader(in);
//            try {
//                String a = br.readLine();
//            } catch (IOException ex) {
//                //Logger.getLogger(FXML_inicioProfessorController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            try {
//                return br.readLine().substring(4);
//            } catch (IOException ex) {
//                //Logger.getLogger(FXML_inicioProfessorController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        } catch (FileNotFoundException ex) {
//                //Logger.getLogger(FXML_inicioProfessorController.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                arquivo.close();
//            } catch (IOException ex) {
//                //Logger.getLogger(FXML_inicioProfessorController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return null;
//
//    }
    //**************************************************************************
    @FXML
    private Pane panel_editar_criarPacote;
    @FXML
    private Pane panel_pacotePergunta;

    @FXML
    void BT_abrirPanelCriarPacote(ActionEvent event) {
        reset();
        pacoteEscolhido = null;
        panel_editar_criarPacote.setVisible(true);
        panel_pacotePergunta.setVisible(false);

    }

    @FXML
    void BT_abrirPanelEditarPacote(ActionEvent event) {     
        int i = tabela_pacotes.getSelectionModel().getSelectedIndex();
        if (i != -1) {
            String a = (String) conteudoTabelaPacotes.get(i).getProfessor().getValue().trim();
            System.out.println("tabela conteudo:" + a.trim());
            if (a.equals(Util.nome_log().trim())) {
                reset();
                System.out.println("pacote Escolhido:" + (String) pacotes.get(i));
                pacoteEscolhido = (String) pacotes.get(i);

                if (pacoteEscolhido != null) {

                    pegarPacote(pacoteEscolhido);
                    list_perguntas.setItems(items);
                    panel_editar_criarPacote.setVisible(true);
                    panel_pacotePergunta.setVisible(false);
                }
            }
        }
    }

    @FXML
    void Fechar_CriarPergunta(MouseEvent event) {
        items.clear();
        panel_editar_criarPacote.setVisible(false);
        panel_pacotePergunta.setVisible(true);
        list_perguntas.setItems(items);
    }
    @FXML
    private Pane panelDesempenho;

    @FXML
    void BT_abrirFiltroPacotes(ActionEvent event) {
        label_nome.setText(Util.nome_log());
        buscarPacotes();
        panel_pacotePergunta.setVisible(true);
        imagem_logo.setVisible(false);
    }

    @FXML
    void BT_fecharFIltroPacotes(MouseEvent event) {
        panel_pacotePergunta.setVisible(false);
        combo_dataPacoteInicio.setValue(null);
        combo_dataPacoteFim.setValue(null);
        txt_assuntoPacotePerguntas.setText("");
        combo_disciplinaPacotePerguntas.setValue(null);
        imagem_logo.setVisible(true);
    }

    @FXML
    void BT_abrirDesempenho(ActionEvent event) {
        label_nome.setText(Util.nome_log());
        buscarDesempenho();
        panelDesempenho.setVisible(true);
        imagem_logo.setVisible(false);
    }

    @FXML
    void BT_fecharDesempenho(MouseEvent event) {
        panelDesempenho.setVisible(false);
        combo_disciplina.getSelectionModel().clearSelection();
        combo_turma.getSelectionModel().clearSelection();
        combo_serie.getSelectionModel().clearSelection();

        data_inicio.setValue(null);
        data_fim.setValue(null);

        txt_nome.setText("");
        txt_assunto.setText("");
        imagem_logo.setVisible(true);

    }

    public void reset() {
        pacoteEscolhido = null;
        items.clear();
        //dificuldadePacote.getSelectedToggle().setSelected(true);

        combo_disciplinaPacote.setValue(null);
        txt_assuntoPacote.setText("");
        txt_enunciado.setText("");
        txt_a.setText("");
        txt_b.setText("");
        txt_c.setText("");
        txt_d.setText("");
        pontuacao = 0;
        cont = 0;
        perguntas.clear();
    }

    //**************************************************************************
    
    @FXML
    void BT_jogar(ActionEvent event) {
        Inicio abrir = new Inicio();
        abrir.abrirScene("pergunta");
    }
    
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
        progress_criarPacote.setVisible(false);
        label_nome.setText(Util.nome_log());
        // TODO
//        combo_disciplina.getItems().add("Português");
//        combo_disciplina.getItems().add("Portugues");
//        combo_disciplina.getItems().add("Quimica");
        Util.listarDisciplinas(combo_disciplina);
        combo_turma.getItems().add("A");
        combo_turma.getItems().add("B");
        combo_turma.getItems().add("C");

        combo_serie.getItems().add("1");
        combo_serie.getItems().add("2");
        combo_serie.getItems().add("3");

        combo_respostaPacote.getItems().add("A");
        combo_respostaPacote.getItems().add("B");
        combo_respostaPacote.getItems().add("C");
        combo_respostaPacote.getItems().add("D");

        coluna_aluno.setCellValueFactory(cellData -> cellData.getValue().getAluno());
        coluna_professor.setCellValueFactory(cellData -> cellData.getValue().getProfessor());
        coluna_assunto.setCellValueFactory(cellData -> cellData.getValue().getPacote());
        coluna_data.setCellValueFactory(cellData -> cellData.getValue().getDate());
        coluna_pontuacao.setCellValueFactory(cellData -> cellData.getValue().getPontuacao());

        coluna_disciplinaPacote.setCellValueFactory(cellData -> cellData.getValue().getDisciplina());
        coluna_assuntoPacote.setCellValueFactory(cellData -> cellData.getValue().getAssunto());
        coluna_professorPacote.setCellValueFactory(cellData -> cellData.getValue().getProfessor());
        coluna_dataPacote.setCellValueFactory(cellData -> cellData.getValue().getData());
        coluna_numeroQuestoesPacote.setCellValueFactory(cellData -> cellData.getValue().getNumeroQuestoes());

        tabela_pacotes.setItems(conteudoTabelaPacotes);

//        combo_disciplinaPacote.getItems().add("Matematica");
//        combo_disciplinaPacote.getItems().add("Portugues");
//        combo_disciplinaPacote.getItems().add("Quimica");
        Util.listarDisciplinas(combo_disciplinaPacote);
//        combo_disciplinaPacotePerguntas.getItems().add("Matematica");
//        combo_disciplinaPacotePerguntas.getItems().add("Portugues");
//        combo_disciplinaPacotePerguntas.getItems().add("Quimica");
        Util.listarDisciplinas(combo_disciplinaPacotePerguntas);
        list_perguntas.setItems(items);

        tabela_desempenho.setRowFactory(tv -> new TableRow<DesempenhoAlunoBean>() {
            float m;
            float p;

            public void updateItem(DesempenhoAlunoBean item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    m = Float.parseFloat(item.getPontuacaoMaxima().getValue());
                    p = Float.parseFloat(item.getPontuacao().getValue());
                }

                if (item == null) {
                    setStyle("");
                } else if (p < (m / 2) || p == (m / 2)) {
                    setStyle("-fx-background-color:#EE3B3B;");
                } else if (p > (m / 2) && p < ((m / 2) + ((m / 2) / 2))) {
                    setStyle("-fx-background-color:#FF7F24;");
                } else if (p > ((m / 2) + ((m / 2) / 2))) {
                    setStyle("-fx-background-color:#43CD80;");
                }
            }
        });
        tabela_desempenho.setItems(conteudoTabelaDesempenho);
    }

}
