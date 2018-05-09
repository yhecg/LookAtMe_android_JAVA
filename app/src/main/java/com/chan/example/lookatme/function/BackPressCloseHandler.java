package com.chan.example.lookatme.function;

import android.app.Activity;
import android.widget.Toast;

/**
 * 뒤로가기 두 번 눌러 앱 종료.
 */

public class BackPressCloseHandler {

    private long backKetPressedTime = 0;
    private Toast toast;

    private Activity activity;

    public BackPressCloseHandler(Activity context){
        this.activity = context;
    }

    public void onBackPressed(){
        if(System.currentTimeMillis() > backKetPressedTime + 2000){
            backKetPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if(System.currentTimeMillis() <= backKetPressedTime + 2000){
            activity.finish();
            toast.cancel();
        }
    }

    public void showGuide(){
        toast = Toast.makeText(activity, "앱을 종료하시려면 한번 더 눌러주시기 바랍니다.", Toast.LENGTH_SHORT);
        toast.show();
    }

}
