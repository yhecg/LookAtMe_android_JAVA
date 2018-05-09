package com.chan.example.lookatme.activity.chat;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chan.example.lookatme.R;
import com.chan.example.lookatme.adapter.ChattingMessageListAdapter;
import com.chan.example.lookatme.function.Basic;
import com.chan.example.lookatme.service.ClientServerConnectService;
import com.chan.example.lookatme.sqlite.DBHelper_chattingMessage;
import com.chan.example.lookatme.sqlite.DBHelper_chattingRoom;
import com.chan.example.lookatme.vo.ChattingMessageVo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * 채팅창 화면 (채팅방 안)
 *
 */
public class ChattingActivity extends AppCompatActivity {

    private static final String TAG = ChattingActivity.class.getSimpleName();

    // 현재 채팅방에 회원 초대하는 창으로 가는 코드.
    private static final int requestCode_member_add = 100;

    // 디바이스 갤러리로 가는 코드.
    private static final int requestCode_album_image_send = 200;

    /**
     * BindView
     *  - toolbar : 화면 맨 상단에 있는 툴바.
     *  - editText_chatting_msg_input : 상대방에게 전달할 채팅 내용을 입력하는 EditText.
     *  - button_chatting_msg_insert : 상대방에게 채팅 내용을 전달하는 Button.
     *  - recyclerView_chatting_msg_list : 해당 채팅방 안에 있는 내용들을 보여주는 리스트뷰.
     */
    @BindView(R.id.toolBar) Toolbar toolbar;
    @BindView(R.id.editText_chatting_msg_input) EditText editText_chatting_msg_input;
    @BindView(R.id.button_chatting_msg_insert) Button button_chatting_msg_insert;
    @BindView(R.id.recyclerView_chatting_msg_list) RecyclerView recyclerView_chatting_msg_list;

    /**
     * 변수
     *  - login_member_email : 현재 로그인한 회원의 이메일
     *  - login_member_name : 현재 로그인한 회원의 이름
     *  - login_member_image : 현재 로그인한 회원의 이미지
     *  - adapter_chattingMessage : 채팅 메세지 Adapter
     *  - arrayList_chattingMessage : 채팅 메세지 ArrayList
     */
    String login_member_email;
    String login_member_name;
    String login_member_image;
    ChattingMessageListAdapter adapter_chattingMessage;
    ArrayList<ChattingMessageVo> arrayList_chattingMessage;

