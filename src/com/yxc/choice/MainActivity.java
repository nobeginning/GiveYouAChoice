package com.yxc.choice;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.umeng.analytics.MobclickAgent;
import com.yxc.choice.entity.Choice;
import com.yxc.choice.entity.ChoiceItem;
import net.youmi.android.AdManager;
import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import net.youmi.android.spot.SpotDialogListener;
import net.youmi.android.spot.SpotManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private WheelView panView;
    private GestureDetector gestureDetector;
    private Button btn_add_choice;
    private static final int FLING_MIN_DISTANCE = 80;
    private static final int FLING_MIN_VELOCITY = 100;
    public static final int ROTATEITEMSELECT_FINISH = 0;
    public ArrayList<String> arrayList;
    private float surfacViewWidth = 0;
    private float surfacViewHeight = 0;
    DisplayMetrics dm;
    ActionBar actionBar;
    boolean hasShownAds = false;
    RelativeLayout layout_wheel;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wheel);
        dm = new DisplayMetrics();
        //友盟初始化
        MobclickAgent.updateOnlineConfig(this);
        //有米广告
        AdManager.getInstance(this).init("fe620c88f720397b", "07f9d4aa1830a149", false);
        //预加载插屏广告
        SpotManager.getInstance(this).loadSpotAds();

        getWindowManager().getDefaultDisplay().getMetrics(dm);
        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        actionBar = getActionBar();

//        surfacViewHeight = panView.getMeasuredHeight();
//        surfacViewWidth = panView.getMeasuredWidth();
        layout_wheel = (RelativeLayout) findViewById(R.id.layout_wheel);
        btn_add_choice = (Button) findViewById(R.id.btn_add_choice);
        btn_add_choice.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        btn_add_choice.setOnClickListener(this);
