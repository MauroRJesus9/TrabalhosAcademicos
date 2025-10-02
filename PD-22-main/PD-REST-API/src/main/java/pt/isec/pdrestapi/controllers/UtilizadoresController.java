package pt.isec.pdrestapi.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.isec.pdrestapi.models.UserConfig;

import java.sql.*;

import static pt.isec.pdrestapi.Application.bd_url;
import static pt.isec.pdrestapi.security.UserAuthenticationProvider.admin;

@RestController
public class UtilizadoresController {
    private Connection dbConn;

    @GetMapping("/lstutilizadores") //GET + URI (lorem/word?length=4) (type = word) / PATH variable = {word}
    public ResponseEntity getUtilizadoresRegistados(@AuthenticationPrincipal Jwt principal)
    {
        if(principal.getClaim("scope").equals("ADMIN")){
            String dados ="Utilizadores registados:\n";
            try{
                dbConn = DriverManager.getConnection(bd_url);
                Statement statement = dbConn.createStatement();
                String sqlQuery = "SELECT username,nome,administrador FROM utilizador";
                ResultSet resultSet = statement.executeQuery(sqlQuery);
                while(resultSet.next()){//Verifica se ainda tem linhas
                    dados += "Username: " + resultSet.getString("username") + " |";
                    dados += " Nome: " + resultSet.getString("nome");
                    if(resultSet.getInt("administrador") == 1){
                        dados += " | Admin: Sim\n";
                    }else{
                        dados += " | Admin: Não\n";
                    }
                }
                resultSet.close();
                statement.close();
                dbConn.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
            return ResponseEntity.ok(dados);
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Necessita permissão de Admin"); //CODIGO 401
        }
    }
    @PostMapping("/rgstutilizador")
    public ResponseEntity registaUtilizador(@RequestBody UserConfig config,@AuthenticationPrincipal Jwt principal)
    {
        boolean user_existe = false;
        if(principal.getClaim("scope").equals("ADMIN")) {
            if (config.getUsername() == null || config.getNome() == null || config.getPassword() == null) {
                return ResponseEntity.badRequest().body("Obrigatório colocar o username, o nome e a password. Verifique e tente novamente."); //error400 - e o body da resposta de erro
            }
            try{
                dbConn = DriverManager.getConnection(bd_url);
                Statement statement = dbConn.createStatement();
                String sqlQuery = "SELECT * FROM utilizador WHERE LOWER(username) = LOWER('" + config.getUsername() + "') OR  LOWER(nome) = LOWER('" + config.getNome() + "')";
                //Apenas se os dados de login coincidirem e o utilizador não estiver logado em outro servidor
                ResultSet resultSet = statement.executeQuery(sqlQuery);
                while (resultSet.next()) //Verifica se ainda tens linhas
                {
                    user_existe = true;
                }
                statement.close();
                resultSet.close();
                dbConn.close();
                if (user_existe) {
                    return ResponseEntity.badRequest().body("O utilizador já está registado."); //error400 - e o body da resposta de erro
                }
                sqlQuery = "INSERT INTO utilizador VALUES (NULL,'" + config.getUsername() + "','" + config.getNome() + "','" + config.getPassword() +"','" + 0 + "','" + 1 + "')";
                dbConn = DriverManager.getConnection(bd_url);
                statement = dbConn.createStatement();
                statement.executeUpdate(sqlQuery);
                statement.close();
                dbConn.close();
                return ResponseEntity.ok("Utilizador registado com sucesso.");
            }catch (SQLException e){
                e.printStackTrace();
                //return de erro
                return ResponseEntity.badRequest().body("Erro ao criar o utilizador. Verifique os dados e tente novamente.");
            }
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Necessita permissão de Admin"); //CODIGO 401
        }
    }

    @PostMapping("/delutilizador")
    public ResponseEntity eliminaUtilizador(@RequestBody UserConfig config,@AuthenticationPrincipal Jwt principal)
    {
        boolean user_existe = false;
        if(principal.getClaim("scope").equals("ADMIN")) {
            if (config.getUsername() == null) {
                return ResponseEntity.badRequest().body("Obrigatório colocar o username para eliminar o utilizador. Verifique e tente novamente."); //error400 - e o body da resposta de erro
            }
            try {
                dbConn = DriverManager.getConnection(bd_url);
                Statement statement = dbConn.createStatement();
                String sqlQuery = "SELECT * FROM utilizador WHERE LOWER(username) = LOWER('" + config.getUsername() + "') ";
                //Apenas se os dados de login coincidirem e o utilizador não estiver logado em outro servidor
                ResultSet resultSet = statement.executeQuery(sqlQuery);
                while (resultSet.next()) //Verifica se ainda tens linhas
                {
                    user_existe = true;
                }
                statement.close();
                resultSet.close();
                dbConn.close();
                if(user_existe){
                    dbConn = DriverManager.getConnection(bd_url);
                    sqlQuery = "DELETE FROM utilizador WHERE LOWER(username) = LOWER('" + config.getUsername() + "')  ";
                    statement = dbConn.createStatement();
                    statement.executeUpdate(sqlQuery);
                    statement.close();
                    dbConn.close();
                    return ResponseEntity.ok("Utilizador eliminado com sucesso.");
                }else{
                    return ResponseEntity.badRequest().body("Utilizador não encontrado na base de dados.Verifique os dados e tente novamente.");
                }
            }catch (SQLException e){
                e.printStackTrace();
                return ResponseEntity.badRequest().body("Erro ao eliminar o utilizador. Verifique os dados e tente novamente.");
            }
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Necessita permissão de Admin"); //CODIGO 401
        }
    }
}
