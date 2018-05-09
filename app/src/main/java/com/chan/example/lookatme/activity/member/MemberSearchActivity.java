package com.chan.example.lookatme.activity.member;

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
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.chan.example.lookatme.R;
import com.chan.example.lookatme.adapter.SearchMemberListAdapter;
import com.chan.example.lookatme.function.Basic;
import com.chan.example.lookatme.function.ClearEditText;
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
 * 전체 게시글(피드)에서(AllPidFragment) 회원 검색 버튼을 클릭하면 나타나는
 * 회원 검색창.
 */
public class MemberSearchActivity extends AppCompatActivity {

    private static final String TAG = MemberSearchActivity.class.getSimpleName();

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
     *  - member_adapter : 회원 리스트 어탭터.
     *  - arrayList_member : 모든 회원의 정보를 담은 리스트.
     *  - arrayList_member_search : 검색한 회원의 정보를 담은 리스트.
     */
    String login_member_email;
    SearchMemberListAdapter member_adapter;
    ArrayList<MemberVo> arrayList_member;
    ArrayList<MemberVo> arrayList_member_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_search);
        ButterKnife.bind(this);

        // 로그인한 사용자 정보
        SharedPreferences spf_login_member = getSharedPreferences("loginMember", MODE_PRIVATE);
        login_member_email = spf_login_member.getString("loginMember_email", "");

        // 리스트뷰 관련
        recyclerView_memberList.setHasFixedSize(true);
        recyclerView_memberList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView_memberList.setLayoutManager(new LinearLayoutManager(this));
        arrayList_member = new ArrayList<>();
        arrayList_member_search = new ArrayList<>();
        member_adapter = new SearchMemberListAdapter(this, arrayList_member_search);
        recyclerView_memberList.setAdapter(member_adapter);

        // 이전 액티비티에서 넘어온 인텐트 정보
        Bundle extras = getIntent().getExtras();
        /**
         * activity_status : 이전 액티비티 이름과 상태를 나타낸다.
         *      전체피드에서 검색으로 넘어온 것인지 회원피드,내피드에서 팔로워, 팔로잉을 클릭해서 넘어온 것이지 구별하기 위해 만들었다.
         *      또한 전체, 팔로워, 팔로잉 회원 리스트를 나누려고 만들었다.
         *      AllPidFragment ==> 전체 피드에서 넘어옴.
         *      follower ==> 회원피드에서 넘어옴. 팔로워를 눌러서 옴.
         *      following ==> 회원피드에서 넘어옴. 팔로잉을 눌러서 옴.
         */
        String activity_status = extras.getString("activity_status");
        switch (activity_status){
            case "AllPidFragment":
                toolBarSetting("회원 검색");
                new MemberAllListInformationServerData().execute();
                break;
            case "follower":
                toolBarSetting("팔로워");
                new MemberFollowerListInformationServerData().execute(extras.getString("board_owner_member_email"));
                break;
            case "following":
                toolBarSetting("팔로잉");
                new MemberFollowingListInformationServerData().execute(extras.getString("board_owner_member_email"));
                break;
        }

        // 회원 검색
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String search_member_name = editText_member_name_search.getText().toString();
                memberSearch(search_member_name);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        };
        editText_member_name_search.addTextChangedListener(textWatcher);

        recyclerView_memberList.addOnItemTouchListener(
            new MemberListItemClickListener(MemberSearchActivity.this, new MemberListItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    MemberVo memberVo = arrayList_member_search.get(position);
                    Intent intent = new Intent(MemberSearchActivity.this, MemberPidActivity.class);
                    intent.putExtra("board_owner_member_email", memberVo.getMember_email());
                    intent.putExtra("board_owner_member_name", memberVo.getMember_name());
                    startActivity(intent);
                }
            })
        );

    }

    /**
     * class
     *  - MemberListItemClickListener : 회원 리스트뷰의 아이템을 클릭했을때 발생하는 리스너.
     *  - MemberAllListInformationServerData : 전체 회원의 정보를 받아옴.
     *  - MemberFollowerListInformationServerData : 해당 회원의 팔로우 회원 정보를 받아옴.
     *  - MemberFollowingListInformationServerData : 해당 회원의 팔로잉 회원 정보를 받아옴.
     */
    private static class MemberListItemClickListener extends RecyclerView.SimpleOnItemTouchListener{
        public interface OnItemClickListener{
            void onItemClick(View view,int position);
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
    private class MemberAllListInformationServerData extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(Basic.server_member_php_directory_url+"member_search_list_information.php")
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
                JSONArray jsonArray = new JSONArray(result);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String member_email = jsonObject.getString("member_email");
                    String member_name = jsonObject.getString("member_name");
                    String member_img = jsonObject.getString("member_img");
                    arrayList_member.add(new MemberVo(member_email,member_name,member_img));
                }
                arrayList_member_search.addAll(arrayList_member);
                member_adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class MemberFollowerListInformationServerData extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("board_owner_member_email", strings[0])
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_member_php_directory_url+"follower_member_information.php")
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
                JSONArray jsonArray = new JSONArray(result);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String member_email = jsonObject.getString("member_email");
                    String member_name = jsonObject.getString("member_name");
                    String member_img = jsonObject.getString("member_img");
                    arrayList_member.add(new MemberVo(member_email,member_name,member_img));
                }
                arrayList_member_search.addAll(arrayList_member);
                member_adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class MemberFollowingListInformationServerData extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("board_owner_member_email", strings[0])
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_member_php_directory_url+"following_member_information.php")
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
                JSONArray jsonArray = new JSONArray(result);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String member_email = jsonObject.getString("member_email");
                    String member_name = jsonObject.getString("member_name");
                    String member_img = jsonObject.getString("member_img");
                    arrayList_member.add(new MemberVo(member_email,member_name,member_img));
                }
                arrayList_member_search.addAll(arrayList_member);
                member_adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * method
     *  - memberSearch : 검색 수행하는 기능.
     */
    private void memberSearch(String name){
        arrayList_member_search.clear();
        if(name.length()==0){
            arrayList_member_search.addAll(arrayList_member);
        }
        else{
            for(int i=0; i<arrayList_member.size(); i++){
                if(arrayList_member.get(i).getMember_name().toLowerCase().contains(name)){
                    arrayList_member_search.add(arrayList_member.get(i));
                }
            }
        }
        member_adapter.notifyDataSetChanged();
    }


    /**
     * 툴바 관련
     * toolBarSetting : 툴바 설정
     * onOptionsItemSelected : 툴바 선택
     */
    private void toolBarSetting(String title){
        toolBar.setTitleTextColor(Color.parseColor("#BA68C8"));
        setSupportActionBar(toolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(title);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 뒤로가기
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }




















}
