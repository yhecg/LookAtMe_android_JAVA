package com.chan.example.lookatme.activity.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.chan.example.lookatme.R;
import com.chan.example.lookatme.activity.chat.ChattingActivity;
import com.chan.example.lookatme.activity.chat.ChattingInviteActivity;
import com.chan.example.lookatme.adapter.ChattingRoomListAdapter;
import com.chan.example.lookatme.function.Basic;
import com.chan.example.lookatme.function.ClearEditText;
import com.chan.example.lookatme.service.ClientServerConnectService;
import com.chan.example.lookatme.sqlite.DBHelper_chattingMessage;
import com.chan.example.lookatme.sqlite.DBHelper_chattingRoom;
import com.chan.example.lookatme.vo.ChattingRoomVo;
import com.facebook.stetho.Stetho;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;


/**
 * 채팅 목록을 나타내는 화면
 */
public class ChatListFragment extends Fragment {

    private static final String TAG = ChatListFragment.class.getSimpleName();

    public ChatListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * BindView
     *  - button_create_chatting_room : 채팅방 생성하기 위해 채팅방에 초대할 사용자들을 선택하는 창으로 이동하기 위한 버튼.
     *  - recyclerView_chatting_room_list : 채팅방 목록을 나타내는 list.
     */
    @BindView(R.id.button_create_chatting_room) FloatingActionButton button_create_chatting_room;
    @BindView(R.id.recyclerView_chatting_room_list) RecyclerView recyclerView_chatting_room_list;

    /**
     * 변수
     *  - dbHelper_chattingRoom : 채팅방 DB.
     *  - dbHelper_chattingMessage : 채팅메세지 DB.
     *  - arrayList_chattingRoom : 채팅방 목록 ArrayList.
     *  - adapter_chattingRoom : 채팅방 목록 Adapter.
     */
    DBHelper_chattingRoom dbHelper_chattingRoom;
    DBHelper_chattingMessage dbHelper_chattingMessage;
    ArrayList<ChattingRoomVo> arrayList_chattingRoom;
    ChattingRoomListAdapter adapter_chattingRoom;

    String login_member_email;
    String login_member_name;
    String login_member_image;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        ButterKnife.bind(this, view);

        dbHelper_chattingRoom = new DBHelper_chattingRoom(getActivity(), "chattingRoom.db", null, 1);
        dbHelper_chattingMessage = new DBHelper_chattingMessage(getActivity(), "chattingMessage.db", null, 1);

        SharedPreferences spf_login_member = getActivity().getSharedPreferences("loginMember", MODE_PRIVATE);
        login_member_email = spf_login_member.getString("loginMember_email", "");

        new LoginMemberInformationServerData().execute();

        listViewAdapterSetting();

        Stetho.initializeWithDefaults(getActivity());

