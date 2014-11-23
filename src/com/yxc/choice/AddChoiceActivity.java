package com.yxc.choice;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.umeng.analytics.MobclickAgent;
import com.yxc.choice.entity.Choice;
import com.yxc.choice.entity.ChoiceItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Robin on 2014/11/22.
 */
public class AddChoiceActivity extends BaseActivity implements View.OnClickListener, ViewChoiceItem.DeleteListener {

    private EditText et_choice_title;
    private ImageButton btn_add;
    private Button btn_save;
    private LinearLayout layout_items;

    ActionBar actionBar;
    int currentChoiceId;
    Choice currentChoice;
    List<ChoiceItem> items;
    List<Integer> needToDelete = new ArrayList<Integer>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_choice);
        Intent intent = getIntent();
        currentChoiceId = intent.getIntExtra("choiceId", 0);
        actionBar = getActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (currentChoiceId>0){
                actionBar.setTitle(R.string.choice_title_edit);
            }
        }
        setupView();
        setupListener();
        showAds(R.id.adLayout);
        if (currentChoiceId > 0) {
            generateExistValue();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onEvent(this, "add_page");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    private void setupView() {
        et_choice_title = (EditText) findViewById(R.id.et_choice_title);
        TextView tv_title = (TextView) findViewById(R.id.tv_title);
        if (currentChoiceId>0){
            tv_title.setText(R.string.choice_title_edit);
        }
        btn_add = (ImageButton) findViewById(R.id.btn_add);
        btn_save = (Button) findViewById(R.id.btn_save);
        layout_items = (LinearLayout) findViewById(R.id.layout_items);
    }

    private void setupListener() {
        btn_add.setOnClickListener(this);
        btn_save.setOnClickListener(this);
    }

    private void generateExistValue() {
        currentChoice = MyApplication.db.findById(currentChoiceId, Choice.class);
        items = MyApplication.db.findAllByWhere(ChoiceItem.class, "choiceId=" + currentChoiceId, "id asc");

        et_choice_title.setText(currentChoice.name);

        for (int i = 0; i < items.size(); i++) {
            ChoiceItem item = items.get(i);
            if (i < layout_items.getChildCount() - 1) {
                View view = layout_items.getChildAt(i);
                if (view instanceof EditText) {
                    ((EditText) view).setText(item.name);
                    view.setTag(item.id);
                } else if (view instanceof ViewChoiceItem){
                    ViewChoiceItem itemView = (ViewChoiceItem)view;
                    itemView.editText.setText(item.name);
                    itemView.editText.setTag(item.id);
                }
            } else {
                EditText et = addItem(null, item);
                et.setText(item.name);
            }
        }
    }

    private EditText addItem(View v, ChoiceItem item) {
        if (layout_items.getChildCount()==16){
            showToast("选项太多了，放不下了");
            return null;
        }
        ViewChoiceItem itemView = new ViewChoiceItem(this);
        itemView.setListener(this);
//        EditText editText = new EditText(this);
        if (item != null) {
            itemView.editText.setTag(item.id);
        }
        itemView.editText.setSingleLine(true);
        itemView.editText.setTextSize(12);
        itemView.editText.setHint("输入一个选项");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout_items.addView(itemView, layout_items.getChildCount() - 1, params);
        return itemView.editText;
    }

    private void doSave(View v) {
        if (et_choice_title.getText().toString().trim().length() == 0) {
            showToast("不写标题没法儿存储啊 -_-!");
            return;
        }
        HashMap<Integer, String> itemMap = getItems();
        if (itemMap.size() == 0) {
            showToast("请不要这么无聊 ~_~... 没有选项怎么选");
            return;
        }
        if (itemMap.size() == 1) {
            showToast("你是在玩我吗 ~!_!~... 只有一个选项还选什么啊");
            return;
        }
        if (currentChoice == null) {
            currentChoice = new Choice();
        }
        currentChoice.setName(et_choice_title.getText().toString());
        if (currentChoice.id > 0) {
            MyApplication.db.update(currentChoice);
        } else {
            MyApplication.db.saveBindId(currentChoice);
        }

        List<ChoiceItem> choiceItemList = new ArrayList<ChoiceItem>();
        List<String> choiceItemStrList = new ArrayList<String>();
        for (Integer key : itemMap.keySet()) {
            ChoiceItem cItem = getExistItem(key);
            if (cItem == null) {
                cItem = new ChoiceItem();
            }
            cItem.setName(itemMap.get(key));
            cItem.setChoiceId(currentChoice.getId());
            if (cItem.id > 0) {
                MyApplication.db.update(cItem);
            } else {
                MyApplication.db.saveBindId(cItem);
            }
            choiceItemList.add(cItem);
            choiceItemStrList.add(itemMap.get(key));
        }
        for (Integer cId: needToDelete){
            MyApplication.db.deleteById(ChoiceItem.class, cId);
        }
        needToDelete.clear();
        DataCenter.getInstance().currentChoice = currentChoice;
        DataCenter.getInstance().currentChoiceItems = choiceItemList;
        DataCenter.getInstance().currentItemsStr = choiceItemStrList;
        showToast("保存成功");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setResult(1001);
                onBackPressed();
            }
        }, 600);
    }

    private ChoiceItem getExistItem(int key) {
        if (items==null){
            return null;
        }
        for (ChoiceItem item : items) {
            if (item.id == key) {
                return item;
            }
        }
        return null;
    }

    private HashMap<Integer, String> getItems() {
        HashMap<Integer, String> result = new HashMap<Integer, String>();
        for (int i = 0; i < layout_items.getChildCount(); i++) {
            View view = layout_items.getChildAt(i);
            if (view instanceof EditText || view instanceof ViewChoiceItem) {
                EditText et = null;
                if (view instanceof EditText) {
                    et = (EditText) view;
                } else {
                    et = ((ViewChoiceItem)view).editText;
                }
                Integer tag = (Integer) et.getTag();
                if (tag == null) {
                    tag = -i;
                }
                String value = et.getText().toString().trim();
                if (!TextUtils.isEmpty(value)) {
                    result.put(tag, value);
                }
            }
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_add) {
            addItem(v, null);
        } else if (v.getId() == R.id.btn_save) {
            doSave(v);
        }
    }

    @Override
    public void doDelete(ViewChoiceItem itemView) {
        Integer tag = (Integer) itemView.editText.getTag();
        layout_items.removeView(itemView);
        if (tag==null){
            tag = 0;
        }
        if (tag>0){
            needToDelete.add(tag);
        }
    }
}