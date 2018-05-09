package com.chan.example.lookatme.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.chan.example.lookatme.R;
import com.chan.example.lookatme.activity.chat.ChattingActivity;
import com.chan.example.lookatme.function.Basic;
import com.chan.example.lookatme.sqlite.DBHelper_chattingMessage;
import com.chan.example.lookatme.sqlite.DBHelper_chattingRoom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class ClientServerConnectService extends Service {

    public ClientServerConnectService(){ }

    private static final String TAG = ClientServerConnectService.class.getSimpleName();

    private Socket socket;
    private String login_member_email;
    public static DataInputStream dataInputStream;
    public static DataOutputStream dataOutputStream;
    DBHelper_chattingRoom dbHelper_chattingRoom;
    DBHelper_chattingMessage dbHelper_chattingMessage;

    @Override
    public void onCreate() {
        super.onCreate();

        // 로그인한 회원 이메일.
        SharedPreferences spf_login_member = getSharedPreferences("loginMember", MODE_PRIVATE);
        login_member_email = spf_login_member.getString("loginMember_email", "");

        dbHelper_chattingRoom = new DBHelper_chattingRoom(getApplicationContext(), "chattingRoom.db", null, 1);
        dbHelper_chattingMessage = new DBHelper_chattingMessage(getApplicationContext(), "chattingMessage.db", null, 1);

        new DataReceive().start();

    }

    private class DataReceive extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                socket = new Socket(Basic.ip, Basic.port);
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                JSONObject jsonObject_start = new JSONObject();
                jsonObject_start.put("data_type","member_information"); // 소켓 서버에서 받는 데이터 종류가 현재 접속한 회원의 정보.
                jsonObject_start.put("login_member_email",login_member_email); // 현재 로그인한(접속한) 회원의 이메일.
                dataOutputStream.writeUTF(jsonObject_start.toString());
                dataOutputStream.flush();

                String data = dataInputStream.readUTF();
                while(data != null){
                    if(data.length() == 0){
                        continue;
                    }
                    JSONObject jsonObject = new JSONObject(data);
                    String data_type = jsonObject.getString("data_type");

                    switch (data_type){
                        case "room": // 채팅방과 관련된 경우
                            String room_type = jsonObject.getString("room_type");
                            switch (room_type){
                                case "create": // 새로운 채팅방 생성
                                    dataType_room__roomType_create(data);
                                    break;
                                case "invite": // 채팅방에 회원이 초대된 경우
                                    dataType_room__roomType_invite(data);
                                    break;
                                case "exit":
                                    dataType_room__roomType_exit(data);
                                    break;
                            }
                            break;
                        case "chatting_message": // 다른 사용자가 보낸 채팅 메세지일 경우
                            String chattingMessage_type = jsonObject.getString("chattingMessage_type");
                            switch(chattingMessage_type){
                                case "message": // 메세지 타입이 메세지일 경우
                                    dataType_chattingMessage__chattingMessageType_message(data);
                                    break;
                                case "image": // 메세지 타입이 이미지일 경우
                                    dataType_chattingMessage__chattingMessageType_image(data);
                                    break;
                            }
                            break;
                    }

                    data = dataInputStream.readUTF();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * dataType_room__roomType_create : 채팅방이 새로 생성되었을때 DB에 채팅방 정보를 insert 시켜주고 채팅 메세지에도 insert 시켜준다.(초대되었다는)
     * dataType_chattingMessage__chattingMessageType_message : 다른 사용자가 보낸 채팅 메세지(메세지).
     * dataType_chattingMessage__chattingMessageType_image : 다른 사용자가 보낸 채팅 메세지(이미지).
     * dataType_room__roomType_invite : 채팅방에 사용자가 초대되었을 경우
     * dataType_room__roomType_exit : 채팅방에 사용자가 나갔을 경우
     */
    private void dataType_room__roomType_create(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
            String chattingRoom_public_key = jsonObject.getString("chattingRoom_public_key"); // 사용자들이 모두 공통적으로 가진 같은 이름의 방 key.
            String chattingRoom_accept_member_email = jsonObject.getString("chattingRoom_accept_member_email"); // 채팅방에 들어 있는 회원들의 이메일.
            String chattingRoom_accept_member_name = jsonObject.getString("chattingRoom_accept_member_name"); // 채팅방에 들어 있는 회원들의 이름.
            String chattingRoom_accept_member_image = jsonObject.getString("chattingRoom_accept_member_image"); // 채팅방에 들어 있는 회원들의 이미지.
            String chattingRoom_name = jsonObject.getString("chattingRoom_name"); // 채팅방 이름.
            String chattingRoom_message_contents = jsonObject.getString("chattingRoom_message_contents"); // 채팅방 목록에서 보여질 마지막 채팅 내용.
            String chattingRoom_message_time = jsonObject.getString("chattingRoom_message_time"); // 채팅방 목록에서 보여질 마지막 채팅 메세지의 시간.
            String chattingMessage_type = jsonObject.getString("chattingMessage_type"); // 채팅메세지 타입.

            dbHelper_chattingRoom.insert(chattingRoom_public_key,chattingRoom_accept_member_email,chattingRoom_accept_member_name,chattingRoom_accept_member_image,chattingRoom_name,chattingRoom_message_contents,chattingRoom_message_time);
            dbHelper_chattingMessage.insert(chattingRoom_public_key,"","","",chattingRoom_message_contents,"",chattingMessage_type);

            notificationSetting(chattingRoom_public_key, chattingRoom_message_contents);

            mCallback.receiveData(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void dataType_chattingMessage__chattingMessageType_message(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
            String chattingRoom_public_key = jsonObject.getString("chattingRoom_public_key"); // 채팅방 고유 키.
            String chattingMessage_send_member_email = jsonObject.getString("chattingMessage_send_member_email"); // 채팅 메세지 보내는 사람의 이메일.
            String chattingMessage_send_member_image = jsonObject.getString("chattingMessage_send_member_image"); // 채팅 메세지 보내는 사람의 이미지.
            String chattingMessage_send_member_name = jsonObject.getString("chattingMessage_send_member_name"); // 채팅 메세지 보내는 사람의 이름.
            String chattingMessage_contents = jsonObject.getString("chattingMessage_contents"); // 채팅 메세지 내용.
            String chattingMessage_time = jsonObject.getString("chattingMessage_time"); // 채팅 메세지 보낸 시간.
            String chattingMessage_type = jsonObject.getString("chattingMessage_type"); // 메세지 타입

            dbHelper_chattingMessage.insert(
                    chattingRoom_public_key,chattingMessage_send_member_email,chattingMessage_send_member_image,chattingMessage_send_member_name,
                    chattingMessage_contents,chattingMessage_time,chattingMessage_type
            );
            dbHelper_chattingRoom.updateChattingRoom_messageContents_messageTime(
                    chattingMessage_contents,chattingMessage_time,
                    1,chattingRoom_public_key // 1은 읽지 않은 채팅 메세지수를 +1 카운트 해주기 위해서.
            );

            if(dbHelper_chattingRoom.select_notification_status(chattingRoom_public_key)==0){
                String notification_contents = chattingMessage_send_member_name + " : " + chattingMessage_contents;
                notificationSetting(chattingRoom_public_key, notification_contents);
            }

            mCallback.receiveData(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void dataType_chattingMessage__chattingMessageType_image(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
            String chattingRoom_public_key = jsonObject.getString("chattingRoom_public_key"); // 채팅방 고유 키.
            String chattingMessage_send_member_email = jsonObject.getString("chattingMessage_send_member_email"); // 채팅 메세지 보내는 사람의 이메일.
            String chattingMessage_send_member_image = jsonObject.getString("chattingMessage_send_member_image"); // 채팅 메세지 보내는 사람의 이미지.
            String chattingMessage_send_member_name = jsonObject.getString("chattingMessage_send_member_name"); // 채팅 메세지 보내는 사람의 이름.
            String chattingMessage_contents = jsonObject.getString("chattingMessage_contents"); // 채팅 메세지 내용.
            String chattingMessage_time = jsonObject.getString("chattingMessage_time"); // 채팅 메세지 보낸 시간.
            String chattingMessage_type = jsonObject.getString("chattingMessage_type"); // 메세지 타입

            dbHelper_chattingMessage.insert(
                    chattingRoom_public_key,chattingMessage_send_member_email,chattingMessage_send_member_image,chattingMessage_send_member_name,
                    chattingMessage_contents,chattingMessage_time,chattingMessage_type
            );
            dbHelper_chattingRoom.updateChattingRoom_messageContents_messageTime(
                    "사진",chattingMessage_time,
                    1,chattingRoom_public_key // 1은 읽지 않은 채팅 메세지수를 +1 카운트 해주기 위해서.
            );

            if(dbHelper_chattingRoom.select_notification_status(chattingRoom_public_key)==0){
                String notification_contents = chattingMessage_send_member_name + " : 사진";
                notificationSetting(chattingRoom_public_key, notification_contents);
            }

            mCallback.receiveData(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void dataType_room__roomType_invite(String data){
        try {
            JSONObject jsonObject_data = new JSONObject(data);

            // 해당 채팅방에 기존에 있던 회원들
            JSONArray jsonArray_origin_member = jsonObject_data.getJSONArray("origin_member");
            for(int i=0; i<jsonArray_origin_member.length(); i++){
                JSONObject jsonObject_member_email = jsonArray_origin_member.getJSONObject(i);
                String member_email = jsonObject_member_email.getString("member_email");
                if(login_member_email.equals(member_email)){
                    JSONObject jsonObject_origin = jsonObject_data.getJSONObject("origin");
                    String chattingRoom_public_key = jsonObject_origin.getString("chattingRoom_public_key");

                    String chattingRoom_accept_member_email = jsonObject_origin.getString("chattingRoom_accept_member_email");
                    String chattingRoom_accept_member_name = jsonObject_origin.getString("chattingRoom_accept_member_name");
                    String chattingRoom_accept_member_image = jsonObject_origin.getString("chattingRoom_accept_member_image");
                    dbHelper_chattingRoom.update_chattingRoom_information(
                            chattingRoom_public_key, chattingRoom_accept_member_email,chattingRoom_accept_member_name,chattingRoom_accept_member_image);

                    String chattingMessage_contents = jsonObject_origin.getString("chattingMessage_contents");
                    String chattingMessage_type = jsonObject_origin.getString("chattingMessage_type");
                    dbHelper_chattingMessage.insert(
                            chattingRoom_public_key,"","","",chattingMessage_contents,"",chattingMessage_type);
                }
            }

            // 해당 채팅방에 새롭게 초대된 회원들
            JSONArray jsonArray_new_member = jsonObject_data.getJSONArray("new_member");
            for(int i=0; i<jsonArray_new_member.length(); i++){
                JSONObject jsonObject_member_email = jsonArray_new_member.getJSONObject(i);
                String member_email = jsonObject_member_email.getString("member_email");
                if(login_member_email.equals(member_email)){
                    JSONObject jsonObject_new = jsonObject_data.getJSONObject("new");
                    String chattingRoom_public_key = jsonObject_new.getString("chattingRoom_public_key");
                    String chattingRoom_accept_member_email = jsonObject_new.getString("chattingRoom_accept_member_email");
                    String chattingRoom_accept_member_name = jsonObject_new.getString("chattingRoom_accept_member_name");
                    String chattingRoom_accept_member_image = jsonObject_new.getString("chattingRoom_accept_member_image");
                    String chattingRoom_name = jsonObject_new.getString("chattingRoom_name");
                    String chattingRoom_message_contents = jsonObject_new.getString("chattingRoom_message_contents");
                    String chattingRoom_message_time = jsonObject_new.getString("chattingRoom_message_time");
                    String chattingMessage_type = jsonObject_new.getString("chattingMessage_type");

                    dbHelper_chattingRoom.insert(chattingRoom_public_key,chattingRoom_accept_member_email,chattingRoom_accept_member_name,chattingRoom_accept_member_image,chattingRoom_name,chattingRoom_message_contents,chattingRoom_message_time);
                    dbHelper_chattingMessage.insert(chattingRoom_public_key,"","","",chattingRoom_message_contents,"",chattingMessage_type);

                    notificationSetting(chattingRoom_public_key, chattingRoom_message_contents);
                }
            }

            mCallback.receiveData(data);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    private void dataType_room__roomType_exit(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            String data_type = jsonObject.getString("data_type");
            String room_type = jsonObject.getString("room_type");
            String chattingRoom_public_key = jsonObject.getString("chattingRoom_public_key");
            String exit_member_email = jsonObject.getString("exit_member_email");
            String exit_member_name = jsonObject.getString("exit_member_name");
            String exit_member_image = jsonObject.getString("exit_member_image");

            String db_chattingRoom_information = dbHelper_chattingRoom.selectChattingRoomInformation(chattingRoom_public_key);
            JSONObject jsonObject_chattingRoom = new JSONObject(db_chattingRoom_information);
            String chattingRoom_accept_member_email = jsonObject_chattingRoom.getString("chattingRoom_accept_member_email");
            String chattingRoom_accept_member_name = jsonObject_chattingRoom.getString("chattingRoom_accept_member_name");
            String chattingRoom_accept_member_image = jsonObject_chattingRoom.getString("chattingRoom_accept_member_image");

            JSONArray jsonArray_email = new JSONArray(chattingRoom_accept_member_email);
            for(int i=0; i<jsonArray_email.length(); i++){
                JSONObject jsonObject_email = jsonArray_email.getJSONObject(i);
                String member_email = jsonObject_email.getString("member_email");
                if(exit_member_email.equals(member_email)){
                    jsonArray_email.remove(i);
                }
            }

            JSONArray jsonArray_name = new JSONArray(chattingRoom_accept_member_name);
            for(int i=0; i<jsonArray_name.length(); i++){
                JSONObject jsonObject_name = jsonArray_name.getJSONObject(i);
                String member_name = jsonObject_name.getString("member_name");
                if(exit_member_name.equals(member_name)){
                    jsonArray_name.remove(i);
                }
            }

            JSONArray jsonArray_image = new JSONArray(chattingRoom_accept_member_image);
            for(int i=0; i<jsonArray_image.length(); i++){
                JSONObject jsonObject_image = jsonArray_image.getJSONObject(i);
                String member_image = jsonObject_image.getString("member_image");
                if(exit_member_image.equals(member_image)){
                    jsonArray_image.remove(i);
                }
            }

            dbHelper_chattingRoom.update_chattingRoom_information(
                    chattingRoom_public_key,jsonArray_email.toString(),jsonArray_name.toString(),jsonArray_image.toString());

            String message_contents = exit_member_name+"님이 채팅방을 나가셨습니다.";
            dbHelper_chattingMessage.insert(chattingRoom_public_key,"","","",message_contents,"","etc");

            JSONObject jsonObject_receiveData = new JSONObject();
            jsonObject_receiveData.put("data_type",data_type);
            jsonObject_receiveData.put("room_type",room_type);
            jsonObject_receiveData.put("chattingRoom_public_key",chattingRoom_public_key);
            jsonObject_receiveData.put("chattingRoom_accept_member_email",jsonArray_email.toString());
            jsonObject_receiveData.put("chattingRoom_accept_member_name",jsonArray_name.toString());
            jsonObject_receiveData.put("chattingRoom_accept_member_image",jsonArray_image.toString());
            jsonObject_receiveData.put("chattingMessage_contents",message_contents);
            jsonObject_receiveData.put("chattingMessage_type","etc");

            Log.d(TAG, jsonObject_receiveData.toString(2));
            String receiveData = jsonObject_receiveData.toString();

            mCallback.receiveData(receiveData);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * notificationSetting : 노티 알림.
     * @param chattingRoom_public_key : 채팅방 고유 키.
     * @param notificationContentText : 알림 내용.
     */
    private void notificationSetting(String chattingRoom_public_key, String notificationContentText){
        Intent intent = new Intent(ClientServerConnectService.this, ChattingActivity.class);
        intent.putExtra("chattingRoom_public_key", chattingRoom_public_key);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP | // 기존 액티비티 스택에 동일한 액티비티가 쌓일 경우 액티비티 하나만 남게.
                        Intent.FLAG_ACTIVITY_SINGLE_TOP // 액티비티 중복 실행 방지를 위해 기존 액티비티를 불러옴.
        );
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        /**
         * 클릭할때까지 액티비티 실행을 보류하는 PendingIntent
         * FLAG_CANCEL_CURRENT : 이전에 생성한 PendingIntent 취소하고 새롭게 만든다.
         * FLAG_NO_CREATE : 이미 생성된 PendingIntent 없다면 null 을 return, 있다면 그 팬딩인텐트를 반환, 즉 재사용 전용.
         * FLAG_ONE_SHOT : 이 flag 로 생성한 PendingIntent 는 일회용.
         * FLAG_UPDATE_CURRENT : 이미 생성된 PendingIntent 가 존재하면 해당 Intent 의 Extra Data 만 변경.
         */
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(ClientServerConnectService.this)
                        .setSmallIcon(R.drawable.icon_app_title)
                        .setContentTitle("LookAtMe")
                        .setContentText(notificationContentText)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);
        notificationManager.notify(0, builder.build());

        SharedPreferences spf = getSharedPreferences("notificationCheck", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spf.edit();
        editor.putString("chattingRoom_public_key", chattingRoom_public_key);
        editor.commit();
    }


    /**
     * 아래 함수들
     *  - ChatListFragment(채팅방 목록 화면) 와 연결시켜준다.
     */
    public class LocalBinder extends Binder{
        public ClientServerConnectService getService(){
            return ClientServerConnectService.this;
        }
    }
    // Binder 객체는 IBinder 인터페이스 상속구현 객체. (Binder : 각각 독립된 프로세서들을 연결해 주는 역할)
    // public class Binder extends Object implements IBinder
    private IBinder iBinder = new LocalBinder(); // 클라이언트가 받을 수 있는 IBinder 객체 생성
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // 액티비티에서 bindService()를 실행하면 호출됨.
        // 리턴한 mBinder 객체는 서비스와 클라이언트 사이의 인터페이스를 정의.
        return iBinder; // 서비스 객체를 리턴.
    }
    public interface ICallback{
        void receiveData(String data); // callback 인터페이스 내의 속이 없는 껍데기 함수.
    }
    private ICallback mCallback;
    public void registerCallback(ICallback iCallback){
        mCallback = iCallback;
    }
}
