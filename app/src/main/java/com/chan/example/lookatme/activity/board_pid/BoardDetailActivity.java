package com.chan.example.lookatme.activity.board_pid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chan.example.lookatme.R;
import com.chan.example.lookatme.activity.main.MyPidFragment;
import com.chan.example.lookatme.activity.member.MemberPidActivity;
import com.chan.example.lookatme.adapter.BoardDetailImageSlideAdapter;
import com.chan.example.lookatme.function.Basic;

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

/**
 * 게시글 목록에서 해당 게시글(아이템)을 클릭해서 넘어오는 게시글 상세정보 액티비티.
 * 해당 게시글을 올린 사용자이면 게시글 수정과 삭제가 가능하다.
 */
public class BoardDetailActivity extends AppCompatActivity {

    private static final String TAG = BoardDetailActivity.class.getSimpleName();

    /**
     * RequestCode
     *  - RequestCode_board_comment : 해당 게시글 댓글창으로 가는 코드
     *  - RequestCode_board_update : 해당 게시글 수정창으로 가는 코드.
     *  - RequestCode_board_delete : 해당 게시글 삭제하는 코드.
     */
    private static final int RequestCode_board_comment = 100;
    private static final int RequestCode_board_update = 200;
    private static final int RequestCode_board_delete = 300;

    /**
     * BindView
     *  - toolBar : 화면 맨 상단에 위치한 툴바
     *  - imageView_writer_member_image : 게시글 작성한 회원 이미지를 보여주는 이미지뷰.
     *  - textView_writer_member_name : 게시글 작성한 회원 이름을 담은 텍스트뷰.
     *  - textView_board_date : 게시글 올린 날짜.
     *  - viewPager_image_slide : 게시글 이미지 나열되는 이미지 슬라이드 뷰페이저.
     *  - imageView_good_image : 게시글 좋아요 이미지.
     *  - textView_good_count : 게시글 좋아요 수.
     *  - imageView_comment_image : 게시글 댓글 이미지.
     *  - textView_comment_count : 게시글 댓글 수.
     *  - textView_board_contents : 게시글 내용.
     */
    @BindView(R.id.toolBar) Toolbar toolBar;
    @BindView(R.id.imageView_writer_member_image) CircleImageView imageView_writer_member_image;
    @BindView(R.id.textView_writer_member_name) TextView textView_writer_member_name;
    @BindView(R.id.textView_board_date) TextView textView_board_date;
    @BindView(R.id.viewPager_image_slide) ViewPager viewPager_image_slide;
    @BindView(R.id.imageView_good_image) ImageView imageView_good_image;
    @BindView(R.id.textView_good_count) TextView textView_good_count;
    @BindView(R.id.imageView_comment_image) ImageView imageView_comment_image;
    @BindView(R.id.textView_comment_count) TextView textView_comment_count;
    @BindView(R.id.textView_board_contents) TextView textView_board_contents;

    /**
     * 변수
     *  - login_member_email : 현재 로그인한 회원 이메일
     *  - board_no : 해당 게시글 번호
     *  - board_writer_member_email : 해당 게시글 작성한 회원 이메일
     *  - activity_status : 현재 창이 이전에 어떤 액티비티에서 열렸는가 확인
     *  - board_writer_memberName : 해당 게시글 작성한 회원 이름
     *
     *  - adapter : 게시글 이미지 슬라이드 어댑터.
     *  - arrayList_image : 게시글 이미지를 담은 ArrayList.
     *
     *  - board_good_image_check : 좋아요 이미지(하트)를 클릭하면 좋아요 상태인지 아닌지 체크하는 변수.
     *  - comment_count : 댓글 수.
     *  - update_check : 게시글이 수정 되었는지 안되었는지 확인 true:수정O false:수정X
     */
    String login_member_email;
    String board_no;
    String board_writer_member_email;
    String activity_status;
    String board_writer_memberName;

    BoardDetailImageSlideAdapter adapter;
    ArrayList arrayList_image;

