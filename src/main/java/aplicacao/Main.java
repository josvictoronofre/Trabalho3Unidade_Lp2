package aplicacao;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        SupermercadoController controller = new SupermercadoController();

        primaryStage.setTitle("Simulador de Supermercado em JavaFX");

        // ==========================================
        // 1. CONFIGURAÇÃO DE TAMANHO DAS LISTAS
        // ==========================================
        controller.getLvVitrine().setPrefHeight(350);
        controller.getLvCarrinho().setPrefHeight(350);
        controller.getLvHistorico().setPrefHeight(120); // Histórico inferior um pouco menor

        // ==========================================
        // 2. BOTÕES SUPERIORES (ARQUIVOS)
        // ==========================================
        Button btnImportar = new Button("📥 Importar Estoque (CSV)");
        btnImportar.setOnAction(e -> controller.importarCSV(primaryStage));

        Button btnExportar = new Button("📤 Exportar Pedidos (CSV)");
        btnExportar.setOnAction(e -> controller.exportarPedidos(primaryStage));

        HBox painelArquivo = new HBox(15, btnImportar, btnExportar);
        painelArquivo.setPadding(new Insets(15));

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

        // Grid Organizador Lateral
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

        // Precisei aumentar manualmente o tamanho da janela
        Scene scene = new Scene(root, 800, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}