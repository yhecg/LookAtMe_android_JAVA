package com.chan.example.lookatme.activity.member;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.chan.example.lookatme.R;
import com.chan.example.lookatme.activity.main.MyPidFragment;
import com.chan.example.lookatme.function.Basic;
import com.chan.example.lookatme.function.ClearEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 내 피드 창에서(MyPidFragment) 프로필 수정버튼을 눌렀을 때 나타나는 화면.
 * 이미지와 이름을 변경하여 프로필을 수정할 수 있다.
 */
public class MemberProfileUpdateActivity extends AppCompatActivity {

    private static final String TAG = MemberProfileUpdateActivity.class.getSimpleName();

    /**
     * RequestCode
     *  - RequestCode_photo_album : 내 디바이스의 앨범으로 가는 코드.
     *
     * 변수
     *  - login_member_email : 현재 로그인한 회원의 이메일
     *  - image_filePath : 현재 이미지가 아닌 앨범에서 새로 이미지를 선택했을 때 해당 이미지의 파일 경로
     *  - image_check : 이미지를 새로 선택했는지(이미지가 변경되었는지) 확인하는 변수
     *  - previous_image : 서버 DB 에 입력되어있는 기존 이미지의 파일 이름.
     */
    private static final int RequestCode_photo_album = 100;
    String login_member_email;
    String image_filePath;
    String image_check = "false";
    String previous_image;

    /**
     * BindView
     *  - linearLayout_represent : 전체 화면을 감싸고 있는 레이아웃
     *  - toolbar : 화면 맨 상단에 있는 툴바
     *  - imageView_member_profile_image : 프로필 이미지뷰
     *  - editText_member_name : 이름을 입력하는 editText
     */
    @BindView(R.id.linearLayout_represent) LinearLayout linearLayout_represent;
    @BindView(R.id.toolBar) Toolbar toolbar;
    @BindView(R.id.imageView_member_profile_image) CircleImageView imageView_member_profile_image;
    @BindView(R.id.editText_member_name) ClearEditText editText_member_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_profile_update);
        ButterKnife.bind(this);

        SharedPreferences spf_login_member = getSharedPreferences("loginMember", MODE_PRIVATE);
        login_member_email = spf_login_member.getString("loginMember_email", "");

