package com.chan.example.lookatme.vo;

public class MemberVo {

    private String member_email; // 회원 이메일
    private String member_name; // 회원 이름
    private String member_img; // 회원 이미지 파일 이름

    // 전체 피드에서 회원 검색할 경우
    public MemberVo(String member_email, String member_name, String member_img) {
        this.member_email = member_email;
        this.member_name = member_name;
        this.member_img = member_img;
    }

    private boolean member_check_status; // 체크되었는지 안되어있는지 확인.

    // 채팅 초대창에서 회원 선택하여 초대할 경우
    public MemberVo(String member_email, String member_name, String member_img, boolean member_check_status) {
        this.member_email = member_email;
        this.member_name = member_name;
        this.member_img = member_img;
        this.member_check_status = member_check_status;
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

    public boolean isMember_check_status() {
        return member_check_status;
    }

    public void setMember_check_status(boolean member_check_status) {
        this.member_check_status = member_check_status;
    }
}
