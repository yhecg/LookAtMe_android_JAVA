package com.chan.example.lookatme.activity.start;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.chan.example.lookatme.R;
import com.chan.example.lookatme.function.Basic;
import com.chan.example.lookatme.function.ClearEditText;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 회원가입창.
 * 로그인창에서 회원가입버튼을 눌렀을때 나타남.
 */
public class JoinActivity extends AppCompatActivity {

    private static final String TAG = JoinActivity.class.getSimpleName();

    /**
     * 프로필 이미지 관련.
     *
     * RequestCode
     *  - RequestCode_photo_album : 사진 앨범 코드.
     *
     * 변수
     *  - image_uri : 사진 uri.
     *  - image_file_path : 사진 저장 경로.
     *  - image_check : true 이면 이미지 등록은 한 것이고 false 이면 이미지 등록을 하지 않은 것이다.
     */
    private static final int RequestCode_photo_album = 100;
    private Uri image_uri;
    private String image_file_path;
    private String image_check = "false";

    /**
     * BindView
     * toolBar_top : 화면 맨 상단에 있는 툴바.
     * imageView_profile : 프로필 이미지. 클릭 시 프로필 사진 찍기 또는 앨범에서 사진을 가져온다.
     * editText_input_email : 이메일 입력하는 EditText.
     * editText_input_pwd : 비밀번호 입력하는 EditText.
     * editText_input_name : 이름 입력하는 EditText.
     * button_joinComplete : 회원가입 완료 버튼. 회원가입이 완료되면 회원가입창이 종료된다.
     * textView_email_check : 회원가입 완료버튼 눌렀을때 이메일을 입력하지 않거나 이메일 형식이 아닐경우 오류메세지 나타난다.
     * textView_pwd_check : 회원가입 완료버튼 눌렀을때 비밀번호를 입력하지 않거나 비밀번호 형식이 아닐경우 오류메세지 나타난다.
     * textView_name_check : 회원가입 완료버튼 눌렀을때 이름을 입력하지 않으면 오류메세지 나타난다.
     */
    @BindView(R.id.toolBar_top)
    Toolbar toolBar_top;
    @BindView(R.id.imageView_profile)
    CircleImageView imageView_profile;
//    ImageView imageView_profile;
    @BindView(R.id.editText_input_email)
    ClearEditText editText_input_email;
    @BindView(R.id.editText_input_pwd)
    ClearEditText editText_input_pwd;
    @BindView(R.id.editText_input_name)
    ClearEditText editText_input_name;
    @BindView(R.id.button_joinComplete)
    Button button_joinComplete;
    @BindView(R.id.textView_email_check)
    TextView textView_email_check;
    @BindView(R.id.textView_pwd_check)
    TextView textView_pwd_check;
    @BindView(R.id.textView_name_check)
    TextView textView_name_check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        ButterKnife.bind(this);
        toolBar_setting();
    }

    /**
     * OnClick 클릭 이벤트.
     * profileImageClick: 프로필 이미지 클릭 시 앨범에서 사진을 가져올 수 있다.
     * joinCompleteButtonClick : 회원가입 완료 버튼 클릭 시 유효성검사를 한 뒤 회원가입에 성공하면 현재 회원가입창은 종료된다.
     */
    @OnClick(R.id.imageView_profile)
    void profileImageClick(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RequestCode_photo_album);
    }
    @OnClick(R.id.button_joinComplete)
    void joinCompleteButtonClick(){
        /**
         * 변수
         *  email : 이메일 입력하는 EditText에 적힌 글자를 담은 String 변수.
         *  pwd : 패스워드 입력하는 EditText에 적힌 글자를 담은 String 변수.
         *  name : 이름 입력하는 EditText에 적힌 글자를 담은 String 변수.
         */
        String email = editText_input_email.getText().toString();
        String pwd = editText_input_pwd.getText().toString();
        String name = editText_input_name.getText().toString();

        /**
         * 유효성검사
         */
        if(TextUtils.isEmpty(email) || email.matches("\\s+") || email.length()==0){
            textView_email_check.setText("이메일을 입력해주세요.");
            textView_email_check.setTextColor(getResources().getColor(R.color.red));
            editText_input_email.requestFocus();
            return;
        }else{
            textView_email_check.setText("");
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            textView_email_check.setText("이메일 형식에 맞게 입력해주세요.");
            textView_email_check.setTextColor(getResources().getColor(R.color.red));
            editText_input_email.requestFocus();
            return;
        }else{
            textView_email_check.setText("");
        }
        if(TextUtils.isEmpty(pwd) || pwd.matches("\\s+") || pwd.length()==0){
            textView_pwd_check.setText("비밀번호를 입력해주세요.");
            textView_pwd_check.setTextColor(getResources().getColor(R.color.red));
            editText_input_pwd.requestFocus();
            return;
        }else{
            textView_pwd_check.setText("");
        }
        Pattern pattern_pwd = Pattern.compile("(^(?=.*[a-zA-Z]+)(?=.*[!@#$%^*+=-]|.*[0-9]+).{6,12}$)");
        Matcher matcher_pwd = pattern_pwd.matcher(pwd);
        if(!matcher_pwd.find()){
            textView_pwd_check.setText("비밀번호 형식에 맞게 입력하세요.");
            textView_pwd_check.setTextColor(getResources().getColor(R.color.red));
            editText_input_pwd.requestFocus();
            return;
        }else{
            textView_pwd_check.setText("");
        }
        if(TextUtils.isEmpty(name) || name.matches("\\s+") || name.length()==0){
            textView_name_check.setText("이름을 입력해주세요.");
            textView_name_check.setTextColor(getResources().getColor(R.color.red));
            editText_input_name.requestFocus();
            return;
        }else{
            textView_name_check.setText("");
        }

        /**
         * 모든 정보가 정확하게 입력이 되었으면 회원가입에 성공하여 데이터가 DB에 저장되고 회원가입창 종료된다.
         */
        new MemberJoinSendServerData().execute(email, pwd, name);
    }

    /**
     * 회원가입 하기 위해 서버에 이미지 파일과 입력한 회원 정보를 보내 서버에서 이미지 파일 업로드를 하고 DB에 정보를 저장한 뒤 해당 액티비티가 종료된다.
     */
    private class MemberJoinSendServerData extends AsyncTask<String,Void,String>{

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(JoinActivity.this, "회원가입이 진행중입니다.", null, true, true);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String member_email = strings[0];
                String member_pwd = strings[1];
                String member_name = strings[2];

                OkHttpClient client = new OkHttpClient();

                if(image_check.equals("true")){
                    String member_img = member_email + "_" + Basic.nowDate("file") + ".jpg";
//                    String member_img = member_email+".jpg";

                    MultipartBody builder = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("image_check", image_check)
                            .addFormDataPart("member_email", member_email)
                            .addFormDataPart("member_pwd", member_pwd)
                            .addFormDataPart("member_name", member_name)
                            .addFormDataPart("member_img", member_img)
                            .addFormDataPart("member_img_file", member_img, RequestBody.create(MultipartBody.FORM, new File(image_file_path)))
                            .build();

                    Request request = new Request.Builder()
                            .url(Basic.server_start_php_directory_url+"member_join.php")
                            .post(builder)
                            .build();

                    client.newCall(request).execute();
                }
                else if(image_check.equals("false")){
                    RequestBody requestBody = new FormBody.Builder()
                            .add("image_check", image_check)
                            .add("member_email", member_email)
                            .add("member_pwd", member_pwd)
                            .add("member_name", member_name)
                            .build();

                    Request request = new Request.Builder()
                            .url(Basic.server_start_php_directory_url+"member_join.php")
                            .post(requestBody)
                            .build();

                    client.newCall(request).execute();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            Intent intent = new Intent(JoinActivity.this, LoginActivity.class);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    /**
     * 툴바 관련
     * toolBar_setting : 회원가입 창 맨 상단에 툴바 설정 세팅.
     * onOptionsItemSelected : 회원가입 창 맨 상단에 툴바 선택 메뉴.
     */
    private void toolBar_setting(){
        setSupportActionBar(toolBar_top); // 액션바와 같게 만들어줌.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); // 커스터마이징 하기 위해 필요.
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼 생김.
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back); // 뒤로가기 버튼 아이콘.
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 뒤로가기
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 앨범 사진 파일 경로와 사진 파일 이름.
     * getImageFilePath : 파일 경로.
     */
    public String getImageFilePath(Uri data) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = this.getContentResolver().query(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String imgPath = cursor.getString(column_index);
        return imgPath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            switch (requestCode){
                case RequestCode_photo_album:
                    try {
                        image_uri = data.getData();
                        image_file_path = getImageFilePath(image_uri);

                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                        Bitmap resize = Bitmap.createScaledBitmap(bitmap, Basic.bitmap_resize, Basic.bitmap_resize, true);
//                        imageView_profile.setImageBitmap(Basic.setRoundCorner(resize));
                        imageView_profile.setImageBitmap(resize);

                        image_check = "true";
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }





}
