package modelo;

public class ProdutoAlimenticio extends Produto {
    // Atributo específico da classe filha
    private String dataValidade; 

    public ProdutoAlimenticio(int id, String nome, double preco, int quantidadeEstoque, String dataValidade) {
        super(id, nome, preco, quantidadeEstoque); // Chama o construtor do pai
        this.dataValidade = dataValidade;
    }

    public String getDataValidade() { return dataValidade; }
}