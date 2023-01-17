package dslab.mailbox;

import dslab.Message;
import dslab.util.Config;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DMTPHandler extends Thread  {

    private Socket serverSocket;
    private Config config;
    private Message message;
    private ConcurrentHashMap<Integer, Message> messages;

    public DMTPHandler(Socket socket, Config config, ConcurrentHashMap<Integer, Message> messages) {
        this.serverSocket=socket;
        this.config=config;
        this.messages=messages;
    }

    @Override
    public void run() {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(serverSocket.getOutputStream(), true);


            writer.println("ok DMTP");

            String request;
            boolean begin=false;
            boolean sender=false;
            boolean receiver=false;

            while ((request = reader.readLine()) != null) {

                if(request.equals("quit")){
                    writer.println("ok bye");
                    serverSocket.close();
                }

                if(!begin){

                    if(request.equals("begin")){
                        begin=true;
                        message=new Message();
                        writer.println("ok");
                    } else {
                        writer.println("error  protocol  error");
                    }
                } else {

                    if(request.startsWith("from")){
                        String[] loginRequest = request.split(" ",2);
                        if(loginRequest.length!=2){
                            writer.println("error invalid input");
                        } else {
                            String start = loginRequest[0];
                            String from = loginRequest[1];
                            message.setFrom(from);
                            writer.println("ok");
                            sender = true;
                        }
                    }
                    else if(request.startsWith("to")){
                        String[] loginRequest = request.split(" ", 2);
                        if(loginRequest.length!=2){
                            writer.println("error invalid input");
                        } else {
                            boolean invalidUser=false;
                            String invalidName="";
                            String start = loginRequest[0];
                            String toList = loginRequest[1];
                            Config user= new Config(config.getString("users.config"));
                            List<String> tos = Arrays.asList(toList.split(","));

                            for(String to: tos){
                                String[] tmp = to.split("@", 2);
                                if(tmp.length!=2){
                                    invalidUser=true;
                                    writer.println("error invalid recipient");
                                    break;
                                } else {
                                    String toHelp= tmp[0];
                                    if(!user.containsKey(toHelp)){
                                        invalidUser=true;
                                        invalidName=to;
                                    }
                                }
                            }
                            if(!invalidUser) {
                                message.setTo(tos);
                                writer.println("ok " + tos.size());
                                receiver = true;
                            } else writer.println("error unknown recipient "+ invalidName);
                        }
                    }

                    else if(request.startsWith("subject")){
                        String[] loginRequest = request.split(" ", 2);
                        if(loginRequest.length!=2){
                            writer.println("error subject has two arguments");
                        } else {
                            String start = loginRequest[0];
                            String subject = loginRequest[1];
                            message.setSubject(subject);
                            writer.println("ok");
                        }

                    }
                    else if(request.startsWith("data")){
                        String[] loginRequest = request.split(" ", 2);
                        if(loginRequest.length!=2){
                            writer.println("error data has two arguments");
                        } else {
                            String start = loginRequest[0];
                            String data = loginRequest[1];
                            message.setData(data);
                            writer.println("ok");
                        }
                    }
                    else if(request.equals("send")){

                        if(!sender){
                            writer.println("error no sender");
                        } else if(!receiver){
                            writer.println("error no receiver");
                        } else {
                            saveMessage(message);
                            writer.println("ok");
                        }
                    }


                }

                writer.flush();
            }

        } catch (SocketException e) {
            System.out.println("SocketException while handling socket: " + e.getMessage());
            // break;
        } catch (IOException e) {
            // you should properly handle all other exceptions
            throw new UncheckedIOException(e);
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    // Ignored because we cannot handle it
                }
            }

        }

    }

    private synchronized void saveMessage(Message msg){
        int id = 1;
        while (messages.containsKey(id)) {
            id++;
        }
        messages.put(id, msg);
    }


}
