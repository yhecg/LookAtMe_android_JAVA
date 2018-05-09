package com.chan.example.lookatme.activity.board_pid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chan.example.lookatme.R;
import com.chan.example.lookatme.function.Basic;

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
 * 게시글에서 수정을 누르면 나타나는 게시글 수정하는 화면.
 */
public class BoardUpdateActivity extends AppCompatActivity {

    private static final String TAG = BoardUpdateActivity.class.getSimpleName();

    /**
     * RequestCode
     *  - RequestCode_image_select_go : 게시글에 게시할 이미지 선택하러 가는 코드.
     */
    private static final int RequestCode_image_select_go = 200;

    ArrayList<String> image_filePath_list; // 이미지 선택창에서(ImageSelectActivity) 받아온 이미지 파일 경로를 담은 리스트.
    int image_check_count; // 이미지 선택창에서(ImageSelectActivity) 받아온 이미지의 갯수를 나타냄.
    boolean sub_image_check_1 = false; // 서브 이미지 1번자리에 사진이 있는지 없는지 확인. true 이면 사진이 있고 false 이면 사진이 없다.
    boolean sub_image_check_2 = false; // 서브 이미지 2번자리에 사진이 있는지 없는지 확인. true 이면 사진이 있고 false 이면 사진이 없다.
    boolean sub_image_check_3 = false; // 서브 이미지 3번자리에 사진이 있는지 없는지 확인. true 이면 사진이 있고 false 이면 사진이 없다.
    String sub_image_filePath_1 = ""; // 서브 이미지 1번 파일 경로.
    String sub_image_filePath_2 = ""; // 서브 이미지 2번 파일 경로.
    String sub_image_filePath_3 = ""; // 서브 이미지 3번 파일 경로.

    String login_member_email; // 로그인한 사용자 이메일.
    String board_no;

