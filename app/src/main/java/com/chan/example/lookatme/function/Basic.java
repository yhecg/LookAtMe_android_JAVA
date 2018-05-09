package com.chan.example.lookatme.function;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 기본적으로 자주 사용하는 것들.
 */

public class Basic {

    /**
     * Service 에서 사용할 ip와 port 번호.
     */
    public static final String ip = "ec2-13-125-244-138.ap-northeast-2.compute.amazonaws.com";
    public static final int port = 5000;


    public static final String server_start_php_directory_url = "http://ec2-13-125-244-138.ap-northeast-2.compute.amazonaws.com/LookAtMe/php/start/";
    public static final String server_main_php_directory_url = "http://ec2-13-125-244-138.ap-northeast-2.compute.amazonaws.com/LookAtMe/php/main/";
    public static final String server_member_php_directory_url = "http://ec2-13-125-244-138.ap-northeast-2.compute.amazonaws.com/LookAtMe/php/member/";
    public static final String server_board_pid_php_directory_url = "http://ec2-13-125-244-138.ap-northeast-2.compute.amazonaws.com/LookAtMe/php/board_pid/";
    public static final String server_chat_php_directory_url = "http://ec2-13-125-244-138.ap-northeast-2.compute.amazonaws.com/LookAtMe/php/chat/";

    public static final String server_member_image_directory_url = "http://ec2-13-125-244-138.ap-northeast-2.compute.amazonaws.com/LookAtMe/profile_img/";
    public static final String server_pid_image_directory_url = "http://ec2-13-125-244-138.ap-northeast-2.compute.amazonaws.com/LookAtMe/pid_img/";
    public static final String server_chatting_image_directory_url = "http://ec2-13-125-244-138.ap-northeast-2.compute.amazonaws.com/LookAtMe/chatting_img/";

    /**
     * bitmap_resize : 비트맵 이미지를 리사이즈할 시 크기.
     */
    public static final int bitmap_resize = 200;

    /**
     * nowData() : 현재 시간 구하는 함수.
     *  - file : 이미지 파일 전송 시 날짜를 파일 이름 뒤에 붙히려고 만듬.
     *  - date : 현재 날짜 시간을 알려준다.
     */
    public static String nowDate(String dateTime){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        String strNow = "";
        if(dateTime.equals("file")){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            strNow = simpleDateFormat.format(date);
        }else if(dateTime.equals("date")){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            strNow = simpleDateFormat.format(date);
        }else if(dateTime.equals("date_change")){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = simpleDateFormat.format(date);
            String time_change =
                    time.substring(0,4) + "년" + time.substring(5,7) + "월" + time.substring(8,10) + "일"
                            + time.substring(11,13) + "시" +time.substring(14,16) + "분";
            strNow = time_change;
        }
        return strNow;
    }

    public static String chattingRoomPublicKeyCreate(String login_member_email){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String time = simpleDateFormat.format(date);
        String chattingRoom_public_key = login_member_email + "_" + time;
        return chattingRoom_public_key;
    }

    /**
     * setRoundCorner() : 비트맵 이미지를 동그랗게.
     */
//    public static Bitmap setRoundCorner(Bitmap bitmap, int pixel) {
    public static Bitmap setRoundCorner(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        int color = 0xff424242;
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        paint.setColor(color);
        canvas.drawARGB(0, 0, 0, 0);
//        canvas.drawRoundRect(rectF, pixel, pixel, paint);
        canvas.drawRoundRect(rectF, 200, 200, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }



}
