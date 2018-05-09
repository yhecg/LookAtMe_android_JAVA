package com.chan.example.lookatme.vo;

import android.graphics.Bitmap;

public class AlbumVo {

    private Bitmap album_image; // 핸드폰 앨범(갤러리)에서 불러온 사진.
    private boolean item_check_status; // 이미지가 체크되었는지 확인.
    private String imageFilename; // 이미지 파일의 이름(경로).

    public AlbumVo(Bitmap album_image, boolean item_check_status, String imageFilename) {
        this.album_image = album_image;
        this.item_check_status = item_check_status;
        this.imageFilename = imageFilename;
    }

    public Bitmap getAlbum_image() {
        return album_image;
    }

    public void setAlbum_image(Bitmap album_image) {
        this.album_image = album_image;
    }

    public boolean isItem_check_status() {
        return item_check_status;
    }

    public void setItem_check_status(boolean item_check_status) {
        this.item_check_status = item_check_status;
    }

    public String getImageFilename() {
        return imageFilename;
    }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }
}
