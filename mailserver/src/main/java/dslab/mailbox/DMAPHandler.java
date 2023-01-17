package dslab.mailbox;

import dslab.Message;
import dslab.util.Config;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DMAPHandler extends Thread {

    private Config config;
    private Socket dmapSocket;
    private ConcurrentHashMap<Integer, Message> messages;
    private String username = null;

    public DMAPHandler(Socket dmapSocket, Config config, ConcurrentHashMap<Integer, Message> messages) {
        this.dmapSocket=dmapSocket;
        this.config=config;
        this.messages=messages;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(dmapSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(dmapSocket.getOutputStream(), true);

            writer.println("ok DMAP");



            String request;
            boolean loggedIn=false;

            while ((request = reader.readLine()) != null) {
                if (request.equals("quit")) {
                    writer.println("ok bye");
                    this.dmapSocket.close();
                }

                if (!loggedIn) {

                    if (request.startsWith("login")) {
                        String[] loginRequest = request.split(" ");
                        if (loginRequest.length == 3) {
                            String login = loginRequest[0];
                            username = loginRequest[1];
                            String password = loginRequest[2];

                            String user = config.getString("users.config");
                            Config userConfig = new Config(user);
                            String pw = "";
                            if (!userConfig.containsKey(username)) {
                                writer.println("error wrong username");
                            } else pw = userConfig.getString(username);

                            if (password == null) {
                                writer.println("error password null");
                                break;
                            }

                            if (password.equals(pw)) {
                                loggedIn = true;
                            } else writer.println("error wrong password");


                        } else writer.println("error login has 2 arguments");

                        if (loggedIn) {
                            writer.println("ok");
                        }

                    } else {
                        writer.println("error not logged in");
                    }
                } else {

                    if (request.equals("logout")) {
                        loggedIn = false;
                        writer.println("ok");
                    } else if (request.startsWith("login")) {
                        writer.println("error already logged in");
                    }
                    else if (request.equals("list")) {
                        if (messages.isEmpty()) {
                            writer.println("error mailbox is empty");
                        } else {
                            boolean hasElement=false;
                            for (Map.Entry<Integer, Message> entry : messages.entrySet()) {
                                Message msg = entry.getValue();
                                String mailaddress=username+ "@" + config.getString("domain");
                                if (msg.getTo().contains(mailaddress)) {
                                    hasElement=true;
                                    writer.println(entry.getKey() + " " + msg.getFrom() + " " + msg.getSubject());
                                }
                            }
                            if(!hasElement){
                                writer.println("error no mail in mailbox");
                            }

                        }
                    } else if (request.startsWith("show")) {
                        String[] showRequest = request.split(" ");
                        if (showRequest.length != 2) {
                            writer.println("error show has 1 argument");
                        } else {
                            String show = showRequest[0];
                            String id = showRequest[1];
                            if (messages.containsKey(Integer.valueOf(id))) {
                                String compare=username+ "@" + config.getString("domain");
                                if(messages.get(Integer.valueOf(id)).getTo().contains(compare)) {
                                    writer.println("from " + messages.get(Integer.valueOf(id)).getFrom());
                                    writer.println("to " + messages.get(Integer.valueOf(id)).getTo());
                                    writer.println("subject " + messages.get(Integer.valueOf(id)).getSubject());
                                    writer.println("data " + messages.get(Integer.valueOf(id)).getData());
                                } else {
                                    writer.println("error message can not be shown to user");
                                }

                            } else {
                                writer.println("error unknown message id " + id);
                            }
                        }
                    } else if (request.startsWith("delete")) {
                        String[] deleteRequest = request.split(" ");
                        if (deleteRequest.length != 2) {
                            writer.println("error delete has 1 argument");
                        } else {
                            String delete = deleteRequest[0];
                            String id = deleteRequest[1];
                            if (messages.containsKey(Integer.valueOf(id))) {
                                String compareName=username+ "@" + config.getString("domain");
                                if(messages.get(Integer.valueOf(id)).getTo().contains(compareName)) {
                                    messages.remove(Integer.valueOf(id));
                                    writer.println("ok");
                                } else {
                                    writer.println("error message can not be deleted by user");
                                }
                            } else {
                                writer.println("error unknown message id " + id);
                            }
                        }
                    } else {
                        writer.println("error protocol error");
                    }

                }

                }

                } catch(IOException e){
                    throw new UncheckedIOException(e);
                }

            }

}