//        int choiceSize = resetChoiceData(0);
        showAds(R.id.adLayout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1000 && resultCode==1001) {
            findViewById(R.id.layout_empty).setVisibility(DataCenter.getInstance().currentItemsStr.size()==0?View.VISIBLE:View.GONE);
            panView.setTitleString(DataCenter.getInstance().currentItemsStr);
            panView.start();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        panView = new WheelView(this);
        panView.setOnTouchListener(new MyOnTouchListener());
        panView.setLongClickable(true);
        if (panView.getParent()==null){
            layout_wheel.addView(panView);
        }
        resetChoiceData(DataCenter.getInstance().currentChoice == null ? 0 : DataCenter.getInstance().currentChoice.id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onEvent(this, "main_page");
    }

    @Override
    public void onBackPressed() {
        // 如果有需要，可以点击后退关闭插屏广告（可选）。
        if (!SpotManager.getInstance(this).disMiss(true)) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        //如果不调用此方法，则按home键的时候会出现图标无法显示的情况。
        layout_wheel.removeView(panView);
        SpotManager.getInstance(this).disMiss(false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //注销有米广播
        SpotManager.getInstance(this).unregisterSceenReceiver();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            if (!hasShownAds) {
                if (SpotManager.getInstance(this).checkLoadComplete()) {
                    SpotManager.getInstance(this).showSpotAds(this, new SpotDialogListener() {
                        @Override
                        public void onShowSuccess() {
                            hasShownAds = true;
                            Log.i("Youmi", "onShowSuccess");
                        }

                        @Override
                        public void onShowFailed() {
                            hasShownAds = true;
                            Log.i("Youmi", "onShowFailed");
                        }

                        @Override
                        public void onSpotClosed() {
                            Log.e("sdkDemo", "closed");
                        }
                    });
                    return false;
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu, menu);
        List<Choice> choices = MyApplication.db.findAll(Choice.class);
        for (Choice choice : choices){
            menu.addSubMenu(10, choice.id, choice.id, choice.name);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_add){
            addChoice(null);
        } else if (item.getItemId()==R.id.action_manage){
            manageChoices();
        } else if (item.getGroupId()==10){
            if (DataCenter.getInstance().currentChoice !=null && item.getItemId()==DataCenter.getInstance().currentChoice.id){

            }else {
                changeChoice(item.getItemId());
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private int resetChoiceData(int cId){
        List<Choice> choices = MyApplication.db.findAll(Choice.class, "id desc");
        if (DataCenter.getInstance().currentChoice !=null){
            boolean isExist = false;
            if (choices.size()>0){
                for (Choice choice : choices){
                    if (choice.id==cId){
                        isExist = true;
                    }
                }
                if (!isExist){
                    DataCenter.getInstance().currentChoice =null;
                }
            }
        }
//        if (DataCenter.getInstance().currentChoice==null){
            if (choices.size()>0){
                if (cId==0 || DataCenter.getInstance().currentChoice ==null) {
                    DataCenter.getInstance().currentChoice = choices.get(0);
                }else{
                    for (Choice choice : choices){
                        if (choice.id==cId){
                            DataCenter.getInstance().currentChoice = choice;
                            break;
                        }
                    }
                }
                List<ChoiceItem> choiceItems = MyApplication.db.findAllByWhere(ChoiceItem.class, "choiceId="+DataCenter.getInstance().currentChoice.id);
                List<String> itemStrs = new ArrayList<String>();
                for (ChoiceItem item: choiceItems){
                    itemStrs.add(item.name);
                }
                DataCenter.getInstance().currentChoiceItems = choiceItems;
                DataCenter.getInstance().currentItemsStr = itemStrs;
                panView.setTitleString(DataCenter.getInstance().currentItemsStr);
                panView.restart();
            }
//        }
        findViewById(R.id.layout_empty).setVisibility(choices.size()==0?View.VISIBLE:View.GONE);
        return choices.size();
    }

    private void changeChoice(int cId){
        Log.d("CCC", cId+"");
        resetChoiceData(cId);
    }

    private void manageChoices(){
        Intent intent = new Intent(this, ManageChoiceActivity.class);
        startActivityForResult(intent, 1001);
    }

    public void begin(boolean bool, float sp) {
        this.panView.setDirection(bool, sp);
        panView.rotateEnable();
    }

    private void addChoice(View view){
        Intent intent = new Intent(this, AddChoiceActivity.class);
        startActivityForResult(intent, 1000);
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.btn_add_choice){
            addChoice(v);
        }
    }

    private class MyOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:

                    break;

                case MotionEvent.ACTION_MOVE:
                    break;

                case MotionEvent.ACTION_UP:
                    break;
            }
            gestureDetector.onTouchEvent(event);
            return true;
        }
    }

    private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        /******************************************************************************
         *  用户按下触摸屏、快速移动后松开即触发这个事件 e1：第1个ACTION_DOWN
         *  MotionEvent e2：最后一个ACTION_MOVE MotionEvent velocityX：X轴上的移动速度，像素/秒
         *  velocityY：Y轴上的移动速度，像素/秒 触发条件 ：
         *  X轴的坐标位移大于FLING_MIN_DISTANCE，且移动速度大于FLING_MIN_VELOCITY个像素/秒  
         ******************************************************************************/
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {

			/*
			 * float f1 = (int) e1.getX(); float f2 = (int) e1.getY(); float f3
			 * = (int) e2.getX(); float f4 = (int) e2.getY(); float distance
			 * =Math.abs( f1 - f2 + f3 - f4);
			 *
			 * //小于移动的距离 if (distance <=FLING_MIN_DISTANCE) { return false; }
			 * float f5 = Math.abs(velocityX)+ Math.abs(velocityY);
			 */
            if (surfacViewWidth==0){
                surfacViewWidth = dm.widthPixels;
            }
            if (surfacViewHeight==0){
                surfacViewHeight = dm.heightPixels;
            }
            float xDistance = Math.abs(e1.getX() - e2.getX());
            float yDistance = Math.abs(e1.getY() - e2.getY());

            if (xDistance >= FLING_MIN_DISTANCE
                    || yDistance >= FLING_MIN_DISTANCE) {
                // 判断是一个滑行的手势了
                if (Math.abs(velocityX) + Math.abs(velocityY) > FLING_MIN_VELOCITY) {
                    float sAngle = (float) computeAngleFromCentre(e1.getX(),
                            e1.getY());
                    float dAngle = (float) computeAngleFromCentre(e2.getX(),
                            e2.getY());

                    float result = dAngle - sAngle;

                    boolean isClockwise = false;
                    //上下滑动
                    if (yDistance > FLING_MIN_DISTANCE){
                        if (e1.getY() > e2.getY()){
                            //上滑
                            if (e1.getX() > dm.widthPixels/2){
                                //逆时针
                            }else{
                                //顺时针
                                isClockwise = true;
                            }
                        }else{
                            //下滑
                            if (e1.getX() < dm.widthPixels/2){
                                //逆时针
                            }else{
                                //顺时针
                                isClockwise = true;
                            }
                        }
                    }

                    //左右滑动
                    if (xDistance > FLING_MIN_DISTANCE){
                        if (e1.getX() > e2.getX()){
                            //左滑
                            if (e1.getY() < dm.heightPixels/2.5){
                                //屏幕上半部分——逆时针
                            }else{
                                //顺时针
                                isClockwise = true;
                            }
                        }else{
                            //右滑
                            if (e1.getY() > dm.heightPixels/2.5){
                                //屏幕下半部分——逆时针
                            }else{
                                //顺时针
                                isClockwise = true;
                            }
                        }
                    }

                    if (isClockwise) {//result < 0
                        // 顺时针
                        System.out.println("顺时针转动，xAngle is " + sAngle
                                + " ,yAngle is" + dAngle + ",result is "
                                + result);
                        begin(true, Math.abs(result));
                    } else {
                        System.out.println("逆时针转动，xAngle is " + sAngle
                                + " ,yAngle is" + dAngle + ",result is "
                                + result);
                        begin(false, Math.abs(result));

                    }

                }
            }
            return false;
        }
    }

    public double computeAngleFromCentre(float x, float y) {
        return Math.atan2(0 - x, 0 - y)
                * 180 / Math.PI;
//        return Math.atan2(this.surfacViewWidth - x, this.surfacViewHeight - y)
//                * 180 / Math.PI;
    }
}
