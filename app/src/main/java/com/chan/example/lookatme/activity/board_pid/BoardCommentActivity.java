package com.chan.example.lookatme.activity.board_pid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.chan.example.lookatme.R;
import com.chan.example.lookatme.activity.member.MemberPidActivity;
import com.chan.example.lookatme.adapter.BoardCommentListAdapter;
import com.chan.example.lookatme.function.Basic;
import com.chan.example.lookatme.vo.BoardCommentVo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BoardCommentActivity extends AppCompatActivity {

    private static final String TAG = BoardCommentActivity.class.getSimpleName();

    /**
     * 변수
     *  - login_member_email : 현재 로그인한 회원의 이메일
     *  - board_no : 현재 게시글의 번호
     *  - comment_adapter : 댓글 어댑터
     *  - comment_arrayList : 댓글 관련 ArrayList
     *  - comment_count : 현재 게시글의 댓글 수. 댓글수가 변경되면 현재 댓글창이 꺼질때 이전 액티비티인 게시글 상세창(BoardDetailActivity)의 댓글 수가 변경.
     */
    String login_member_email;
    String board_no;
    BoardCommentListAdapter comment_adapter;
    ArrayList<BoardCommentVo> comment_arrayList;
    int comment_count;

    /**
     * BindView
     *  - toolBar : 화면 맨 상단에 위치한 툴바
     *  - imageView_login_member_image : 현재 로그인한 회원의 프로필 이미지
     *  - editText_comment_insert : 댓글을 입력할 수 입력창
     *  - recyclerView_board_comment_list : 현재 게시글의 댓글 목록
     */
    @BindView(R.id.toolBar) Toolbar toolBar;
    @BindView(R.id.imageView_login_member_image) CircleImageView imageView_login_member_image;
    @BindView(R.id.editText_comment_insert) EditText editText_comment_insert;
    @BindView(R.id.recyclerView_board_comment_list) RecyclerView recyclerView_board_comment_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_comment);
        ButterKnife.bind(this);

        // 로그인한 회원 이메일.
        SharedPreferences spf_login_member = getSharedPreferences("loginMember", MODE_PRIVATE);
        login_member_email = spf_login_member.getString("loginMember_email", "");

        // 게시글 상세창(BoardDetailActivity)에서 넘어온 인텐트 정보.
        Bundle extras = getIntent().getExtras();
        board_no = extras.getString("board_no"); // 게시글 번호
        comment_count = extras.getInt("comment_count"); // 댓글 수.

        toolBarSetting();

        // 리스트뷰 관련
        comment_arrayList = new ArrayList<>();
        recyclerView_board_comment_list.setHasFixedSize(true);
        recyclerView_board_comment_list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView_board_comment_list.setLayoutManager(new LinearLayoutManager(this));

        comment_adapter = new BoardCommentListAdapter(this, comment_arrayList, new BoardCommentListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                final BoardCommentVo commentVo = comment_arrayList.get(position);
                switch (view.getId()){

                    // 회원 이미지를 눌렀을 경우
                    case R.id.imageView_comment_write_member_profile_image:
                        Intent intent = new Intent(BoardCommentActivity.this, MemberPidActivity.class);
                        intent.putExtra("board_owner_member_email", commentVo.getMember_email());
                        intent.putExtra("board_owner_member_name", commentVo.getMember_name());
                        startActivity(intent);
                        break;

                    // 답글 달기를 눌렀을 경우
                    case R.id.textView_comment_comments_insert:
                        final EditText editText = new EditText(BoardCommentActivity.this);
                        editText.setSingleLine(true);
                        final AlertDialog.Builder builder = new AlertDialog.Builder(BoardCommentActivity.this);
                        builder.setTitle("댓글 입력");
                        builder.setView(editText);
                        builder.setPositiveButton("등록", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new CommentCommentsInsertServerData().execute(commentVo.getBoardComment_no(), editText.getText().toString(),String.valueOf(position));
                            }
                        });
                        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                        break;

                    // 수정 이미지를 눌렀을 경우
                    case R.id.imageView_comment_edit:
                        final CharSequence[] items = {"수정","삭제","취소"};
                        AlertDialog.Builder builder_edit = new AlertDialog.Builder(BoardCommentActivity.this);
                        builder_edit.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (String.valueOf(items[i])){
                                    case "수정":
                                        final EditText editText_update = new EditText(BoardCommentActivity.this);
                                        editText_update.setText(commentVo.getBoardComment_contents());
                                        AlertDialog.Builder builder_update = new AlertDialog.Builder(BoardCommentActivity.this);
                                        builder_update.setTitle("댓글 수정");
                                        builder_update.setView(editText_update);
                                        builder_update.setPositiveButton("수정", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                new CommentUpdateServerData().execute(commentVo.getBoardComment_no(), editText_update.getText().toString(),String.valueOf(position));
                                            }
                                        });
                                        builder_update.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        });
                                        builder_update.show();
                                        break;
                                    case "삭제":
                                        AlertDialog.Builder builder_delete = new AlertDialog.Builder(BoardCommentActivity.this);
                                        builder_delete.setMessage("댓글을 삭제하시겠습니까?");
                                        builder_delete.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                new CommentDeleteServerData().execute(commentVo.getBoardComment_depth(),commentVo.getBoardComment_no(), String.valueOf(position));
                                            }
                                        });
                                        builder_delete.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                            }
                                        });
                                        builder_delete.show();
                                        break;
                                    case "취소":
                                        break;
                                }
                            }
                        });
                        builder_edit.show();
                        break;
                }
                
            }
        });
        recyclerView_board_comment_list.setAdapter(comment_adapter);

        new BoardCommentAllInformationServerData().execute();
    }


    /**
     * class
     *  - CommentDeleteServerData : 댓글 삭제.
     *  - CommentUpdateServerData : 댓글 수정.
     *  - CommentCommentsInsertServerData : 댓글의 답글(대댓글) 등록.
     *  - CommentInsertServerData : 댓글 등록.
     *  - BoardCommentAllInformationServerData : 처음 댓글창이 나타날때 처음에 보여지는 모든 정보를 불러온다.
     *                                         해당 게시글 댓글 목록에 대한 정보를 리스트뷰에 뿌려주고
     *                                         현재 로그인한 회원 이미지를 받아와 댓글 입력창쪽에 뿌려준다.
     */
    private class CommentDeleteServerData extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("boardComment_depth", strings[0])
                        .add("boardComment_no", strings[1])
                        .add("comment_position", strings[2])