//        profileImageSizeSetting();

        toolBarSetting();

        new MemberInformationServerData().execute();

    }

    /**
     * 툴바 관련.
     *  - toolBarSetting : 툴바 설정
     *  - onCreateOptionsMenu : 툴바 메뉴
     *  - onOptionsItemSelected : 툴바 선택
     */
    private void toolBarSetting(){
        toolbar.setTitleTextColor(Color.parseColor("#BA68C8"));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); // 커스터마이징 하기 위해 필요.
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("프로필 수정");
        actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.member_profile_update_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.success:
                new MemberUpdateServerData().execute(editText_member_name.getText().toString());
                break;
        }
        return true;
    }

    /**
     * OnClick 클릭 이벤트
     *  - profileImageClick : 프로필 이미지뷰를 클릭했을 때 이벤트. 프로필 이미지뷰를 클릭하면 사진 앨범으로 이동한다.
     */
    @OnClick(R.id.imageView_member_profile_image) void profileImageClick(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RequestCode_photo_album);
    }

    /**
     * class
     *  - MemberInformationServerData : 서버에서 회원 이미지와 이름 정보를 받아와서 이미지와 이름 view 에 정보를 뿌려준다.
     *  - MemberUpdateServerData : 변경된 이미지와 이름을 서버 DB 에 새로 저장하고 이미지가 변경되었을때
     *                             서버에 기존 이미지 파일을 삭제하고 새로운 이미지 파일을 업로드 시킨다.
     *                             프로필 수정이 완료되면 해당 액티비티는 종료되고 내피드(MyPidFragment)에 변경된 정보가 넘어가고 내피드에서 정보가 최신화된다.
     */
    private class MemberInformationServerData extends AsyncTask<Void,Void,String>{
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MemberProfileUpdateActivity.this, "잠시만 기다려주세요.", null, true, true);
        }
        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("member_email", login_member_email)
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_member_php_directory_url + "member_information.php")
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
                previous_image = jsonObject.getString("member_img");
                String member_name = jsonObject.getString("member_name");
                if(!previous_image.equals("") && previous_image != null){
                    Glide.with(MemberProfileUpdateActivity.this).load(Basic.server_member_image_directory_url+previous_image).asBitmap().into(imageView_member_profile_image);
                }
                editText_member_name.setText(member_name);
                progressDialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class MemberUpdateServerData extends AsyncTask<String,Void,JSONObject>{
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MemberProfileUpdateActivity.this, "잠시만 기다려주세요.", null, true, true);
        }
        @Override
        protected JSONObject doInBackground(String... strings) {
            try {
                OkHttpClient client = new OkHttpClient();
                String member_name = strings[0];
                if(image_check.equals("true")){
                    String member_img = login_member_email + "_" + Basic.nowDate("file") + ".jpg";
//                    String member_img = login_member_email+".jpg";
                    MultipartBody builder = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("image_check", image_check)
                            .addFormDataPart("member_email", login_member_email)
                            .addFormDataPart("previous_image", previous_image)
                            .addFormDataPart("member_name", member_name)
                            .addFormDataPart("member_img", member_img)
                            .addFormDataPart("member_img_file", member_img, RequestBody.create(MultipartBody.FORM, new File(image_filePath)))
                            .build();
                    Request request = new Request.Builder()
                            .url(Basic.server_member_php_directory_url+"member_update.php")
                            .post(builder)
                            .build();
                    client.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("image_update_check", true);
                    jsonObject.put("member_img", member_img);
                    return jsonObject;
                }else if(image_check.equals("false")){
                    RequestBody requestBody = new FormBody.Builder()
                            .add("image_check", image_check)
                            .add("member_email", login_member_email)
                            .add("member_name", member_name)
                            .build();
                    Request request = new Request.Builder()
                            .url(Basic.server_member_php_directory_url+"member_update.php")
                            .post(requestBody)
                            .build();
                    client.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("image_update_check", false);
                    return jsonObject;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            progressDialog.dismiss();
            try {
                Intent intent = new Intent(MemberProfileUpdateActivity.this, MyPidFragment.class);
                boolean image_update_check = jsonObject.getBoolean("image_update_check");
                intent.putExtra("image_update_check", image_update_check);
                if(image_update_check == true){
                    intent.putExtra("member_img", jsonObject.getString("member_img"));
                }
                setResult(Activity.RESULT_OK, intent);
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            switch (requestCode){
                case RequestCode_photo_album:
                    try {
                        image_filePath = getImageFilePath(data.getData());
                        image_check = "true";
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                        Bitmap resize = Bitmap.createScaledBitmap(bitmap, Basic.bitmap_resize, Basic.bitmap_resize, true);
                        imageView_member_profile_image.setImageBitmap(resize);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    /**
     * 앨범 사진 파일 경로와 사진 파일 이름.
     * getImageFilePath() : 파일 경로.
     */
    public String getImageFilePath(Uri data) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = this.getContentResolver().query(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String imgPath = cursor.getString(column_index);
        return imgPath;
    }

    /**
     * profileImageSizeSetting() : 이미지뷰 크기 조정.
     */
    private void profileImageSizeSetting(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)imageView_member_profile_image.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels;
        deviceWidth = deviceWidth / 2;
        imageView_member_profile_image.getLayoutParams().width = deviceWidth;
        imageView_member_profile_image.getLayoutParams().height = deviceWidth;
        imageView_member_profile_image.requestLayout();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
//        int device_width = linearLayout_represent.getWidth();
//        int image_size = device_width / 2;
//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)imageView_member_profile_image.getLayoutParams();
//        params.width = image_size;
//        params.height = image_size;
//        imageView_member_profile_image.setLayoutParams(params);

//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        ((Activity)imageView_member_profile_image.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int deviceWidth = displayMetrics.widthPixels;
//        deviceWidth = deviceWidth / 2;
//        imageView_member_profile_image.getLayoutParams().width = deviceWidth;
//        imageView_member_profile_image.getLayoutParams().height = deviceWidth;
//        imageView_member_profile_image.requestLayout();
    }
}
