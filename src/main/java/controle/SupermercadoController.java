package controle;

import dados.EstoqueRepository; // Importando o nosso novo back-end
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
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
    
    // Instanciando o gerenciador de dados (back-end)
    private EstoqueRepository estoqueRepo = new EstoqueRepository();
    
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

    public void carregarEstoqueAutomatico() {
        // O repositório faz o trabalho pesado de ler o arquivo e nos entrega a lista pronta
        this.estoqueOriginal = estoqueRepo.carregarEstoque();
        atualizarTela();
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

    public void finalizarCompra() {
        if (carrinho.getItens().isEmpty()) {
            mostrarAlerta("Aviso", "Seu carrinho está vazio!");
            return;
        }

        // 1. Abate a quantidade na memória (Atenção: sua lógica original já reduzia 1 
        // no adicionarSelecionado(), repita o abate aqui apenas se for necessário)
        for (Produto itemCarrinho : carrinho.getItens()) {
            for (Produto itemEstoque : estoqueOriginal) {
                if (itemEstoque.getNome().equals(itemCarrinho.getNome())) {
                    // Como você já abateu no adicionarSelecionado(), o código abaixo pode ser redundante, 
                    // mas mantive conforme você enviou para preservar sua regra de negócio:
                    int novaQuantidade = itemEstoque.getQuantidadeEstoque() - 1; 
                    itemEstoque.setQuantidadeEstoque(Math.max(0, novaQuantidade)); 
                }
            }
        }

        // 2. Chama o back-end para gravar as alterações no arquivo físico
        estoqueRepo.salvarEstoque(estoqueOriginal);

        // 3. Atualiza o histórico visual
        double total = carrinho.calcularTotal();
        lvHistorico.getItems().add("Pedido: R$ " + String.format("%.2f", total));
        
        carrinho.limpar();
        atualizarTela();
        mostrarAlerta("Sucesso", "Compra finalizada e estoque atualizado!");
    }

    // O método importarCSV usa FileChooser (elemento visual do front), então ele fica aqui,
    // mas note que após processar o arquivo selecionado, poderíamos usar o estoqueRepo para salvar se quiséssemos.
    public void importarCSV(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos CSV", "*.csv"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String linha;
                estoqueOriginal.clear();
                br.readLine(); 

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
                // Opcional: estoqueRepo.salvarEstoque(estoqueOriginal); // Para fixar esse novo estoque como o padrão
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

    public ListView<String> getLvVitrine() { return lvVitrine; }
    public ListView<String> getLvCarrinho() { return lvCarrinho; }
    public ListView<String> getLvHistorico() { return lvHistorico; }
    public Label getLblTotal() { return lblTotal; }
}