package aplicacao;

import controle.SupermercadoController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import modelo.Usuario;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // O programa sempre inicia direcionando para a tela de login
        LoginView login = new LoginView();
        login.exibir(primaryStage);
        
        // Garante que a tela de login nasça centralizada na tela
        primaryStage.centerOnScreen();
    }

    // Este método é invocado pela LoginView após a validação bem-sucedida do usuário
    public void abrirJanelaPrincipal(Stage primaryStage, Usuario usuarioLogado) {
        SupermercadoController controller = new SupermercadoController();

        primaryStage.setTitle("Simulador de Supermercado em JavaFX");

        // ==========================================
        // 1. CONFIGURAÇÃO DE TAMANHO DAS LISTAS
        // ==========================================
        controller.getLvVitrine().setPrefHeight(350);
        controller.getLvCarrinho().setPrefHeight(350);
        controller.getLvHistorico().setPrefHeight(120);

        // ==========================================
        // 2. BOTÕES SUPERIORES (ARQUIVOS, LOGOUT E FECHAR)
        // ==========================================
        Button btnImportar = new Button("📥 Importar Estoque (CSV)");
        btnImportar.setOnAction(e -> controller.importarCSV(primaryStage));

        // RESTRIÇÃO DE ACESSO: Desativa o botão se o usuário não for gerente
        if (!usuarioLogado.ehGerente()) {
            btnImportar.setDisable(true);
            btnImportar.setText("📥 Importar (Apenas Gerentes)");
        }

        Button btnExportar = new Button("📤 Exportar Pedidos (CSV)");
        btnExportar.setOnAction(e -> controller.exportarPedidos(primaryStage));

        // Rótulo para exibir as credenciais do operador atual
        Label lblUsuario = new Label("👤 Usuário: " + usuarioLogado.getLogin() + " (" + usuarioLogado.getCargo() + ")");
        lblUsuario.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d;");

        // BOTÃO DE LOGOUT: Instancia uma nova tela de login e redefine o cenário da janela
        Button btnLogout = new Button("🚪 Log Out");
        btnLogout.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: bold;");
        btnLogout.setOnAction(e -> {
            LoginView login = new LoginView();
            login.exibir(primaryStage);
        });

        // NOVO BOTÃO: Botão específico para fechar o aplicativo direto da Main
        Button btnFecharApp = new Button("❌ Fechar Sistema");
        btnFecharApp.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnFecharApp.setOnAction(e -> {
            System.exit(0); // Encerra a aplicação imediatamente
        });

        // Componente invisível usado para empurrar o bloco do usuário/controles para o canto direito
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Montagem do painel superior com o botão de logout e o botão de fechar incluídos
        HBox painelArquivo = new HBox(15, btnImportar, btnExportar, spacer, lblUsuario, btnLogout, btnFecharApp);
        painelArquivo.setPadding(new Insets(15));
        painelArquivo.setStyle("-fx-alignment: center-left;");

        // ==========================================
        // 3. PAINEL CENTRAL (VITRINE E CARRINHO)
        // ==========================================
        // Vitrine (Esquerda)
        VBox boxVitrine = new VBox(8, new Label("Produtos Disponíveis:"), controller.getLvVitrine());
        Button btnAdicionar = new Button("➕ Adicionar ao Carrinho");
        btnAdicionar.setMaxWidth(Double.MAX_VALUE);
        btnAdicionar.setStyle("-fx-font-weight: bold; -fx-padding: 8;");
        btnAdicionar.setOnAction(e -> controller.adicionarSelecionado());
        boxVitrine.getChildren().add(btnAdicionar);

        // Carrinho (Direita)
        controller.getLblTotal().setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        VBox boxCarrinho = new VBox(8, new Label("Seu Carrinho:"), controller.getLvCarrinho(), controller.getLblTotal());
        Button btnFinalizar = new Button("✅ Finalizar Compra");
        btnFinalizar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        btnFinalizar.setMaxWidth(Double.MAX_VALUE);
        btnFinalizar.setOnAction(e -> controller.finalizarCompra());
        boxCarrinho.getChildren().add(btnFinalizar);

        // Grid Organizador das colunas centrais (50% de largura para cada)
        GridPane gridCentral = new GridPane();
        gridCentral.setHgap(20);
        gridCentral.setPadding(new Insets(0, 15, 15, 15));
        
        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(50);
        gridCentral.getColumnConstraints().addAll(col1, col2);
        
        gridCentral.add(boxVitrine, 0, 0);
        gridCentral.add(boxCarrinho, 1, 0);

        // ==========================================
        // 4. PAINEL INFERIOR (HISTÓRICO)
        // ==========================================
        VBox boxHistorico = new VBox(8, new Label("Pedidos Realizados nesta Sessão:"), controller.getLvHistorico());
        boxHistorico.setPadding(new Insets(0, 15, 15, 15));

        // ==========================================
        // 5. ESTRUTURAÇÃO DA JANELA PRINCIPAL
        // ==========================================
        BorderPane root = new BorderPane();
        root.setTop(painelArquivo);
        root.setCenter(gridCentral);
        root.setBottom(boxHistorico);

        // Define a cena principal com tamanho ajustado para acomodar a nova barra de ferramentas
        Scene scene = new Scene(root, 920, 650); // Aumentei um pouco a largura para caber o novo botão sem esmagar o layout
        primaryStage.setScene(scene);
        
        // CENTRALIZAÇÃO DO SUPERMERCADO: Força a janela principal a se alinhar de acordo com o novo tamanho
        primaryStage.centerOnScreen();
        
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}