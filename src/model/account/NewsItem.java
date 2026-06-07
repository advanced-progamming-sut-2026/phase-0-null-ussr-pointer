package model.account;
public class NewsItem {
    private String title;
    private String content;
    private int date;

    //constructor
    public NewsItem(String title , String content , int date) {
        this.title = title;
        this.content = content;
        this.date = date;
    }

    //getter
    public String getTitle() { return this.title; }
    public String getContent() { return this.content; }
    public int getDate() { return this.date; }

}