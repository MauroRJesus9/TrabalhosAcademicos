package pt.isec.pdrestapi.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.sql.*;

import static pt.isec.pdrestapi.Application.bd_url;

@RestController
public class ReservasController {
    private Connection dbConn;

    @GetMapping("/reservaspagas") //GET + URI (lorem/word?length=4) (type = word) / PATH variable = {word}
    public ResponseEntity getReservasPagas(Principal principal)
    {
        String dados = "Reservas pagas:\n";
        String sqlQuery = "SELECT espetaculo.descricao,espetaculo.data_hora from reserva,espetaculo " +
                "WHERE reserva.id_espetaculo = espetaculo.id " +
                "AND id_utilizador='" + getIdUser(principal) + "' AND pago=1";
        try{
            dbConn = DriverManager.getConnection(bd_url);
            Statement statement = dbConn.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            while(resultSet.next()){//Verifica se ainda tem linhas
                dados += "Nome: " + resultSet.getString("descricao");
                dados += " Data/Hora: " + resultSet.getString("data_hora") + "\n";
            }
            resultSet.close();
            statement.close();
            dbConn.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
        if(dados.equals("Reservas pagas:\n")){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sem reservas pagas para serem mostradas."); //CODIGO 501 : Type desco.
        }

        return ResponseEntity.ok(dados); //CODIGO ok : 200;
    }

    @GetMapping("/reservasaguardampag") //GET + URI (lorem/word?length=4) (type = word) / PATH variable = {word}
    public ResponseEntity getReservasAguardamPagamento(Principal principal)
    {
        String dados = "Reservas que aguardam pagamento:\n";
        String sqlQuery = "SELECT espetaculo.descricao,espetaculo.data_hora from reserva,espetaculo " +
                "WHERE reserva.id_espetaculo = espetaculo.id " +
                "AND id_utilizador ='" + getIdUser(principal) + "' AND pago=0";
        try{
            dbConn = DriverManager.getConnection(bd_url);
            Statement statement = dbConn.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            while(resultSet.next()){//Verifica se ainda tem linhas
                dados += "Nome: " + resultSet.getString("descricao");
                dados += " Data/Hora: " + resultSet.getString("data_hora") + "\n";
            }
            resultSet.close();
            statement.close();
            dbConn.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
        if(dados.equals("Reservas que aguardam pagamento:\n")){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sem reservas que aguardam pagamento para serem mostradas." + principal.getName()); //CODIGO 501 : Type desco.
        }

        return ResponseEntity.ok(dados); //CODIGO ok : 200;
    }

    public int getIdUser(Principal principal){
        int id_user = 0;
        try {
            dbConn = DriverManager.getConnection(bd_url);
            Statement statement = dbConn.createStatement();
            String sqlQuery = "SELECT id FROM utilizador WHERE username LIKE '" +  principal.getName() + "'";
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            while (resultSet.next()) {//Verifica se ainda tem linhas
                id_user = resultSet.getInt("id");
            }
            resultSet.close();
            statement.close();
            dbConn.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return id_user;
    }
}
