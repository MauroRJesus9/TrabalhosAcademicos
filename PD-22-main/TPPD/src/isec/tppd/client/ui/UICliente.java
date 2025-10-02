package isec.tppd.client.ui;

import isec.tppd.client.threads.ThreadComServidor;
import isec.tppd.client.threads.ThreadMostraLugaresDisp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

public class UICliente {
    private Socket cliSocket;
    private Scanner scanner;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public UICliente(Socket cliSocket) {
        this.cliSocket = cliSocket;
        scanner = new Scanner(System.in);
        oos = null;
        ois = null;
    }


    public String UIautenticacao() {
        int op = 0;
        String comando = null;
        System.out.println("- Bem-Vindo à bilheteira online -");
        System.out.println("1- Autenticação de utilizador");
        System.out.println("2- Novo Registo");
        System.out.println("3- Sair");
        do {
            op = scanner.nextInt();
            scanner.nextLine();
            switch (op) {
                case 1 -> {
                    System.out.println("Introduza o username:");
                    comando = "1-";
                    comando += scanner.nextLine();
                    System.out.println("Introduza a sua password:");
                    comando += ":" + scanner.nextLine();
                }
                case 2 -> {
                    System.out.println("Introduza o seu nome:");
                    comando = "2-";
                    comando += scanner.next();
                    System.out.println("Introduza o seu username:");
                    comando += ":" + scanner.next();
                    System.out.println("Introduza a sua password");
                    comando += ":" + scanner.next();
                }
                case 3 -> {
                    comando = "sair";
                }
            }
        } while (op < 1 || op > 3);
        return comando;
    }

    public String UImenu(boolean admin, int id_user) {
        int op = 0, op2 = 0;
        String comando = null;
        System.out.println("- Menu -");
        System.out.println("1- Alterar dados de utilizador");
        System.out.println("2- Consulta reservas");
        System.out.println("3- Consultar espetaculos");
        System.out.println("4- Selecionar um espetáculo");
        System.out.println("5- Logout");
        if (admin) {
            System.out.println("- Área de Admin -");
            System.out.println("6- Inserir um espetáculo a partir de um ficheiro");
            System.out.println("7- Tornar um espetáculo visível para os utilizadores");
            System.out.println("8- Eliminar um espetaculo");
        }
        do {
            System.out.println("Selecione a opção:");
            op = scanner.nextInt();
            scanner.nextLine();
            switch (op) {
                case 1 -> {
                    comando += "alt_user";
                    System.out.println("-> Alteração de dados de utilizador \n(insira - caso não pretenda alterar o campo em questão)");
                    System.out.println("Introduza o nome que pretende:");
                    comando += ":" + scanner.next();
                    System.out.println("Introduza o seu username:");
                    comando += ":" + scanner.next();
                    System.out.println("Introduza a sua password");
                    comando += ":" + scanner.next();
                }
                case 2 -> {
                    System.out.println("Consulta de reservas");
                    System.out.println("1- Reservas que aguardam pagamento");
                    System.out.println("2- Reservas pagas");
                    do{
                        op2 = scanner.nextInt();
                        switch (op2) {
                            case 1 -> {
                                comando = "get_reservas_aguardam_pagamento";
                            }
                            case 2 -> {
                                comando = "get_reservas_pagas";
                            }
                        }
                    }while(op2 < 1 || op2 > 2);
                }
                case 3 -> {
                    System.out.println("Consulta de espetáculos\n(insira - caso não pretenda filtrar o campo em questão)");
                    System.out.println("Pesquisar por nome");
                    comando = "get_espet:" + scanner.nextLine();
                    System.out.println("Pesquisar por localidade");
                    comando += ":" + scanner.nextLine();
                    System.out.println("Pesquisar por género");
                    comando += ":" + scanner.nextLine();
                    System.out.println("Pesquisar por data (formato: YYYY-MM-DD HH:MM)");
                    comando += ":" + scanner.nextLine();
                    System.out.println("Pesquisar por classificação etária");
                    comando += ":" + scanner.nextLine();
                }
                case 4 -> {
                    comando = "get_espetaculos_24horas"; //Envia pedido ao server para return espetac que vao acontecer nas prox 24hr ou mais
                }
                case 5 -> {
                    comando = "logout";
                }
                case 6 ->{
                    if (admin) {
                        System.out.println("Introduza o nome do ficheiro que pretende:");
                        comando = "upload_file:" + scanner.nextLine();
                        try {
                            cliSocket.setSoTimeout(120000); //Sendo uma operação grande, aumentar o timeout
                        }catch (SocketException e){

                        }
                    } else {
                        System.out.println("Não tem acesso admin.");
                        op = 8;
                    }
                }
                case 7 ->{
                    if(admin){
                        comando = "get_espetaculos_n_visiveis"; //Retorna a lista de espetaculos não visiveis aos utilizadores
                    }else{
                        System.out.println("Não tem acesso admin.");
                        op = 9;
                    }
                }
                case 8 ->{
                    if (admin) {
                        comando = "getespetaculosvisiveis";
                    } else {
                        System.out.println("Não tem acesso admin.");
                        op = 9;
                    }
                }
            }
        } while (op < 1 || op > 8);
        return comando;
    }

