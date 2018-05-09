package com.chan.example.lookatme.adapter;


import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chan.example.lookatme.R;
import com.chan.example.lookatme.function.Basic;
import com.chan.example.lookatme.vo.BoardVo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FollowPidBoardListAdapter extends RecyclerView.Adapter<FollowPidBoardListAdapter.ViewHolder>{

    public class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.textView_board_no) TextView textView_board_no;
        @BindView(R.id.textView_member_email) TextView textView_member_email;
        @BindView(R.id.imageView_board_image) ImageView imageView_board_image;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private Context mContext;
    private ArrayList<BoardVo> mArrayList;

    public FollowPidBoardListAdapter(Context context, ArrayList<BoardVo> arrayList) {
        this.mContext = context;
        this.mArrayList = arrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.fragment_follow_pid_board_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BoardVo boardVo = mArrayList.get(position);
        holder.itemView.setTag(boardVo);

        Glide.with(mContext).load(Basic.server_pid_image_directory_url + boardVo.getBoard_img()).asBitmap().into(holder.imageView_board_image);
        holder.textView_board_no.setText(boardVo.getBoard_no());
        holder.textView_member_email.setText(boardVo.getMember_email());

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)holder.itemView.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels; // 핸드폰의 가로 해상도를 구함.
        deviceWidth = deviceWidth / 3;
        holder.itemView.getLayoutParams().width = deviceWidth;
        holder.itemView.getLayoutParams().height = deviceWidth;
        holder.itemView.requestLayout();

    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }





















}
