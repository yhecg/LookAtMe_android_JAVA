package com.chan.example.lookatme.vo;

public class BoardVo {

    private String board_no; // 게시글 번호
    private String member_email; // 게시글 작성한 회원 이메일
    private String board_img; // 게시글 대표 이미지 ( 맨 첫번째 이미지 )

    public BoardVo(String board_no, String member_email, String board_img) {
        this.board_no = board_no;
        this.member_email = member_email;
        this.board_img = board_img;
    }

    public String getBoard_no() {
        return board_no;
    }

    public void setBoard_no(String board_no) {
        this.board_no = board_no;
    }

    public String getMember_email() {
        return member_email;
    }

    public void setMember_email(String member_email) {
        this.member_email = member_email;
    }

    public String getBoard_img() {
        return board_img;
    }

    public void setBoard_img(String board_img) {
        this.board_img = board_img;
    }
}
