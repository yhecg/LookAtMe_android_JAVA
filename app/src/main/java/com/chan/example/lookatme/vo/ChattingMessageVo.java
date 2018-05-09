package com.chan.example.lookatme.vo;


public class ChattingMessageVo {

    private String chattingMessage_send_member_email;
    private String chattingMessage_send_member_image;
    private String chattingMessage_send_member_name;
    private String chattingMessage_contents;
    private String chattingMessage_time;
    private String chattingMessage_type;

    public ChattingMessageVo(String chattingMessage_send_member_email, String chattingMessage_send_member_image, String chattingMessage_send_member_name, String chattingMessage_contents, String chattingMessage_time, String chattingMessage_type) {
        this.chattingMessage_send_member_email = chattingMessage_send_member_email;
        this.chattingMessage_send_member_image = chattingMessage_send_member_image;
        this.chattingMessage_send_member_name = chattingMessage_send_member_name;
        this.chattingMessage_contents = chattingMessage_contents;
        this.chattingMessage_time = chattingMessage_time;
        this.chattingMessage_type = chattingMessage_type;
    }

    public String getChattingMessage_send_member_email() {
        return chattingMessage_send_member_email;
    }

    public void setChattingMessage_send_member_email(String chattingMessage_send_member_email) {
        this.chattingMessage_send_member_email = chattingMessage_send_member_email;
    }

    public String getChattingMessage_send_member_image() {
        return chattingMessage_send_member_image;
    }

    public void setChattingMessage_send_member_image(String chattingMessage_send_member_image) {
        this.chattingMessage_send_member_image = chattingMessage_send_member_image;
    }

    public String getChattingMessage_send_member_name() {
        return chattingMessage_send_member_name;
    }

    public void setChattingMessage_send_member_name(String chattingMessage_send_member_name) {
        this.chattingMessage_send_member_name = chattingMessage_send_member_name;
    }

    public String getChattingMessage_contents() {
        return chattingMessage_contents;
    }

    public void setChattingMessage_contents(String chattingMessage_contents) {
        this.chattingMessage_contents = chattingMessage_contents;
    }

    public String getChattingMessage_time() {
        return chattingMessage_time;
    }

    public void setChattingMessage_time(String chattingMessage_time) {
        this.chattingMessage_time = chattingMessage_time;
    }

    public String getChattingMessage_type() {
        return chattingMessage_type;
    }

    public void setChattingMessage_type(String chattingMessage_type) {
        this.chattingMessage_type = chattingMessage_type;
    }
}
