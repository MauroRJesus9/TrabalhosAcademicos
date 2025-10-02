package pt.isec.pdrestapi.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.Calendar;

import static pt.isec.pdrestapi.Application.bd_url;

@RestController
@RequestMapping("espetaculos") //URI intermedia apenas para este controller, ou seja sempre necessario /lorem/ para aceder
public class EspetaculosController {
    private Connection dbConn;

    @GetMapping
    public ResponseEntity getText(@RequestParam(value="data_inicio", required=false) String data_inicio,@RequestParam(value="data_fim", required=false) String data_fim)
    {
        String dados = "Espetaculos a ocorrer nos futuros dias:\n";
        //data: YYYY-MM-DD
        try{
            dbConn = DriverManager.getConnection(bd_url);
            Statement statement = dbConn.createStatement();
            ResultSet resultSet = statement.executeQuery(getSQLQuery(data_inicio,data_fim));
            while (resultSet.next()) //Verifica se ainda tens linhas
            {
                dados += "Nome: " + resultSet.getString("descricao");
                dados += " Tipo: " + resultSet.getString("tipo");
                dados += " Data/Hora: " + resultSet.getString("data_hora");
                dados += " Duracao: " + resultSet.getString("duracao");
                dados += " Local: " + resultSet.getString("local");
                dados += " Localidade: " + resultSet.getString("localidade");
                dados += " País: " + resultSet.getString("pais");
                dados += " Classificação Etária: " + resultSet.getString("classificacao_etaria") + "\n";
            }
            statement.close();
            resultSet.close();
            dbConn.close();
        }catch (SQLException e){
            e.printStackTrace();
        }

        if(dados.equals("Espetaculos a ocorrer nos futuros dias:\n")){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Não foram encontrados espetáculos."); //CODIGO 501 : Type desco.
        }

        return ResponseEntity.ok(dados); //CODIGO ok : 200;
    }

    private String getSQLQuery(String data_inicio, String data_fim){
        String sqlQuery;
        Calendar cal = Calendar.getInstance();
        String data_24hr = null;
        if(cal.get(Calendar.DAY_OF_MONTH) <= 9){
            data_24hr = cal.get(Calendar.YEAR) +"-"+(cal.get(Calendar.MONTH)+1)+"-0"+ cal.get(Calendar.DAY_OF_MONTH); //dd-MM-YYYY
        }else{
            data_24hr = cal.get(Calendar.YEAR) +"-"+(cal.get(Calendar.MONTH)+1)+"-"+ cal.get(Calendar.DAY_OF_MONTH); //dd-MM-YYYY
        }
        if(data_inicio != null & data_fim != null){
            sqlQuery = "SELECT descricao,tipo,data_hora,duracao,local,localidade,pais,classificacao_etaria from espetaculo " +
                    "WHERE data_hora > '" + data_inicio + "' AND data_hora < '" + data_fim + "' AND visivel = 1";
        }else if(data_inicio != null){
            sqlQuery = "SELECT descricao,tipo,data_hora,duracao,local,localidade,pais,classificacao_etaria from espetaculo " +
                    "WHERE data_hora > '" + data_inicio + "' AND visivel = 1";
        }else if(data_fim != null){
            sqlQuery = "SELECT descricao,tipo,data_hora,duracao,local,localidade,pais,classificacao_etaria from espetaculo " +
                    "WHERE data_hora > '" + data_24hr + "' AND data_hora < '" + data_fim + "' AND visivel = 1";
        }else{
            sqlQuery = "SELECT descricao,tipo,data_hora,duracao,local,localidade,pais,classificacao_etaria from espetaculo " +
                    "WHERE data_hora > '" + data_24hr + "' AND visivel = 1";
        }
        return sqlQuery;
    }
}
