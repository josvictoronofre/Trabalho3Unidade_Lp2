package controle;

import dados.EstoqueRepository;
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
import java.util.List;

public class SupermercadoController {
    private ArrayList<Produto> estoqueOriginal = new ArrayList<>();
    private CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
    
    // Instanciando o gerenciador de dados (back-end)
    private EstoqueRepository estoqueRepo = new EstoqueRepository();
    
    // NOVA LISTA: Guarda os carrinhos (objetos) reais vendidos para fins de exportação detalhada
    private List<List<Produto>> historicoCarrinhosObjetos = new ArrayList<>();
    
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

        // 1. Abate a quantidade na memória
        for (Produto itemCarrinho : carrinho.getItens()) {
            for (Produto itemEstoque : estoqueOriginal) {
                if (itemEstoque.getNome().equals(itemCarrinho.getNome())) {
                    int novaQuantidade = itemEstoque.getQuantidadeEstoque() - 1; 
                    itemEstoque.setQuantidadeEstoque(Math.max(0, novaQuantidade)); 
                }
            }
        }

        // 2. Chama o back-end para gravar as alterações no arquivo físico
        estoqueRepo.salvarEstoque(estoqueOriginal);

        // 3. SALVA A "FOTO" DO CARRINHO ATUAL NO HISTÓRICO DE BASTIDORES
        List<Produto> copiaItensDoPedido = new ArrayList<>(carrinho.getItens());
        historicoCarrinhosObjetos.add(copiaItensDoPedido);

        // 4. Atualiza o histórico visual da tela
        double total = carrinho.calcularTotal();
        lvHistorico.getItems().add("Pedido #" + historicoCarrinhosObjetos.size() + " - Total: R$ " + String.format("%.2f", total));
        
        // 5. Limpa e atualiza
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
                atualizarTela();
                mostrarAlerta("Sucesso", "Estoque importado via CSV!");
            } catch (Exception e) {
                mostrarAlerta("Erro", "Falha ao ler o arquivo CSV.");
            }
        }
    }

    public void exportarPedidos(Stage stagePrincipal) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório de Pedidos Detalhado");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos CSV (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("relatorio_pedidos_detalhado.csv");
        
        File arquivo = fileChooser.showSaveDialog(stagePrincipal);
        
        if (arquivo != null) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(arquivo))) {
                
                // 1. Escreve o cabeçalho
                bw.write("ID_Pedido,Item_Nome,Preco_Unitario,Quantidade,Subtotal_Item,Total_Pedido");
                bw.newLine();
                
                int idPedido = 1;
                
                // 2. Itera sobre a lista de listas de produtos salvos nos bastidores
                for (List<Produto> produtosDoPedido : historicoCarrinhosObjetos) {
                    
                    // Calcula o total somando os preços dos produtos contidos nesta venda
                    double totalDoPedido = 0;
                    for (Produto p : produtosDoPedido) {
                        totalDoPedido += p.getPreco();
                    }
                    
                    boolean primeiraLinhaDoPedido = true;
                    
                    // 3. Detalha item por item no arquivo
                    for (Produto prod : produtosDoPedido) {
                        String nome = prod.getNome();
                        double preco = prod.getPreco();
                        int qtd = 1; 
                        double subtotal = preco * qtd;
                        
                        // O total geral do pedido só aparece fixado ao lado da primeira linha dele
                        String totalString = primeiraLinhaDoPedido ? String.format("%.2f", totalDoPedido) : "";
                        
                        String linha = String.format("%d,%s,%.2f,%d,%.2f,%s", 
                                idPedido, 
                                nome, 
                                preco, 
                                qtd, 
                                subtotal, 
                                totalString
                        );
                        
                        bw.write(linha);
                        bw.newLine();
                        
                        primeiraLinhaDoPedido = false;
                    }
                    
                    // Linha divisória estética entre as notas fiscais
                    bw.write("---,---,---,---,---,---");
                    bw.newLine();
                    
                    idPedido++;
                }
                
                mostrarAlerta("Sucesso", "Relatório de pedidos exportado com detalhes com sucesso!");
                
            } catch (IOException ex) {
                System.out.println("Erro ao exportar pedidos: " + ex.getMessage());
                mostrarAlerta("Erro", "Não foi possível exportar os pedidos.");
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