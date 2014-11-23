package com.yxc.choice;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
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
import java.util.Map;

/**
 * Created by Robin on 2014/11/22.
 */
public class ManageChoiceActivity extends BaseActivity {

    List<Choice> choices = new ArrayList<Choice>();
    Map<Integer, List<ChoiceItem>> choiceItems = new HashMap<Integer, List<ChoiceItem>>();
    ExpandableListView exlist_choices;
    ChoiceAdapter adapter;
    ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_choice);
        actionBar = getActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        exlist_choices = (ExpandableListView) findViewById(R.id.exlist_choices);
        refreshData();
        adapter = new ChoiceAdapter();
        exlist_choices.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onEvent(this, "manage_page");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_add){
            addChoice();
        }else {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void addChoice(){
        Intent intent = new Intent(this, AddChoiceActivity.class);
        startActivityForResult(intent, 1000);
    }

    private void refreshData(){
        choices.clear();
        choiceItems.clear();
        choices = MyApplication.db.findAll(Choice.class);
        for (Choice choice : choices){
            List<ChoiceItem> items = MyApplication.db.findAllByWhere(ChoiceItem.class, "choiceId="+ choice.id);
            if (items==null){
                continue;
            }
            choiceItems.put(choice.id, items);
        }
        if (adapter!=null){
            adapter.notifyDataSetChanged();
        }
    }

    private void deleteChoice(Choice choice){
        MyApplication.db.deleteByWhere(ChoiceItem.class, "choiceId=" + choice.id);
        MyApplication.db.delete(choice);
    }

    class ChoiceAdapter extends BaseExpandableListAdapter{

        @Override
        public int getGroupCount() {
            return choices.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return choiceItems.get(choices.get(groupPosition).id).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return choices.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return choiceItems.get(choices.get(groupPosition).id).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return choices.get(groupPosition).id;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return choiceItems.get(choices.get(groupPosition).id).get(childPosition).id;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView==null){
                convertView = View.inflate(ManageChoiceActivity.this, R.layout.cell_choice_group, null);
            }
            final Choice choice = choices.get(groupPosition);
            TextView tvGroupName = (TextView) convertView.findViewById(R.id.tv_group_name);
            Button btnDel = (Button) convertView.findViewById(R.id.btn_del);
            Button btnEdit = (Button) convertView.findViewById(R.id.btn_edit);
            ImageButton btnIndicator = (ImageButton) convertView.findViewById(R.id.ib_indicator);
            tvGroupName.setText(choice.name);
            btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteChoice(choice);
                    showToast("已删除");
                    refreshData();
                }
            });
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ManageChoiceActivity.this, AddChoiceActivity.class);
                    intent.putExtra("choiceId", choice.getId());
                    startActivity(intent);
                }
            });
            btnIndicator.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(exlist_choices.isGroupExpanded(groupPosition)){
                        exlist_choices.collapseGroup(groupPosition);
                        ((ImageButton)v).setImageResource(R.drawable.ic_menu_more_right);
                    }else{
                        exlist_choices.expandGroup(groupPosition);
                        ((ImageButton)v).setImageResource(R.drawable.ic_menu_more_down);
                    }
                }
            });
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView==null){
                convertView = View.inflate(ManageChoiceActivity.this, R.layout.cell_choice_item, null);
            }
            ChoiceItem item = choiceItems.get(choices.get(groupPosition).id).get(childPosition);
            ((TextView)convertView).setText(item.name);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }
}