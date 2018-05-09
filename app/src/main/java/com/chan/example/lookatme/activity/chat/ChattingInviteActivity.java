package com.chan.example.lookatme.activity.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.chan.example.lookatme.R;
import com.chan.example.lookatme.adapter.ChattingInviteMemberListAdapter;
import com.chan.example.lookatme.function.Basic;
import com.chan.example.lookatme.function.ClearEditText;
import com.chan.example.lookatme.service.ClientServerConnectService;
import com.chan.example.lookatme.sqlite.DBHelper_chattingMessage;
import com.chan.example.lookatme.sqlite.DBHelper_chattingRoom;
import com.chan.example.lookatme.vo.MemberVo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 채팅방 생성 화면. 사용자들을 선택하여 채팅방을 생성할 수 있다.
 * 사용자들을 선택하고 상단 오른쪽 생성 버튼을 누르면 채팅방이 생성된다.
 */
public class ChattingInviteActivity extends AppCompatActivity {

    private static final String TAG = ChattingInviteActivity.class.getSimpleName();

    /**
     * BindView
     *  - toolBar : 화면 맨 상단에 위치한 툴바
     *  - editText_member_name_search : 화면 맨 상단에 위치한 회원 이름 검색하는 EditText
     *  - recyclerView_memberList : 회원 목록을 나타내는 RecyclerView
     */
    @BindView(R.id.toolBar) Toolbar toolBar;
    @BindView(R.id.editText_member_name_search) ClearEditText editText_member_name_search;
    @BindView(R.id.recyclerView_memberList) RecyclerView recyclerView_memberList;

    /**
     * 변수
     *  - login_member_email : 로그인한 사용자 이메일.
     *  - login_member_name : 로그인한 사용자 이름.
     *  - login_member_image : 로그인한 사용자 이미지.
     *  - member_adapter : 회원 리스트 어탭터.
     *  - arrayList_member : 모든 회원의 정보를 담은 리스트.
     *  - arrayList_member_search : 검색한 회원의 정보를 담은 리스트.
     */
    String login_member_email;
    String login_member_name;
    String login_member_image;
    ChattingInviteMemberListAdapter member_adapter;
    ArrayList<MemberVo> arrayList_member;
    ArrayList<MemberVo> arrayList_member_search;

    /**
     * 인텐트 변수
     *  - previous_activity : 현재 액티비티를 열기 전 액티비티.
     *  - chattingRoom_public_key : 채팅방 고유 키
     *  - chattingRoom_accept_member_email : 채팅방에 있는 회원 이메일.
     *  - chattingRoom_accept_member_name : 채팅방에 있는 회원 이름.
     *  - chattingRoom_accept_member_image : 채팅방에 있는 회원 이미지.
     */
    String previous_activity;
    String chattingRoom_public_key;
    String chattingRoom_accept_member_email;
    String chattingRoom_accept_member_name;
    String chattingRoom_accept_member_image;

