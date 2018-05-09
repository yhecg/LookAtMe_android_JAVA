package com.chan.example.lookatme.adapter;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chan.example.lookatme.R;
import com.chan.example.lookatme.function.Basic;
import com.chan.example.lookatme.vo.ChattingRoomVo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class ChattingRoomListAdapter extends RecyclerView.Adapter<ChattingRoomListAdapter.ViewHolder>{

    public class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.textView_chattingRoom_public_key) TextView textView_chattingRoom_public_key;
        @BindView(R.id.textView_chattingRoom_accept_member_email) TextView textView_chattingRoom_accept_member_email;
//        @BindView(R.id.textView_chattingRoom_accept_member_image) TextView textView_chattingRoom_accept_member_image;
        @BindView(R.id.textView_chattingRoom_name) TextView textView_chattingRoom_name;
        @BindView(R.id.textView_chattingRoom_message_contents) TextView textView_chattingRoom_message_contents;
        @BindView(R.id.textView_chattingRoom_message_time) TextView textView_chattingRoom_message_time;
        @BindView(R.id.circleImageView_member_image) CircleImageView circleImageView_member_image;
        @BindView(R.id.textView_chattingRoom_message_unread_count) TextView textView_chattingRoom_message_unread_count;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    private Context mContext;
    private ArrayList<ChattingRoomVo> mArrayList;

    public ChattingRoomListAdapter(Context context, ArrayList<ChattingRoomVo> arrayList) {
        this.mContext = context;
        this.mArrayList = arrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.fragment_chat_list_room_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChattingRoomVo chattingRoomVo = mArrayList.get(position);
        holder.itemView.setTag(chattingRoomVo);

        holder.textView_chattingRoom_public_key.setText(chattingRoomVo.getChattingRoom_public_key());
        holder.textView_chattingRoom_accept_member_email.setText(chattingRoomVo.getChattingRoom_accept_member_email());
        holder.textView_chattingRoom_name.setText(chattingRoomVo.getChattingRoom_name());
        holder.textView_chattingRoom_message_contents.setText(chattingRoomVo.getChattingRoom_message_contents());

        if(chattingRoomVo.getChattingRoom_message_unread_count() == 0){
            holder.textView_chattingRoom_message_unread_count.setVisibility(View.GONE);
        }else{
            holder.textView_chattingRoom_message_unread_count.setVisibility(View.VISIBLE);
            holder.textView_chattingRoom_message_unread_count.setText(String.valueOf(chattingRoomVo.getChattingRoom_message_unread_count()));
        }


        String compare_1 = Basic.nowDate("date").substring(0,10);
        String compare_2 = chattingRoomVo.getChattingRoom_message_time().substring(0,10);
        if(compare_1.equals(compare_2)){
            holder.textView_chattingRoom_message_time.setText(chattingRoomVo.getChattingRoom_message_time().substring(11,16));
        }else{
            holder.textView_chattingRoom_message_time.setText(chattingRoomVo.getChattingRoom_message_time().substring(0,10));
        }

        try {
            JSONArray jsonArray = new JSONArray(chattingRoomVo.getChattingRoom_accept_member_email());
//            holder.textView_chattingRoom_member_count.setText(String.valueOf(jsonArray.length()));
            if(jsonArray.length()<=2) {
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String member_email = jsonObject.getString("member_email");

                    // 로그인한 사용자 정보
                    SharedPreferences spf_login_member = mContext.getSharedPreferences("loginMember", MODE_PRIVATE);
                    String login_member_email = spf_login_member.getString("loginMember_email", "");

                    if(!member_email.equals(login_member_email)){
                        JSONArray jsonArray_image = new JSONArray(chattingRoomVo.getChattingRoom_accept_member_image());
                        JSONObject jsonObject_image = jsonArray_image.getJSONObject(i);
                        Glide.with(mContext).load(Basic.server_member_image_directory_url+jsonObject_image.getString("member_image")).asBitmap().into(holder.circleImageView_member_image);
                    }
                }
            }else{
                holder.circleImageView_member_image.setImageResource(R.drawable.icon_app_title);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }





























}