//                        .add("board_no", board_no)
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_board_pid_php_directory_url + "comment_delete.php")
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
                String comment_parent_child_check = jsonObject.getString("comment_parent_child_check");
                switch (comment_parent_child_check){
                    case "parent":
                        String boardComment_no = jsonObject.getString("boardComment_no");

                        Iterator<BoardCommentVo> iterator = comment_arrayList.iterator();
                        while(iterator.hasNext()){
                            BoardCommentVo a = iterator.next();
                            if(a.getBoardComment_parent().equals(boardComment_no)){
                                iterator.remove();
                                comment_count--;
                            }
                        }
                        comment_adapter.notifyDataSetChanged();
                        break;
                    case "child":
                        int comment_position = Integer.parseInt(jsonObject.getString("comment_position"));
                        comment_arrayList.remove(comment_position);
                        comment_adapter.notifyDataSetChanged();
                        comment_count--;
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class CommentUpdateServerData extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("boardComment_no", strings[0])
                        .add("boardComment_contents", strings[1])
                        .add("comment_position", strings[2])
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_board_pid_php_directory_url + "comment_update.php")
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
                String boardComment_no = jsonObject.getString("boardComment_no");
                String member_email = jsonObject.getString("member_email");
                String member_name = jsonObject.getString("member_name");
                String member_img = jsonObject.getString("member_img");
                String boardComment_contents = jsonObject.getString("boardComment_contents");
                String boardComment_date = jsonObject.getString("boardComment_date");
                String boardComment_depth = jsonObject.getString("boardComment_depth");
                String boardComment_parent = jsonObject.getString("boardComment_parent");
                int comment_position = Integer.parseInt(jsonObject.getString("comment_position"));

                String comment_date =
                        boardComment_date.substring(0,4) + "년" + boardComment_date.substring(5,7) + "월" + boardComment_date.substring(8,10) + "일"
                                + boardComment_date.substring(11,13) + "시" +boardComment_date.substring(14,16) + "분";

                comment_arrayList.set(comment_position, new BoardCommentVo(boardComment_no,member_email,member_name,member_img,boardComment_contents,comment_date,boardComment_depth,boardComment_parent));
                comment_adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class CommentCommentsInsertServerData extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("login_member_email",login_member_email)
                        .add("board_no",board_no)
                        .add("boardComment_no", strings[0])
                        .add("boardComment_contents", strings[1])
                        .add("arrayList_position", strings[2])
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_board_pid_php_directory_url + "comment_comments_insert.php")
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
                String boardComment_no = jsonObject.getString("boardComment_no"); // 댓글 번호
                String member_email = jsonObject.getString("member_email"); // 댓글 작성자 이메일
                String member_name = jsonObject.getString("member_name"); // 댓글 작성자 이름
                String member_img = jsonObject.getString("member_img"); // 댓글 작성자 이미지
                String boardComment_contents = jsonObject.getString("boardComment_contents"); // 댓글 내용
                String boardComment_date = jsonObject.getString("boardComment_date"); // 댓글 작성 시간
                String boardComment_depth = jsonObject.getString("boardComment_depth"); // 댓글 깊이 ( 댓글인지 대댓글인지 확인 )
                String boardComment_parent = jsonObject.getString("boardComment_parent"); // 댓글 깊이 ( 댓글인지 대댓글인지 확인 )
                int arrayList_position = Integer.parseInt(jsonObject.getString("arrayList_position")); // 댓글(대댓글 X)의 위치
                int comment_comments_count = Integer.parseInt(jsonObject.getString("comment_comments_count")) - 1; // 댓글의 답글 수

                String comment_date =
                        boardComment_date.substring(0,4) + "년" + boardComment_date.substring(5,7) + "월" + boardComment_date.substring(8,10) + "일"
                                + boardComment_date.substring(11,13) + "시" +boardComment_date.substring(14,16) + "분";

                int listView_item_position = arrayList_position+comment_comments_count;
                comment_arrayList.add(listView_item_position, new BoardCommentVo(boardComment_no,member_email,member_name,member_img,boardComment_contents,comment_date,boardComment_depth,boardComment_parent));
                comment_adapter.notifyDataSetChanged();
                recyclerView_board_comment_list.scrollToPosition(listView_item_position);
                comment_count++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class CommentInsertServerData extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("login_member_email",login_member_email)
                        .add("board_no",board_no)
                        .add("boardComment_contents", strings[0])
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_board_pid_php_directory_url + "comment_insert.php")
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
                String boardComment_no = jsonObject.getString("boardComment_no");
                String member_email = jsonObject.getString("member_email");
                String member_name = jsonObject.getString("member_name");
                String member_img = jsonObject.getString("member_img");
                String boardComment_contents = jsonObject.getString("boardComment_contents");
                String boardComment_date = jsonObject.getString("boardComment_date");
                String boardComment_depth = jsonObject.getString("boardComment_depth");
                String boardComment_parent = jsonObject.getString("boardComment_parent");

                String comment_date =
                        boardComment_date.substring(0,4) + "년" + boardComment_date.substring(5,7) + "월" + boardComment_date.substring(8,10) + "일"
                                + boardComment_date.substring(11,13) + "시" +boardComment_date.substring(14,16) + "분";

                comment_arrayList.add(new BoardCommentVo(boardComment_no,member_email,member_name,member_img,boardComment_contents,comment_date,boardComment_depth,boardComment_parent));
                comment_adapter.notifyDataSetChanged();
                recyclerView_board_comment_list.scrollToPosition(comment_adapter.getItemCount()-1);
                editText_comment_insert.setText("");
                comment_count++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class BoardCommentAllInformationServerData extends AsyncTask<Void,Void,String>{
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(BoardCommentActivity.this, "잠시만 기다려주세요.", null, true, true);
        }
        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("board_no", board_no)
                        .add("login_member_email", login_member_email)
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_board_pid_php_directory_url + "comment_information.php")
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

                JSONArray jsonArray_login_member_information = jsonObject.getJSONArray("login_member_information");
                JSONObject jsonObject_login_member_information = jsonArray_login_member_information.getJSONObject(0);
                String login_member_image = jsonObject_login_member_information.getString("login_member_image"); // 현재 로그인한 사용자의 프로필 이미지

                // 댓글 입력창 쪽에 현재 로그인한 회원의 이미지를 뿌려준다.
                Glide.with(BoardCommentActivity.this).load(Basic.server_member_image_directory_url + login_member_image).asBitmap().into(imageView_login_member_image);

                // 댓글 리스트
                JSONArray jsonArray_comment_list_information = jsonObject.getJSONArray("comment_list_information");
                for(int i=0; i<jsonArray_comment_list_information.length(); i++){
                    JSONObject jsonObject_comment_list_information = jsonArray_comment_list_information.getJSONObject(i);
                    String boardComment_no = jsonObject_comment_list_information.getString("boardComment_no");
                    String member_email = jsonObject_comment_list_information.getString("member_email");
                    String member_name = jsonObject_comment_list_information.getString("member_name");
                    String member_img = jsonObject_comment_list_information.getString("member_img");
                    String boardComment_contents = jsonObject_comment_list_information.getString("boardComment_contents");
                    String boardComment_date = jsonObject_comment_list_information.getString("boardComment_date");
                    String boardComment_depth = jsonObject_comment_list_information.getString("boardComment_depth");
                    String boardComment_parent = jsonObject_comment_list_information.getString("boardComment_parent");

                    String comment_date =
                            boardComment_date.substring(0,4) + "년" + boardComment_date.substring(5,7) + "월" + boardComment_date.substring(8,10) + "일"
                                    + boardComment_date.substring(11,13) + "시" +boardComment_date.substring(14,16) + "분";
                    comment_arrayList.add(new BoardCommentVo(boardComment_no,member_email,member_name,member_img,boardComment_contents,comment_date,boardComment_depth,boardComment_parent));
                }
                comment_adapter.notifyDataSetChanged();
                recyclerView_board_comment_list.scrollToPosition(comment_adapter.getItemCount()-1);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(BoardCommentActivity.this, BoardDetailActivity.class);
        intent.putExtra("comment_count", comment_count);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * OnClick 클릭 이벤트
     *  - commentInsert_imageButtonClick : 댓글 등록하는 버튼을 누르면 댓글 등록이 된다.
     */
    @OnClick(R.id.imageView_comment_insert) void commentInsert_imageButtonClick(){
        new CommentInsertServerData().execute(editText_comment_insert.getText().toString());
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
        actionBar.setTitle("댓글");
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
