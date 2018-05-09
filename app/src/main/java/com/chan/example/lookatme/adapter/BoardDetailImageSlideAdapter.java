package com.chan.example.lookatme.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.chan.example.lookatme.R;
import com.chan.example.lookatme.function.Basic;

import java.util.ArrayList;


public class BoardDetailImageSlideAdapter extends PagerAdapter{

    private LayoutInflater inflater;
    private Context context;
    private ArrayList arrayList_image;

    public BoardDetailImageSlideAdapter(Context context, ArrayList arrayList) {
        this.context = context;
        this.arrayList_image = arrayList;
    }

    @Override
    public int getCount() {
        return arrayList_image.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==((LinearLayout)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.activity_board_detail_image_slide, container, false);
        ImageView imageView = (ImageView)v.findViewById(R.id.imageView_slide_image);
        Glide.with(context).load(Basic.server_pid_image_directory_url+arrayList_image.get(position)).asBitmap().into(imageView);
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.invalidate();
    }
}
