package modelo;

import java.util.ArrayList;

public class CarrinhoDeCompras {
    private ArrayList<Produto> itens;

    public CarrinhoDeCompras() {
        this.itens = new ArrayList<>();
    }

    public void adicionar(Produto p) {
        this.itens.add(p);
    }

    public ArrayList<Produto> getItens() { 
        return itens; 
    }

    public double calcularTotal() {
        double total = 0;
        for (Produto p : itens) {
            total += p.getPreco();
        }
        return total;
    }

    public void limpar() {
        this.itens.clear();
    }
}