    DBHelper_chattingRoom dbHelper_chattingRoom;
    DBHelper_chattingMessage dbHelper_chattingMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_invite);
        ButterKnife.bind(this);

        dbHelper_chattingRoom = new DBHelper_chattingRoom(this, "chattingRoom.db", null, 1);
        dbHelper_chattingMessage = new DBHelper_chattingMessage(this, "chattingMessage.db", null, 1);

        // 로그인한 사용자 정보
        SharedPreferences spf_login_member = getSharedPreferences("loginMember", MODE_PRIVATE);
        login_member_email = spf_login_member.getString("loginMember_email", "");

        Bundle extras = getIntent().getExtras();
        previous_activity = extras.getString("previous_activity");
        switch(previous_activity){
            case "ChatListFragment":
                break;
            case "ChattingActivity":
                chattingRoom_public_key = extras.getString("chattingRoom_public_key");
                chattingRoom_accept_member_email = extras.getString("chattingRoom_accept_member_email");
                chattingRoom_accept_member_name = extras.getString("chattingRoom_accept_member_name");
                chattingRoom_accept_member_image = extras.getString("chattingRoom_accept_member_image");
                break;
        }

        listViewAdapterSetting();
        toolBarSetting();
        new MemberAllListInformationServerData().execute();
        memberSearch();

        recyclerView_memberList.addOnItemTouchListener(
            new MemberListItemClickListener(ChattingInviteActivity.this, new MemberListItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    listViewItemTouch(position);
                }
            })
        );

    }

    /**
     * class
     *  - MemberListItemClickListener : 회원 리스트뷰의 아이템을 클릭했을때 발생하는 리스너.
     *  - MemberAllListInformationServerData : 전체 회원의 정보를 받아옴. 로그인한 사용자의 이름과 이미지도 받음.
     */
    private static class MemberListItemClickListener extends RecyclerView.SimpleOnItemTouchListener{
        public interface OnItemClickListener{
            void onItemClick(View view, int position);
        }
        private OnItemClickListener mListener;
        private GestureDetector mGestureDetector;
        public MemberListItemClickListener(Context context, OnItemClickListener listener){
            this.mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
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
    private class MemberAllListInformationServerData extends AsyncTask<Void,Void,String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("login_member_email", login_member_email)
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_chat_php_directory_url+"chatting_invite_activity_information.php")
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
                JSONObject jsonObject_data = new JSONObject(result);
                JSONArray jsonArray_member_list = jsonObject_data.getJSONArray("member_list");
                for(int i=0; i<jsonArray_member_list.length(); i++){
                    JSONObject jsonObject = jsonArray_member_list.getJSONObject(i);
                    String member_email = jsonObject.getString("member_email");
                    String member_name = jsonObject.getString("member_name");
                    String member_img = jsonObject.getString("member_img");
                    if(!login_member_email.equals(member_email)){
                        arrayList_member.add(new MemberVo(member_email,member_name,member_img,false));
                    }
                }
                arrayList_member_search.addAll(arrayList_member);
                member_adapter.notifyDataSetChanged();

                JSONObject jsonObject_login_member_name = jsonObject_data.getJSONObject("login_member_information");
                login_member_name = jsonObject_login_member_name.getString("login_member_name");
                login_member_image = jsonObject_login_member_name.getString("login_member_image");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * method
     *  - listViewAdapterSetting : 리스트뷰 관련 설정.
     *  - memberSearch : 검색 수행하는 기능.
     *  - listViewItemTouch : 리스트뷰 아이템 클릭시 체크박스 변경.
     */
    private void listViewAdapterSetting(){
        recyclerView_memberList.setHasFixedSize(true);
        recyclerView_memberList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView_memberList.setLayoutManager(new LinearLayoutManager(this));
        arrayList_member = new ArrayList<>();
        arrayList_member_search = new ArrayList<>();
        member_adapter = new ChattingInviteMemberListAdapter(this, arrayList_member_search);
        recyclerView_memberList.setAdapter(member_adapter);
    }
    private void memberSearch(){
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String search_member_name = editText_member_name_search.getText().toString();
                arrayList_member_search.clear();
                if(search_member_name.length()==0){
                    arrayList_member_search.addAll(arrayList_member);
                }
                else{
                    for(int i=0; i<arrayList_member.size(); i++){
                        if(arrayList_member.get(i).getMember_name().toLowerCase().contains(search_member_name)){
                            arrayList_member_search.add(arrayList_member.get(i));
                        }
                    }
                }
                member_adapter.notifyDataSetChanged();
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        };
        editText_member_name_search.addTextChangedListener(textWatcher);
    }
    private void listViewItemTouch(int position){
        MemberVo memberVo_member_search = arrayList_member_search.get(position);
        String member_email = memberVo_member_search.getMember_email();
        for(int i=0; i<arrayList_member.size(); i++){
            MemberVo memberVo_member = arrayList_member.get(i);
            String original_member_email = memberVo_member.getMember_email();
            if(member_email.equals(original_member_email)){
                if(memberVo_member.isMember_check_status() == false){
                    memberVo_member.setMember_check_status(true);
                    memberVo_member_search.setMember_check_status(true);
                    member_adapter.notifyDataSetChanged();
                }else{
                    memberVo_member.setMember_check_status(false);
                    memberVo_member_search.setMember_check_status(false);
                    member_adapter.notifyDataSetChanged();
                }
            }
        }
    }

    /**
     * 툴바 관련
     * toolBarSetting : 툴바 설정
     * onCreateOptionsMenu : 툴바 메뉴
     * onOptionsItemSelected : 툴바 선택
     */
    private void toolBarSetting(){
        toolBar.setTitleTextColor(Color.parseColor("#BA68C8"));
        setSupportActionBar(toolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("대화 상대 초대");
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatting_invite_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 뒤로가기
                onBackPressed();
                break;
            case R.id.success:
                switch(previous_activity){
                    case "ChatListFragment":
                        previous_activity__ChatListFragment();
                        break;
                    case "ChattingActivity":
                        previous_activity__ChattingActivity();
                        break;
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * previous_activity__ChatListFragment : 새로 채팅방을 생성할때 사용.
     * previous_activity__ChattingActivity : 기존 채팅방에 회원을 초대할때 사용.
     */
    private void previous_activity__ChatListFragment(){
        try {
            JSONArray jsonArray_invite_member_email = new JSONArray();
            JSONObject jsonObject_invite_login_member_email = new JSONObject();
            jsonObject_invite_login_member_email.put("member_email", login_member_email);
            jsonArray_invite_member_email.put(jsonObject_invite_login_member_email);

            JSONArray jsonArray_invite_member_name = new JSONArray();
            JSONObject jsonObject_invite_login_member_name = new JSONObject();
            jsonObject_invite_login_member_name.put("member_name", login_member_name);
            jsonArray_invite_member_name.put(jsonObject_invite_login_member_name);

            JSONArray jsonArray_invite_member_image = new JSONArray();
            JSONObject jsonObject_invite_login_member_image = new JSONObject();
            jsonObject_invite_login_member_image.put("member_image", login_member_image);
            jsonArray_invite_member_image.put(jsonObject_invite_login_member_image);

            String chattingRoom_name = login_member_name;
            int idx = 0;
            for(int i=0; i<arrayList_member.size(); i++){
                if(arrayList_member.get(i).isMember_check_status() == true){
                    JSONObject jsonObject_invite_member_email = new JSONObject();
                    jsonObject_invite_member_email.put("member_email", arrayList_member.get(i).getMember_email());
                    jsonArray_invite_member_email.put(jsonObject_invite_member_email);

                    JSONObject jsonObject_invite_member_name = new JSONObject();
                    jsonObject_invite_member_name.put("member_name", arrayList_member.get(i).getMember_name());
                    jsonArray_invite_member_name.put(jsonObject_invite_member_name);

                    JSONObject jsonObject_invite_member_image = new JSONObject();
                    jsonObject_invite_member_image.put("member_image", arrayList_member.get(i).getMember_img());
                    jsonArray_invite_member_image.put(jsonObject_invite_member_image);

                    chattingRoom_name += ","+arrayList_member.get(i).getMember_name();
                    idx++;
                }
            }
            if(idx == 0){
                Toast.makeText(this, "대화에 초대할 회원(들)을 선택하세요.", Toast.LENGTH_SHORT).show();
            }else{
                String invite_name_message = chattingRoom_name.replace(login_member_name+",","");

                JSONObject jsonObject_dataOutputStream = new JSONObject();
                jsonObject_dataOutputStream.put("data_type", "room"); // 데이터 타입.
                jsonObject_dataOutputStream.put("room_type", "create"); // 채팅방 타입.
                jsonObject_dataOutputStream.put("chattingRoom_public_key", Basic.chattingRoomPublicKeyCreate(login_member_email)); // 사용자들이 모두 공통적으로 가진 같은 이름의 방 key.
                jsonObject_dataOutputStream.put("chattingRoom_accept_member_email", jsonArray_invite_member_email.toString()); // 채팅방에 들어 있는 회원들의 이메일.
                jsonObject_dataOutputStream.put("chattingRoom_accept_member_name", jsonArray_invite_member_name.toString()); // 채팅방에 들어 있는 회원들의 이름.
                jsonObject_dataOutputStream.put("chattingRoom_accept_member_image", jsonArray_invite_member_image.toString()); // 채팅방에 들어 있는 회원들의 이미지.
                jsonObject_dataOutputStream.put("chattingRoom_name", chattingRoom_name); // 채팅방 이름.
                jsonObject_dataOutputStream.put("chattingRoom_message_contents", login_member_name+"님이 "+invite_name_message + "님을 초대하였습니다."); // 채팅방 목록에서 보여질 마지막 채팅 내용.
                jsonObject_dataOutputStream.put("chattingRoom_message_time", Basic.nowDate("date")); // 채팅방 목록에서 보여질 마지막 채팅 메세지의 시간.
                jsonObject_dataOutputStream.put("chattingMessage_type", "etc"); // 채팅메세지 타입

                ClientServerConnectService.dataOutputStream.writeUTF(jsonObject_dataOutputStream.toString());
                ClientServerConnectService.dataOutputStream.flush();
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void previous_activity__ChattingActivity(){
        try {
            JSONArray jsonArray_email = new JSONArray(chattingRoom_accept_member_email); // 현재 방에 있는 회원들 이메일 ==> 초대된 회원도 포함된 회원들 이메일
            JSONArray jsonArray_name = new JSONArray(chattingRoom_accept_member_name); // 현재 방에 있는 회원들 이름 ==> 초대된 회원도 포함된 화원들 이름
            JSONArray jsonArray_image = new JSONArray(chattingRoom_accept_member_image); // 현재 방에 있는 회원들 이미지 ==> 초대된 회원도 포함된 회원들 이미지
            JSONArray jsonArray_data_send_email = new JSONArray(chattingRoom_accept_member_email); // 초대를 진행하는 회원(나)을 제외한 나머지 회원들


            String chattingRoom_name = ""; // 초대될 회원이 갖게될 채팅방 이름
            String invite_member_names = ""; // 초대될 회원들의 이름,
            JSONArray jsonArray_origin_member_email = new JSONArray(chattingRoom_accept_member_email); // 기존 회원들
            JSONArray jsonArray_new_member_email = new JSONArray(); // 새롭게 초대된 회원들
            int index = 0; // 체크 된 수
            for(int i=0; i<jsonArray_name.length(); i++){
                JSONObject jsonObject = jsonArray_name.getJSONObject(i);
                chattingRoom_name += jsonObject.getString("member_name") + ",";
            }
            for(int i=0; i<arrayList_member.size(); i++){
                if(arrayList_member.get(i).isMember_check_status() == true){
                    JSONObject jsonObject_email = new JSONObject();
                    jsonObject_email.put("member_email", arrayList_member.get(i).getMember_email());
                    jsonArray_email.put(jsonObject_email);
                    jsonArray_data_send_email.put(jsonObject_email);

                    JSONObject jsonObject_name = new JSONObject();
                    jsonObject_name.put("member_name", arrayList_member.get(i).getMember_name());
                    jsonArray_name.put(jsonObject_name);

                    JSONObject jsonObject_image = new JSONObject();
                    jsonObject_image.put("member_image", arrayList_member.get(i).getMember_img());
                    jsonArray_image.put(jsonObject_image);

                    chattingRoom_name += arrayList_member.get(i).getMember_name() + ",";
                    invite_member_names += arrayList_member.get(i).getMember_name() + ",";

                    JSONObject jsonObject_new_member_email = new JSONObject();
                    jsonObject_new_member_email.put("member_email", arrayList_member.get(i).getMember_email());
                    jsonArray_new_member_email.put(jsonObject_new_member_email);

                    index++;
                }
            }

            chattingRoom_name = chattingRoom_name.substring(0, chattingRoom_name.length()-1);
            invite_member_names = invite_member_names.substring(0, invite_member_names.length()-1);

            if(index == 0){
                Toast.makeText(this, "대화에 초대할 회원(들)을 선택하세요.", Toast.LENGTH_SHORT).show();
            }else{
                JSONObject jsonObject_data = new JSONObject();

                // 서버와 서비스단에서 나뉠 정보
                jsonObject_data.put("data_type", "room"); // 데이터 타입.
                jsonObject_data.put("room_type", "invite"); // 채팅방 타입.
                for(int i=0; i<jsonArray_data_send_email.length(); i++){
                    JSONObject jsonObject_data_send_email = jsonArray_data_send_email.getJSONObject(i);
                    String member_email = jsonObject_data_send_email.getString("member_email");
                    if(login_member_email.equals(member_email)){
                        jsonArray_data_send_email.remove(i);
                    }
                }
                jsonObject_data.put("data_send_member_email", jsonArray_data_send_email.toString()); // 서버에서 이 회원들에게만 데이터 전송

                // 기존에 채팅방에 있던 회원들 이메일
                jsonObject_data.put("origin_member", jsonArray_origin_member_email);

                // 새롭게 채팅방에 추가된 회원들 이메일
                jsonObject_data.put("new_member", jsonArray_new_member_email);

                // 채팅방에 기존에 있던 회원들에게 보낼 정보
                JSONObject jsonObject_origin = new JSONObject();
                jsonObject_origin.put("chattingRoom_public_key", chattingRoom_public_key); // 사용자들이 모두 공통적으로 가진 같은 이름의 방 key.
                jsonObject_origin.put("chattingRoom_accept_member_email", jsonArray_email.toString()); // chattingRoom update
                jsonObject_origin.put("chattingRoom_accept_member_name", jsonArray_name.toString()); // chattingRoom update
                jsonObject_origin.put("chattingRoom_accept_member_image", jsonArray_image.toString()); // chattingRoom update
                jsonObject_origin.put("chattingMessage_contents", login_member_name+"님이 "+invite_member_names + "님을 초대하였습니다."); // chattingMessage insert
                jsonObject_origin.put("chattingMessage_type", "etc"); // chattingMessage insert
                jsonObject_data.put("origin",jsonObject_origin);

                // 새롭게 초대된 회원들에게 보낼 정보
                JSONObject jsonObject_new = new JSONObject();
                jsonObject_new.put("chattingRoom_public_key", chattingRoom_public_key); // 사용자들이 모두 공통적으로 가진 같은 이름의 방 key.
                jsonObject_new.put("chattingRoom_accept_member_email", jsonArray_email.toString()); // chattingRoom insert
                jsonObject_new.put("chattingRoom_accept_member_name", jsonArray_name.toString()); // chattingRoom insert
                jsonObject_new.put("chattingRoom_accept_member_image", jsonArray_image.toString()); // chattingRoom insert
                jsonObject_new.put("chattingRoom_name", chattingRoom_name); // chattingRoom insert
                jsonObject_new.put("chattingRoom_message_contents", login_member_name+"님이 "+invite_member_names + "님을 초대하였습니다."); // chattingRoom insert,chattingMessage insert
                jsonObject_new.put("chattingRoom_message_time", Basic.nowDate("date")); // chattingRoom insert,chattingMessage insert
                jsonObject_new.put("chattingMessage_type", "etc"); // chattingMessage insert
                jsonObject_data.put("new", jsonObject_new);

                dbHelper_chattingRoom.update_chattingRoom_information(
                        jsonObject_origin.getString("chattingRoom_public_key"),jsonObject_origin.getString("chattingRoom_accept_member_email"),
                        jsonObject_origin.getString("chattingRoom_accept_member_name"),jsonObject_origin.getString("chattingRoom_accept_member_image"));
                dbHelper_chattingMessage.insert(
                        jsonObject_origin.getString("chattingRoom_public_key"),"","","",
                        jsonObject_origin.getString("chattingMessage_contents"),"",jsonObject_origin.getString("chattingMessage_type"));

                ClientServerConnectService.dataOutputStream.writeUTF(jsonObject_data.toString());
                ClientServerConnectService.dataOutputStream.flush();

                Intent intent = new Intent(this, ChattingActivity.class);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }









}
