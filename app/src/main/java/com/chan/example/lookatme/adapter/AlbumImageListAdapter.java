package com.chan.example.lookatme.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.chan.example.lookatme.R;
import com.chan.example.lookatme.vo.AlbumVo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AlbumImageListAdapter
        extends RecyclerView.Adapter<AlbumImageListAdapter.ViewHolder>{

    private Context context;
    private ArrayList<AlbumVo> arrayList;

    public AlbumImageListAdapter(Context context, ArrayList<AlbumVo> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_image_select_gallery_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final AlbumVo albumVo = arrayList.get(position);
        holder.itemView.setTag(albumVo);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)holder.itemView.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels; // 핸드폰의 가로 해상도를 구함.
        deviceWidth = deviceWidth / 3;
        holder.itemView.getLayoutParams().height = deviceWidth;
        holder.itemView.requestLayout();

        holder.imageView_gallery_image.setImageBitmap(albumVo.getAlbum_image());
        holder.textView_imageFilePath.setText(albumVo.getImageFilename());
        if(albumVo.isItem_check_status() == true){
            holder.checkBox_image_select.setChecked(true);
            albumVo.setItem_check_status(true);
        }else{
            holder.checkBox_image_select.setChecked(false);
            albumVo.setItem_check_status(false);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.imageView_gallery_image)
        ImageView imageView_gallery_image;
        @BindView(R.id.checkBox_image_select)
        CheckBox checkBox_image_select;
        @BindView(R.id.textView_imageFilePath)
        TextView textView_imageFilePath;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
