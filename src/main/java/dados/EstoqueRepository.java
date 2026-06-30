package dados;

import modelo.Produto;
import modelo.ProdutoAlimenticio;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class EstoqueRepository {
    private final String CAMINHO_ARQUIVO = "estoque.csv";

    // Método responsável por ler o CSV e devolver a lista de produtos carregada
    public ArrayList<Produto> carregarEstoque() {
        ArrayList<Produto> produtos = new ArrayList<>();
        File arquivo = new File(CAMINHO_ARQUIVO);
        
        if (!arquivo.exists()) {
            System.out.println("Arquivo " + CAMINHO_ARQUIVO + " não encontrado. Iniciando vazio.");
            return produtos;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            br.readLine(); // Pula o cabeçalho (Nome,Preco,Quantidade)

            int id = 1;
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;

                String[] colunas = linha.split(",");
                if (colunas.length >= 3) {
                    String nome = colunas[0].trim();
                    double preco = Double.parseDouble(colunas[1].trim());
                    int qtd = Integer.parseInt(colunas[2].trim());
                    
                    produtos.add(new ProdutoAlimenticio(id++, nome, preco, qtd, "N/A"));
                }
            }
            System.out.println("Estoque carregado do disco com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao carregar o estoque: " + e.getMessage());
        }
        return  produtos;
    }

    // Método responsável por receber a lista atual e sobrescrever o CSV no disco
    public void salvarEstoque(List<Produto> estoque) {
        File arquivo = new File(CAMINHO_ARQUIVO);
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(arquivo))) {
            bw.write("Nome,Preco,Quantidade");
            bw.newLine();
            
            for (Produto p : estoque) {
                String linha = p.getNome() + "," + p.getPreco() + "," + p.getQuantidadeEstoque();
                bw.write(linha);
                bw.newLine();
            }
            System.out.println("Arquivo " + CAMINHO_ARQUIVO + " sincronizado no disco!");
        } catch (Exception e) {
            System.out.println("Erro ao salvar o estoque: " + e.getMessage());
        }
    }
}