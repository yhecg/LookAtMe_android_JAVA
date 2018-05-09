package com.chan.example.lookatme.activity.board_pid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.chan.example.lookatme.R;
import com.chan.example.lookatme.adapter.AlbumImageListAdapter;
import com.chan.example.lookatme.function.Basic;
import com.chan.example.lookatme.vo.AlbumVo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 게시글 등록 화면에서 이미지를 클릭하면 넘어오는 이미지 선택하는 화면.
 * 이미지를 최소 하나, 최대 세개를 선택하여 체크 표시를 누르면 게시글 등록 화면에 이미지와 정보가 넘어간다.
 */
public class ImageSelectActivity extends AppCompatActivity {

    private static final String TAG = ImageSelectActivity.class.getSimpleName();

    /**
     * BindView
     *  - toolbar : 화면 맨 상단에 있는 툴바.
     *  - recyclerView_album_image_list : 앨범에서 불러온 이미지 정보들을 뿌려줄 리스트뷰.
     */
    @BindView(R.id.toolBar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView_album_image_list)
    RecyclerView recyclerView_album_image_list;

    /**
     * status : 사진 선택창이 게시글 등록에서 열린건지 게시글 수정에서 열린건지 확인. insert, update
     * adapter : 이미지 파일에 대한 정보를 담은 리스트의 어댑터.
     * album_image_arrayList : 이미지 파일에 대한 정보를 담은 리스트.
     * check_count : 게시글에 이미지 등록 할 이미지가 체크된 수.
     * image_filePath_list : 체크된 이미지 파일 경로를 담은 리스트.
     */
    String status;
    AlbumImageListAdapter adapter;
    ArrayList<AlbumVo> album_image_arrayList;
    int image_check_count;
    ArrayList<String> image_filePath_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);
        ButterKnife.bind(this);

        toolBarSetting(); // 툴바 설정.

        Bundle extras = getIntent().getExtras();
        status = extras.getString("status");

        // 리스트뷰 관련.
        album_image_arrayList = new ArrayList<>();
        recyclerView_album_image_list.setHasFixedSize(true);
        adapter = new AlbumImageListAdapter(this, album_image_arrayList);
        recyclerView_album_image_list.setAdapter(adapter);
        AlbumImageLoad();

        // 앨범에서 가져온 이미지 정보 리스트에서 해당 아이템(이미지) 클릭 이벤트.
        image_filePath_list = new ArrayList<>();
        recyclerView_album_image_list.addOnItemTouchListener(
            new AlbumItemImageClickListener(this, recyclerView_album_image_list,
                new AlbumItemImageClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        AlbumVo albumVo = album_image_arrayList.get(position);
                        String imageFileName = albumVo.getImageFilename(); // 이미지 파일 경로
                        if(albumVo.isItem_check_status() == false){
                            if(image_check_count < 3){
                                image_check_count++;
                                albumVo.setItem_check_status(true);
                                album_image_arrayList.set(position, new AlbumVo(albumVo.getAlbum_image(), albumVo.isItem_check_status(), imageFileName));
                                adapter.notifyItemChanged(position);
                                image_filePath_list.add(imageFileName);
                            }else{
                                Toast.makeText(ImageSelectActivity.this, "이미지는 최대 3개만 선택 가능합니다.", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            if(image_check_count <= 3){
                                int index = image_filePath_list.indexOf(albumVo.getImageFilename());
                                image_filePath_list.remove(index);

                                image_check_count--;
                                albumVo.setItem_check_status(false);
                                album_image_arrayList.set(position, new AlbumVo(albumVo.getAlbum_image(), albumVo.isItem_check_status(), imageFileName));
                                adapter.notifyItemChanged(position);
                            }
                        }
                    }
                    @Override
                    public void onItemLongClick(View view, int position) {

                    }
                }
            )
        );
    }

    /**
     * 툴바 관련.
     *  - toolBarSetting : 툴바 설정
     *  - onCreateOptionsMenu : 툴바 메뉴
     *  - onOptionsItemSelected : 툴바 선택
     */
    private void toolBarSetting(){
        toolbar.setTitleTextColor(Color.parseColor("#BA68C8"));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); // 커스터마이징 하기 위해 필요.
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("사진 선택");
        actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_select_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.success:
                if(image_check_count == 0){
                    Toast.makeText(this, "최소 1개의 이미지를 선택하세요.", Toast.LENGTH_SHORT).show();
                    break;
                }else{
                    if(status.equals("insert")){
                        Intent intent = new Intent(ImageSelectActivity.this, BoardInsertActivity.class);
                        intent.putExtra("image_check_count", image_check_count); // int
                        intent.putExtra("image_filePath_list", image_filePath_list); // arrayList
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }else if(status.equals("update")){
                        Intent intent = new Intent(ImageSelectActivity.this, BoardUpdateActivity.class);
                        intent.putExtra("image_check_count", image_check_count); // int
                        intent.putExtra("image_filePath_list", image_filePath_list); // arrayList
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 앨범에서 불러온 이미지 정보를 리스트뷰에 뿌려주는데 이때 뿌려진 리스트뷰 아이템을
     * 클릭하거나 롱클릭했을때 일어나는 이벤트 리스너.
     */
    private static class AlbumItemImageClickListener extends RecyclerView.SimpleOnItemTouchListener{
        public interface OnItemClickListener{
            void onItemClick(View view, int position);
            void onItemLongClick(View view, int position);
        }
        private OnItemClickListener mListener;
        private GestureDetector mGestureDetector;
        public AlbumItemImageClickListener(Context context, final RecyclerView recyclerView, final OnItemClickListener listener) {
            this.mListener = listener;

            // GestureDetector.SimpleOnGestureListener() ==> 모든 제스처의 하위 집합만 듣고 싶을 때 확장되는 편리한 클래스
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e){
                    return true;
                }
                @Override
                public void onLongPress(MotionEvent e){
                    View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if(childView != null && listener != null){
                        mListener.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView));
                    }
                }
            });
        }
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e){
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if(child != null && mListener != null && mGestureDetector.onTouchEvent(e)){
                mListener.onItemClick(child, rv.getChildAdapterPosition(child));
                return true;
            }
            return false;
        }
    }

    /**
     * 앨범(갤러리)에서 이미지를 불러와서 리스트뷰에 이미지 정보를 뿌려준다.
     * AlbumImageLoad
     */
    private Cursor imageCursor;
    private void AlbumImageLoad(){
        String[] proj = {MediaStore.Images.Media.DATA};
        imageCursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, null, null, null);
        if(imageCursor != null && imageCursor.moveToFirst()){
            String filePath;
            int dataCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
            do{
                filePath = imageCursor.getString(dataCol);
                if(filePath != null){
                    File imageFile = new File(filePath);
                    if(imageFile.exists()){
                        BitmapFactory.Options options = new BitmapFactory.Options();
//                        options.inJustDecodeBounds = true;
                        options.inSampleSize = 2;
                        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
                        Bitmap resize = Bitmap.createScaledBitmap(bitmap, Basic.bitmap_resize, Basic.bitmap_resize, true);
                        album_image_arrayList.add(new AlbumVo(resize, false, filePath));
                    }
                }
            }while (imageCursor.moveToNext());
        }
        Collections.reverse(album_image_arrayList); // arrayList 순서 뒤집기. 사진이 최신것 부터 나오게 하기 위해서.
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageCursor.close();
    }





}