    /**
     * BindView
     *  - toolbar : 화면 맨 상단에 있는 툴바.
     *  - imageView_represent_img : 대표 이미지. 이미지 세개 중 하나를 클릭하면 대표이미지에 보여진다.
     *  - imageView_sub_image_1 : 1번 이미지 자리. 이미지가 선택되서 이 자리에 이미지가 나타나면 클릭 시 대표 이미지 자리에 이미지가 나타난다.
     *  - textView_sub_image_1_text : 1번 이미지 텍스트.
     *  - imageView_sub_image_2 : 2번 이미지 자리. 이미지가 선택되서 이 자리에 이미지가 나타나면 클릭 시 대표 이미지 자리에 이미지가 나타난다.
     *  - textView_sub_image_2_text : 2번 이미지 텍스트.
     *  - imageView_sub_image_3 : 3번 이미지 자리. 이미지가 선택되서 이 자리에 이미지가 나타나면 클릭 시 대표 이미지 자리에 이미지가 나타난다.
     *  - textView_sub_image_3_text : 3번 이미지 텍스트.
     *  - editText_contents_input : 게시글 내용 입력하는 EditText.
     *  - textView_contents_text_count : 게시글 내용 글자 숫자를 나타냄 ( ex ==> 0/100 ).
     */
    @BindView(R.id.toolBar) Toolbar toolbar;
    @BindView(R.id.imageView_represent_image) ImageView imageView_represent_image;
    @BindView(R.id.imageView_sub_image_1) ImageView imageView_sub_image_1;
    @BindView(R.id.textView_sub_image_1_text) TextView textView_sub_image_1_text;
    @BindView(R.id.imageView_sub_image_2) ImageView imageView_sub_image_2;
    @BindView(R.id.textView_sub_image_2_text) TextView textView_sub_image_2_text;
    @BindView(R.id.imageView_sub_image_3) ImageView imageView_sub_image_3;
    @BindView(R.id.textView_sub_image_3_text) TextView textView_sub_image_3_text;
    @BindView(R.id.editText_contents_input) EditText editText_contents_input;
    @BindView(R.id.textView_contents_text_count) TextView textView_contents_text_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_update);
        ButterKnife.bind(this);

        SharedPreferences spf_login_member = getSharedPreferences("loginMember", MODE_PRIVATE);
        login_member_email = spf_login_member.getString("loginMember_email", "");

        image_check_count = 0;

        Bundle extras = getIntent().getExtras();
        board_no = extras.getString("board_no");

        // 툴바 관련.
        toolbar.setTitleTextColor(Color.parseColor("#BA68C8"));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); // 커스터마이징 하기 위해 필요.
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("게시글 수정");
        actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        represent_and_sub_image_size_change();

        image_filePath_list = new ArrayList<>();

        editText_contents_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                textView_contents_text_count.setText(editable.length() + "/100");
            }
        });

        new BoardInformationServerData().execute();

    }

    /**
     * BoardInformationServerData : 게시물의 정보를 가져온다.
     * BoardInsertSendServerData : 게시물을 수정하기 위해 서버에 선택한 이미지와 게시글 내용 등 정보들을 보내서 DB 에 저장한다.
     */
    private class BoardInformationServerData extends AsyncTask<Void,Void,String>{
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(BoardUpdateActivity.this,"잠시만 기다려주세요.",null,true,true);
        }
        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("board_no", board_no)
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_board_pid_php_directory_url + "board_update_information.php")
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
            progressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String board_img_1 = jsonObject.getString("board_img_1");
                String board_img_2 = jsonObject.getString("board_img_2");
                String board_img_3 = jsonObject.getString("board_img_3");
                String board_contents = jsonObject.getString("board_contents");
                int image_count = Integer.parseInt(jsonObject.getString("image_count"));

                switch (image_count){
                    case 1:
                        Glide.with(BoardUpdateActivity.this).load(Basic.server_pid_image_directory_url+board_img_1).asBitmap().into(imageView_sub_image_1);
                        textView_sub_image_1_text.setVisibility(View.INVISIBLE);
                        break;
                    case 2:
                        Glide.with(BoardUpdateActivity.this).load(Basic.server_pid_image_directory_url+board_img_1).asBitmap().into(imageView_sub_image_1);
                        Glide.with(BoardUpdateActivity.this).load(Basic.server_pid_image_directory_url+board_img_2).asBitmap().into(imageView_sub_image_2);
                        textView_sub_image_1_text.setVisibility(View.INVISIBLE);
                        textView_sub_image_2_text.setVisibility(View.INVISIBLE);
                        break;
                    case 3:
                        Glide.with(BoardUpdateActivity.this).load(Basic.server_pid_image_directory_url+board_img_1).asBitmap().into(imageView_sub_image_1);
                        Glide.with(BoardUpdateActivity.this).load(Basic.server_pid_image_directory_url+board_img_2).asBitmap().into(imageView_sub_image_2);
                        Glide.with(BoardUpdateActivity.this).load(Basic.server_pid_image_directory_url+board_img_3).asBitmap().into(imageView_sub_image_3);
                        textView_sub_image_1_text.setVisibility(View.INVISIBLE);
                        textView_sub_image_2_text.setVisibility(View.INVISIBLE);
                        textView_sub_image_3_text.setVisibility(View.INVISIBLE);
                        break;
                }
                editText_contents_input.setText(board_contents);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
    private class BoardUpdateSendServerData extends AsyncTask<String, Void, String>{
        ProgressDialog progressDialog;
        OkHttpClient client = new OkHttpClient();
        Response response;

        /**
         * imageCount0() : 서버에 저장할 이미지 갯수 0개, 즉 내용만 수정한다.
         *  - imageFile_count : 선택된 이미지 파일 갯수.
         *  - login_member_email : 현재 로그인한 회원 이메일.
         *  - contents : 게시글 내용.
         *  - imageFile_path_1 : 1번 이미지 파일 경로(핸드폰).
         */
        private void imageCount0(int imageFile_count, String login_member_email, String contents){
            try {
                RequestBody requestBody = new FormBody.Builder()
                        .add("board_no", board_no)
                        .add("imageFile_count", String.valueOf(imageFile_count))
                        .add("member_email", login_member_email)
                        .add("board_contents", contents)
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_board_pid_php_directory_url + "board_update.php")
                        .post(requestBody)
                        .build();
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * imageCount1() : 서버에 저장할 이미지 갯수가 하나일 경우 하나의 이미지와 게시글 내용이 담긴 데이터를 서버로 보낸다.
         *  - imageFile_count : 선택된 이미지 파일 갯수.
         *  - login_member_email : 현재 로그인한 회원 이메일.
         *  - contents : 게시글 내용.
         *  - imageFile_path_1 : 1번 이미지 파일 경로(핸드폰).
         */
        private void imageCount1(int imageFile_count, String login_member_email, String contents, String imageFile_path_1){
            try {
                String imageFile_name_1 = login_member_email + "_1_" + Basic.nowDate("file") + ".jpg"; // 서버 DB 에 저장될 1번 이미지 파일 이름.
                MultipartBody builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("board_no", board_no)
                        .addFormDataPart("imageFile_count", String.valueOf(imageFile_count))
                        .addFormDataPart("member_email", login_member_email)
                        .addFormDataPart("board_contents", contents)
                        .addFormDataPart("board_img_1", imageFile_name_1)
                        .addFormDataPart("image_file_1", imageFile_name_1, RequestBody.create(MultipartBody.FORM, new File(imageFile_path_1)))
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_board_pid_php_directory_url + "board_update.php")
                        .post(builder)
                        .build();
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * imageCount2() : 서버에 저장할 이미지 갯수가 세개일 경우 세개의 이미지와 게시글 내용이 담긴 데이터를 서버로 보낸다.
         *  - imageFile_count : 선택된 이미지 파일 갯수.
         *  - login_member_email : 현재 로그인한 회원 이메일.
         *  - contents : 게시글 내용.
         *  - imageFile_path_1 : 1번 이미지 파일 경로(핸드폰).
         *  - imageFile_path_2 : 2번 이미지 파일 경로(핸드폰).
         */
        private void imageCount2(int imageFile_count, String login_member_email, String contents, String imageFile_path_1, String imageFile_path_2){
            try {
                String imageFile_name_1 = login_member_email + "_1_" + Basic.nowDate("file") + ".jpg"; // 서버 DB 에 저장될 1번 이미지 파일 이름.
                String imageFile_name_2 = login_member_email + "_2_" + Basic.nowDate("file") + ".jpg"; // 서버 DB 에 저장될 2번 이미지 파일 이름.
                MultipartBody builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("board_no", board_no)
                        .addFormDataPart("imageFile_count", String.valueOf(imageFile_count))
                        .addFormDataPart("member_email", login_member_email)
                        .addFormDataPart("board_contents", contents)
                        .addFormDataPart("board_img_1", imageFile_name_1)
                        .addFormDataPart("image_file_1", imageFile_name_1, RequestBody.create(MultipartBody.FORM, new File(imageFile_path_1)))
                        .addFormDataPart("board_img_2", imageFile_name_2)
                        .addFormDataPart("image_file_2", imageFile_name_2, RequestBody.create(MultipartBody.FORM, new File(imageFile_path_2)))
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_board_pid_php_directory_url + "board_update.php")
                        .post(builder)
                        .build();
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * imageCount3() : 서버에 저장할 이미지 갯수가 두개일 경우 두개의 이미지와 게시글 내용이 담긴 데이터를 서버로 보낸다.
         *  - imageFile_count : 선택된 이미지 파일 갯수.
         *  - login_member_email : 현재 로그인한 회원 이메일.
         *  - contents : 게시글 내용.
         *  - imageFile_path_1 : 1번 이미지 파일 경로(핸드폰).
         *  - imageFile_path_2 : 2번 이미지 파일 경로(핸드폰).
         *  - imageFile_path_3 : 3번 이미지 파일 경로(핸드폰).
         */
        private void imageCount3(int imageFile_count, String login_member_email, String contents, String imageFile_path_1, String imageFile_path_2, String imageFile_path_3){
            try {
                String imageFile_name_1 = login_member_email + "_1_" + Basic.nowDate("file") + ".jpg"; // 서버 DB 에 저장될 1번 이미지 파일 이름.
                String imageFile_name_2 = login_member_email + "_2_" + Basic.nowDate("file") + ".jpg"; // 서버 DB 에 저장될 2번 이미지 파일 이름.
                String imageFile_name_3 = login_member_email + "_3_" + Basic.nowDate("file") + ".jpg"; // 서버 DB 에 저장될 3번 이미지 파일 이름.
                MultipartBody builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("board_no", board_no)
                        .addFormDataPart("imageFile_count", String.valueOf(imageFile_count))
                        .addFormDataPart("member_email", login_member_email)
                        .addFormDataPart("board_contents", contents)
                        .addFormDataPart("board_img_1", imageFile_name_1)
                        .addFormDataPart("image_file_1", imageFile_name_1, RequestBody.create(MultipartBody.FORM, new File(imageFile_path_1)))
                        .addFormDataPart("board_img_2", imageFile_name_2)
                        .addFormDataPart("image_file_2", imageFile_name_2, RequestBody.create(MultipartBody.FORM, new File(imageFile_path_2)))
                        .addFormDataPart("board_img_3", imageFile_name_3)
                        .addFormDataPart("image_file_3", imageFile_name_3, RequestBody.create(MultipartBody.FORM, new File(imageFile_path_3)))
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_board_pid_php_directory_url + "board_update.php")
                        .post(builder)
                        .build();
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(BoardUpdateActivity.this, "게시물을 수정중입니다.", null, true, true);
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                switch (Integer.parseInt(strings[0])){
                    case 0:
                        imageCount0(Integer.parseInt(strings[0]),strings[1],strings[2]);
                        break;
                    case 1:
                        imageCount1(Integer.parseInt(strings[0]),strings[1],strings[2],strings[3]);
                        break;
                    case 2:
                        imageCount2(Integer.parseInt(strings[0]),strings[1],strings[2],strings[3],strings[4]);
                        break;
                    case 3:
                        imageCount3(Integer.parseInt(strings[0]),strings[1],strings[2],strings[3],strings[4],strings[5]);
                        break;
                }
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                Intent intent = new Intent(BoardUpdateActivity.this, BoardDetailActivity.class);
                intent.putExtra("board_img_1", jsonObject.getString("board_img_1"));
                intent.putExtra("board_img_2", jsonObject.getString("board_img_2"));
                intent.putExtra("board_img_3", jsonObject.getString("board_img_3"));
                intent.putExtra("board_contents", jsonObject.getString("board_contents"));
                setResult(Activity.RESULT_OK, intent);
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * OnClick 클릭 이벤트
     *  - representImageClick() : 대표 이미지(이미지뷰)를 클릭하면 이미지 선택 화면으로 (ImageSelectActivity) 이동한다.
     *  - subImageClick() : 서브 이미지를 클릭하면 이미지가 있는 경우 대표 이미지 자리로 이동한다.
     *      subImageClick() switch case
     *          - imageView_sub_image_1 : 서브 이미지 1번을 클릭했을 경우 1번 서브 이미지를 대표 이미지 자리에 넣어 보여준다.
     *          - imageView_sub_image_2 : 서브 이미지 2번을 클릭했을 경우 2번 서브 이미지를 대표 이미지 자리에 넣어 보여준다.
     *          - imageView_sub_image_3 : 서브 이미지 3번을 클릭했을 경우 3번 서브 이미지를 대표 이미지 자리에 넣어 보여준다.
     */
    @OnClick(R.id.imageView_represent_image)
    void boardUpdateRepresentImageClick(){
        Intent intent = new Intent(BoardUpdateActivity.this, ImageSelectActivity.class);
        intent.putExtra("status", "update");
        startActivityForResult(intent, RequestCode_image_select_go);
    }
    @OnClick({ R.id.imageView_sub_image_1, R.id.imageView_sub_image_2, R.id.imageView_sub_image_3 })
    void boardUpdateSubImageClick(View view){
        switch (view.getId()){
            case R.id.imageView_sub_image_1:
                if(sub_image_check_1 == true){
                    Bitmap bitmap = BitmapFactory.decodeFile(sub_image_filePath_1);
                    Bitmap resize = Bitmap.createScaledBitmap(bitmap, Basic.bitmap_resize, Basic.bitmap_resize, true);
                    imageView_represent_image.setImageBitmap(resize);
                }
                break;
            case R.id.imageView_sub_image_2:
                if(sub_image_check_2 == true){
                    Bitmap bitmap = BitmapFactory.decodeFile(sub_image_filePath_2);
                    Bitmap resize = Bitmap.createScaledBitmap(bitmap, Basic.bitmap_resize, Basic.bitmap_resize, true);
                    imageView_represent_image.setImageBitmap(resize);
                }
                break;
            case R.id.imageView_sub_image_3:
                if(sub_image_check_3 == true){
                    Bitmap bitmap = BitmapFactory.decodeFile(sub_image_filePath_3);
                    Bitmap resize = Bitmap.createScaledBitmap(bitmap, Basic.bitmap_resize, Basic.bitmap_resize, true);
                    imageView_represent_image.setImageBitmap(resize);
                }
                break;
        }
    }

    /**
     * 툴바 관련.
     *  - onCreateOptionsMenu : 툴바 메뉴
     *  - onOptionsItemSelected : 툴바 선택
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.board_update_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.success:
                switch (image_check_count){
                    case 0:
                        // 이미지 파일 갯수, 현재 로그인한 사용자 이메일, 게시글 내용
                        new BoardUpdateSendServerData().execute(
                                String.valueOf(image_check_count), login_member_email,
                                editText_contents_input.getText().toString());
                        break;
                    case 1:
                        // 이미지 파일 갯수, 현재 로그인한 사용자 이메일, 게시글 내용, 1번 이미지 파일 경로
                        new BoardUpdateSendServerData().execute(
                                String.valueOf(image_check_count), login_member_email,
                                editText_contents_input.getText().toString(), sub_image_filePath_1);
                        break;
                    case 2:
                        // 이미지 파일 갯수, 현재 로그인한 사용자 이메일, 게시글 내용, 1번 이미지 파일 경로, 2번 이미지 파일 경로
                        new BoardUpdateSendServerData().execute(
                                String.valueOf(image_check_count), login_member_email,
                                editText_contents_input.getText().toString(), sub_image_filePath_1, sub_image_filePath_2);
                        break;
                    case 3:
                        // 이미지 파일 갯수, 현재 로그인한 사용자 이메일, 게시글 내용, 1번 이미지 파일 경로, 2번 이미지 파일 경로, 3번 이미지 파일 경로
                        new BoardUpdateSendServerData().execute(
                                String.valueOf(image_check_count), login_member_email,
                                editText_contents_input.getText().toString(), sub_image_filePath_1, sub_image_filePath_2, sub_image_filePath_3);
                        break;
                }
                break;
        }
        return true;
    }

    /**
     * 대표 이미지와 서브 이미지들(3개)의 크기를 변화시켜준다.
     * represent_and_sub_image_size_change()
     */
    private void represent_and_sub_image_size_change(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)imageView_represent_image.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels; // 핸드폰의 가로 해상도를 구함.

        int represent_image = deviceWidth / 2; // 대표 이미지 크기.
        imageView_represent_image.getLayoutParams().width = represent_image;
        imageView_represent_image.getLayoutParams().height = represent_image;
        imageView_represent_image.requestLayout();

        int sub_image = deviceWidth / 4; // 서브 이미지 3개 크기.
        imageView_sub_image_1.getLayoutParams().width = sub_image;
        imageView_sub_image_1.getLayoutParams().height = sub_image;
        imageView_sub_image_1.requestLayout();
        imageView_sub_image_2.getLayoutParams().width = sub_image;
        imageView_sub_image_2.getLayoutParams().height = sub_image;
        imageView_sub_image_2.requestLayout();
        imageView_sub_image_3.getLayoutParams().width = sub_image;
        imageView_sub_image_3.getLayoutParams().height = sub_image;
        imageView_sub_image_3.requestLayout();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            switch (requestCode){
                case RequestCode_image_select_go:
                    image_filePath_list = data.getStringArrayListExtra("image_filePath_list");
                    image_check_count = data.getIntExtra("image_check_count", 0);

                    // 대표 이미지와 서브 이미지들을 비워주는 (지정된 데이터 값을 없애주는) 초기화 작업.
                    imageView_represent_image.setImageResource(R.drawable.icon_square);
                    textView_sub_image_1_text.setVisibility(View.VISIBLE);
                    textView_sub_image_2_text.setVisibility(View.VISIBLE);
                    textView_sub_image_3_text.setVisibility(View.VISIBLE);
                    imageView_sub_image_1.setImageResource(R.drawable.icon_square_2);
                    imageView_sub_image_2.setImageResource(R.drawable.icon_square_2);
                    imageView_sub_image_3.setImageResource(R.drawable.icon_square_2);
                    sub_image_check_1 = false;
                    sub_image_filePath_1 = "";
                    sub_image_check_2 = false;
                    sub_image_filePath_2 = "";
                    sub_image_check_3 = false;
                    sub_image_filePath_3 = "";

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;

                    switch (image_check_count){
                        case 1:
                            Bitmap bitmap_1 = BitmapFactory.decodeFile(image_filePath_list.get(0), options);
                            Bitmap resize_1 = Bitmap.createScaledBitmap(bitmap_1, Basic.bitmap_resize, Basic.bitmap_resize, true);
                            imageView_represent_image.setImageBitmap(resize_1);

                            Bitmap bitmap_1_count_1 = BitmapFactory.decodeFile(image_filePath_list.get(0), options);
                            Bitmap resize_1_count_1 = Bitmap.createScaledBitmap(bitmap_1_count_1, Basic.bitmap_resize, Basic.bitmap_resize, true);
                            imageView_sub_image_1.setImageBitmap(resize_1_count_1);
                            textView_sub_image_1_text.setVisibility(View.INVISIBLE);
                            sub_image_check_1 = true;
                            sub_image_filePath_1 = image_filePath_list.get(0);
                            break;
                        case 2:
                            Bitmap bitmap_2 = BitmapFactory.decodeFile(image_filePath_list.get(0), options);
                            Bitmap resize_2 = Bitmap.createScaledBitmap(bitmap_2, Basic.bitmap_resize, Basic.bitmap_resize, true);
                            imageView_represent_image.setImageBitmap(resize_2);

                            Bitmap bitmap_1_count_2 = BitmapFactory.decodeFile(image_filePath_list.get(0), options);
                            Bitmap resize_1_count_2 = Bitmap.createScaledBitmap(bitmap_1_count_2, Basic.bitmap_resize, Basic.bitmap_resize, true);
                            imageView_sub_image_1.setImageBitmap(resize_1_count_2);
                            textView_sub_image_1_text.setVisibility(View.INVISIBLE);
                            sub_image_check_1 = true;
                            sub_image_filePath_1 = image_filePath_list.get(0);

                            Bitmap bitmap_2_count_2 = BitmapFactory.decodeFile(image_filePath_list.get(1), options);
                            Bitmap resize_2_count_2 = Bitmap.createScaledBitmap(bitmap_2_count_2, Basic.bitmap_resize, Basic.bitmap_resize, true);
                            imageView_sub_image_2.setImageBitmap(resize_2_count_2);
                            textView_sub_image_2_text.setVisibility(View.INVISIBLE);
                            sub_image_check_2 = true;
                            sub_image_filePath_2 = image_filePath_list.get(1);
                            break;
                        case 3:
                            Bitmap bitmap_3 = BitmapFactory.decodeFile(image_filePath_list.get(0), options);
                            Bitmap resize_3 = Bitmap.createScaledBitmap(bitmap_3, Basic.bitmap_resize, Basic.bitmap_resize, true);
                            imageView_represent_image.setImageBitmap(resize_3);

                            Bitmap bitmap_1_count_3 = BitmapFactory.decodeFile(image_filePath_list.get(0), options);
                            Bitmap resize_1_count_3 = Bitmap.createScaledBitmap(bitmap_1_count_3, Basic.bitmap_resize, Basic.bitmap_resize, true);
                            imageView_sub_image_1.setImageBitmap(resize_1_count_3);
                            textView_sub_image_1_text.setVisibility(View.INVISIBLE);
                            sub_image_check_1 = true;
                            sub_image_filePath_1 = image_filePath_list.get(0);

                            Bitmap bitmap_2_count_3 = BitmapFactory.decodeFile(image_filePath_list.get(1), options);
                            Bitmap resize_2_count_3 = Bitmap.createScaledBitmap(bitmap_2_count_3, Basic.bitmap_resize, Basic.bitmap_resize, true);
                            imageView_sub_image_2.setImageBitmap(resize_2_count_3);
                            textView_sub_image_2_text.setVisibility(View.INVISIBLE);
                            sub_image_check_2 = true;
                            sub_image_filePath_2 = image_filePath_list.get(1);

                            Bitmap bitmap_3_count_3 = BitmapFactory.decodeFile(image_filePath_list.get(2), options);
                            Bitmap resize_3_count_3 = Bitmap.createScaledBitmap(bitmap_3_count_3, Basic.bitmap_resize, Basic.bitmap_resize, true);
                            imageView_sub_image_3.setImageBitmap(resize_3_count_3);
                            textView_sub_image_3_text.setVisibility(View.INVISIBLE);
                            sub_image_check_3 = true;
                            sub_image_filePath_3 = image_filePath_list.get(2);
                            break;
                    }
                    break;
            }
        }
    }
}
