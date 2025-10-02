package isec.tppd.server.threads;

import isec.tppd.server.Servidor;
import isec.tppd.server.classesaux.HeartBeat;
import isec.tppd.server.classesaux.ServerInfo;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;

public class ThreadPrivCliente extends Thread{ //Comunicação com o cliente
    private Socket cliSocket;
    private String bd_url;
    private HeartBeat heartBeat;
    private ServerInfo dadosServer;
    private Connection dbConn;
    private boolean autenticado;
    private boolean admin;
    private int id_user;
    private ArrayList<String> comandosSQL;
    private ThreadEnviaHeartBeat threadEnviaHeartBeat;
    private Servidor servidor;

    public ThreadPrivCliente(Socket cliSocket, String bd_url, HeartBeat heartBeat, ServerInfo dadosServer
            , ArrayList<String> comandosSQL, ThreadEnviaHeartBeat threadEnviaHeartBeat, Servidor servidor) throws SQLException{
        this.cliSocket = cliSocket;
        this.bd_url = bd_url;
        this.dadosServer = dadosServer;
        this.heartBeat = heartBeat;
        this.autenticado = false;
        this.admin = false;
        this.comandosSQL = comandosSQL;
        this.threadEnviaHeartBeat = threadEnviaHeartBeat;
        this.servidor = servidor;
    }

