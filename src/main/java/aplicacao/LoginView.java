package aplicacao;

import dados.UsuarioRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modelo.Usuario;

public class LoginView {
    // Instância do repositório para buscar os dados no CSV
    private UsuarioRepository usuarioRepo = new UsuarioRepository();

    public void exibir(Stage stagePrincipal) {
        stagePrincipal.setTitle("Login - Sistema Supermercado");

        // ==========================================
        // 1. COMPONENTES VISUAIS DA TELA
        // ==========================================
        Label lblTitulo = new Label("🔐 Acesso ao Sistema");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextField txtUsuario = new TextField();
        txtUsuario.setPromptText("Digite seu Usuário");
        txtUsuario.setMaxWidth(220);
        txtUsuario.setStyle("-fx-padding: 6;");

        // PasswordField oculta os caracteres digitados automaticamente (••••)
        PasswordField txtSenha = new PasswordField();
        txtSenha.setPromptText("Digite sua Senha");
        txtSenha.setMaxWidth(220);
        txtSenha.setStyle("-fx-padding: 6;");

        Button btnEntrar = new Button("Entrar");
        btnEntrar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        btnEntrar.setPrefWidth(220);

        // Botão para encerrar a aplicação direto da tela de login
        Button btnSairApp = new Button("❌ Sair do Programa");
        btnSairApp.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        btnSairApp.setPrefWidth(220);

        // Feedback visual para mensagens de erro
        Label lblErro = new Label();
        lblErro.setStyle("-fx-text-fill: red; -fx-font-size: 12px; -fx-font-weight: bold;");

        // ==========================================
        // 2. LÓGICA DE EVENTOS (BOTÕES)
        // ==========================================
        
        // Ação do Botão Entrar
        btnEntrar.setOnAction(e -> {
            String loginDigitado = txtUsuario.getText().trim();
            String senhaDigitada = txtSenha.getText().trim();

            // Validação de segurança inicial para campos vazios
            if (loginDigitado.isEmpty() || senhaDigitada.isEmpty()) {
                lblErro.setText("Por favor, preencha todos os campos!");
                return;
            }

            // Consulta o "back-end" para validar as credenciais passadas
            Usuario usuarioLogado = usuarioRepo.autenticar(loginDigitado, senhaDigitada);

            if (usuarioLogado != null) {
                // Login com sucesso: redireciona para a tela do supermercado
                Main telaPrincipal = new Main();
                telaPrincipal.abrirJanelaPrincipal(stagePrincipal, usuarioLogado); 
            } else {
                // Credenciais inválidas: limpa o campo de senha e avisa o usuário
                lblErro.setText("Usuário ou senha incorretos!");
                txtSenha.clear();
            }
        });

        // Ação do Botão Sair
        btnSairApp.setOnAction(e -> {
            System.exit(0); // Encerra o processo Java imediatamente
        });

        // ==========================================
        // 3. CONSTRUÇÃO DO LAYOUT E CENÁRIO
        // ==========================================
        // VBox empilha os elementos na vertical com espaçamento de 15px entre eles
        VBox layout = new VBox(15, lblTitulo, txtUsuario, txtSenha, btnEntrar, btnSairApp, lblErro);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        // Criação da cena com dimensões ajustadas para não cortar elementos
        Scene cenaLogin = new Scene(layout, 350, 340);
        stagePrincipal.setScene(cenaLogin);
        
        // CENTRALIZAÇÃO: Calcula e posiciona a janela no meio do monitor do usuário
        stagePrincipal.centerOnScreen();
        
        stagePrincipal.show();
    }
}