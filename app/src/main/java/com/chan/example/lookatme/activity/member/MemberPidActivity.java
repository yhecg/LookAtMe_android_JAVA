package com.chan.example.lookatme.activity.member;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chan.example.lookatme.R;
import com.chan.example.lookatme.activity.board_pid.BoardDetailActivity;
import com.chan.example.lookatme.adapter.MemberPidBoardListAdapter;
import com.chan.example.lookatme.function.Basic;
import com.chan.example.lookatme.vo.BoardVo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MemberPidActivity extends AppCompatActivity {

    private static final String TAG = MemberPidActivity.class.getSimpleName();

    /**
     * RequestCode
     *  - RequestCode_board_item_detail_go : 해당 게시글 상세정보창으로 가는 코드.
     */
    private static final int RequestCode_board_item_detail_go = 100;

    /**
     * BindView
     *  - imageView_profile : 프로필 이미지가 있는 이미지뷰.
     *  - textView_board_count : 게시글 수를 나타내는 텍스트뷰.
     *  - textView_follower_count : 팔로워 수를 나타내는 텍스트뷰.
     *  - textView_following_count : 팔로잉 수를 나타내는 텍스트뷰.
     *  - recyclerView_board_list : 게시글 정보를 담은 리스트.
     *  - toolBar : 화면 맨 상단에 있는 툴바.
     *  - button_follow_before : 현재 피드의 회원을 팔로우 하지 않았을 경우 나타나는 버튼. 팔로우 하기 버튼.
     *  - button_follow_after : 현재 피드의 회원을 팔로우 했을 경우 나타나는 버튼. 팔로우 취소 버튼.
     *  - button_follow_lookatme : 현재 피드가 로그인한 회원의 피드일때 나타나는 버튼.
     */
    @BindView(R.id.imageView_profile) CircleImageView imageView_profile;
    @BindView(R.id.textView_board_count) TextView textView_board_count;
    @BindView(R.id.textView_follower_count) TextView textView_follower_count;
    @BindView(R.id.textView_following_count) TextView textView_following_count;
    @BindView(R.id.recyclerView_board_list) RecyclerView recyclerView_board_list;
    @BindView(R.id.toolBar) Toolbar toolBar;
    @BindView(R.id.button_follow_before) Button button_follow_before;
    @BindView(R.id.button_follow_after) Button button_follow_after;
    @BindView(R.id.button_follow_lookatme) Button button_follow_lookatme;

    /**
     * 변수
     *  - login_member_email : 현재 로그인한 회원의 이메일
     *  - board_owner_member_email : 현재 피드 주인의 이메일
     *  - board_owner_member_name : 현재 피드 주인의 이름
     *  - adapter_board : 게시글에 대한 정보를 담은 리스트의 어댑터.
     *  - arrayList_board : 게시글에 대한 정보를 담은 ArrayList.
     */
    String login_member_email;
    String board_owner_member_email;
    String board_owner_member_name;
    MemberPidBoardListAdapter adapter_board;
    ArrayList<BoardVo> arrayList_board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_pid);
        ButterKnife.bind(this);

        // 로그인한 회원 정보
        SharedPreferences spf_login_member = getSharedPreferences("loginMember", MODE_PRIVATE);
        login_member_email = spf_login_member.getString("loginMember_email", "");

        // 이전 액티비티에서 넘어온 인텐트 정보.
        Bundle extras = getIntent().getExtras();
        board_owner_member_email = extras.getString("board_owner_member_email"); // 게시글 작성한 회원 이메일.
        board_owner_member_name = extras.getString("board_owner_member_name"); // 게시글 작성한 회원 이름.

        // 리스트뷰 관련.
        arrayList_board = new ArrayList<>();
        recyclerView_board_list.setHasFixedSize(true);
        adapter_board = new MemberPidBoardListAdapter(MemberPidActivity.this, arrayList_board);
        recyclerView_board_list.setAdapter(adapter_board);

        // 내 정보와(프로필이미지,팔로워,팔로잉) 내가 작성한 게시글의 정보를 불러온다.
        new MemberPidInformationServerData().execute();

        // 게시글(리스트뷰 아이템)을 클릭했을때 해당 게시글 상세정보창으로 이동.
        recyclerView_board_list.addOnItemTouchListener(
            new BoardListItemClickListener(this,
                new BoardListItemClickListener.OnItemClickListener(){
                    @Override
                    public void onItemClick(View view, int position) {
                        BoardVo boardVo = arrayList_board.get(position);
                        Intent intent = new Intent(MemberPidActivity.this, BoardDetailActivity.class);
                        intent.putExtra("board_no", boardVo.getBoard_no()); // 게시글 번호
                        intent.putExtra("member_email", boardVo.getMember_email()); // 게시글 작성한 회원 이메일
                        intent.putExtra("activity_status", "MemberPidActivity"); // 현재 액티비티에서 넘어갔다는 변수
                        startActivityForResult(intent, RequestCode_board_item_detail_go);
                    }
                }
            )
        );

        toolBarSetting();

    }

    /**
     * OnClick 클릭 이벤트
     *  - followerSearchMemberActivityGo : 팔로워 회원 목록창으로 이동.
     *  - followingSearchMemberActivityGo : 팔로잉 회원 목록창으로 이동.
     *  - followGo : 상대방 팔로우한다.
     *  - followCancel : 상대방 팔로우 취소한다.
     */
    @OnClick({R.id.textView_follower_count, R.id.textView_follower_text}) void followerSearchMemberActivityGo(){
        Intent intent = new Intent(MemberPidActivity.this, MemberSearchActivity.class);
        intent.putExtra("activity_status", "follower");
        intent.putExtra("board_owner_member_email", board_owner_member_email);
        startActivity(intent);
    }
    @OnClick({R.id.textView_following_count, R.id.textView_following_text}) void followingSearchMemberActivityGo(){
        Intent intent = new Intent(MemberPidActivity.this, MemberSearchActivity.class);
        intent.putExtra("activity_status", "following");
        intent.putExtra("board_owner_member_email", board_owner_member_email);
        startActivity(intent);
    }
    @OnClick(R.id.button_follow_before) void followGo(){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                OkHttpClient client = new OkHttpClient();
                try {
                    RequestBody requestBody = new FormBody.Builder()
                            .add("board_owner_member_email",board_owner_member_email)
                            .add("login_member_email",login_member_email)
                            .build();
                    Request request = new Request.Builder()
                            .url(Basic.server_member_php_directory_url+"follow_go.php")
                            .post(requestBody)
                            .build();
                    client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                int count = Integer.parseInt(textView_follower_count.getText().toString());
                count++;
                textView_follower_count.setText(String.valueOf(count));
                button_follow_before.setVisibility(View.GONE);
                button_follow_after.setVisibility(View.VISIBLE);
                button_follow_lookatme.setVisibility(View.GONE);
            }
        }.execute();
    }
    @OnClick(R.id.button_follow_after) void followCancel(){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                OkHttpClient client = new OkHttpClient();
                try {
                    RequestBody requestBody = new FormBody.Builder()
                            .add("board_owner_member_email",board_owner_member_email)
                            .add("login_member_email",login_member_email)
                            .build();
                    Request request = new Request.Builder()
                            .url(Basic.server_member_php_directory_url+"follow_cancel.php")
                            .post(requestBody)
                            .build();
                    client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                int count = Integer.parseInt(textView_follower_count.getText().toString());
                count--;
                textView_follower_count.setText(String.valueOf(count));
                button_follow_before.setVisibility(View.VISIBLE);
                button_follow_after.setVisibility(View.GONE);
                button_follow_lookatme.setVisibility(View.GONE);
            }
        }.execute();
    }

    /**
     * class
     *  - BoardListItemClickListener : 게시글 리스트뷰 아이템을 클릭했을때 일어나는 이벤트 리스너.
     *  - MemberPidInformationServerData : 해당 피드의 주인(회원)의 정보와 작성한 게시글을 불러온다.
     */
    private static class BoardListItemClickListener extends RecyclerView.SimpleOnItemTouchListener{
        public interface OnItemClickListener{
            void onItemClick(View view, int position);
        }
        private OnItemClickListener mListener;
        private GestureDetector mGestureDetector;
        public BoardListItemClickListener(Context context, OnItemClickListener listener){
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
    private class MemberPidInformationServerData extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("member_email", board_owner_member_email)
                        .add("login_member_email", login_member_email)
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_member_php_directory_url + "member_pid_information.php")
                        .post(requestBody)
                        .build();
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result);

                // 프로필 이미지란에 들어갈 이미지를 서버에서 받아와서 넣어준다.
                JSONArray jsonArray_member = jsonObject.getJSONArray("member_information");
                JSONObject jsonObject_member = jsonArray_member.getJSONObject(0);
                String member_img = jsonObject_member.getString("member_img");
                if(!member_img.equals("") && member_img != null){
                    Glide.with(MemberPidActivity.this).load(Basic.server_member_image_directory_url+member_img).asBitmap().into(imageView_profile);
                }

                // 게시글 정보를 서버에서 받아와서 리스트뷰에 뿌려준다.
                JSONArray jsonArray_board = jsonObject.getJSONArray("board_information");
                for(int i=0; i<jsonArray_board.length(); i++){
                    JSONObject jsonObject_board = jsonArray_board.getJSONObject(i);
                    String board_no = jsonObject_board.getString("board_no");
                    String member_email = jsonObject_board.getString("member_email");
                    String board_img = jsonObject_board.getString("board_img");
                    arrayList_board.add(0, new BoardVo(board_no, member_email, board_img));
                }
                adapter_board.notifyDataSetChanged();

                // 게시글 갯수.
                textView_board_count.setText(String.valueOf(arrayList_board.size()));

                // 팔로워 수
                String follower_count = jsonObject_member.getString("follower_count");
                textView_follower_count.setText(follower_count);

                // 팔로잉 수
                String following_count = jsonObject_member.getString("following_count");
                textView_following_count.setText(following_count);

                // 로그인한 회원이 현재 피드의 주인인지 아닌지 확인하여 알맞은 버튼 보여주기.
                if(login_member_email.equals(board_owner_member_email)){
                    button_follow_before.setVisibility(View.GONE);
                    button_follow_after.setVisibility(View.GONE);
                    button_follow_lookatme.setVisibility(View.VISIBLE);
                }else{
                    String follow_check = jsonObject_member.getString("follow_check");
                    if(!follow_check.equals("") && !follow_check.equals("null") && follow_check != null){
                        button_follow_before.setVisibility(View.GONE);
                        button_follow_after.setVisibility(View.VISIBLE);
                        button_follow_lookatme.setVisibility(View.GONE);
                    }else{
                        button_follow_before.setVisibility(View.VISIBLE);
                        button_follow_after.setVisibility(View.GONE);
                        button_follow_lookatme.setVisibility(View.GONE);
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 툴바 관련
     * toolBarSetting : 툴바 설정
     * onOptionsItemSelected : 툴바 선택
     */
    private void toolBarSetting(){
        toolBar.setTitleTextColor(Color.parseColor("#BA68C8"));
        setSupportActionBar(toolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(board_owner_member_name);
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
