package modelo;

public abstract class Produto {
    private int id;
    private String nome;
    private double preco;
    private int quantidadeEstoque; // Atributo encapsulado

    public Produto(int id, String nome, double preco, int quantidadeEstoque) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
    }

    // Getters e Setters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public double getPreco() { return preco; }
    
    public int getQuantidadeEstoque() { return quantidadeEstoque; }
    
    public void setQuantidadeEstoque(int quantidadeEstoque) {
        if (quantidadeEstoque >= 0) {
            this.quantidadeEstoque = quantidadeEstoque;
        }
    }

    public boolean reduzirEstoque() {
        if (this.quantidadeEstoque > 0) {
            this.quantidadeEstoque--;
            return true;
        }
        return false;
    }
}