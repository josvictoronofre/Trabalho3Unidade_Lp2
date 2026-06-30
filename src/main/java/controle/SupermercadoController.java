package controle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import modelo.CarrinhoDeCompras;
import modelo.Produto;
import modelo.ProdutoAlimenticio;

import java.io.*;
import java.util.ArrayList;

public class SupermercadoController {
    private ArrayList<Produto> estoqueOriginal = new ArrayList<>();
    private CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
    
    // Listas especiais do JavaFX para atualizar a tela automaticamente
    private ObservableList<String> itensVitrine = FXCollections.observableArrayList();
    private ObservableList<String> itensCarrinho = FXCollections.observableArrayList();
    private ObservableList<String> historicoPedidos = FXCollections.observableArrayList();

    private ListView<String> lvVitrine = new ListView<>(itensVitrine);
    private ListView<String> lvCarrinho = new ListView<>(itensCarrinho);
    private ListView<String> lvHistorico = new ListView<>(historicoPedidos);
    private Label lblTotal = new Label("Total: R$ 0.00");

    public SupermercadoController() {
        carregarEstoqueAutomatico();
        atualizarTela();
    }

 // Substitua o antigo inicializarEstoquePadrao por este:
    public void carregarEstoqueAutomatico() {
        // Procura o arquivo "estoque.csv" na pasta raiz do seu projeto
        File arquivo = new File("estoque.csv");
        
        if (!arquivo.exists()) {
            System.out.println("Arquivo estoque.csv não encontrado na raiz. Iniciando zerado.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            estoqueOriginal.clear();
            
            // Pula o cabeçalho (Nome,Preco,Quantidade)
            br.readLine(); 

            int id = 1;
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;

                String[] colunas = linha.split(",");
                if (colunas.length >= 3) {
                    String nome = colunas[0].trim();
                    double preco = Double.parseDouble(colunas[1].trim());
                    int qtd = Integer.parseInt(colunas[2].trim());
                    
                    estoqueOriginal.add(new ProdutoAlimenticio(id++, nome, preco, qtd, "N/A"));
                }
            }
            atualizarTela();
            System.out.println("Estoque carregado automaticamente via CSV com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao carregar o estoque automático: " + e.getMessage());
        }
    }

    public void adicionarSelecionado() {
        int index = lvVitrine.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            Produto p = estoqueOriginal.get(index);
            if (p.reduzirEstoque()) {
                carrinho.adicionar(p);
                atualizarTela();
            } else {
                mostrarAlerta("Aviso", "Produto esgotado!");
            }
        }
    }
    
    public void salvarEstoqueNoCSV() {
        File arquivo = new File("estoque.csv");
        
        // O BufferedWriter vai sobrescrever o arquivo antigo com os dados atualizados
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(arquivo))) {
            // Escreve o cabeçalho padrão
            bw.write("Nome,Preco,Quantidade");
            bw.newLine();
            
            // Percorre o estoque atual da memória e grava linha por linha
            for (Produto p : estoqueOriginal) {
                String linha = p.getNome() + "," + p.getPreco() + "," + p.getQuantidadeEstoque();
                bw.write(linha);
                bw.newLine();
            }
            System.out.println("Arquivo estoque.csv atualizado com sucesso no disco!");
        } catch (Exception e) {
            System.out.println("Erro ao salvar o estoque no arquivo: " + e.getMessage());
        }
    }

    public void finalizarCompra() {
        if (carrinho.getItens().isEmpty()) {
            mostrarAlerta("Aviso", "Seu carrinho está vazio!");
            return;
        }

        // 1. Abate a quantidade dos produtos no estoque da memória
        for (Produto itemCarrinho : carrinho.getItens()) {
            for (Produto itemEstoque : estoqueOriginal) {
                if (itemEstoque.getNome().equals(itemCarrinho.getNome())) {
                    // Diminui 1 unidade do estoque (ou a quantidade comprada)
                    int novaQuantidade = itemEstoque.getQuantidadeEstoque() - 1; 
                    itemEstoque.setQuantidadeEstoque(Math.max(0, novaQuantidade)); // Garante que não fica negativo
                }
            }
        }

        // 2. Grava as novas quantidades direto no arquivo estoque.csv
        salvarEstoqueNoCSV();

        // 3. Restante do seu fluxo normal (adicionar ao histórico, limpar carrinho, etc.)
        double total = carrinho.calcularTotal();
        lvHistorico.getItems().add("Pedido: R$ " + String.format("%.2f", total));
        
        carrinho.limpar();
        atualizarTela();
        mostrarAlerta("Sucesso", "Compra finalizada e estoque atualizado!");
    }

    public void importarCSV(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos CSV", "*.csv"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String linha;
                estoqueOriginal.clear();
                br.readLine(); // Pula o cabeçalho

                int id = 1;
                while ((linha = br.readLine()) != null) {
                    String[] colunas = linha.split(",");
                    if (colunas.length >= 3) {
                        String nome = colunas[0].trim();
                        double preco = Double.parseDouble(colunas[1].trim());
                        int qtd = Integer.parseInt(colunas[2].trim());
                        estoqueOriginal.add(new ProdutoAlimenticio(id++, nome, preco, qtd, "N/A"));
                    }
                }
                carrinho.limpar();
                atualizarTela();
                mostrarAlerta("Sucesso", "Estoque importado via CSV!");
            } catch (Exception e) {
                mostrarAlerta("Erro", "Falha ao ler o arquivo CSV.");
            }
        }
    }

    public void exportarPedidos(Stage stage) {
        if (historicoPedidos.isEmpty()) {
            mostrarAlerta("Aviso", "Nenhum pedido para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("relatorio_pedidos.csv");
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("Relatorio de Pedidos");
                for (String ped : historicoPedidos) {
                    pw.println(ped);
                }
                mostrarAlerta("Sucesso", "Pedidos exportados para CSV!");
            } catch (Exception e) {
                mostrarAlerta("Erro", "Falha ao salvar o arquivo.");
            }
        }
    }

    private void atualizarTela() {
        itensVitrine.clear();
        for (Produto p : estoqueOriginal) {
            itensVitrine.add(p.getNome() + " - R$ " + p.getPreco() + " (Estoque: " + p.getQuantidadeEstoque() + ")");
        }

        itensCarrinho.clear();
        for (Produto p : carrinho.getItens()) {
            itensCarrinho.add(p.getNome() + " - R$ " + p.getPreco());
        }

        lblTotal.setText("Total: R$ " + String.format("%.2f", carrinho.calcularTotal()));
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Getters para montar o layout na classe Main
    public ListView<String> getLvVitrine() { return lvVitrine; }
    public ListView<String> getLvCarrinho() { return lvCarrinho; }
    public ListView<String> getLvHistorico() { return lvHistorico; }
    public Label getLblTotal() { return lblTotal; }
}