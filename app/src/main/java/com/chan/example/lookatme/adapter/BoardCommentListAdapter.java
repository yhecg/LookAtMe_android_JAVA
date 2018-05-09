package com.chan.example.lookatme.adapter;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chan.example.lookatme.R;
import com.chan.example.lookatme.function.Basic;
import com.chan.example.lookatme.vo.BoardCommentVo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;


public class BoardCommentListAdapter extends RecyclerView.Adapter<BoardCommentListAdapter.ViewHolder>{

    public class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.relativeLayout) RelativeLayout relativeLayout;
        @BindView(R.id.imageView_comment_write_member_profile_image) CircleImageView imageView_comment_write_member_profile_image;
        @BindView(R.id.textView_comment_write_date) TextView textView_comment_write_date;
        @BindView(R.id.textView_comment_write_member_name) TextView textView_comment_write_member_name;
        @BindView(R.id.textView_comment_contents) TextView textView_comment_contents;
        @BindView(R.id.textView_comment_no) TextView textView_comment_no;
        @BindView(R.id.textView_comment_write_member_email) TextView textView_comment_write_member_email;
        @BindView(R.id.textView_comment_depth) TextView textView_comment_depth;
        @BindView(R.id.textView_comment_comments_insert) TextView textView_comment_comments_insert;
        @BindView(R.id.imageView_comment_edit) ImageView imageView_comment_edit;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    private Context mContext;
    private ArrayList<BoardCommentVo> mArrayList;
    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }
    private OnItemClickListener onItemClickListener;

    public BoardCommentListAdapter(Context mContext, ArrayList<BoardCommentVo> mArrayList, OnItemClickListener onItemClickListener) {
        this.mContext = mContext;
        this.mArrayList = mArrayList;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.activity_board_comment_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final BoardCommentVo commentVo = mArrayList.get(position);
        holder.itemView.setTag(commentVo);

        // 로그인한 회원 이메일.
        SharedPreferences spf_login_member = mContext.getSharedPreferences("loginMember", MODE_PRIVATE);
        String login_member_email = spf_login_member.getString("loginMember_email", "");

        if(commentVo.getMember_email().equals(login_member_email)){
            holder.imageView_comment_edit.setVisibility(View.VISIBLE);
            holder.imageView_comment_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(view,position);
                }
            });
        }else{
            holder.imageView_comment_edit.setVisibility(View.GONE);
        }

        Glide.with(mContext).load(Basic.server_member_image_directory_url + commentVo.getMember_img()).asBitmap().into(holder.imageView_comment_write_member_profile_image);
        holder.imageView_comment_write_member_profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClick(view,position);
            }
        });

        holder.textView_comment_write_date.setText(commentVo.getBoardComment_date());
        holder.textView_comment_write_member_name.setText(commentVo.getMember_name());
        holder.textView_comment_contents.setText(commentVo.getBoardComment_contents());
        holder.textView_comment_no.setText(commentVo.getBoardComment_no());
        holder.textView_comment_write_member_email.setText(commentVo.getMember_email());
        holder.textView_comment_depth.setText(commentVo.getBoardComment_depth());


        if(commentVo.getBoardComment_depth().equals("0")){
            holder.textView_comment_comments_insert.setVisibility(View.VISIBLE);
            holder.relativeLayout.setPadding(48,30,48,30);
            holder.textView_comment_comments_insert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(view,position);
                }
            });
        }else if(commentVo.getBoardComment_depth().equals("1")){
            holder.textView_comment_comments_insert.setVisibility(View.GONE);
            holder.relativeLayout.setPadding(150,30,48,30);
        }


    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

}
