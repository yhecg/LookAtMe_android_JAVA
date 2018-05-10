package com.chan.example.lookatme.activity.start;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import com.chan.example.lookatme.R;
import com.chan.example.lookatme.activity.main.MainActivity;
import com.chan.example.lookatme.function.BackPressCloseHandler;
import com.chan.example.lookatme.function.Basic;
import com.chan.example.lookatme.function.ClearEditText;
import com.chan.example.lookatme.service.ClientServerConnectService;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * LookAtMe 앱을 처음 시작할 때 로그인 화면.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    /**
     * requestCode_join : 회원가입 창과 연결된 requestCode.
     */
    private static int requestCode_join = 1;
    SharedPreferences spf_login_member;
    SharedPreferences.Editor spf_editor_login_member;

    /**
     * 뒤로가기 두 번 눌러 앱 종료.
     */
    private BackPressCloseHandler backPressCloseHandler;

    /**
     * BindView
     * editText_input_email : 이메일 입력하는 EditText.
     * editText_input_pwd : 비밀번호 입력하는 EditText.
     * button_login_lookAtMe : LookAtMe 계정으로 로그인 버튼.
     * button_join : 회원가입 버튼.
     */
    @BindView(R.id.editText_input_email)
    ClearEditText editText_input_email;
    @BindView(R.id.editText_input_pwd)
    ClearEditText editText_input_pwd;
    @BindView(R.id.button_login_lookAtMe)
    Button button_login_lookAtMe;
    @BindView(R.id.button_join)
    Button button_join;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        backPressCloseHandler = new BackPressCloseHandler(this);

        spf_login_member = getSharedPreferences("loginMember", MODE_PRIVATE);
        spf_editor_login_member = spf_login_member.edit();

    }

    /**
     * OnClick 클릭 이벤트
     *  - joinButtonClick : 버튼 클릭 시 회원가입 창으로 이동.
     *  - lookAtMeLoginButtonClick : 버튼 클릭 시 아이디 비밀번호를 확인하여 로그인창으로 이동.
     */
    @OnClick(R.id.button_join)
    void joinButtonClick(){
        Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
        startActivityForResult(intent, requestCode_join);
    }
    @OnClick(R.id.button_login_lookAtMe)
    void lookAtMeLoginButtonClick(){
        String member_email = editText_input_email.getText().toString();
        String member_pwd = editText_input_pwd.getText().toString();
        new MemberLoginSendServerData().execute(member_email,member_pwd);
    }

    /**
     * 로그인을 하기 위해 아이디 비밀번호를 서버에서 확인한 후 맞으면 메인화면으로(MainActivity) 이동, 틀리면 토스메세지를 띄우고 다시 입력해야 한다.
     */
    private class MemberLoginSendServerData extends AsyncTask<String, Void, String>{

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(LoginActivity.this, "잠시만 기다려주세요.", null, true, true);
        }

        @Override
        protected String doInBackground(String... strings) {
            String member_email = strings[0];
            String member_pwd = strings[1];
            OkHttpClient client = new OkHttpClient();
            try {
                RequestBody requestBody = new FormBody.Builder()
                        .add("member_email",member_email)
                        .add("member_pwd",member_pwd)
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_start_php_directory_url+"member_login.php")
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
            progressDialog.dismiss();
            if(result.equals("login_false")){
                Toast.makeText(LoginActivity.this, "아이디 비밀번호를 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
            }else{
                String member_email = editText_input_email.getText().toString();
                spf_editor_login_member.putString("loginMember_email", member_email);
                spf_editor_login_member.commit();

                Intent intent_service = new Intent(LoginActivity.this, ClientServerConnectService.class);
                startService(intent_service);

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == requestCode_join){
                Toast.makeText(this, "회원가입을 축하드립니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }
}
