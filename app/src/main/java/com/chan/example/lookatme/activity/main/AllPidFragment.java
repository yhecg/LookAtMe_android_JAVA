package com.chan.example.lookatme.activity.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.chan.example.lookatme.activity.member.MemberSearchActivity;
import com.chan.example.lookatme.adapter.AllPidBoardListAdapter;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * 전체 사용자의 피드(사용자가 올린 게시글)를 나타낸다.
 */
public class AllPidFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public AllPidFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private static final String TAG = AllPidFragment.class.getSimpleName();

    private static final int RequestCode_board_item_detail_go = 100;

    /**
     * BindView
     *  - recyclerView_all_pid : 전체 사용자의 게시글을 나타내는 RecyclerView
     *  - button_member_search : 특정 사용자를 검색하는 창으로 이동하는 버튼.
     *  - swipeRefreshLayout : 게시글 목록 새로고침.
     */
    @BindView(R.id.recyclerView_all_pid) RecyclerView recyclerView_all_pid;
    @BindView(R.id.button_member_search) FloatingActionButton button_member_search;
    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;

    /**
     * 변수
     *  - login_member_email : 현재 로그인한 사용자의 이메일
     *  - allPid_board_adapter : 전체 게시글에 대한 정보를 담은 리스트 어댑터
     *  - allPid_board_arrayList : 전체 게시글에 대한 정보를 담은 ArrayList
     */
    String login_member_email;
    AllPidBoardListAdapter allPid_board_adapter;
    ArrayList<BoardVo> allPid_board_arrayList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_all_pid, container, false);
        ButterKnife.bind(this, view);

        // 로그인한 회원 정보
        SharedPreferences spf_login_member = getActivity().getSharedPreferences("loginMember", MODE_PRIVATE);
        login_member_email = spf_login_member.getString("loginMember_email", "");

        // 리스트뷰 관련
        allPid_board_arrayList = new ArrayList<>();
        recyclerView_all_pid.setHasFixedSize(true);
        allPid_board_adapter = new AllPidBoardListAdapter(getActivity(), allPid_board_arrayList);
        recyclerView_all_pid.setAdapter(allPid_board_adapter);

        new AllPidInformationServerData().execute(); // 전체 게시글 정보를 불러온다.

        // 게시글(리스트뷰 아이템)을 클릭했을때 해당 게시글 상세정보창으로 이동.
        recyclerView_all_pid.addOnItemTouchListener(
            new BoardLIstItemImageClickListener(
                getActivity(),
                new BoardLIstItemImageClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        BoardVo boardVo = allPid_board_arrayList.get(position);
                        Intent intent = new Intent(getActivity(), BoardDetailActivity.class);
                        intent.putExtra("board_no", boardVo.getBoard_no()); // 게시글 번호
                        intent.putExtra("member_email", boardVo.getMember_email()); // 게시글 작성한 회원 이메일
                        intent.putExtra("activity_status", "AllPidFragment"); // 현재 액티비티에서 넘어갔다는 변수
                        startActivityForResult(intent, RequestCode_board_item_detail_go);
                    }
                }
            )
        );

        swipeRefreshLayout.setOnRefreshListener(this);

        return view;
    }

    /**
     * OnClick 클릭 이벤트
     *  - memberSearchGo : 회원 검색창으로 이동.
     */
    @OnClick(R.id.button_member_search) void memberSearchGo(){
        Intent intent = new Intent(getActivity(), MemberSearchActivity.class);
        intent.putExtra("activity_status", "AllPidFragment");
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        new RefreshAllPidInformationServerData().execute();
    }

    /**
     * class
     *  - BoardLIstItemImageClickListener : 전체 게시글 정보를 리스트뷰에 뿌려주는데 이때 뿌려진 리스트뷰 아이템을 클릭했을때 일어나는 이벤트 리스너
     */
    private static class BoardLIstItemImageClickListener extends RecyclerView.SimpleOnItemTouchListener{
        public interface OnItemClickListener{
            void onItemClick(View view, int position);
        }
        private OnItemClickListener mListener;
        private GestureDetector mGestureDetector;
        public BoardLIstItemImageClickListener(Context context, OnItemClickListener listener){
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
     * class
     *  - AllPidInformationServerData : 전체 게시글의 정보를 불러온다.
     *  - RefreshAllPidInformationServerData : 새로고침을 하게 되면 전체 게시글의 정보를 다시 불러온다.
     */
    private class AllPidInformationServerData extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(Basic.server_main_php_directory_url+"allpid_information.php")
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
                    allPid_board_arrayList.add(0, new BoardVo(board_no,member_email,board_img));
                }
                allPid_board_adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class RefreshAllPidInformationServerData extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(Basic.server_main_php_directory_url+"allpid_information.php")
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
                allPid_board_arrayList.clear();
                JSONArray jsonArray = new JSONArray(result);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String board_no = jsonObject.getString("board_no");
                    String member_email = jsonObject.getString("member_email");
                    String board_img = jsonObject.getString("board_img");
                    allPid_board_arrayList.add(0, new BoardVo(board_no,member_email,board_img));
                }
                allPid_board_adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }






















}
