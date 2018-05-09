package com.chan.example.lookatme.vo;

public class ChattingRoomVo {

    private String chattingRoom_public_key;
    private String chattingRoom_accept_member_email;
    private String chattingRoom_accept_member_image;
    private String chattingRoom_name;
    private String chattingRoom_message_contents;
    private String chattingRoom_message_time;
    private int chattingRoom_message_unread_count;

    public ChattingRoomVo(String chattingRoom_public_key, String chattingRoom_accept_member_email, String chattingRoom_accept_member_image, String chattingRoom_name, String chattingRoom_message_contents, String chattingRoom_message_time, int chattingRoom_message_unread_count) {
        this.chattingRoom_public_key = chattingRoom_public_key;
        this.chattingRoom_accept_member_email = chattingRoom_accept_member_email;
        this.chattingRoom_accept_member_image = chattingRoom_accept_member_image;
        this.chattingRoom_name = chattingRoom_name;
        this.chattingRoom_message_contents = chattingRoom_message_contents;
        this.chattingRoom_message_time = chattingRoom_message_time;
        this.chattingRoom_message_unread_count = chattingRoom_message_unread_count;
    }

    public String getChattingRoom_public_key() {
        return chattingRoom_public_key;
    }

    public void setChattingRoom_public_key(String chattingRoom_public_key) {
        this.chattingRoom_public_key = chattingRoom_public_key;
    }

    public String getChattingRoom_accept_member_email() {
        return chattingRoom_accept_member_email;
    }

    public void setChattingRoom_accept_member_email(String chattingRoom_accept_member_email) {
        this.chattingRoom_accept_member_email = chattingRoom_accept_member_email;
    }

    public String getChattingRoom_accept_member_image() {
        return chattingRoom_accept_member_image;
    }

    public void setChattingRoom_accept_member_image(String chattingRoom_accept_member_image) {
        this.chattingRoom_accept_member_image = chattingRoom_accept_member_image;
    }

    public String getChattingRoom_name() {
        return chattingRoom_name;
    }

    public void setChattingRoom_name(String chattingRoom_name) {
        this.chattingRoom_name = chattingRoom_name;
    }

    public String getChattingRoom_message_contents() {
        return chattingRoom_message_contents;
    }

    public void setChattingRoom_message_contents(String chattingRoom_message_contents) {
        this.chattingRoom_message_contents = chattingRoom_message_contents;
    }

    public String getChattingRoom_message_time() {
        return chattingRoom_message_time;
    }

    public void setChattingRoom_message_time(String chattingRoom_message_time) {
        this.chattingRoom_message_time = chattingRoom_message_time;
    }

    public int getChattingRoom_message_unread_count() {
        return chattingRoom_message_unread_count;
    }

    public void setChattingRoom_message_unread_count(int chattingRoom_message_unread_count) {
        this.chattingRoom_message_unread_count = chattingRoom_message_unread_count;
    }
}
