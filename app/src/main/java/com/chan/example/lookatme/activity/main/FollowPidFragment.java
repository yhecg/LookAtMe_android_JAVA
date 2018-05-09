package com.chan.example.lookatme.activity.main;

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

import com.chan.example.lookatme.R;
import com.chan.example.lookatme.activity.board_pid.BoardDetailActivity;
import com.chan.example.lookatme.adapter.FollowPidBoardListAdapter;
import com.chan.example.lookatme.function.Basic;
import com.chan.example.lookatme.vo.BoardVo;

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

import static android.content.Context.MODE_PRIVATE;


public class FollowPidFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = FollowPidFragment.class.getSimpleName();

    public FollowPidFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * requestCode
     *  - RequestCode_board_item_detail_go : 게시글 상세정보창으로 가는 코드.
     */
    private static final int RequestCode_board_item_detail_go = 100;

    /**
     * BindView
     *  - recyclerView_follow_member_board_list : 팔로우한 사용자의 게시글 목록.
     *  - swipeRefreshLayout : 게시글 목록 새로고침.
     */
    @BindView(R.id.recyclerView_follow_member_board_list) RecyclerView recyclerView_follow_member_board_list;
    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;

    /**
     * 변수
     *  - login_member_email : 현재 로그인한 사용자의 이메일.
     *  - followPid_board_adapter : 팔로우한 사용자의 게시글에 대한 정보를 담은 리스트 어댑터.
     *  - followPid_board_arrayList : 팔로우한 사용자의 게시글에 대한 정보를 담은 ArrayList.
     */
    String login_member_email;
    FollowPidBoardListAdapter followPid_board_adapter;
    ArrayList<BoardVo> followPid_board_arrayList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_follow_pid, container, false);
        ButterKnife.bind(this, view);

        // 로그인한 회원 정보
        SharedPreferences spf_login_member = getActivity().getSharedPreferences("loginMember", MODE_PRIVATE);
        login_member_email = spf_login_member.getString("loginMember_email", "");

        // 리스트뷰 관련
        followPid_board_arrayList = new ArrayList<>();
        recyclerView_follow_member_board_list.setHasFixedSize(true);
        followPid_board_adapter = new FollowPidBoardListAdapter(getActivity(), followPid_board_arrayList);
        recyclerView_follow_member_board_list.setAdapter(followPid_board_adapter);

        // 게시글(리스트뷰 아이템)을 클릭했을때 해당 게시글 상세정보창으로 이동.
        recyclerView_follow_member_board_list.addOnItemTouchListener(
            new FollowMemberBoardLIstItemImageClickListener(
                getActivity(),
                new FollowMemberBoardLIstItemImageClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        BoardVo boardVo = followPid_board_arrayList.get(position);
                        Intent intent = new Intent(getActivity(), BoardDetailActivity.class);
                        intent.putExtra("board_no", boardVo.getBoard_no()); // 게시글 번호
                        intent.putExtra("member_email", boardVo.getMember_email()); // 게시글 작성한 회원 이메일
                        intent.putExtra("activity_status", "FollowPidFragment"); // 현재 액티비티에서 넘어갔다는 변수
                        startActivityForResult(intent, RequestCode_board_item_detail_go);
                    }
                }
            )
        );

        new FollowPidInformationServerData().execute();

        swipeRefreshLayout.setOnRefreshListener(this);

        return view;
    }

    @Override
    public void onRefresh() {
        new RefreshFollowPidInformationServerData().execute();
    }

    /**
     * class
     *  - RefreshFollowPidInformationServerData : 새로고침을 할 경우 팔로우한 사용자의 게시글 정보를 불러와서 리스트뷰에 뿌려준다.
     *  - FollowPidInformationServerData : 팔로우한 사용자의 게시글 정보를 불러와서 리스트뷰에 뿌려준다.
     *  - FollowMemberBoardLIstItemImageClickListener : 팔로우한 사용자의 게시글 정보를 리스트뷰에 뿌려주는데 이때 뿌려진 리스트뷰 아이템을 클릭했을때 일어나는 이벤트 리스너.
     */
    private class RefreshFollowPidInformationServerData extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("login_member_email",login_member_email)
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_main_php_directory_url + "followpid_information.php")
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
                followPid_board_arrayList.clear();
                JSONArray jsonArray = new JSONArray(result);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String board_no = jsonObject.getString("board_no");
                    String member_email = jsonObject.getString("member_email");
                    String board_img = jsonObject.getString("board_img");
                    followPid_board_arrayList.add(0, new BoardVo(board_no,member_email,board_img));
                }
                followPid_board_adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class FollowPidInformationServerData extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("login_member_email",login_member_email)
                        .build();
                Request request = new Request.Builder()
                        .url(Basic.server_main_php_directory_url + "followpid_information.php")
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
                    String board_no = jsonObject.getString("board_no");
                    String member_email = jsonObject.getString("member_email");
                    String board_img = jsonObject.getString("board_img");
                    followPid_board_arrayList.add(0, new BoardVo(board_no,member_email,board_img));
                }
                followPid_board_adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private static class FollowMemberBoardLIstItemImageClickListener extends RecyclerView.SimpleOnItemTouchListener{
        public interface OnItemClickListener{
            void onItemClick(View view, int position);
        }
        private OnItemClickListener mListener;
        private GestureDetector mGestureDetector;
        public FollowMemberBoardLIstItemImageClickListener(Context context, OnItemClickListener listener){
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











}
