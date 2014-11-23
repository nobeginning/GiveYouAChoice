package com.yxc.choice;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * Created by Robin on 2014/11/22.
 */
public class ViewChoiceItem extends LinearLayout {

    public interface DeleteListener{
        public void doDelete(ViewChoiceItem itemView);
    }

    EditText editText;
    ImageButton imageButton;
    DeleteListener listener;

    public DeleteListener getListener() {
        return listener;
    }

    public void setListener(DeleteListener listener) {
        this.listener = listener;
    }

    public ViewChoiceItem(Context context) {
        super(context);
        init(context);
    }

    public ViewChoiceItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewChoiceItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init(Context context){
        this.setOrientation(HORIZONTAL);
        this.setGravity(Gravity.BOTTOM);
        editText = new EditText(context);
        editText.setSingleLine(true);
        editText.setTextSize(12);
        editText.setHint("输入一个选项");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        this.addView(editText, params);

        imageButton = new ImageButton(context);
        imageButton.setBackgroundColor(getResources().getColor(R.color.clear));
        imageButton.setImageResource(R.drawable.btn_del);
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (ViewChoiceItem.this.getParent()!=null){
//                    ViewGroup parent = (ViewGroup)(ViewChoiceItem.this.getParent());
//                    parent.removeView(ViewChoiceItem.this);
//                }
                if (listener!=null){
                    listener.doDelete(ViewChoiceItem.this);
                }
            }
        });

        this.addView(imageButton, params);
    }
}