    /**
     * 변수
     *  - dbHelper_chattingRoom : 채팅방 DB.
     *  - dbHelper_chattingMessage : 채팅메세지 DB.
     *  - chattingRoom_public_key : 현재 입장한 방 고유 키
     *  - chattingRoom_accept_member_email : 현재 입장한 채팅방에 있는 회원들 이메일(JSON)
     *  - chattingRoom_accept_member_name : 현재 입장한 채팅방에 있는 회원들 이름(JSON)
     *  - chattingRoom_accept_member_image : 현재 입장한 채팅방에 있는 회원들 이미지(JSON)
     *  - chattingRoom_name : 현재 입장한 채팅방 이름.
     */
    DBHelper_chattingRoom dbHelper_chattingRoom;
    DBHelper_chattingMessage dbHelper_chattingMessage;
    String chattingRoom_public_key;
    String chattingRoom_accept_member_email;
    String chattingRoom_accept_member_name;
    String chattingRoom_accept_member_image;
    String chattingRoom_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);
        ButterKnife.bind(this);

        dbHelper_chattingRoom = new DBHelper_chattingRoom(this, "chattingRoom.db", null, 1);
        dbHelper_chattingMessage = new DBHelper_chattingMessage(this, "chattingMessage.db", null, 1);

        // 로그인한 회원 이메일.
        SharedPreferences spf_login_member = getSharedPreferences("loginMember", MODE_PRIVATE);
        login_member_email = spf_login_member.getString("loginMember_email", "");

        // 이전 액티비티에서 넘어온 인텐트 정보.
        Bundle extras = getIntent().getExtras();
        chattingRoom_public_key = extras.getString("chattingRoom_public_key");

        // 쉐어드에 담긴 정보가 현재 채팅방의 정보와 같다면 알림을 없애준다.
        SharedPreferences spf = getSharedPreferences("notificationCheck", Context.MODE_PRIVATE);
        if(chattingRoom_public_key.equals(spf.getString("chattingRoom_public_key",""))){
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(0);
        }

        // 읽지 않은 채팅 메세지 수 초기화.
        dbHelper_chattingRoom.update_chattingRoom_message_unread_count_reset(chattingRoom_public_key);

        String db_chattingRoom_information = dbHelper_chattingRoom.selectChattingRoomInformation(chattingRoom_public_key);
        try {
            JSONObject jsonObject = new JSONObject(db_chattingRoom_information);
            chattingRoom_accept_member_email = jsonObject.getString("chattingRoom_accept_member_email");
            chattingRoom_accept_member_name = jsonObject.getString("chattingRoom_accept_member_name");
            chattingRoom_accept_member_image = jsonObject.getString("chattingRoom_accept_member_image");
            chattingRoom_name = jsonObject.getString("chattingRoom_name");
            toolBarSetting(chattingRoom_name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        listViewAdapterSetting();

        new ChattingLoginMemberInformationServerData().execute(); // 현재 로그인한 회원의 이름과 프로필 이미지 정보를 받아온다.

    }

    /**
     * OnClick 이벤트
     *  - chatting_message_send : 화면 하단 우측에 있는 버튼. 채팅 메세지를 보내는 버튼.
     */
    @OnClick(R.id.button_chatting_msg_insert) void chatting_message_send(){
        String message = editText_chatting_msg_input.getText().toString();
        if(message.equals("") || message == null || TextUtils.isEmpty(message) || message.matches("\\s+") || message.length()==0){
            Toast.makeText(this, "메세지를 입력해주세요.", Toast.LENGTH_SHORT).show();
        }else{
            editText_chatting_msg_input.setText("");
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("data_type", "chatting_message"); // 데이터 타입은 채팅 메세지
                jsonObject.put("chattingRoom_public_key", chattingRoom_public_key); // 채팅방 고유 키.
                jsonObject.put("chattingMessage_send_member_email", login_member_email); // 채팅 메세지 보내는 사람의 이메일.
                jsonObject.put("chattingMessage_send_member_image", login_member_image); // 채팅 메세지 보내는 사람의 이미지.
                jsonObject.put("chattingMessage_send_member_name", login_member_name); // 채팅 메세지 보내는 사람의 이름.
                jsonObject.put("chattingMessage_contents", message); // 채팅 메세지 내용.
                jsonObject.put("chattingMessage_time", Basic.nowDate("date")); // 채팅 메세지 보낸 시간.
                jsonObject.put("chattingMessage_type", "message"); // 메세지 타입은 이미지.
                jsonObject.put("chattingRoom_accept_member_email", chattingRoom_accept_member_email); // 채팅방에 있는 사용자들 이메일. 서버에서 이 사용자들에게만 메세지를 보내주기 위해.

                dbHelper_chattingMessage.insert(
                        jsonObject.getString("chattingRoom_public_key"), jsonObject.getString("chattingMessage_send_member_email"),
                        jsonObject.getString("chattingMessage_send_member_image"), jsonObject.getString("chattingMessage_send_member_name"), jsonObject.getString("chattingMessage_contents"),
                        jsonObject.getString("chattingMessage_time"), jsonObject.getString("chattingMessage_type")
                );
                dbHelper_chattingRoom.updateChattingRoom_messageContents_messageTime(
                        jsonObject.getString("chattingMessage_contents"),jsonObject.getString("chattingMessage_time"),
                        0,jsonObject.getString("chattingRoom_public_key") // 0 은 채팅메세지 보낸 자신이기 때문에 읽지 않은 채팅메세지 수를 +1 증가시킬 필요가 없다.
                );

                arrayList_chattingMessage.add(new ChattingMessageVo(
                        jsonObject.getString("chattingMessage_send_member_email"),jsonObject.getString("chattingMessage_send_member_image"),jsonObject.getString("chattingMessage_send_member_name"),
                        jsonObject.getString("chattingMessage_contents"),jsonObject.getString("chattingMessage_time"),jsonObject.getString("chattingMessage_type")
                ));
                adapter_chattingMessage.notifyItemInserted(adapter_chattingMessage.getItemCount()-1);
                recyclerView_chatting_msg_list.scrollToPosition(adapter_chattingMessage.getItemCount()-1);

                ClientServerConnectService.dataOutputStream.writeUTF(jsonObject.toString());
                ClientServerConnectService.dataOutputStream.flush();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 서비스 관련
     * clientServerConnectService, serviceConnection, mCallback
     */
    private ClientServerConnectService clientServerConnectService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // 서비스와 연결되었을 때 호출됨.
            // 서비스 객체를 전역변수로 저장.
            ClientServerConnectService.LocalBinder localBinder = (ClientServerConnectService.LocalBinder) iBinder;
            clientServerConnectService = localBinder.getService(); // 서비스가 제공하는 메소드 호출하여 서비스쪽 객체를 전달받을 수 있음.
            clientServerConnectService.registerCallback(mCallback); // 콜백 등록
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // 서비스와 연결이 끊겼을 때 호출되는 메소드
        }
    };
    private ClientServerConnectService.ICallback mCallback = new ClientServerConnectService.ICallback() {
        @Override
        public void receiveData(String data) {
            try {
                JSONObject jsonObject = new JSONObject(data);
                String data_type = jsonObject.getString("data_type"); // 데이터 타입은 채팅 메세지
                switch(data_type){
                    case "chatting_message": // 다른 사용자가 보낸 채팅 메세지일 경우
                        dataType_chatting_message(data);
                        break;
                    case "room":
                        String room_type = jsonObject.getString("room_type");
                        switch(room_type){
                            case "invite": // 채팅방에 새로운 회원이 초대되었을때
                                dataType_room__roomType_invite(data);
                                break;
                            case "exit":
                                dataType_room__roomType_exit(data);
                                break;
                        }
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 서비스 관련 메소드
     *  - dataType_chatting_message : 다른 사용자가 보낸 체팅메세지일 경우
     *  - dataType_room__roomType_invite : 해당 채팅방에 회원이 초대된 경우
     *  - dataType_room__roomType_exit : 채팅방에서 사람이 나갔을때
     */
    private void dataType_chatting_message(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
            String data_chattingRoom_public_key = jsonObject.getString("chattingRoom_public_key"); // 채팅방 고유 키.
            String data_chattingMessage_send_member_email = jsonObject.getString("chattingMessage_send_member_email"); // 채팅 메세지 보내는 사람의 이메일.
            String data_chattingMessage_send_member_image = jsonObject.getString("chattingMessage_send_member_image"); // 채팅 메세지 보내는 사람의 이미지.
            String data_chattingMessage_send_member_name = jsonObject.getString("chattingMessage_send_member_name"); // 채팅 메세지 보내는 사람의 이름.
            String data_chattingMessage_contents = jsonObject.getString("chattingMessage_contents"); // 채팅 메세지 내용.
            String data_chattingMessage_time = jsonObject.getString("chattingMessage_time"); // 채팅 메세지 보낸 시간.
            String data_chattingMessage_type = jsonObject.getString("chattingMessage_type"); // 메세지 타입

            if(chattingRoom_public_key.equals(data_chattingRoom_public_key)){
                arrayList_chattingMessage.add(new ChattingMessageVo(
                        data_chattingMessage_send_member_email,data_chattingMessage_send_member_image,data_chattingMessage_send_member_name,
                        data_chattingMessage_contents,data_chattingMessage_time,data_chattingMessage_type
                ));
                Thread thread = new Thread(){
                    Handler handler = new Handler(Looper.getMainLooper()){
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            adapter_chattingMessage.notifyItemInserted(adapter_chattingMessage.getItemCount()-1);
                            recyclerView_chatting_msg_list.scrollToPosition(adapter_chattingMessage.getItemCount()-1);
                        }
                    };
                    @Override
                    public void run() {
                        super.run();
                        Message message = handler.obtainMessage();
                        handler.sendMessage(message);
                    }
                };
                thread.start();
                dbHelper_chattingRoom.update_chattingRoom_message_unread_count_reset(data_chattingRoom_public_key);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void dataType_room__roomType_invite(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONObject jsonObject_origin = jsonObject.getJSONObject("origin");
            String data_chattingRoom_public_key = jsonObject_origin.getString("chattingRoom_public_key");
            String data_chattingMessage_contents = jsonObject_origin.getString("chattingMessage_contents");
            String data_chattingMessage_type = jsonObject_origin.getString("chattingMessage_type");
            if(chattingRoom_public_key.equals(data_chattingRoom_public_key)){
                chattingRoom_accept_member_email = jsonObject_origin.getString("chattingRoom_accept_member_email");
                chattingRoom_accept_member_name = jsonObject_origin.getString("chattingRoom_accept_member_name");
                chattingRoom_accept_member_image = jsonObject_origin.getString("chattingRoom_accept_member_image");

                arrayList_chattingMessage.add(new ChattingMessageVo("","","",data_chattingMessage_contents,"",data_chattingMessage_type));
                Thread thread = new Thread(){
                    Handler handler = new Handler(Looper.getMainLooper()){
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
//                            adapter_chattingMessage.notifyDataSetChanged();
                            adapter_chattingMessage.notifyItemInserted(adapter_chattingMessage.getItemCount()-1);
                            recyclerView_chatting_msg_list.scrollToPosition(adapter_chattingMessage.getItemCount()-1);
                        }
                    };
                    @Override
                    public void run() {
                        super.run();
                        Message message = handler.obtainMessage();
                        handler.sendMessage(message);
                    }
                };
                thread.start();
//                adapter_chattingMessage.notifyItemInserted(adapter_chattingMessage.getItemCount()-1);
//                recyclerView_chatting_msg_list.scrollToPosition(adapter_chattingMessage.getItemCount()-1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void dataType_room__roomType_exit(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
            String data_chattingRoom_public_key = jsonObject.getString("chattingRoom_public_key");
            String data_chattingRoom_accept_member_email = jsonObject.getString("chattingRoom_accept_member_email");
            String data_chattingRoom_accept_member_name = jsonObject.getString("chattingRoom_accept_member_name");
            String data_chattingRoom_accept_member_image = jsonObject.getString("chattingRoom_accept_member_image");
            String data_chattingMessage_contents = jsonObject.getString("chattingMessage_contents");
            String data_chattingMessage_type= jsonObject.getString("chattingMessage_type");

            if(data_chattingRoom_public_key.equals(chattingRoom_public_key)){
                chattingRoom_accept_member_email = data_chattingRoom_accept_member_email;
                chattingRoom_accept_member_name = data_chattingRoom_accept_member_name;
                chattingRoom_accept_member_image = data_chattingRoom_accept_member_image;

                arrayList_chattingMessage.add(new ChattingMessageVo("","","",data_chattingMessage_contents,"",data_chattingMessage_type));
                Thread thread = new Thread(){
                    Handler handler = new Handler(Looper.getMainLooper()){
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            adapter_chattingMessage.notifyItemInserted(adapter_chattingMessage.getItemCount()-1);
                            recyclerView_chatting_msg_list.scrollToPosition(adapter_chattingMessage.getItemCount()-1);
                        }
                    };
                    @Override
                    public void run() {
                        super.run();
                        Message message = handler.obtainMessage();
                        handler.sendMessage(message);
                    }
                };
                thread.start();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(resultCode){
            case Activity.RESULT_OK:
                switch(requestCode){
                    case requestCode_member_add:
                        String db_chattingRoom_information = dbHelper_chattingRoom.selectChattingRoomInformation(chattingRoom_public_key);
                        try {
                            JSONObject jsonObject = new JSONObject(db_chattingRoom_information);
                            chattingRoom_accept_member_email = jsonObject.getString("chattingRoom_accept_member_email");
                            chattingRoom_accept_member_name = jsonObject.getString("chattingRoom_accept_member_name");
                            chattingRoom_accept_member_image = jsonObject.getString("chattingRoom_accept_member_image");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case requestCode_album_image_send:
                        String chatting_image_name = login_member_email + "_" + Basic.nowDate("file") + ".jpg";
                        new ChattingImageSendServerData().execute(chatting_image_name,getImageFilePath(data.getData()));
                        break;
                }
                break;
        }
    }

    /**
     * getImageFilePath : 앨범에서 가져온 파일 경로.
     * ChattingImageSendServerData : 상대방에게 이미지 파일 전송.
     */
    public String getImageFilePath(Uri data) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = this.getContentResolver().query(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String imgPath = cursor.getString(column_index);
        return imgPath;
    }
    private class ChattingImageSendServerData extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            String chatting_image_name = strings[0];
            String image_file_path = strings[1];
            try {
                OkHttpClient client = new OkHttpClient();
                MultipartBody multipartBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("chatting_img_file", chatting_image_name, RequestBody.create(MultipartBody.FORM, new File(image_file_path)))
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_chat_php_directory_url+"chatting_image_send.php")
                        .post(multipartBody)
                        .build();
                client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return chatting_image_name;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                String chatting_image_name = result;

                JSONObject jsonObject_chatting = new JSONObject();
                jsonObject_chatting.put("data_type", "chatting_message"); // 데이터 타입은 채팅 메세지
                jsonObject_chatting.put("chattingRoom_public_key", chattingRoom_public_key); // 채팅방 고유 키.
                jsonObject_chatting.put("chattingMessage_send_member_email", login_member_email); // 채팅 메세지 보내는 사람의 이메일.
                jsonObject_chatting.put("chattingMessage_send_member_image", login_member_image); // 채팅 메세지 보내는 사람의 이미지.
                jsonObject_chatting.put("chattingMessage_send_member_name", login_member_name); // 채팅 메세지 보내는 사람의 이름.
                jsonObject_chatting.put("chattingMessage_contents", chatting_image_name); // 채팅 메세지 내용. 이미지 파일 이름.
                jsonObject_chatting.put("chattingMessage_time", Basic.nowDate("date")); // 채팅 메세지 보낸 시간.
                jsonObject_chatting.put("chattingMessage_type", "image"); // 메세지 타입은 이미지.
                jsonObject_chatting.put("chattingRoom_accept_member_email", chattingRoom_accept_member_email); // 채팅방에 있는 사용자들 이메일. 서버에서 이 사용자들에게만 메세지를 보내주기 위해.

                dbHelper_chattingMessage.insert(
                        jsonObject_chatting.getString("chattingRoom_public_key"), jsonObject_chatting.getString("chattingMessage_send_member_email"),
                        jsonObject_chatting.getString("chattingMessage_send_member_image"), jsonObject_chatting.getString("chattingMessage_send_member_name"),
                        jsonObject_chatting.getString("chattingMessage_contents"), jsonObject_chatting.getString("chattingMessage_time"),
                        jsonObject_chatting.getString("chattingMessage_type")
                );
                dbHelper_chattingRoom.updateChattingRoom_messageContents_messageTime(
                        "사진",jsonObject_chatting.getString("chattingMessage_time"),
                        0,jsonObject_chatting.getString("chattingRoom_public_key") // 0 은 채팅메세지 보낸 자신이기 때문에 읽지 않은 채팅메세지 수를 +1 증가시킬 필요가 없다.
                );

                arrayList_chattingMessage.add(new ChattingMessageVo(
                        jsonObject_chatting.getString("chattingMessage_send_member_email"),jsonObject_chatting.getString("chattingMessage_send_member_image"),
                        jsonObject_chatting.getString("chattingMessage_send_member_name"),jsonObject_chatting.getString("chattingMessage_contents"),
                        jsonObject_chatting.getString("chattingMessage_time"),jsonObject_chatting.getString("chattingMessage_type")
                ));
                adapter_chattingMessage.notifyItemInserted(adapter_chattingMessage.getItemCount()-1);
                recyclerView_chatting_msg_list.scrollToPosition(adapter_chattingMessage.getItemCount()-1);

                ClientServerConnectService.dataOutputStream.writeUTF(jsonObject_chatting.toString());
                ClientServerConnectService.dataOutputStream.flush();

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onResume() {
        Intent intent_bindService = new Intent(ChattingActivity.this, ClientServerConnectService.class);
        bindService(intent_bindService, serviceConnection, Context.BIND_AUTO_CREATE);

        arrayList_chattingMessage.clear();
        String db_chattingMessage_list = dbHelper_chattingMessage.selectChattingMessageList(chattingRoom_public_key);
        if(!db_chattingMessage_list.equals("no_data")){
            try {
                JSONArray jsonArray = new JSONArray(db_chattingMessage_list);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String chattingMessage_send_member_email = jsonObject.getString("chattingMessage_send_member_email");
                    String chattingMessage_send_member_image = jsonObject.getString("chattingMessage_send_member_image");
                    String chattingMessage_send_member_name = jsonObject.getString("chattingMessage_send_member_name");
                    String chattingMessage_contents = jsonObject.getString("chattingMessage_contents");
                    String chattingMessage_time = jsonObject.getString("chattingMessage_time");
                    String chattingMessage_type = jsonObject.getString("chattingMessage_type");
                    arrayList_chattingMessage.add(
                            new ChattingMessageVo(
                                    chattingMessage_send_member_email,chattingMessage_send_member_image,chattingMessage_send_member_name,
                                    chattingMessage_contents,chattingMessage_time,chattingMessage_type));
                }
                adapter_chattingMessage.notifyDataSetChanged();
                recyclerView_chatting_msg_list.scrollToPosition(adapter_chattingMessage.getItemCount()-1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        dbHelper_chattingRoom.update_notification_status_1_notiNo(chattingRoom_public_key);

        super.onResume();
    }

    @Override
    protected void onPause() {
        unbindService(serviceConnection);
        dbHelper_chattingRoom.update_notification_status_0_notiOk(chattingRoom_public_key);
        super.onPause();
    }

    /**
     * 툴바 관련.
     *  - toolBarSetting_false : 툴바 설정.
     *  - onCreateOptionsMenu : 툴바 메뉴
     *  - onOptionsItemSelected : 툴바 선택
     */
    private void toolBarSetting(String chattingRoom_name){
        toolbar.setTitleTextColor(Color.parseColor("#BA68C8"));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); // 커스터마이징 하기 위해 필요.
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(chattingRoom_name);
        actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatting_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.chattingRoom_member_invite: // 회원 초대
                Intent intent = new Intent(this, ChattingInviteActivity.class);
                intent.putExtra("previous_activity", "ChattingActivity");
                intent.putExtra("chattingRoom_public_key", chattingRoom_public_key);
                intent.putExtra("chattingRoom_accept_member_email", chattingRoom_accept_member_email);
                intent.putExtra("chattingRoom_accept_member_name", chattingRoom_accept_member_name);
                intent.putExtra("chattingRoom_accept_member_image", chattingRoom_accept_member_image);
                startActivityForResult(intent, requestCode_member_add);
                break;
            case R.id.chatting_message_send:
                Intent intent_album = new Intent(Intent.ACTION_PICK);
                intent_album.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent_album.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent_album, requestCode_album_image_send);
                break;
        }
        return true;
    }

    /**
     * class
     *  - ChattingLoginMemberInformationServerData : 채팅 화면이 처음 보여질때 현재 로그인한 회원의 이름과 프로필 이미지 정보를 받아온다.
     */
    private class ChattingLoginMemberInformationServerData extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("login_member_email",login_member_email)
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_chat_php_directory_url+"chatting_activity_login_member_information.php")
                        .post(requestBody)
                        .build();
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                login_member_image = jsonObject.getString("login_member_image");
                login_member_name = jsonObject.getString("login_member_name");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * listViewAdapterSetting : 리스트뷰 관련 셋팅.
     */
    private void listViewAdapterSetting(){
        recyclerView_chatting_msg_list.setHasFixedSize(true);
//        recyclerView_chatting_msg_list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
//        recyclerView_chatting_msg_list.setLayoutManager(new LinearLayoutManager(this));
        arrayList_chattingMessage = new ArrayList<>();
        adapter_chattingMessage = new ChattingMessageListAdapter(this, arrayList_chattingMessage);
        recyclerView_chatting_msg_list.setAdapter(adapter_chattingMessage);
    }

}
