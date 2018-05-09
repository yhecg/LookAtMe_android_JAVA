package com.chan.example.lookatme.vo;


public class BoardCommentVo {

    private String boardComment_no;
    private String member_email;
    private String member_name;
    private String member_img;
    private String boardComment_contents;
    private String boardComment_date;
    private String boardComment_depth;
    private String boardComment_parent;

    public BoardCommentVo(String boardComment_no, String member_email, String member_name, String member_img, String boardComment_contents, String boardComment_date, String boardComment_depth, String boardComment_parent) {
        this.boardComment_no = boardComment_no;
        this.member_email = member_email;
        this.member_name = member_name;
        this.member_img = member_img;
        this.boardComment_contents = boardComment_contents;
        this.boardComment_date = boardComment_date;
        this.boardComment_depth = boardComment_depth;
        this.boardComment_parent = boardComment_parent;
    }

    public String getBoardComment_no() {
        return boardComment_no;
    }

    public void setBoardComment_no(String boardComment_no) {
        this.boardComment_no = boardComment_no;
    }

    public String getMember_email() {
        return member_email;
    }

    public void setMember_email(String member_email) {
        this.member_email = member_email;
    }

    public String getMember_name() {
        return member_name;
    }

    public void setMember_name(String member_name) {
        this.member_name = member_name;
    }

    public String getMember_img() {
        return member_img;
    }

    public void setMember_img(String member_img) {
        this.member_img = member_img;
    }

    public String getBoardComment_contents() {
        return boardComment_contents;
    }

    public void setBoardComment_contents(String boardComment_contents) {
        this.boardComment_contents = boardComment_contents;
    }

    public String getBoardComment_date() {
        return boardComment_date;
    }

    public void setBoardComment_date(String boardComment_date) {
        this.boardComment_date = boardComment_date;
    }

    public String getBoardComment_depth() {
        return boardComment_depth;
    }

    public void setBoardComment_depth(String boardComment_depth) {
        this.boardComment_depth = boardComment_depth;
    }

    public String getBoardComment_parent() {
        return boardComment_parent;
    }

    public void setBoardComment_parent(String boardComment_parent) {
        this.boardComment_parent = boardComment_parent;
    }
}