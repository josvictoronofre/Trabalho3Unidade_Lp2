package dados;

import modelo.Usuario;
import java.io.*;
import java.util.ArrayList;

public class UsuarioRepository {
    private final String CAMINHO_ARQUIVO = "usuarios.csv";

    public Usuario autenticar(String login, String senha) {
        File arquivo = new File(CAMINHO_ARQUIVO);
        
        // Mock/Usuário padrão caso o arquivo não exista
        if (!arquivo.exists()) {
            if (login.equals("admin") && senha.equals("1234")) {
                return new Usuario("admin", "1234", "GERENTE");
            }
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            br.readLine(); // Pula cabeçalho (Login,Senha,Cargo)

            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;

                String[] colunas = linha.split(",");
                if (colunas.length >= 3) {
                    String loginSalvo = colunas[0].trim();
                    String senhaSalva = colunas[1].trim();
                    String cargoSalvo = colunas[2].trim();

                    // Se bater as credenciais, criamos o objeto Usuario e o retornamos
                    if (loginSalvo.equals(login) && senhaSalva.equals(senha)) {
                        return new Usuario(loginSalvo, senhaSalva, cargoSalvo);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao ler usuários: " + e.getMessage());
        }
        return null; // Retorna null se não encontrar ninguém
    }
}