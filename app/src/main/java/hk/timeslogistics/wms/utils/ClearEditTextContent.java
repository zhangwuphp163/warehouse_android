package hk.timeslogistics.wms.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import hk.timeslogistics.wms.R;

public class ClearEditTextContent {
    @SuppressLint("ClickableViewAccessibility")
    public static void setupClearEditText(Context context, final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 在输入框内容改变之前的操作
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 在输入框内容改变时的操作
                if (s.length() > 0) {
                    editText.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.clear_edit_text_grey), null);
                } else {
                    editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                // 在输入框内容改变之后的操作
            }
        });
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText editText = (EditText) v;
                Drawable drawableRight = editText.getCompoundDrawables()[2];
                // 判断drawableRight是否被点击
                if (event.getAction() == MotionEvent.ACTION_UP && drawableRight != null && event.getRawX() >= (editText.getRight() - drawableRight.getBounds().width())) {
                    // 清空EditText中的内容
                    editText.setText("");
                    editText.requestFocus();
                    return true;
                }
                return false;
            }
        });
    }
}
