package modelo;

public class Usuario {
    private String login;
    private String senha;
    private String cargo;

    public Usuario(String login, String senha, String cargo) {
        this.login = login;
        this.senha = senha;
        this.cargo = cargo.toUpperCase(); // Garante que fique em maiúsculo para evitar erros de digitação
    }

    // Getters e Setters
    public String getLogin() { 
        return login; 
    }
    
    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() { 
        return senha; 
    }
    
    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getCargo() { 
        return cargo; 
    }
    
    public void setCargo(String cargo) {
        this.cargo = cargo.toUpperCase();
    }
    
    // Método utilitário para facilitar o controle de acesso no sistema
    public boolean ehGerente() {
        return "GERENTE".equals(this.cargo);
    }
}