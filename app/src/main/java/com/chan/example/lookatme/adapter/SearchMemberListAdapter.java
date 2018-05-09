package com.chan.example.lookatme.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chan.example.lookatme.R;
import com.chan.example.lookatme.function.Basic;
import com.chan.example.lookatme.vo.MemberVo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class SearchMemberListAdapter extends RecyclerView.Adapter<SearchMemberListAdapter.ViewHolder>{

    public class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.circleImageView_member_image) CircleImageView circleImageView_member_image;
        @BindView(R.id.textView_member_name) TextView textView_member_name;
        @BindView(R.id.textView_member_email) TextView textView_member_email;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private Context mContext;
    private ArrayList<MemberVo> mArrayList;

    public SearchMemberListAdapter(Context context, ArrayList<MemberVo> arrayList) {
        this.mContext = context;
        this.mArrayList = arrayList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.activity_member_search_member_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MemberVo memberVo = mArrayList.get(position);
        holder.itemView.setTag(memberVo);

        Glide.with(mContext).load(Basic.server_member_image_directory_url+memberVo.getMember_img()).asBitmap().into(holder.circleImageView_member_image);
        holder.textView_member_name.setText(memberVo.getMember_name());
        holder.textView_member_email.setText(memberVo.getMember_email());
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }


}
