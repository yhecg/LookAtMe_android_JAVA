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
import com.chan.example.lookatme.vo.ChattingMessageVo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class ChattingMessageListAdapter extends RecyclerView.Adapter<ChattingMessageListAdapter.ViewHolder>{

    public class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.layout__messageType_etc) RelativeLayout layout__messageType_etc;
            @BindView(R.id.textView_etc_contents) TextView textView_etc_contents;

        @BindView(R.id.layout__messageType_else) RelativeLayout layout__messageType_else;
            @BindView(R.id.layout__messageType_else__you) RelativeLayout layout__messageType_else__you;
                @BindView(R.id.circleImageView_message_you_member_image) CircleImageView circleImageView_message_you_member_image;
                @BindView(R.id.textView_message_you_member_name) TextView textView_message_you_member_name;

                @BindView(R.id.layout__messageType_message) RelativeLayout layout__messageType_message;
                    @BindView(R.id.textView_message_you_contents) TextView textView_message_you_contents;
                    @BindView(R.id.textView_message_you_time) TextView textView_message_you_time;
                @BindView(R.id.layout__messageType_image) RelativeLayout layout__messageType_image;
                    @BindView(R.id.imageView_image_you_contents) ImageView imageView_image_you_contents;
                    @BindView(R.id.textView_image_you_time) TextView textView_image_you_time;

            @BindView(R.id.layout__messageType_else__me) RelativeLayout layout__messageType_else__me;
                @BindView(R.id.layout__me_message_type_message) RelativeLayout layout__me_message_type_message;
                    @BindView(R.id.textView_message_me_contents) TextView textView_message_me_contents;
                    @BindView(R.id.textView_message_me_time) TextView textView_message_me_time;
                @BindView(R.id.layout__me_message_type_image) RelativeLayout layout__me_message_type_image;
                    @BindView(R.id.imageView_image_me_contents) ImageView imageView_image_me_contents;
                    @BindView(R.id.textView_image_me_time) TextView textView_image_me_time;




        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private Context mContext;
    private ArrayList<ChattingMessageVo> mArrayList;

    public ChattingMessageListAdapter(Context context, ArrayList<ChattingMessageVo> arrayList){
        this.mContext = context;
        this.mArrayList = arrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.activity_chatting_message_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChattingMessageVo vo = mArrayList.get(position);
        holder.itemView.setTag(vo);

        // 로그인한 회원 이메일.
        SharedPreferences spf_login_member = mContext.getSharedPreferences("loginMember", MODE_PRIVATE);
        String login_member_email = spf_login_member.getString("loginMember_email", "");

        if(vo.getChattingMessage_type().equals("etc")){
            holder.layout__messageType_etc.setVisibility(View.VISIBLE);
            holder.layout__messageType_else.setVisibility(View.GONE);
            holder.textView_etc_contents.setText(vo.getChattingMessage_contents());
        }else{
            holder.layout__messageType_etc.setVisibility(View.GONE);
            holder.layout__messageType_else.setVisibility(View.VISIBLE);

            if(!login_member_email.equals(vo.getChattingMessage_send_member_email())){
                holder.layout__messageType_else__you.setVisibility(View.VISIBLE);
                holder.layout__messageType_else__me.setVisibility(View.GONE);
                Glide.with(mContext).load(Basic.server_member_image_directory_url+vo.getChattingMessage_send_member_image()).asBitmap().into(holder.circleImageView_message_you_member_image);
                holder.textView_message_you_member_name.setText(vo.getChattingMessage_send_member_name());

                if(vo.getChattingMessage_type().equals("message")){
                    holder.layout__messageType_message.setVisibility(View.VISIBLE);
                    holder.layout__messageType_image.setVisibility(View.GONE);
                    holder.textView_message_you_contents.setText(vo.getChattingMessage_contents());
                    String time_compare_1 = Basic.nowDate("date").substring(0,10);
                    String time_compare_2 = vo.getChattingMessage_time().substring(0,10);
                    if(time_compare_1.equals(time_compare_2)){
                        holder.textView_message_you_time.setText(vo.getChattingMessage_time().substring(11,16));
                    }else{
                        holder.textView_message_you_time.setText(vo.getChattingMessage_time().substring(0,10));
                    }
                }else if(vo.getChattingMessage_type().equals("image")){
                    holder.layout__messageType_message.setVisibility(View.GONE);
                    holder.layout__messageType_image.setVisibility(View.VISIBLE);
                    Glide.with(mContext).load(Basic.server_chatting_image_directory_url+vo.getChattingMessage_contents()).asBitmap().into(holder.imageView_image_you_contents);
                    String time_compare_1 = Basic.nowDate("date").substring(0,10);
                    String time_compare_2 = vo.getChattingMessage_time().substring(0,10);
                    if(time_compare_1.equals(time_compare_2)){
                        holder.textView_image_you_time.setText(vo.getChattingMessage_time().substring(11,16));
                    }else{
                        holder.textView_image_you_time.setText(vo.getChattingMessage_time().substring(0,10));
                    }
                }

            }else{
                holder.layout__messageType_else__me.setVisibility(View.VISIBLE);
                holder.layout__messageType_else__you.setVisibility(View.GONE);

                if(vo.getChattingMessage_type().equals("message")){
                    holder.layout__me_message_type_message.setVisibility(View.VISIBLE);
                    holder.layout__me_message_type_image.setVisibility(View.GONE);
                    holder.textView_message_me_contents.setText(vo.getChattingMessage_contents());
                    String time_compare_1 = Basic.nowDate("date").substring(0,10);
                    String time_compare_2 = vo.getChattingMessage_time().substring(0,10);
                    if(time_compare_1.equals(time_compare_2)){
                        holder.textView_message_me_time.setText(vo.getChattingMessage_time().substring(11,16));
                    }else{
                        holder.textView_message_me_time.setText(vo.getChattingMessage_time().substring(0,10));
                    }
                }else if(vo.getChattingMessage_type().equals("image")){
                    holder.layout__me_message_type_message.setVisibility(View.GONE);
                    holder.layout__me_message_type_image.setVisibility(View.VISIBLE);
                    Glide.with(mContext).load(Basic.server_chatting_image_directory_url+vo.getChattingMessage_contents()).asBitmap().into(holder.imageView_image_me_contents);
                    String time_compare_1 = Basic.nowDate("date").substring(0,10);
                    String time_compare_2 = vo.getChattingMessage_time().substring(0,10);
                    if(time_compare_1.equals(time_compare_2)){
                        holder.textView_image_me_time.setText(vo.getChattingMessage_time().substring(11,16));
                    }else{
                        holder.textView_image_me_time.setText(vo.getChattingMessage_time().substring(0,10));
                    }
                }


            }
        }


    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }









}
