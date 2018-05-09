package com.chan.example.lookatme.function;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.chan.example.lookatme.R;

/**
 * X 버튼을 누르면 text 가 지워지는 EditText.
 */

public class ClearEditText extends AppCompatEditText
        implements TextWatcher, View.OnTouchListener, View.OnFocusChangeListener{

    private Drawable clearDrawable;
    private OnFocusChangeListener onFocusChangeListener;
    private OnTouchListener onTouchListener;

    /**
     * EditText에 X 버튼 추가.
     * ClearEditText, setClearIconVisible, init
     */
    public ClearEditText(Context context) {
        super(context);
        init();
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void setClearIconVisible(boolean visible){
        /**
         * setCompoundDrawables()는 setDrawablePadding과 같은 효과를 준다.
         * X 버튼이 보여져야 하는 경우 EditText의 오른쪽에 위치시킨다.
         */
        clearDrawable.setVisible(visible,false);
        setCompoundDrawables(null,null,visible ? clearDrawable : null,null);
    }

    private void init(){
        /**
         * 롤리팝 이하 버전을 위해 Compat을 이용해서 wrapDrawable을 만들어주고
         * DrawableCompat을 이용해서 X 이미지를 hint의 색깔에 맞춰서
         * 같은 색으로 맞출 수 있도록 Tint를 적용해준다.
         * getIntrinsicWidth()와 getIntrinsicHeight()를 이용해서 크기를 지정해 준다.
         */
//        Drawable tempDrawable = ContextCompat.getDrawable(getContext(), R.drawable.abc_ic_clear_material);
        Drawable tempDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_clear_black_24dp);
        clearDrawable = DrawableCompat.wrap(tempDrawable);
        DrawableCompat.setTintList(clearDrawable, getHintTextColors());
        clearDrawable.setBounds(0,0,clearDrawable.getIntrinsicWidth(),clearDrawable.getIntrinsicHeight());

        setClearIconVisible(false);

        super.setOnTouchListener(this);
        super.setOnFocusChangeListener(this);
        addTextChangedListener(this);
    }

    /**
     * 텍스트 길이에 따라 X 버튼 보이기 / 없애기
     * onTextChanged, beforeTextChanged, afterTextChanged
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        /**
         * Focus 상태일때만 판단을 하며 텍스트의 길이가 0보다 크면 보여주고 그렇지 않으면 보여주지 않는다.
         */
        if(isFocused()){
            setClearIconVisible(s.length() > 0);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * X 버튼이 눌리는 경우 텍스트 초기화.
     * onTouch, setOnTouchListener
     * X 버튼이 보여지고 있고 터치 위치가 X 버튼의 위치일 경우 Text를 초기화.
     * 텍스트 에러를 표시해주고 있었다면 에러도 없애주기 위해 setError(null) 처리를 해준다.
     * 이 ClearEditText 에도 터치리스너가 추가 될 수 있으므로 터치리스너를 설정할 수 있는 함수를
     * 제공해주고 터치리스너에게 터치값을 넘겨주어야 한다.
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int x = (int)motionEvent.getX();
        if(clearDrawable.isVisible() && x > getWidth() - getPaddingRight() - clearDrawable.getIntrinsicWidth()){
            if(motionEvent.getAction() == motionEvent.ACTION_UP){
                setError(null);
                setText(null);
            }
            return true;
        }

        if(onTouchListener != null){
            return onTouchListener.onTouch(view, motionEvent);
        }else{
            return false;
        }
    }

    @Override
    public void setOnTouchListener(OnTouchListener onTouchListener){
        this.onTouchListener = onTouchListener;
    }

    /**
     * EditText에 포커스가 있을때에만 X 버튼을 보이기.
     * onFocusChange, setOnFocusChangeListener
     * 이 EditText에 포커스가 있는 경우는 텍스트의 길이에 따라서 X 버튼의 표시여부를 결정.
     * 포커스가 없는 경우는 X 버튼을 보여주지 않는다.
     * 위의 터치리스너와 마찬가지로 포커스리스너 또한 EditText에 추가될 수 있으므로
     * 설정할 수 있는 함수를 제공하고 포커스 변화값을 넘겨주어야 한다.
     */
    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if(hasFocus){
            setClearIconVisible(getText().length() > 0);
        }else{
            setClearIconVisible(false);
        }

        if(onFocusChangeListener != null){
            onFocusChangeListener.onFocusChange(view, hasFocus);
        }
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener onFocusChangeListenr){
        this.onFocusChangeListener = onFocusChangeListenr;
    }

}
