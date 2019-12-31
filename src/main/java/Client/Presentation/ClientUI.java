package Client.Presentation;

import Client.Client;
import Client.Request.Request;
import Client.Request.SignIn;

import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class ClientUI extends Thread {

    private Client client;
    private Block block;


    public ClientUI(Client client, Block block){
        this.client=client;
        this.block=block;


    }

    public void run(){
        int option;
        while (true) {
            showMenuInicial();
            option = read_menu_output();
            if (option == 0) {
                client.shutdown();
                break;
                }
        }

            }



    public void showMenuInicial() {

        System.out.println("1 -Registar | 2 -SetUp username/password | 3 - GET 10 | 4 - Publicar Mensagem | 5 - Subscrever topicos | 0 - Sair");
    }


    public int read_menu_output(){
        int option = this.readOpt();

        switch(option){
            case 0:
                break;
            case 1:
                menu_register();
                break;
            case 2:
                menu_setUp();
                break;
            case 3 :
                client.getMessages();
                break;
            case 4 :
                menu_message();
                break;

            case 5 :
                menu_topics();
                break;
            default:{
                System.out.println("Por favor insira um número das opeções dadas");
                showMenuInicial();
                break;
            }
        }
        return option;

    }



    public void menu_register(){
        String username, password;
        Scanner is = new Scanner(System.in);

        System.out.println("Username:");
        username = is.nextLine();
        System.out.println("Password:");
        password = is.nextLine();

        client.register(username,password);

        }

    public void menu_setUp(){
        String username, password;
        Scanner is = new Scanner(System.in);

        System.out.println("Username:");
        username = is.nextLine();
        System.out.println("Password:");
        password = is.nextLine();

        client.setUsername(username);
        client.setPassword(password);
    }

    public void menu_message(){
        String topic,message;
        int numTopics;
        Set<String> topics = new HashSet<>();
        Scanner is = new Scanner(System.in);

        System.out.println("Quantos tópicos estão associados à mensagem?");
        System.out.println("Responda com um numero:");

        numTopics= readOpt();
        System.out.println("Escreva o nome dos topicos");

        for(int i = 0;i<numTopics;i++){
            topic= is.nextLine();
            topics.add(topic);
        }

        System.out.println("Escreva a mensagem");

        message=is.nextLine();

        client.postMessage(topics,message);
    }

    public void menu_topics(){
        String topic;
        int numTopics;

        Set<String> topics = new HashSet<>();
        Scanner is = new Scanner(System.in);

        System.out.println("Quantos tópicos estão associados à mensagem?");
        System.out.println("Responda com um numero:");

        numTopics= readOpt();
        System.out.println("Escreva o nome dos topicos");

        for(int i = 0;i<numTopics;i++){
            topic= is.nextLine();
            topics.add(topic);
        }


        client.postTopics(topics);

    }


    public int readOpt(){
        int option = -1;
        boolean valid = false;
        String msg;
        Scanner is = new Scanner(System.in);

        while(!valid){
            try{
                msg = is.nextLine();
                option = Integer.parseInt(msg);
                valid = true;
            }
            catch (NumberFormatException e){
                System.out.println("Input inválido. Insira um dígito.\n");
            }
        }

        return option;
    }



    }