        recyclerView_chatting_room_list.addOnItemTouchListener(
                new ChattingListItemClickListener(getActivity(), recyclerView_chatting_room_list,
                        new ChattingListItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                ChattingRoomVo vo = arrayList_chattingRoom.get(position);
                                Intent intent = new Intent(getActivity(), ChattingActivity.class);
                                intent.putExtra("chattingRoom_public_key", vo.getChattingRoom_public_key());
                                startActivity(intent);
                            }
                            @Override
                            public void onItemLongClick(View v, int position) {
                                recyclerViewItemLongClick(position);
                            }
                        })
        );

        return view;
    }

    /**
     * service bind callback
     *  - dataType_room__roomType_create : 새로운 채팅방이 생성될 경우.
     *  - dataType_chattingMessage__chattingMessageType_message : 다른 사용자가 보낸 채팅 메세지(메세지).
     *  - dateType_room__roomType_invite : 새로운 회원이 초대 되었을 경우 (초대된 회원만 해당)
     *  - dataType_room__roomType_exit : 채팅방에서 사람이 나갔을때
     */
    private void dataType_room__roomType_create(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
            String chattingRoom_public_key = jsonObject.getString("chattingRoom_public_key"); // 사용자들이 모두 공통적으로 가진 같은 이름의 방 key.
            String chattingRoom_accept_member_email = jsonObject.getString("chattingRoom_accept_member_email"); // 채팅방에 들어 있는 회원들의 이메일.
            String chattingRoom_accept_member_image = jsonObject.getString("chattingRoom_accept_member_image"); // 채팅방에 들어 있는 회원들의 이미지.
            String chattingRoom_name = jsonObject.getString("chattingRoom_name"); // 채팅방 이름.
            String chattingRoom_message_contents = jsonObject.getString("chattingRoom_message_contents"); // 채팅방 목록에서 보여질 마지막 채팅 내용.
            String chattingRoom_message_time = jsonObject.getString("chattingRoom_message_time"); // 채팅방 목록에서 보여질 마지막 채팅 메세지의 시간.

            arrayList_chattingRoom.add(0, new ChattingRoomVo(chattingRoom_public_key,chattingRoom_accept_member_email,chattingRoom_accept_member_image,chattingRoom_name,chattingRoom_message_contents,chattingRoom_message_time,0));

            Thread thread = new Thread(){
                Handler handler = new Handler(Looper.getMainLooper()){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        adapter_chattingRoom.notifyDataSetChanged();
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void dataType_chattingMessage__chattingMessageType_message(){
        arrayList_chattingRoom.clear();
        String db_chattingRoom_list = dbHelper_chattingRoom.selectChattingRoomList();
        if(!db_chattingRoom_list.equals("no_data")){
            try {
                JSONArray jsonArray = new JSONArray(db_chattingRoom_list);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String chattingRoom_public_key = jsonObject.getString("chattingRoom_public_key");
                    String chattingRoom_accept_member_email = jsonObject.getString("chattingRoom_accept_member_email");
                    String chattingRoom_accept_member_image = jsonObject.getString("chattingRoom_accept_member_image");
                    String chattingRoom_name = jsonObject.getString("chattingRoom_name");
                    String chattingRoom_message_contents = jsonObject.getString("chattingRoom_message_contents");
                    String chattingRoom_message_time = jsonObject.getString("chattingRoom_message_time");
                    int chattingRoom_message_unread_count = jsonObject.getInt("chattingRoom_message_unread_count");
                    arrayList_chattingRoom.add(new ChattingRoomVo(
                            chattingRoom_public_key,chattingRoom_accept_member_email,
                            chattingRoom_accept_member_image,chattingRoom_name,chattingRoom_message_contents,
                            chattingRoom_message_time,chattingRoom_message_unread_count));
                }
                Thread thread = new Thread(){
                    Handler handler = new Handler(Looper.getMainLooper()){
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            adapter_chattingRoom.notifyDataSetChanged();
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
    private void dateType_room__roomType_invite(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);

            JSONArray jsonArray_new_member = jsonObject.getJSONArray("new_member");
            for(int i=0; i<jsonArray_new_member.length(); i++){
                JSONObject jsonObject_new_member = jsonArray_new_member.getJSONObject(i);
                String member_email = jsonObject_new_member.getString("member_email");
                if(login_member_email.equals(member_email)){
                    JSONObject jsonObject_new = jsonObject.getJSONObject("new");
                    String data_chattingRoom_public_key = jsonObject_new.getString("chattingRoom_public_key");
                    String data_chattingRoom_accept_member_email = jsonObject_new.getString("chattingRoom_accept_member_email");
                    String data_chattingRoom_accept_member_image = jsonObject_new.getString("chattingRoom_accept_member_image");
                    String data_chattingRoom_name = jsonObject_new.getString("chattingRoom_name");
                    String data_chattingRoom_message_contents = jsonObject_new.getString("chattingRoom_message_contents");
                    String data_chattingRoom_message_time = jsonObject_new.getString("chattingRoom_message_time");

                    arrayList_chattingRoom.add(0, new ChattingRoomVo(
                            data_chattingRoom_public_key,data_chattingRoom_accept_member_email,data_chattingRoom_accept_member_image,
                            data_chattingRoom_name,data_chattingRoom_message_contents,data_chattingRoom_message_time,0));

                    Thread thread = new Thread(){
                        Handler handler = new Handler(Looper.getMainLooper()){
                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                adapter_chattingRoom.notifyDataSetChanged();
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
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void dataType_room__roomType_exit(){
        arrayList_chattingRoom.clear();
        String db_chattingRoom_list = dbHelper_chattingRoom.selectChattingRoomList();
        if(!db_chattingRoom_list.equals("no_data")){
            try {
                JSONArray jsonArray = new JSONArray(db_chattingRoom_list);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String chattingRoom_public_key = jsonObject.getString("chattingRoom_public_key");
                    String chattingRoom_accept_member_email = jsonObject.getString("chattingRoom_accept_member_email");
                    String chattingRoom_accept_member_image = jsonObject.getString("chattingRoom_accept_member_image");
                    String chattingRoom_name = jsonObject.getString("chattingRoom_name");
                    String chattingRoom_message_contents = jsonObject.getString("chattingRoom_message_contents");
                    String chattingRoom_message_time = jsonObject.getString("chattingRoom_message_time");
                    int chattingRoom_message_unread_count = jsonObject.getInt("chattingRoom_message_unread_count");
                    arrayList_chattingRoom.add(new ChattingRoomVo(
                            chattingRoom_public_key,chattingRoom_accept_member_email,
                            chattingRoom_accept_member_image,chattingRoom_name,chattingRoom_message_contents,
                            chattingRoom_message_time,chattingRoom_message_unread_count));
                }
                Thread thread = new Thread(){
                    Handler handler = new Handler(Looper.getMainLooper()){
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            adapter_chattingRoom.notifyDataSetChanged();
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

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
                String data_type = jsonObject.getString("data_type"); // 데이터 타입
                switch (data_type){
                    case "room": // 채팅방 관련일 경우
                        String room_type = jsonObject.getString("room_type"); // 채팅방 타입
                        switch (room_type){
                            case "create": // 새로운 채팅방 생성
                                dataType_room__roomType_create(data);
                                break;
                            case "invite": // 새로운 회원이 초대 되었을 경우 (초대된 회원만 해당)
                                dateType_room__roomType_invite(data);
                                break;
                            case "exit":
                                dataType_room__roomType_exit();
                                break;
                        }
                        break;
                    case "chatting_message": // 다른 사용자가 보낸 채팅 메세지일 경우
                        dataType_chattingMessage__chattingMessageType_message();
//                        String chattingMessage_type = jsonObject.getString("chattingMessage_type");
//                        switch(chattingMessage_type){
//                            case "message": // 메세지 타입이 메세지일 경우
//                                dataType_chattingMessage__chattingMessageType_message();
//                                break;
//                            case "image": // 메세지 타입이 이미지일 경우
//                                break;
//                        }
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    public void onResume() {
        Intent intent_bindService = new Intent(getActivity(), ClientServerConnectService.class);
        getActivity().bindService(intent_bindService, serviceConnection, Context.BIND_AUTO_CREATE);

        arrayList_chattingRoom.clear();
        String db_chattingRoom_list = dbHelper_chattingRoom.selectChattingRoomList();
        if(!db_chattingRoom_list.equals("no_data")){
            try {
                JSONArray jsonArray = new JSONArray(db_chattingRoom_list);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String chattingRoom_public_key = jsonObject.getString("chattingRoom_public_key");
                    String chattingRoom_accept_member_email = jsonObject.getString("chattingRoom_accept_member_email");
                    String chattingRoom_accept_member_image = jsonObject.getString("chattingRoom_accept_member_image");
                    String chattingRoom_name = jsonObject.getString("chattingRoom_name");
                    String chattingRoom_message_contents = jsonObject.getString("chattingRoom_message_contents");
                    String chattingRoom_message_time = jsonObject.getString("chattingRoom_message_time");
                    int chattingRoom_message_unread_count = jsonObject.getInt("chattingRoom_message_unread_count");
                    arrayList_chattingRoom.add(new ChattingRoomVo(
                            chattingRoom_public_key,chattingRoom_accept_member_email,
                            chattingRoom_accept_member_image,chattingRoom_name,chattingRoom_message_contents,
                            chattingRoom_message_time,chattingRoom_message_unread_count));
                }
                adapter_chattingRoom.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        getActivity().unbindService(serviceConnection);
        super.onPause();
    }

    /**
     * OnClick 클릭 이벤트
     *  - createChattingRoom : 채팅방 생성하기 위해 채팅방에 초대할 사용자들을 선택하는 창으로 이동.
     */
    @OnClick(R.id.button_create_chatting_room) void createChattingRoom_memberInvite(){
        Intent intent = new Intent(getActivity(), ChattingInviteActivity.class);
        intent.putExtra("previous_activity", "ChatListFragment");
        startActivity(intent);
    }

    /**
     * recyclerView item Long Click
     */
    private void recyclerViewItemLongClick(final int position){
        ChattingRoomVo vo = arrayList_chattingRoom.get(position);
        final String chattingRoom_public_key = vo.getChattingRoom_public_key();
        final String chattingRoom_accept_member_email = vo.getChattingRoom_accept_member_email();
        final String chattingRoom_accept_member_image = vo.getChattingRoom_accept_member_image();
        final String chattingRoom_name = vo.getChattingRoom_name();
        final String chattingRoom_message_contents = vo.getChattingRoom_message_contents();
        final String chattingRoom_message_time = vo.getChattingRoom_message_time();
        final int chattingRoom_message_unread_count = vo.getChattingRoom_message_unread_count();

        final CharSequence[] items = {"채팅방 이름 변경", "채팅방 나가기", "취소"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(chattingRoom_name);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("채팅방 이름 변경")){
                    AlertDialog.Builder builder_update = new AlertDialog.Builder(getActivity());
                    builder_update.setTitle("채팅방 이름 변경");
                    final ClearEditText clearEditText = new ClearEditText(getActivity());
                    clearEditText.setText(chattingRoom_name);
                    builder_update.setView(clearEditText);
                    builder_update.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String room_title_name = clearEditText.getText().toString();
                            dbHelper_chattingRoom.update_chattingRoom_name(chattingRoom_public_key, room_title_name);
                            arrayList_chattingRoom.set(position,
                                    new ChattingRoomVo(
                                            chattingRoom_public_key,chattingRoom_accept_member_email,chattingRoom_accept_member_image,
                                            room_title_name,chattingRoom_message_contents,chattingRoom_message_time,chattingRoom_message_unread_count
                                    )
                            );
                            adapter_chattingRoom.notifyItemChanged(position);
                        }
                    });
                    builder_update.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    });
                    builder_update.show();
                }
                else if(items[i].equals("채팅방 나가기")){
                    AlertDialog.Builder builder_exit = new AlertDialog.Builder(getActivity());
                    builder_exit.setMessage("채팅방을 나가시겠습니까?");
                    builder_exit.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                JSONArray jsonArray_email = new JSONArray(chattingRoom_accept_member_email);
                                for(int j=0; j<jsonArray_email.length(); j++){
                                    JSONObject jsonObject_email = jsonArray_email.getJSONObject(j);
                                    String member_email = jsonObject_email.getString("member_email");
                                    if(login_member_email.equals(member_email)){
                                        jsonArray_email.remove(j);
                                    }
                                }

                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("data_type","room");
                                jsonObject.put("room_type","exit");
                                jsonObject.put("chattingRoom_public_key",chattingRoom_public_key);
                                jsonObject.put("exit_member_email",login_member_email);
                                jsonObject.put("exit_member_name",login_member_name);
                                jsonObject.put("exit_member_image",login_member_image);
                                jsonObject.put("chattingRoom_accept_member_email",jsonArray_email.toString());

                                ClientServerConnectService.dataOutputStream.writeUTF(jsonObject.toString());
                                ClientServerConnectService.dataOutputStream.flush();

                                dbHelper_chattingRoom.delete_chatting_room(chattingRoom_public_key);
                                dbHelper_chattingMessage.delete_chatting_message(chattingRoom_public_key);
                                arrayList_chattingRoom.remove(position);
                                adapter_chattingRoom.notifyItemRemoved(position);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    builder_exit.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {  }
                    });
                    builder_exit.show();
                }
            }
        });
        builder.show();
    }

    /**
     * listViewAdapterSetting : 리스트뷰 관련 셋팅.
     */
    private void listViewAdapterSetting(){
        recyclerView_chatting_room_list.setHasFixedSize(true);
        recyclerView_chatting_room_list.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView_chatting_room_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        arrayList_chattingRoom = new ArrayList<>();
        adapter_chattingRoom = new ChattingRoomListAdapter(getActivity(), arrayList_chattingRoom);
        recyclerView_chatting_room_list.setAdapter(adapter_chattingRoom);
    }

    /**
     * class
     *  - ChattingListItemClickListener : 채팅 목록 리스트의 아이템을 클릭했을때 일어나는 이벤트 리스너.
     */
    private static class ChattingListItemClickListener extends RecyclerView.SimpleOnItemTouchListener{
        public interface OnItemClickListener{
            void onItemClick(View view, int position);
            void onItemLongClick(View v, int position);
        }
        private OnItemClickListener mListener;
        private GestureDetector mGestureDetector;
        public ChattingListItemClickListener(Context context , final RecyclerView recyclerView, OnItemClickListener listener){
            this.mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
                @Override
                public void onLongPress(MotionEvent e) {
                    View view = recyclerView.findChildViewUnder(e.getX(),e.getY());
                    if(view != null && mListener != null){
                        mListener.onItemLongClick(view, recyclerView.getChildAdapterPosition(view));
                    }
                }
            });
        }
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(),e.getY());
            if(child != null && mListener != null && mGestureDetector.onTouchEvent(e)){
                mListener.onItemClick(child, rv.getChildAdapterPosition(child));
                return true;
            }
            return false;
        }
    }

    /**
     * LoginMemberInformationServerData : 로그인한 회원 정보
     */
    private class LoginMemberInformationServerData extends AsyncTask<Void,String,String>{
        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("login_member_email", login_member_email)
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_main_php_directory_url+"chatlistfragment_loginmemberinformation.php")
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
                login_member_name = jsonObject.getString("login_member_name");
                login_member_image = jsonObject.getString("login_member_image");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }





















}
