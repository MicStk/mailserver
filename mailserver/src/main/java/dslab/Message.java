package dslab;

import java.util.List;

public class Message {

    private List<String> to;
    private String from;
    private String subject;
    private String data;
    private int id;

    public Message(){

    }

    public Message(int id, List<String> to, String from, String subject, String data){
        this.id=id;
        this.to=to;
        this.from=from;
        this.subject=subject;
        this.data=data;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
