package com.chan.example.lookatme.activity.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chan.example.lookatme.R;
import com.chan.example.lookatme.activity.board_pid.BoardDetailActivity;
import com.chan.example.lookatme.activity.board_pid.BoardInsertActivity;
import com.chan.example.lookatme.activity.member.MemberProfileUpdateActivity;
import com.chan.example.lookatme.activity.member.MemberSearchActivity;
import com.chan.example.lookatme.adapter.MyPidBoardListAdapter;
import com.chan.example.lookatme.function.Basic;
import com.chan.example.lookatme.vo.BoardVo;

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

import static android.content.Context.MODE_PRIVATE;

/**
 * 내 피드 정보를 볼 수 있는 프래그먼트.
 * 나의 개인 정보와 내가 올린 사진들을 보고 등록하고 수정하고 삭제가 가능하며
 * 팔로우, 팔로워 사람들을 관리 할 수 있다.
 */
public class MyPidFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public MyPidFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private static final String TAG = MyPidFragment.class.getSimpleName();

    /**
     * RequestCode
     *  - RequestCode_board_insert_go : 게시글 등록하는 창으로 가는 코드.
     *  - RequestCode_member_profile_update_go : 회원 정보 수정창으로 가는 코드.
     *  - RequestCode_board_item_detail_go : 해당 게시글 상세정보창으로 가는 코드.
     */
    private static final int RequestCode_board_insert_go = 100;
    private static final int RequestCode_member_profile_update_go = 200;
    private static final int RequestCode_board_item_detail_go = 300;

    /**
     * BindView
     *  - imageView_profile : 프로필 이미지가 있는 이미지뷰.
     *  - textView_board_count : 게시글 수를 나타내는 텍스트뷰.
     *  - textView_follower_count : 팔로워 수를 나타내는 텍스트뷰.
     *  - textView_following_count : 팔로잉 수를 나타내는 텍스트뷰.
     *  - button_profile_update_go : 프로필 정보를 수정하는 수정창으로 가는 버튼.
     *  - swipeRefreshLayout : 전체 정보 새로고침(내 정보, 게시글 정보).
     */
    @BindView(R.id.imageView_profile) CircleImageView imageView_profile;
    @BindView(R.id.textView_board_count) TextView textView_board_count;
    @BindView(R.id.textView_follower_count) TextView textView_follower_count;
    @BindView(R.id.textView_following_count) TextView textView_following_count;
    @BindView(R.id.button_profile_update_go) Button button_profile_update_go;
    @BindView(R.id.recyclerView_board) RecyclerView recyclerView_board;
    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;

    /**
     * adapter : 게시글에 대한 정보를 담은 리스트의 어댑터.
     * board_arrayList : 게시글에 대한 정보를 담은 ArrayList.
     * login_member_email : 현재 로그인된 사용자의 이메일.
     */
    MyPidBoardListAdapter adapter;
    ArrayList<BoardVo> board_arrayList;
    String login_member_email;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_my_pid, container, false);
        ButterKnife.bind(this, view);

        SharedPreferences spf_login_member = getActivity().getSharedPreferences("loginMember", MODE_PRIVATE);
        login_member_email = spf_login_member.getString("loginMember_email", "");

        // 리스트뷰 관련.
        board_arrayList = new ArrayList<>();
        recyclerView_board.setHasFixedSize(true);
        adapter = new MyPidBoardListAdapter(getActivity(), board_arrayList);
        recyclerView_board.setAdapter(adapter);

        // 내 정보와(프로필이미지,팔로워,팔로잉) 내가 작성한 게시글의 정보를 불러온다.
        new MyPidInformationServerData().execute();

        // 게시글(리스트뷰 아이템)을 클릭했을때 해당 게시글 상세정보창으로 이동.
        recyclerView_board.addOnItemTouchListener(
            new BoardPidItemImageClickListener(
                getActivity(),
                new BoardPidItemImageClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        BoardVo boardVo = board_arrayList.get(position);
                        Intent intent = new Intent(getActivity(), BoardDetailActivity.class);
                        intent.putExtra("board_no", boardVo.getBoard_no()); // 게시글 번호
                        intent.putExtra("member_email", boardVo.getMember_email()); // 게시글 작성한 회원 이메일
                        intent.putExtra("activity_status", "MyPidFragment"); // 현재 액티비티에서 넘어갔다는 변수
                        startActivityForResult(intent, RequestCode_board_item_detail_go);
                    }
                }
            )
        );

        swipeRefreshLayout.setOnRefreshListener(this);

        return view;
    }

    @Override
    public void onRefresh() {
        new RefreshMyPidInformationServerData().execute();
    }

    /**
     * class
     *  - BoardPidItemImageClickListener : 내가 작성한 게시글(피드) 정보를 리스트뷰에 뿌려주는데 이때 뿌려진 리스트뷰 아이템을 클릭했을때 일어나는 이벤트 리스너
     */
    private static class BoardPidItemImageClickListener extends RecyclerView.SimpleOnItemTouchListener{
        public interface OnItemClickListener{
            void onItemClick(View view, int position);
        }
        private OnItemClickListener mListener;
        private GestureDetector mGestureDetector;
        public BoardPidItemImageClickListener(Context context,OnItemClickListener listener){
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

    /**
     * OnClick 클릭 이벤트
     *  - followerSearchMemberActivityGo : 팔로워 회원 목록창으로 이동.
     *  - followingSearchMemberActivityGo : 팔로잉 회원 목록창으로 이동.
     *  - boardInsertGo : 게시글 등록하는 창으로 이동한다.
     *  - memberProfileUpdateGo : 프로필 수정 버튼을 누르면 프로필 수정하는 창으로 이동한다.
     */
    @OnClick({R.id.textView_follower_count, R.id.textView_follower_text}) void followerSearchMemberActivityGo(){
        Intent intent = new Intent(getActivity(), MemberSearchActivity.class);
        intent.putExtra("activity_status", "follower");
        intent.putExtra("board_owner_member_email", login_member_email);
        startActivity(intent);
    }
    @OnClick({R.id.textView_following_count, R.id.textView_following_text}) void followingSearchMemberActivityGo(){
        Intent intent = new Intent(getActivity(), MemberSearchActivity.class);
        intent.putExtra("activity_status", "following");
        intent.putExtra("board_owner_member_email", login_member_email);
        startActivity(intent);
    }
    @OnClick(R.id.button_board_insert_go) void boardInsertGo(){
        Intent intent = new Intent(getActivity(), BoardInsertActivity.class);
        startActivityForResult(intent, RequestCode_board_insert_go);
    }
    @OnClick(R.id.button_profile_update_go) void memberProfileUpdateGo(){
        Intent intent = new Intent(getActivity(), MemberProfileUpdateActivity.class);
        startActivityForResult(intent, RequestCode_member_profile_update_go);
    }

    /**
     * class
     *  RefreshMyPidInformationServerData : 새로고침 할 경우 내 정보와(프로필이미지,팔로워,팔로잉) 내가 작성한 게시글의 정보를 다시 불러온다.
     *  MyPidInformationServerData : 내 정보와(프로필이미지,팔로워,팔로잉) 내가 작성한 게시글의 정보를 불러온다.
     */
    private class RefreshMyPidInformationServerData extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("member_email", login_member_email)
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_main_php_directory_url + "mypid_information.php")
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
                board_arrayList.clear();
                JSONObject jsonObject = new JSONObject(result);

                // 프로필 이미지란에 들어갈 이미지를 서버에서 받아와서 넣어준다.
                JSONArray jsonArray_member = jsonObject.getJSONArray("member_information");
                JSONObject jsonObject_member = jsonArray_member.getJSONObject(0);
                String member_img = jsonObject_member.getString("member_img");
                if(!member_img.equals("") && member_img != null){
                    Glide.with(getActivity()).load(Basic.server_member_image_directory_url+member_img).asBitmap().into(imageView_profile);
                }

                // 게시글 정보를 서버에서 받아와서 리스트뷰에 뿌려준다.
                JSONArray jsonArray_board = jsonObject.getJSONArray("board_information");
                for(int i=0; i<jsonArray_board.length(); i++){
                    JSONObject jsonObject_board = jsonArray_board.getJSONObject(i);
                    String board_no = jsonObject_board.getString("board_no");
                    String member_email = jsonObject_board.getString("member_email");
                    String board_img = jsonObject_board.getString("board_img");
                    board_arrayList.add(0, new BoardVo(board_no, member_email, board_img));
                }
                adapter.notifyDataSetChanged();

                // 게시글 갯수.
                textView_board_count.setText(String.valueOf(board_arrayList.size()));

                // 팔로워 수
                String follower_count = jsonObject_member.getString("follower_count");
                textView_follower_count.setText(follower_count);

                // 팔로잉 수
                String following_count = jsonObject_member.getString("following_count");
                textView_following_count.setText(following_count);

                swipeRefreshLayout.setRefreshing(false);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class MyPidInformationServerData extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("member_email", login_member_email)
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_main_php_directory_url + "mypid_information.php")
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
                    Glide.with(getActivity()).load(Basic.server_member_image_directory_url+member_img).asBitmap().into(imageView_profile);
                }

                // 게시글 정보를 서버에서 받아와서 리스트뷰에 뿌려준다.
                JSONArray jsonArray_board = jsonObject.getJSONArray("board_information");
                for(int i=0; i<jsonArray_board.length(); i++){
                    JSONObject jsonObject_board = jsonArray_board.getJSONObject(i);
                    String board_no = jsonObject_board.getString("board_no");
                    String member_email = jsonObject_board.getString("member_email");
                    String board_img = jsonObject_board.getString("board_img");
                    board_arrayList.add(0, new BoardVo(board_no, member_email, board_img));
                }
                adapter.notifyDataSetChanged();

                // 게시글 갯수.
                textView_board_count.setText(String.valueOf(board_arrayList.size()));

                // 팔로워 수
                String follower_count = jsonObject_member.getString("follower_count");
                textView_follower_count.setText(follower_count);

                // 팔로잉 수
                String following_count = jsonObject_member.getString("following_count");
                textView_following_count.setText(following_count);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case Activity.RESULT_OK:
                switch (requestCode){
                    // 게시글을 등록하면 등록된 게시글이 게시글 리스트에 추가된다. BoardInsertActivity 에서 정보가 넘어옴.
                    case RequestCode_board_insert_go:
                        String board_no = data.getExtras().getString("board_no"); // 게시글 번호
                        String member_email = data.getExtras().getString("member_email"); // 게시글 번호
                        String board_img = data.getExtras().getString("board_img"); // 게시글 이미지 (서버 DB 에 저장되어 있는 이미지파일 이름)
                        board_arrayList.add(0, new BoardVo(board_no,member_email,board_img));
                        adapter.notifyItemInserted(0);
                        recyclerView_board.scrollToPosition(0);
                        textView_board_count.setText(String.valueOf(board_arrayList.size()));
                        break;

                    // 프로필 수정창에서 프로필 수정을 완료하면 프로필 정보가 수정되서 보여진다.
                    case RequestCode_member_profile_update_go:
                        boolean image_update_check = data.getExtras().getBoolean("image_update_check");
                        if(image_update_check == true){
                            String member_img = data.getExtras().getString("member_img");
                            Glide.with(getActivity()).load(Basic.server_member_image_directory_url+member_img).asBitmap().into(imageView_profile);
                        }
                        break;

                    case RequestCode_board_item_detail_go:
                        String status = data.getExtras().getString("status");
                        // 게시글 상세창에서 게시글을 삭제했을 경우.
                        if(status.equals("delete")){
                            String board_no_boardDetailActivity = data.getExtras().getString("board_no");
                            Iterator<BoardVo> iterator = board_arrayList.iterator();
                            while(iterator.hasNext()){
                                BoardVo boardVo = iterator.next();
                                if(board_no_boardDetailActivity.equals(boardVo.getBoard_no()) ){
                                    iterator.remove();
                                }
                            }
                            adapter.notifyDataSetChanged();

                            int board_count = Integer.parseInt(textView_board_count.getText().toString());
                            textView_board_count.setText(String.valueOf(board_count-1));
                        }

                        else if(status.equals("update")){
                            board_arrayList.clear();
                            new MyPidInformationServerData().execute();
                        }
                        break;
                }
                break;
        }
    }
}