    @Override
    public void run(){
        synchronized (dadosServer){
            dadosServer.setQuant(dadosServer.getQuant() + 1); //Incrementa número de clientes ligados
        }
        synchronized (heartBeat){
            heartBeat.setTotal_ligacoes(dadosServer.getQuant()); //Atualiza valores também para serem enviados no heartbeat
        }
        threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            oos = new ObjectOutputStream(cliSocket.getOutputStream());
            ois = new ObjectInputStream(cliSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(true){
            try{
                String msg = (String) ois.readObject();
                //System.out.println("Comando Cliente: " + msg);
                if (!autenticado) {
                    if(msg != null){
                        char cod = msg.charAt(0);
                        oos.writeUnshared(verificaDadosIniciais(cod, msg));
                        oos.flush();
                        if(msg.equalsIgnoreCase("sair")){
                            System.out.println("O Cliente saiu a fechar a ligação...");
                            break;
                        }
                    }
                } else {
                    //System.out.println("Comando Cliente: " + msg);
                    if(msg != null){
                        oos.writeUnshared(verificaComandoCli(msg));
                        oos.flush();
                    }else{
                        oos.writeUnshared(null); //Caso não tenha informação a enviar ao cliente
                        oos.flush();
                    }
                }
            }catch (SocketTimeoutException e){
                servidor.sendUpdateCallbackRMI(" Conexão perdida com o cliente : IP: " + cliSocket.getInetAddress().toString() + " PORT TCP: " + cliSocket.getPort() + " USERNAME: " + getUsername());
                break;
            } catch (IOException | ClassNotFoundException e) {
                //throw new RuntimeException(e);
            }
        }
        synchronized (dadosServer){
            dadosServer.setQuant(dadosServer.getQuant() - 1); //Decrementa número de clientes ligados
        }
        synchronized (heartBeat){
            heartBeat.setTotal_ligacoes(dadosServer.getQuant()); //Atualiza valores também para serem enviados no heartbeat
        }
        threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
    }

    //Funções de pesquisa BD && verificacoes

    private String getUsername(){
        String username = null;
        try{
            dbConn = DriverManager.getConnection(bd_url);
            Statement statement = dbConn.createStatement();
            String sqlQuery = "SELECT username FROM utilizador WHERE id =" + id_user;
            //Apenas se os dados de login coincidirem e o utilizador não estiver logado em outro servidor
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            while (resultSet.next()) //Verifica se ainda tens linhas
            {
                username = resultSet.getString("username");
            }
            statement.close();
            resultSet.close();
            dbConn.close();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return username;
    }

    private String verificaDadosIniciais(char cod,String msg){
        if(cod == '1'){ //Pedido de autenticação
            if(pedidoAutenticacao(msg)){ //Escreve para o cliente que foi autenticado
                autenticado = true;
                if(admin){
                    return "Sucesso na autenticacao-Admin:"+id_user;
                }else{
                    return "Sucesso na autenticacao:"+id_user;
                }
            }else{
                return "Erro na autenticacao";
            }
        }else if(cod == '2'){ //Pedido de registo
            if(pedidoRegisto(msg)){ //Escreve para o cliente foi registado e por isso foi autenticado
                autenticado = true;
                return "Sucesso no registo:"+id_user;
            }else{
                return "Erro no registo";
            }
        }else if(msg.equalsIgnoreCase("sair")){
            return "Saida_confirmada";
        }
        return null;
    }

    private boolean pedidoAutenticacao(String comando){
        //pedir a base de dados se os dados q cliente meteu coincide
        boolean user_existe = false;
        boolean user_logged = false;
        comando = comando.substring(2); //Remove o 1-
        String[] dados = comando.split(":", 2); //username:password
        try {
            dbConn = DriverManager.getConnection(bd_url);
            Statement statement = dbConn.createStatement();
            String sqlQuery = "SELECT id,username,administrador FROM utilizador WHERE LOWER(username) = LOWER('" + dados[0] + "') AND password = '" + dados[1] + "'";
            //Apenas se os dados de login coincidirem e o utilizador não estiver logado em outro servidor
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            while (resultSet.next()) //Verifica se ainda tens linhas
            {
                user_existe = true;
                admin = (resultSet.getInt("administrador") == 1);
                id_user = resultSet.getInt("id");
                servidor.sendUpdateCallbackRMI("Novo Login : USERNAME: " + resultSet.getString("username"));
            }
            statement.close();
            resultSet.close();
            dbConn.close();

            if (user_existe) {
                String sqlQuery1 = "UPDATE utilizador SET autenticado=1 WHERE LOWER(username)=LOWER('" + dados[0] + "') AND password='" + dados[1] + "'";
                synchronized (heartBeat){
                    heartBeat.setStatus("Prepare");
                    heartBeat.setQuery_sql(sqlQuery1);
                }
                threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
                Thread.sleep(1500); //Espera 1 segundo pela confirmação
                synchronized (heartBeat){
                    if (heartBeat.getStatus().equals("Commit")) {
                        comandosSQL.add(sqlQuery1);
                        dbConn = DriverManager.getConnection(bd_url);
                        statement = dbConn.createStatement();
                        statement.executeUpdate(sqlQuery1);
                        statement.close();
                        dbConn.close();
                        heartBeat.setQuery_sql("null");
                        heartBeat.setPrepareConfirmed(false);
                        heartBeat.setStatus("null");
                        user_logged = true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return user_logged;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return user_logged;
    }

    public boolean pedidoRegisto(String comando) {
        boolean user_logged = false;
        boolean user_created = false;
        boolean user_existe = false;
        comando = comando.substring(2); //Remove o 2-
        String [] dados = comando.split(":",3); //nome:username:password

        try{
            dbConn = DriverManager.getConnection(bd_url);
            Statement statement = dbConn.createStatement();
            String sqlQuery = "SELECT * FROM utilizador WHERE LOWER(username) = LOWER('" + dados[1] + "') OR  LOWER(nome) = LOWER('" + dados[0] + "')";
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
                return false;
            }
            sqlQuery = "INSERT INTO utilizador VALUES (NULL,'" + dados[1] + "','" + dados[0] + "','" + dados[2] + "','" + 0 + "','" + 1 + "')";
            synchronized (heartBeat){
                heartBeat.setStatus("Prepare");
                heartBeat.setQuery_sql(sqlQuery);
            }
            threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
            Thread.sleep(1500); //Espera 1 segundo pela confirmação
            synchronized (heartBeat){
                if(heartBeat.getStatus().equals("Commit")){
                    comandosSQL.add(sqlQuery);
                    //bd
                    dbConn = DriverManager.getConnection(bd_url);
                    statement = dbConn.createStatement();
                    statement.executeUpdate(sqlQuery);
                    statement.close();
                    dbConn.close();
                    //
                    heartBeat.setQuery_sql("null");
                    heartBeat.setPrepareConfirmed(false);
                    heartBeat.setStatus("null");
                    user_created = true;
                }
            }
            if(user_created){
                servidor.sendUpdateCallbackRMI("Novo Registo : USERNAME: " + dados[1]);
                dbConn = DriverManager.getConnection(bd_url);
                Statement statement1 = dbConn.createStatement();
                String sqlQuery1 = "SELECT id from utilizador WHERE LOWER(username)=LOWER('"+dados[1]+"') && password='"+dados[2]+"'";
                ResultSet resultSet1 = statement1.executeQuery(sqlQuery1);
                id_user = resultSet1.getInt("id");
                statement1.close();
                resultSet1.close();
                dbConn.close();
                sqlQuery1 = "UPDATE utilizador SET autenticado=1 WHERE LOWER(username)=LOWER('" + dados[0] + "') AND password='" + dados[1] + "'";
                synchronized (heartBeat){
                    heartBeat.setStatus("Prepare");
                    heartBeat.setQuery_sql(sqlQuery1);
                }
                threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
                Thread.sleep(1500); //Espera 1 segundo pela confirmação
                synchronized (heartBeat){
                    if (heartBeat.getStatus().equals("Commit")) {
                        comandosSQL.add(sqlQuery1);
                        dbConn = DriverManager.getConnection(bd_url);
                        statement = dbConn.createStatement();
                        statement.executeUpdate(sqlQuery1);
                        statement.close();
                        dbConn.close();
                        heartBeat.setQuery_sql("null");
                        heartBeat.setPrepareConfirmed(false);
                        heartBeat.setStatus("null");
                        user_logged = true;
                    }
                }
            }
            return user_logged;
        }catch (SQLException e){
            return user_logged;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String verificaComandoCli(String msg){
        String dados = null;
        if(msg.contains("alt_user:")){ //Pedido de alteração de user
            if(alteraCliente(msg)){
                dados = "user_alterado_sucesso";
            }else{
                dados = "user_alterado_erro";
            }
        }else if(msg.contains("get_reservas_aguardam_pagamento")){ //return reservas aguardam pagam.
            dados = getReservasAguardamPagamento();
        }else if(msg.contains("get_reservas_pagas")) { //return reservas pagas
            dados = getReservasPagas();
        }else if(msg.contains("get_espet:")){ //return espetaculos c/ filtros
            dados = consutaEspetaculos(msg);
        }else if(msg.contains("get_espetaculos_24horas")){ //return espetaculos que acontecem em 24hr.
            dados = getEspetaculos24horas();
        }else if(msg.contains("upload_file:")){ //upload espetaculo a partir de um ficheiro
            if(uploadFile(msg)){
                dados = "upload_file_sucesso";
            }else{
                dados = "upload_file_erro";
            }
        }else if(msg.contains("delete_espetaculo:")){ //Admin : apagar um espetaculo e as reservas não pagas
            if(apagarEspetaculo(msg)){
                dados = "delete_espetaculo_sucesso";
            }else{
                dados = "delete_espetaculo_erro";
            }
        }else if(msg.contains("logout")){ //Fazer logout do user
            if(logoutUser(msg)){
                servidor.sendUpdateCallbackRMI("Novo Logout : USERNAME: " + getUsername());
                dados = "logout_sucesso";
            }else{
                dados = "logout_erro";
            }
        }else if(msg.contains("selecionar_espetaculo")){ //Mostra lugares disponíveis no espetaculo em questão
            dados = getLugaresDisponiveis(msg);
        }else if(msg.contains("reserva:")){ //Indicado as reservas a efetuar
            if(verificaReserva(msg)){
                dados = "reserva_sucesso";
            }else {
                dados = "reserva_erro";
            }
        }else if(msg.contains("get_espetaculos_n_visiveis")){
            dados = getEspetaculosNVisiveis();
        }else if(msg.contains("set-visivel")){
            if(setEspetaculoVisivel(msg)){
                dados = "espetaculo_visivel_sucesso";
            }else{
                dados = "espetaculo_visivel_erro";
            }
        }else if(msg.contains("pagamento")){ //Verifica o pagamento
            dados = verificaPagamentoReserva(msg);
            //dados = "Pagamento realizado com sucesso";
        }else if(msg.contains("getespetaculosvisiveis")){
            dados = getEspetaculosVisiveis();
        }
        return dados;
    }

    private boolean alteraCliente(String comando){
        String [] dados = comando.split(":",4); //nome:username:password
        if(dados[1].equals("-") && dados[2].equals("-") && dados[3].equals("-"))
            return false;
        try {
            String sqlQuery1 = "UPDATE utilizador SET ";
            if(!dados[1].equals("-"))
                sqlQuery1 += ("nome='"+dados[1]+"'");
            if(!dados[2].equals("-"))
                if(!dados[1].equals("-"))
                    sqlQuery1 += (",username='"+dados[2]+"'");
                else
                    sqlQuery1 += ("username='"+dados[2]+"'");
            if(!dados[3].equals("-"))
                if(!dados[2].equals("-") || !dados[1].equals("-"))
                    sqlQuery1 += (",password='"+dados[3]+"'");
                else
                    sqlQuery1 += ("password='"+dados[3]+"'");
            sqlQuery1 +=  " WHERE id="+id_user;
            synchronized (heartBeat){
                heartBeat.setStatus("Prepare");
                heartBeat.setQuery_sql(sqlQuery1);
            }
            threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
            Thread.sleep(1500); //Espera 1 segundo pela confirmação
            synchronized (heartBeat){
                if(heartBeat.getStatus().equals("Commit")){
                    comandosSQL.add(sqlQuery1);
                    dbConn = DriverManager.getConnection(bd_url);
                    Statement statement1 = dbConn.createStatement();
                    statement1.executeUpdate(sqlQuery1);
                    statement1.close();
                    dbConn.close();
                    heartBeat.setQuery_sql("null");
                    heartBeat.setPrepareConfirmed(false);
                    heartBeat.setStatus("null");
                }
            }
        } catch (SQLException e)
        {
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private String consutaEspetaculos(String comando){
        String result = "Espetáculos:\n";
        Statement statement = null;
        String [] dados = comando.split(":",6); //nome:localidade:genero:data:classificacao_etaria
        try {

            String sqlQuery = "SELECT descricao,tipo,data_hora,duracao,local,localidade,pais,classificacao_etaria from espetaculo WHERE visivel = 1";
            if (!dados[1].equals("-"))
                sqlQuery += (" AND LOWER(descricao)=LOWER('" + dados[1] + "')");

            if (!dados[2].equals("-")) {
                sqlQuery += (" AND LOWER(localidade)=LOWER('" + dados[2] + "')");
            }

            if (!dados[3].equals("-")) {
                sqlQuery += (" AND LOWER(tipo)=LOWER('" + dados[3] + "')");
            }

            if (!dados[4].equals("-")) {
                sqlQuery += (" AND data_hora='" + dados[4] + "'");
            }

            if (!dados[5].equals("-")) {
                sqlQuery += (" AND LOWER(classificacao_etaria)=LOWER('" + dados[5] + "')");
            }
            //bd
            dbConn = DriverManager.getConnection(bd_url);
            statement = dbConn.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            while(resultSet.next()) {//Verifica se ainda tem linhas
                result += "Nome: " + resultSet.getString("descricao");
                result += " | Localidade: " + resultSet.getString("localidade");
                result += " | Género: " + resultSet.getString("tipo");
                result += " | Data: " + resultSet.getString("data_hora");
                result += " | Classificação Etária: " + resultSet.getString("classificacao_etaria");
                result += "\n";
            }
            resultSet.close();
            statement.close();
            dbConn.close();
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return result;
    }

    private String getReservasAguardamPagamento(){
        String dados = "Reservas que aguardam pagamento:\n";
        Statement statement = null;
        try {

            String sqlQuery = "SELECT espetaculo.descricao,espetaculo.data_hora from reserva,espetaculo " +
                    "WHERE reserva.id_espetaculo = espetaculo.id " +
                    "AND id_utilizador='" + id_user + "' AND pago=0";
            dbConn = DriverManager.getConnection(bd_url);
            statement = dbConn.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            while(resultSet.next()){//Verifica se ainda tem linhas
                dados += "Nome: " + resultSet.getString("descricao");
                dados += " Data/Hora: " + resultSet.getString("data_hora") + "\n";
            }
            resultSet.close();
            statement.close();
            dbConn.close();
            //dbConn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dados;
    }

    private String getReservasPagas(){
        String dados = "Reservas pagas:\n";
        Statement statement = null;
        try {

            String sqlQuery = "SELECT espetaculo.descricao,espetaculo.data_hora from reserva,espetaculo " +
                    "WHERE reserva.id_espetaculo = espetaculo.id " +
                    "AND id_utilizador='" + id_user + "' AND pago=1";
            dbConn = DriverManager.getConnection(bd_url);
            statement = dbConn.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            while(resultSet.next()){//Verifica se ainda tem linhas
                dados += "Nome: " + resultSet.getString("descricao");
                dados += " Data/Hora: " + resultSet.getString("data_hora") + "\n";
            }
            resultSet.close();
            statement.close();
            dbConn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dados;
    }

    private String getEspetaculos24horas(){
        String dados = "Espetaculos a ocorrer nos futuros dias:\n";
        Calendar cal = Calendar.getInstance();
        String data_24hr = null;
        if(cal.get(Calendar.DAY_OF_MONTH) <= 9){
            data_24hr = cal.get(Calendar.YEAR) +"-"+(cal.get(Calendar.MONTH)+1)+"-0"+ cal.get(Calendar.DAY_OF_MONTH); //dd-MM-YYYY
        }else{
            data_24hr = cal.get(Calendar.YEAR) +"-"+(cal.get(Calendar.MONTH)+1)+"-"+ cal.get(Calendar.DAY_OF_MONTH); //dd-MM-YYYY
        }
        Statement statement = null;
        try {

            String sqlQuery = "SELECT descricao,tipo,data_hora,duracao,local,localidade,pais,classificacao_etaria from espetaculo " +
                    "WHERE data_hora > '" + data_24hr + "' AND visivel = 1";
            //System.out.println(sqlQuery);
            dbConn = DriverManager.getConnection(bd_url);
            statement = dbConn.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            statement.executeQuery(sqlQuery);
            while(resultSet.next()) {//Verifica se ainda tem linhas
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
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return dados;
    }

    private boolean uploadFile(String msg){
        String [] dados = msg.split(":",2);
        String splitBy = ";";
        String line = "";
        String titulo, tipo, data_hora, duracao,local, localidade,pais, classificacao;
        int id_espetaculo = 0; //Guarda o id do espetaculo que foi inserido
        try{
            BufferedReader br = new BufferedReader(new FileReader("cliente_files/" + dados[1]));
            line = br.readLine();
            String[] dados_fich = line.split(splitBy); //descricao
            titulo = dados_fich[1].substring(1, dados_fich[1].length() - 1); //Remove as aspas iniciais e finais
            line = br.readLine();
            dados_fich = line.split(splitBy); //tipo
            tipo = dados_fich[1].substring(1, dados_fich[1].length() - 1);
            line = br.readLine();
            dados_fich = line.split(splitBy); //data
            data_hora = dados_fich[1].substring(1, dados_fich[1].length() - 1)
                    + "-" + dados_fich[2].substring(1, dados_fich[2].length() - 1)
                    + "-" + dados_fich[3].substring(1, dados_fich[3].length() - 1) + " ";
            line = br.readLine();
            dados_fich = line.split(splitBy); //hora
            data_hora += dados_fich[1].substring(1, dados_fich[1].length() - 1)
                    + ":" + dados_fich[2].substring(1, dados_fich[2].length() - 1);
            line = br.readLine();
            dados_fich = line.split(splitBy); //duracao
            duracao = dados_fich[1].substring(1, dados_fich[1].length() - 1);
            line = br.readLine();
            dados_fich = line.split(splitBy); //local
            local = dados_fich[1].substring(1, dados_fich[1].length() - 1);
            line = br.readLine();
            dados_fich = line.split(splitBy); //localidade
            localidade = dados_fich[1].substring(1, dados_fich[1].length() - 1);
            line = br.readLine();
            dados_fich = line.split(splitBy); //pais
            pais = dados_fich[1].substring(1, dados_fich[1].length() - 1);
            line = br.readLine();
            dados_fich = line.split(splitBy); //classificacao
            classificacao = dados_fich[1].substring(1, dados_fich[1].length() - 1);
            //Insere o espetaculo e guarda o id para inserir lugar
            try{

                String sqlQuery = "INSERT INTO espetaculo VALUES (NULL,'" + titulo + "','" + tipo + "','" + data_hora + "','" + duracao
                        + "','" + local + "','" + localidade + "','" + pais + "','" + classificacao + "',0)";
                synchronized (heartBeat){
                    heartBeat.setStatus("Prepare");
                    heartBeat.setQuery_sql(sqlQuery);
                }
                threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
                Thread.sleep(1500); //Espera 1 segundo pela confirmação
                synchronized (heartBeat) {
                    if (heartBeat.getStatus().equals("Commit")) {
                        comandosSQL.add(sqlQuery);
                        dbConn = DriverManager.getConnection(bd_url);
                        Statement statement = dbConn.createStatement();
                        statement.executeUpdate(sqlQuery);
                        statement.close();
                        dbConn.close();
                        heartBeat.setQuery_sql("null");
                        heartBeat.setPrepareConfirmed(false);
                        heartBeat.setStatus("null");
                    }
                }

                String sqlQuery1 = "SELECT id from espetaculo WHERE LOWER(descricao)=LOWER('" + titulo+"')";
                dbConn = DriverManager.getConnection(bd_url);
                Statement statement1 = dbConn.createStatement();
                ResultSet resultSet1 = statement1.executeQuery(sqlQuery1);
                id_espetaculo = resultSet1.getInt("id");
                statement1.close();
                resultSet1.close();
                dbConn.close();
            }catch (SQLException e){
                return false;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            line = br.readLine(); //Linha em branco
            line = br.readLine(); //Linha com indicacoes
            while((line = br.readLine()) != null){
                dados_fich = line.split(splitBy);
                for(int i=1;i<dados_fich.length;i++){
                    if(!insereLugar(dados_fich[0].substring(1, dados_fich[0].length() - 1),
                            dados_fich[i].substring(1, dados_fich[i].length() - 1), id_espetaculo)){
                        return false;
                    }
                    Thread.sleep(500);
                }
            }
            return true;
        } catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
        return false;
    }

    private boolean insereLugar(String fila, String lugar_preco, int id_espetaculo){
        String [] dados = lugar_preco.split(":",2); //lugar:preco
        //Insere na tabela lugar
        try{
            String sqlQuery = "INSERT INTO lugar VALUES (NULL,'" + fila + "','" + dados[0] + "','" + dados[1] + "','" + id_espetaculo + "')";
            synchronized (heartBeat){
                heartBeat.setStatus("Prepare");
                heartBeat.setQuery_sql(sqlQuery);
            }
            System.out.println(sqlQuery);
            threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
            Thread.sleep(1500); //Espera 1 segundo pela confirmação
            synchronized(heartBeat) {
                if (heartBeat.getStatus().equals("Commit")) {
                    comandosSQL.add(sqlQuery);
                    dbConn = DriverManager.getConnection(bd_url);
                    Statement statement = dbConn.createStatement();
                    statement.executeUpdate(sqlQuery);
                    statement.close();
                    dbConn.close();
                    heartBeat.setQuery_sql("null");
                    heartBeat.setPrepareConfirmed(false);
                    heartBeat.setStatus("null");
                }
            }
            return true;
        }catch (SQLException e){
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getEspetaculosVisiveis(){
        String result = "Espetáculos visíveis:\n";
        try {

            String sqlQuery = "SELECT descricao,tipo,data_hora,duracao,local,localidade,pais,classificacao_etaria from espetaculo " +
                    "WHERE visivel = 1";
            //System.out.println(sqlQuery);
            dbConn = DriverManager.getConnection(bd_url);
            Statement statement = dbConn.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            statement.executeQuery(sqlQuery);
            while(resultSet.next()) {//Verifica se ainda tem linhas
                result += "Nome: " + resultSet.getString("descricao");
                result += " Tipo: " + resultSet.getString("tipo");
                result += " Data/Hora: " + resultSet.getString("data_hora");
                result += " Duracao: " + resultSet.getString("duracao");
                result += " Local: " + resultSet.getString("local");
                result += " Localidade: " + resultSet.getString("localidade");
                result += " País: " + resultSet.getString("pais");
                result += " Classificação Etária: " + resultSet.getString("classificacao_etaria") + "\n";
            }
            statement.close();
            resultSet.close();
            dbConn.close();
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return result;
    }

    private boolean apagarEspetaculo(String comando){
        String [] dados = comando.split(":",2);
        int id_espetaculo = 0;
        boolean espetaculo_existe = false;
        try{
            String sqlQuery1 = "SELECT id from espetaculo WHERE LOWER(descricao)=LOWER('" + dados[1]+"')";
            dbConn = DriverManager.getConnection(bd_url);
            Statement statement1 = dbConn.createStatement();
            ResultSet resultSet1 = statement1.executeQuery(sqlQuery1);
            if(resultSet1.isBeforeFirst()){
                id_espetaculo = resultSet1.getInt("id");
                espetaculo_existe = true;
            }
            statement1.close();
            resultSet1.close();
            dbConn.close();
            if(espetaculo_existe) {
                sqlQuery1 = "SELECT id from reserva WHERE id_espetaculo=" + id_espetaculo + " AND pago = 0";
                dbConn = DriverManager.getConnection(bd_url);
                statement1 = dbConn.createStatement();
                resultSet1 = statement1.executeQuery(sqlQuery1);
                while (resultSet1.next()) {
                    sqlQuery1 = "DELETE FROM reserva_lugar WHERE id_reserva=" + resultSet1.getInt("id");
                    synchronized (heartBeat) {
                        heartBeat.setStatus("Prepare");
                        heartBeat.setQuery_sql(sqlQuery1);
                    }
                    threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
                    Thread.sleep(1500); //Espera 1 segundo pela confirmação
                    synchronized (heartBeat) {
                        if (heartBeat.getStatus().equals("Commit")) {
                            comandosSQL.add(sqlQuery1);
                            dbConn = DriverManager.getConnection(bd_url);
                            Statement statement2 = dbConn.createStatement();
                            statement2.executeUpdate(sqlQuery1);
                            statement2.close();
                            dbConn.close();
                            heartBeat.setQuery_sql("null");
                            heartBeat.setPrepareConfirmed(false);
                            heartBeat.setStatus("null");
                        }
                    }
                }
                statement1.close();
                resultSet1.close();
                dbConn.close();
                //Apagar as reservas não pagas associadas ao espetaculo
                String sqlQuery2 = "DELETE FROM reserva WHERE id_espetaculo=" + id_espetaculo + " AND pago = 0";
                synchronized (heartBeat) {
                    heartBeat.setStatus("Prepare");
                    heartBeat.setQuery_sql(sqlQuery2);
                }
                threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
                Thread.sleep(1500); //Espera 1 segundo pela confirmação
                synchronized (heartBeat) {
                    if (heartBeat.getStatus().equals("Commit")) {
                        comandosSQL.add(sqlQuery2);
                        dbConn = DriverManager.getConnection(bd_url);
                        statement1 = dbConn.createStatement();
                        statement1.executeUpdate(sqlQuery2);
                        statement1.close();
                        dbConn.close();
                        heartBeat.setQuery_sql("null");
                        heartBeat.setPrepareConfirmed(false);
                        heartBeat.setStatus("null");
                    }
                }
                String sqlQuery = "UPDATE espetaculo set visivel=0 WHERE id = " + id_espetaculo;
                synchronized (heartBeat) {
                    heartBeat.setStatus("Prepare");
                    heartBeat.setQuery_sql(sqlQuery);
                }
                threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
                Thread.sleep(1500); //Espera 1 segundo pela confirmação
                synchronized (heartBeat) {
                    if (heartBeat.getStatus().equals("Commit")) {
                        comandosSQL.add(sqlQuery);
                        dbConn = DriverManager.getConnection(bd_url);
                        Statement statement = dbConn.createStatement();
                        statement.executeUpdate(sqlQuery);
                        statement.close();
                        dbConn.close();
                        heartBeat.setQuery_sql("null");
                        heartBeat.setPrepareConfirmed(false);
                        heartBeat.setStatus("null");
                    }
                }
                return true;
            }
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private boolean logoutUser(String comando){
        try{

            String sqlQuery = "UPDATE utilizador SET autenticado= 0 WHERE id=" + id_user;
            synchronized (heartBeat){
                heartBeat.setStatus("Prepare");
                heartBeat.setQuery_sql(sqlQuery);
            }
            threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
            Thread.sleep(1500); //Espera 1 segundo pela confirmação
            synchronized (heartBeat) {
                if (heartBeat.getStatus().equals("Commit")) {
                    comandosSQL.add(sqlQuery);
                    dbConn = DriverManager.getConnection(bd_url);
                    Statement statement = dbConn.createStatement();
                    statement.executeUpdate(sqlQuery);
                    statement.close();
                    dbConn.close();
                    heartBeat.setQuery_sql("null");
                    heartBeat.setPrepareConfirmed(false);
                    heartBeat.setStatus("null");
                    autenticado = false;
                }
            }
            return true;
        }catch (SQLException e){
            //e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getLugaresDisponiveis(String comando){
        String [] dados = comando.split(":",2);
        String result = "Lugares disponíveis- Espetáculo: " + dados[1] + " \n";
        int id_espetaculo;
        try{
            String sqlQuery1 = "SELECT id from espetaculo WHERE LOWER(descricao)=LOWER('"+ dados[1] +"')"; //Id_espetaculo
            dbConn = DriverManager.getConnection(bd_url);
            Statement statement = dbConn.createStatement();
            ResultSet resultSet1 = statement.executeQuery(sqlQuery1);
            id_espetaculo = resultSet1.getInt("id");
            statement.close();
            resultSet1.close();
            dbConn.close();
            String sqlQuery = "SELECT fila,assento,preco FROM lugar WHERE lugar.id NOT IN (SELECT id_lugar FROM reserva_lugar) AND espetaculo_id=" + id_espetaculo;
            System.out.println(sqlQuery);
            dbConn = DriverManager.getConnection(bd_url);
            statement = dbConn.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            while(resultSet.next()) {//Verifica se ainda tem linhas
                result += "Fila:" +resultSet.getString("fila")+" Assento: "+resultSet.getString("assento")+" Preço:"+resultSet.getString("preco")+"\n";
            }
            statement.close();
            resultSet.close();
            dbConn.close();
        }catch (SQLException e){
            return result;
            //throw new RuntimeException(e);
        }
        return result;
    }

    private boolean verificaReserva(String comando){ //reserva:espetaculo(nome):numero_lugares:Fila;Lugar;Fila;Lugar
        String [] dados = comando.split(":",4);
        String [] dados1 = dados[3].split(";",Integer.parseInt(dados[2])*2); //Separar fila;lugar
        int id_espetaculo;
        int id_reserva;
        String sqlQuery,sqlQuery4;
        ResultSet resultSet,resultSet1;
        Calendar cal = Calendar.getInstance();
        String data_hora = cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.MONTH)+ "-"+cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR_OF_DAY) + ":"+cal.get(Calendar.MINUTE);
        try{
            String sqlQuery1 = "SELECT id from espetaculo WHERE LOWER(descricao)=LOWER('"+ dados[1] +"')";
            dbConn = DriverManager.getConnection(bd_url);
            Statement statement = dbConn.createStatement();
            resultSet1 = statement.executeQuery(sqlQuery1);
            id_espetaculo = resultSet1.getInt("id");
            statement.close();
            resultSet1.close();
            dbConn.close();
            for(int i=0;i<dados1.length/2;i+=2){
                sqlQuery = "SELECT id FROM lugar,reserva_lugar WHERE espetaculo_id="+id_espetaculo+
                        " AND LOWER(fila)=LOWER('"+dados1[i]+"') AND LOWER(assento)=LOWER('"+dados1[i+1]+"') AND id_lugar=id";
                //System.out.println(sqlQuery);
                dbConn = DriverManager.getConnection(bd_url);
                statement = dbConn.createStatement();
                resultSet = statement.executeQuery(sqlQuery);
                if (resultSet.next()) {
                    return false;
                }
                resultSet.close();
                statement.close();
                dbConn.close();
            }
            String sqlQuery3 = "INSERT INTO reserva VALUES (NULL,'"+data_hora+"',0," +id_user+","+id_espetaculo+")";
            synchronized (heartBeat){
                heartBeat.setStatus("Prepare");
                heartBeat.setQuery_sql(sqlQuery3);
            }
            threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
            Thread.sleep(1500); //Espera 1 segundo pela confirmação
            synchronized (heartBeat) {
                if (heartBeat.getStatus().equals("Commit")) {
                    comandosSQL.add(sqlQuery3);
                    dbConn = DriverManager.getConnection(bd_url);
                    statement = dbConn.createStatement();
                    statement.executeUpdate(sqlQuery3);
                    statement.close();
                    dbConn.close();
                    heartBeat.setQuery_sql("null");
                    heartBeat.setPrepareConfirmed(false);
                    heartBeat.setStatus("null");
                }
            }
            sqlQuery1 = "SELECT id FROM reserva WHERE data_hora = '"+data_hora+"' AND id_espetaculo = " + id_espetaculo; //id reserva
            dbConn = DriverManager.getConnection(bd_url);
            statement = dbConn.createStatement();
            resultSet1 = statement.executeQuery(sqlQuery1);
            id_reserva = resultSet1.getInt("id");
            statement.close();
            resultSet1.close();
            dbConn.close();

            for(int i=0;i<dados1.length/2;i+=2) {
                //id_lugar
                sqlQuery = "SELECT id FROM lugar WHERE espetaculo_id="+id_espetaculo+" AND LOWER(fila)=LOWER('"+dados1[i]+"') AND LOWER(assento)=LOWER('"+dados1[i+1]+"')";
                dbConn = DriverManager.getConnection(bd_url);
                statement = dbConn.createStatement();
                resultSet = statement.executeQuery(sqlQuery);
                sqlQuery4="INSERT INTO reserva_lugar VALUES ("+id_reserva+","+resultSet.getInt("id")+")";
                statement.close();
                resultSet.close();
                dbConn.close();
                synchronized (heartBeat){
                    heartBeat.setStatus("Prepare");
                    heartBeat.setQuery_sql(sqlQuery4);
                }
                threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
                Thread.sleep(1500); //Espera 1 segundo pela confirmação
                synchronized (heartBeat) {
                    if (heartBeat.getStatus().equals("Commit")) {
                        comandosSQL.add(sqlQuery4);
                        dbConn = DriverManager.getConnection(bd_url);
                        statement = dbConn.createStatement();
                        statement.executeUpdate(sqlQuery4);
                        statement.close();
                        dbConn.close();
                        heartBeat.setQuery_sql("null");
                        heartBeat.setPrepareConfirmed(false);
                        heartBeat.setStatus("null");
                    }
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private String getEspetaculosNVisiveis(){
        String result = "Espetaculos Não Visiveis Aos Utilizadores:\n";
        try{

            String sqlQuery1 = "SELECT descricao from espetaculo WHERE visivel = 0";
            dbConn = DriverManager.getConnection(bd_url);
            Statement statement = dbConn.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery1);
            while(resultSet.next()) {//Verifica se ainda tem linhas
                result += "- " +resultSet.getString("descricao") +"\n";
            }
            statement.close();
            resultSet.close();
            dbConn.close();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return result;
    }

    private boolean setEspetaculoVisivel(String comando){ //Torna o espetaculo visivel para os utilizadores
        String [] dados = comando.split(":",2);
        try{

            Statement statement;
            String sqlQuery = "UPDATE espetaculo SET visivel= 1 WHERE LOWER(descricao)=LOWER('" + dados[1] + "')";
            synchronized (heartBeat){
                heartBeat.setStatus("Prepare");
                heartBeat.setQuery_sql(sqlQuery);
            }
            threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
            Thread.sleep(1500); //Espera 1 segundo pela confirmação
            synchronized (heartBeat) {
                if (heartBeat.getStatus().equals("Commit")) {
                    comandosSQL.add(sqlQuery);
                    dbConn = DriverManager.getConnection(bd_url);
                    statement = dbConn.createStatement();
                    statement.executeUpdate(sqlQuery);
                    statement.close();
                    dbConn.close();
                    heartBeat.setQuery_sql("null");
                    heartBeat.setPrepareConfirmed(false);
                    heartBeat.setStatus("null");
                }
            }
            return true;
        }catch (SQLException e){
            //e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String verificaPagamentoReserva(String comando)  {
        String dados[] = comando.split("_",2);
        String result = null;
        int id_reserva;
        try{
            if(dados[0].equalsIgnoreCase("realiza")){ //Atribui como pago a ultima reserva feita pelo cliente
                Statement statement;
                String sqlQuery = "UPDATE reserva SET pago= 1 WHERE id = (SELECT MAX(id) FROM reserva WHERE id_utilizador = "+id_user+") ";
                synchronized (heartBeat){
                    heartBeat.setStatus("Prepare");
                    heartBeat.setQuery_sql(sqlQuery);
                }
                threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
                try{
                    Thread.sleep(1500); //Espera 1 segundo pela confirmação
                }catch (InterruptedException e){
                    return null;
                }
                synchronized (heartBeat) {
                    if (heartBeat.getStatus().equals("Commit")) {
                        comandosSQL.add(sqlQuery);
                        dbConn = DriverManager.getConnection(bd_url);
                        statement = dbConn.createStatement();
                        statement.executeUpdate(sqlQuery);
                        statement.close();
                        dbConn.close();
                        heartBeat.setQuery_sql("null");
                        heartBeat.setPrepareConfirmed(false);
                        heartBeat.setStatus("null");
                    }
                }
                result = "Pagamento realizado com sucesso";
            }else if(dados[0].equalsIgnoreCase("recusa")){ //Elimina a ultima reserva feita pelo cliente já que se recusou a pagar
                String sqlQuery1 = "SELECT id from reserva WHERE id = (SELECT MAX(id) FROM reserva WHERE id_utilizador = "+id_user+")";
                System.out.println(sqlQuery1);
                dbConn = DriverManager.getConnection(bd_url);
                Statement statement1 = dbConn.createStatement();
                ResultSet resultSet1 = statement1.executeQuery(sqlQuery1);
                id_reserva = resultSet1.getInt("id");
                statement1.close();
                resultSet1.close();
                dbConn.close();
                //Apagar as reservas não pagas associadas ao espetaculo
                sqlQuery1 = "DELETE FROM reserva_lugar WHERE id_reserva="+ id_reserva;
                synchronized (heartBeat){
                    heartBeat.setStatus("Prepare");
                    heartBeat.setQuery_sql(sqlQuery1);
                }
                threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
                Thread.sleep(1500); //Espera 1 segundo pela confirmação
                synchronized (heartBeat){
                    if(heartBeat.getStatus().equals("Commit")){
                        comandosSQL.add(sqlQuery1);
                        dbConn = DriverManager.getConnection(bd_url);
                        Statement statement2 = dbConn.createStatement();
                        statement2.executeUpdate(sqlQuery1);
                        statement2.close();
                        dbConn.close();
                        heartBeat.setQuery_sql("null");
                        heartBeat.setPrepareConfirmed(false);
                        heartBeat.setStatus("null");
                    }
                }

                sqlQuery1 = "DELETE FROM reserva WHERE id="+ id_reserva;
                synchronized (heartBeat){
                    heartBeat.setStatus("Prepare");
                    heartBeat.setQuery_sql(sqlQuery1);
                }
                threadEnviaHeartBeat.interrupt(); //Para interromper o sleep da Thread
                Thread.sleep(1500); //Espera 1 segundo pela confirmação
                synchronized (heartBeat){
                    if(heartBeat.getStatus().equals("Commit")){
                        comandosSQL.add(sqlQuery1);
                        dbConn = DriverManager.getConnection(bd_url);
                        Statement statement3 = dbConn.createStatement();
                        statement3.executeUpdate(sqlQuery1);
                        statement3.close();
                        dbConn.close();
                        heartBeat.setQuery_sql("null");
                        heartBeat.setPrepareConfirmed(false);
                        heartBeat.setStatus("null");
                    }
                }
                result = "Pagamento não foi realizado a tempo, os lugares voltaram a ficar disponíveis";
            }
        }catch (SQLException e){
            e.printStackTrace();
            return result;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