    public String verificaComandosServidor(String msgRec)  {
        String comando = null;
        if(msgRec.contains("user_alterado:")){
            if(msgRec.contains("sucesso")){
                System.out.println("O seu utilizador foi alterado com sucesso");
            }else if(msgRec.contains("erro")){
                System.out.println("Erro ao alterar o seu utilizador");
            }
        }else if(msgRec.contains("Reservas que aguardam pagamento:")){
            System.out.println(msgRec);
        }else if(msgRec.contains("Reservas pagas:")){
            System.out.println(msgRec);
        }else if(msgRec.contains("Espetáculos:")){
            System.out.println(msgRec);
            System.out.println("\nPretende selecionar um espetaculo para mais detalhes? (S- Sim ou N-Não)");
            char aux = scanner.next().charAt(0);
            scanner.nextLine();
            if(aux == 's'){
                System.out.println("Introduza o nome do espetaculo:");
                comando = "selecionar_espetaculo:" + scanner.nextLine();
            }
        }else if(msgRec.contains("Espetaculos a ocorrer nos futuros dias:")){
            System.out.println(msgRec);
            System.out.println("\nPretende selecionar um espetaculo para mais detalhes? (S- Sim ou N-Não)");
            char aux = scanner.next().charAt(0);
            scanner.nextLine();
            if(Character.toLowerCase(aux) == 's'){
                System.out.println("Introduza o nome do espetaculo:");
                comando = "selecionar_espetaculo:" + scanner.nextLine();
            }
        }else if(msgRec.contains("upload_file")) {
            if (msgRec.contains("sucesso")) {
                System.out.println("O ficheiro foi carregado com sucesso para a base de dados");
                try {
                    cliSocket.setSoTimeout(10000); //Colocar o timeout inicial
                } catch (SocketException e) {

                }
            } else if (msgRec.contains("erro")) {
                System.out.println("Erro ao carregar o ficheiro para a base de dados");
            }
        }else if(msgRec.contains("Espetáculos visíveis:")){
            System.out.println(msgRec);
            System.out.println("Introduza o nome do espetaculo que pretende eliminar:");
            comando = "delete_espetaculo:" + scanner.nextLine();
        }else if(msgRec.contains("delete_espetaculo")){
            if(msgRec.contains("sucesso")){
                System.out.println("O espetaculo foi apagado com sucesso");
            }else if(msgRec.contains("erro")){
                System.out.println("Erro ao tentar eliminar o espetaculo");
            }
        }else if(msgRec.contains("logout")){
            if(msgRec.contains("sucesso")){
                System.out.println("Logout realizado com sucesso");
            }else if(msgRec.contains("erro")){
                System.out.println("Erro ao realizar o logout");
            }
        }else if(msgRec.contains("Lugares disponíveis- Espetáculo:")){
            //System.out.println(msgRec);
            String [] dados = msgRec.split(":",3);
            dados[1] =  dados[1].substring(1, dados[1].length() - 6);
            String comando_rep_server = "selecionar_espetaculo:" + dados[1]; //Comando para o servidor voltar a enviar a lista de lugares disp
            int aux,lugar;
            String fila;
            ThreadMostraLugaresDisp threadMostraLugaresDisp = new ThreadMostraLugaresDisp(cliSocket,comando_rep_server,msgRec,oos,ois);
            threadMostraLugaresDisp.start();
            System.out.println("Pretende reservar quantos lugares para o espetaculo " + dados[1] + "? (0-nenhum)");
            aux = scanner.nextInt();scanner.nextLine();
            comando = "reserva:"+dados[1]+":" + aux + ":";
            ArrayList<String> filas = new ArrayList<>();
            ArrayList<Integer> lugares = new ArrayList<>();
            for(int i= 0;i<aux;i++) {
                System.out.println("Introduza a fila que pretende?");
                fila = Character.toString(scanner.next().charAt(0)).toUpperCase();
                System.out.println("Introduza o número do lugar que pretende:");
                lugar = scanner.nextInt();
                scanner.nextLine();
                synchronized (msgRec){
                    if(msgRec.contains("Fila:"+fila+" Assento: "+lugar) && (i+1 < aux || aux == 1)){ // Verifica lugar e senao for a ultima tentativa
                        filas.add(fila);
                        lugares.add(lugar);
                    }else{
                        System.out.println("O lugar não se encontra disponível, escolha outro.");
                        i--; //Retira uma tentativa gasta
                    }
                    if(i+1 == aux) { //última iteração
                        for(int j= 0;j<filas.size();j++) {
                            if(msgRec.contains("Fila:"+filas.get(j)+" Assento: "+lugares.get(j))){ //Se ainda se mantiverem os lugares cria o comando de reserva
                                comando += filas.get(j);
                                comando += ";" + lugares.get(j);
                                comando += ";";
                            }else{
                                System.out.println("Um dos lugares que escolheu anteriormente ficaram indisponíveis. Volte a selecionar novos lugares.");
                                i=0;
                                break;
                            }
                        }
                    }
                }
            }
            comando = comando.substring(0,comando.length()-1); //Remover o ; final
            threadMostraLugaresDisp.interrupt();
            try{
                threadMostraLugaresDisp.join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }else if(msgRec.contains("reserva")){
            if(msgRec.contains("sucesso")){
                System.out.println("A sua reserva foi realizada com sucesso. Efetue o pagamento para assistir ao espetaculo.");
                System.out.println("\nPretende efectuar o pagamento? (S- Sim ou N-Não)");
                char aux = scanner.next().charAt(0);
                scanner.nextLine();
                if(Character.toLowerCase(aux) == 's'){
                    return "realiza_pagamento";
                }
                try {
                    Thread.sleep(10000);
                }catch (InterruptedException e){

                }
                comando = "recusa_pagamento";
            }else {
                System.out.println("Erro a realizar a sua reserva");
            }
        }else if(msgRec.contains("Espetaculos Não Visiveis Aos Utilizadores:")){
            System.out.println(msgRec);
            System.out.println("Introduza o nome do espetaculo que pretende tornar visivel:");
            comando = "set-visivel:" + scanner.nextLine();
        }else if(msgRec.contains("espetaculo_visivel")){
            if(msgRec.contains("sucesso")){
                System.out.println("Sucesso ao tornar o espetaculo visivel.");
            }else{
                System.out.println("Erro ao tornar o espetaculo visivel.");
            }
        }else if(msgRec.contains("Pagamento")){
            System.out.println(msgRec);
            comando = "null";
        }
        return comando;
    }


    //Getters & Setters
    public ObjectOutputStream getOos() {
        return oos;
    }

    public void setOos(ObjectOutputStream oos) {
        this.oos = oos;
    }

    public ObjectInputStream getOis() {
        return ois;
    }

    public void setOis(ObjectInputStream ois) {
        this.ois = ois;
    }
}
