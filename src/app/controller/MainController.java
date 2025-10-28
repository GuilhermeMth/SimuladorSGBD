package app.controller;

import app.model.Coluna;
import app.model.InterpretadorSQL;
import app.model.Linha;
import app.model.Tabela;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.text.Font; // Import necessário para estilização básica no código Java

public class MainController {

    @FXML
    private TextArea sqlTextArea;

    @FXML
    private Button executarButton;

    @FXML
    private TableView<Linha> resultadoTableView;

    @FXML
    private Label statusLabel;

    // Componente adicionado no FXML para o glossário
    @FXML
    private Button glossarioButton;

    private InterpretadorSQL interpretador;

    @FXML
    public void initialize() {
        this.interpretador = new InterpretadorSQL();
        sqlTextArea.setText("-- Bem-vindo ao Simulador SGBD!\n-- Crie suas tabelas e faça consultas.\n\nCREATE TABLE cidades (id INT PRIMARY KEY, nome_cidade STRING);\n\nCREATE TABLE usuarios (id INT PRIMARY KEY, nome STRING, id_cidade INT REFERENCES cidades(id));\n\nINSERT INTO cidades (id, nome_cidade) VALUES (10, 'Recife');\nINSERT INTO cidades (id, nome_cidade) VALUES (20, 'Olinda');\n\nINSERT INTO usuarios (id, nome, id_cidade) VALUES (1, 'Alice', 10);\nINSERT INTO usuarios (id, nome, id_cidade) VALUES (2, 'Bob', 20);\n\nSELECT * FROM usuarios JOIN cidades ON usuarios.id_cidade = cidades.id;");

        // Estilização (mantida do seu código anterior)
        statusLabel.setStyle("-fx-font-weight: bold;");
    }

    @FXML
    private void handleExecutarSQL() {
        String[] comandos = sqlTextArea.getText().split(";");

        for (String comando : comandos) {
            comando = comando.replaceAll("--.*", "").trim();
            if (comando.isEmpty()) {
                continue;
            }

            try {
                Object resultado = interpretador.executar(comando);
                limparTabelaResultado();

                if (resultado instanceof Tabela) {
                    exibirResultado((Tabela) resultado);
                    atualizarStatus("Comando SELECT executado com sucesso!", false);
                } else if (resultado instanceof String) {
                    atualizarStatus((String) resultado, false);
                }
            } catch (Exception e) {
                limparTabelaResultado();
                atualizarStatus("Erro: " + e.getMessage(), true);
                break;
            }
        }
    }

    /**
     * Exibe o glossário de comandos SQL em uma nova janela (Stage) modal.
     */
    @FXML
    private void handleExibirGlossario() {
        // Texto do glossário formatado com quebras de linha literais (\n)
        String glossarioTexto = "Comandos Suportados:\n\n" +
                "1. SELECT:\n" +
                "  Utilizado para consultar dados de tabelas. Ex: SELECT * FROM tabela;\n\n" +
                "2. CREATE TABLE:\n" +
                "  Cria uma nova tabela no banco de dados. Ex: CREATE TABLE nome (coluna INT PRIMARY KEY);\n\n" +
                "3. INSERT INTO:\n" +
                "  Adiciona uma nova linha de dados na tabela. Ex: INSERT INTO tabela (coluna) VALUES (valor);\n\n" +
                "4. DELETE FROM:\n" +
                "  Remove linhas de uma tabela. Ex: DELETE FROM tabela WHERE condicao;\n\n" +
                "5. DROP TABLE:\n" +
                "  Exclui uma tabela inteira. Ex: DROP TABLE nome_tabela;";

        // Cria a Label de conteúdo
        Label contentLabel = new Label(glossarioTexto);
        contentLabel.setWrapText(true); // Garante quebra de linha

        // Define uma largura preferencial para o texto quebrar e não ficar em uma linha só,
        // garantindo que sizeToScene não crie uma janela extremamente larga.
        contentLabel.setPrefWidth(400);

        // Estilização: Apenas o título "Comandos Suportados" em negrito
        contentLabel.setStyle("-fx-padding: 15px; -fx-font-size: 14px; -fx-text-fill: #333333;");
        contentLabel.setFont(Font.font("System", 14)); // Define fonte padrão

        // Cria o botão de fechar
        Button closeButton = new Button("Fechar");
        closeButton.setOnAction(e -> ((Stage) closeButton.getScene().getWindow()).close());
        closeButton.setPrefWidth(100);

        // Layout da janela
        VBox root = new VBox(15, contentLabel, closeButton);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #ffffff;");

        // Configuração da Janela (Stage)
        Stage stage = new Stage();
        stage.setTitle("Glossário de Comandos SQL");

        // DEFINE SCENE SEM TAMANHO FIXO (O TAMANHO SERÁ DETERMINADO PELO CONTEÚDO)
        stage.setScene(new Scene(root));

        stage.initModality(Modality.APPLICATION_MODAL); // Bloqueia a janela principal
        stage.setResizable(false);

        // AJUSTA O TAMANHO DA JANELA PARA O TAMANHO IDEAL DO CONTEÚDO
        stage.sizeToScene();

        stage.showAndWait();
    }

    private void exibirResultado(Tabela tabela) throws Exception {
        if (tabela == null || tabela.getNumColunas() == 0) {
            return;
        }

        // 1. Cria as Colunas da TableView, usando loops for clássicos
        Coluna[] colunasArray = tabela.getColunasArray();

        for (int i = 0; i < tabela.getNumColunas(); i++) {
            Coluna coluna = colunasArray[i];

            // É crucial usar 'final' para que o índice possa ser usado no lambda
            final int indiceColuna = i;

            TableColumn<Linha, Object> tableColumn = new TableColumn<>(coluna.getNome());

            // Define como cada célula da coluna obterá seu valor, AGORA USANDO O ÍNDICE
            tableColumn.setCellValueFactory(cellData -> {
                Linha linha = cellData.getValue();
                Object valor = linha.getDadoPorIndice(indiceColuna);
                return new SimpleObjectProperty<>(valor != null ? valor : "NULL");
            });

            resultadoTableView.getColumns().add(tableColumn);
        }

        // 2. Adiciona os dados (linhas) à TableView
        // O JavaFX exige uma ObservableList, então copiamos o array clássico para uma
        ObservableList<Linha> dadosObservaveis = FXCollections.observableArrayList();
        Linha[] linhasArray = tabela.getLinhasArray();

        // Loop for clássico para copiar o array para a ObservableList
        for (int i = 0; i < tabela.getNumLinhas(); i++) {
            dadosObservaveis.add(linhasArray[i]);
        }

        resultadoTableView.setItems(dadosObservaveis);
    }

    private void limparTabelaResultado() {
        resultadoTableView.getColumns().clear();
        resultadoTableView.getItems().clear();
    }

    private void atualizarStatus(String mensagem, boolean isError) {
        statusLabel.setText("Status: " + mensagem);
        // Usa as cores da paleta CSS: Vermelho para Erro, Verde Escuro para Sucesso
        if (isError) {
            statusLabel.setTextFill(Color.RED);
        } else {
            statusLabel.setTextFill(Color.web("#008000")); // Verde escuro
        }
    }
}