    boolean board_good_image_check;
    int comment_count;
    boolean update_check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_detail);
        ButterKnife.bind(this);

        // 로그인한 회원 이메일.
        SharedPreferences spf_login_member = getSharedPreferences("loginMember", MODE_PRIVATE);
        login_member_email = spf_login_member.getString("loginMember_email", "");

        // 이전 액티비티에서 넘어온 인텐트 정보.
        Bundle extras = getIntent().getExtras();
        board_no = extras.getString("board_no"); // 게시글 번호.
        board_writer_member_email = extras.getString("member_email"); // 게시글 작성한 회원 이메일.
        activity_status = extras.getString("activity_status");

        arrayList_image = new ArrayList();
        adapter = new BoardDetailImageSlideAdapter(BoardDetailActivity.this, arrayList_image);

        update_check = false;

        toolBarSetting();

        new BoardDetailServerData().execute();

    }

    /**
     * OnClick 클릭 이벤트
     *  - memberPidGo : 해당 회원의 피드로 이동한다.
     *  - goodImageClick : 좋아요 이미지를 클릭하면 좋아요/좋지않아요 상태가 변한다.
     *  - commentClick_BoardCommentGo : 댓글 이미지나 댓글 수를 클릭하면 댓글창으로 이동한다.
     */
    @OnClick({R.id.imageView_writer_member_image, R.id.textView_writer_member_name}) void memberPidGo(){
        Intent intent = new Intent(BoardDetailActivity.this, MemberPidActivity.class);
        intent.putExtra("board_owner_member_email", board_writer_member_email);
        intent.putExtra("board_owner_member_name", board_writer_memberName);
        startActivity(intent);
    }
    @OnClick(R.id.imageView_good_image) void goodImageClick(){
        if(board_good_image_check == true) { // 좋아요 상태.
            new BoardGoodServerData().execute("delete");
        }
        else if(board_good_image_check == false) { // 좋아요 아닌 상태.
            new BoardGoodServerData().execute("insert");
        }
    }
    @OnClick({R.id.imageView_comment_image,R.id.textView_comment_count}) void commentClick_BoardCommentGo(){
        Intent intent = new Intent(BoardDetailActivity.this, BoardCommentActivity.class);
        intent.putExtra("board_no", board_no);
        intent.putExtra("comment_count", comment_count);
        startActivityForResult(intent, RequestCode_board_comment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case Activity.RESULT_OK:
                switch (requestCode){
                    case RequestCode_board_comment:
                        comment_count = data.getIntExtra("comment_count", comment_count);
                        textView_comment_count.setText(String.valueOf(comment_count));
                        break;

                    case RequestCode_board_update:
                        update_check = true;
                        String board_img_1 = data.getStringExtra("board_img_1");
                        String board_img_2 = data.getStringExtra("board_img_2");
                        String board_img_3 = data.getStringExtra("board_img_3");
                        String board_contents = data.getStringExtra("board_contents");

                        arrayList_image.clear();
                        if (!board_img_1.equals("")){
                            arrayList_image.add(board_img_1);
                        }
                        if (!board_img_2.equals("")){
                            arrayList_image.add(board_img_2);
                        }
                        if (!board_img_3.equals("")){
                            arrayList_image.add(board_img_3);
                        }
                        textView_board_contents.setText(board_contents);
                        viewPager_image_slide.setAdapter(adapter);
                        break;
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(update_check == true){
            Intent intent = new Intent(BoardDetailActivity.this, MyPidFragment.class);
            intent.putExtra("status","update");
            setResult(Activity.RESULT_OK, intent);
            finish();
        }else if(update_check == false){
            finish();
        }
    }

    /**
     * class
     *  - BoardDetailServerData : 처음 게시글 상세창이 나타날때 처음에 보여지는 모든 정보를 불러온다.
     *  - BoardGoodServerData : 좋아요 인지 아닌지 상태 변화에 따라 서버에서 데이터를 추가하거나 지운다.
     *  - BoardDeleteServerData : 게시글 삭제. 해당 게시글정보와 댓글, 좋아요가 모두 삭제된다.
     */
    private class BoardDetailServerData extends AsyncTask<Void,Void,String>{
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(BoardDetailActivity.this,"잠시만 기다려주세요.",null,true,true);
        }
        @Override
        protected String doInBackground(Void... Voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("board_writer_member_email",board_writer_member_email)
                        .add("board_no",board_no)
                        .add("member_email", login_member_email)
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_board_pid_php_directory_url+"board_detail.php")
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
                String board_writer_member_image = jsonObject.getString("board_writer_member_image"); // 게시글 작성자의 이미지
                String board_writer_member_name = jsonObject.getString("board_writer_member_name"); // 게시글 작성자의 이름
                String board_date = jsonObject.getString("board_date"); // 게시글 작성자의 이름
                String board_image_1 = jsonObject.getString("board_image_1"); // 게시글 1번 이미지
                String board_image_2 = jsonObject.getString("board_image_2"); // 게시글 2번 이미지
                String board_image_3 = jsonObject.getString("board_image_3"); // 게시글 3번 이미지
                String board_contents = jsonObject.getString("board_contents"); // 게시글 내용
                String boardGood_count = jsonObject.getString("boardGood_count"); // 게시글 좋아요 갯수
                String boardGood_check = jsonObject.getString("boardGood_check"); // 현재 게시글에 내가 좋아요를 눌렀는지 안눌렀는지 확인
                String boardComment_count = jsonObject.getString("boardComment_count"); // 현재 게시글에 댓글 수

                board_writer_memberName = board_writer_member_name;

                // 게시글 올린 사람의 이미지 정보
                Glide.with(BoardDetailActivity.this).load(Basic.server_member_image_directory_url+board_writer_member_image).asBitmap().into(imageView_writer_member_image);

                // 게시글 올린 사람의 이름
                textView_writer_member_name.setText(board_writer_member_name);

                // 해당 게시글 올린 날짜.
                String board_date_year_month_day = board_date.substring(0,4) + "년" + board_date.substring(5,7) + "월" + board_date.substring(8,10) + "일";
                textView_board_date.setText(board_date_year_month_day);

                // 게시글 이미지
                arrayList_image.add(board_image_1);
                if(!board_image_2.equals("") && board_image_2 != null){
                    arrayList_image.add(board_image_2);
                }
                if(!board_image_3.equals("") && board_image_3 != null){
                    arrayList_image.add(board_image_3);
                }
                viewPager_image_slide.setAdapter(adapter);

                // 게시글 좋아요
                textView_good_count.setText(boardGood_count);
                if(boardGood_check.equals("null")){
                    board_good_image_check = false;
                    imageView_good_image.setImageResource(R.drawable.icon_heart);
                }else{
                    board_good_image_check = true;
                    imageView_good_image.setImageResource(R.drawable.icon_heart_red);
                }

                // 게시글 댓글 수
                textView_comment_count.setText(boardComment_count);
                comment_count = Integer.parseInt(boardComment_count);

                // 게시글 내용
                textView_board_contents.setText(board_contents);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            progressDialog.dismiss();
        }
    }
    private class BoardGoodServerData extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("board_no", board_no)
                        .add("member_email", login_member_email)
                        .build();
                Request request;
                if(params[0].equals("insert")){
                    request = new Request.Builder()
                            .url(Basic.server_board_pid_php_directory_url+"boardGood_insert.php")
                            .post(requestBody)
                            .build();
                    client.newCall(request).execute();
                }
                else if(params[0].equals("delete")){
                    request = new Request.Builder()
                            .url(Basic.server_board_pid_php_directory_url+"boardGood_delete.php")
                            .post(requestBody)
                            .build();
                    client.newCall(request).execute();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return params[0];
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.equals("insert")){
                imageView_good_image.setImageResource(R.drawable.icon_heart_red);
                board_good_image_check = true;
                int good_count = Integer.parseInt(textView_good_count.getText().toString());
                good_count++;
                textView_good_count.setText(String.valueOf(good_count));
            }else if(result.equals("delete")){
                imageView_good_image.setImageResource(R.drawable.icon_heart);
                board_good_image_check = false;
                int good_count = Integer.parseInt(textView_good_count.getText().toString());
                good_count--;
                textView_good_count.setText(String.valueOf(good_count));
            }
        }
    }
    private class BoardDeleteServerData extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("board_no",board_no)
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_board_pid_php_directory_url + "board_delete.php")
                        .post(requestBody)
                        .build();
                client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Intent intent = new Intent(BoardDetailActivity.this, MyPidFragment.class);
            intent.putExtra("board_no",board_no);
            intent.putExtra("status","delete");
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    /**
     * 툴바 관련.
     *  - toolBarSetting_false : 툴바 설정.
     *  - onCreateOptionsMenu : 툴바 메뉴
     *  - onOptionsItemSelected : 툴바 선택
     */
    private void toolBarSetting(){
        toolBar.setTitleTextColor(Color.parseColor("#BA68C8"));
        setSupportActionBar(toolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); // 커스터마이징 하기 위해 필요.
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("게시글");
        actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (activity_status.equals("MyPidFragment")){
            getMenuInflater().inflate(R.menu.board_detail_menu, menu);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.update:
                Intent intent_board_update = new Intent(BoardDetailActivity.this, BoardUpdateActivity.class);
                intent_board_update.putExtra("board_no",board_no);
                startActivityForResult(intent_board_update, RequestCode_board_update);
                break;
            case R.id.delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(BoardDetailActivity.this);
                builder.setMessage("해당 게시글을 삭제하시겠습니까?");
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                });
                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new BoardDeleteServerData().execute();
                    }
                });
                builder.show();
                break;
        }
        return true;
    }
}
