package com.yxc.choice;

import com.yxc.choice.entity.Choice;
import com.yxc.choice.entity.ChoiceItem;

import java.util.List;

/**
 * Created by Robin on 2014/11/22.
 */
public class DataCenter {

    private static DataCenter dataCenter;

    private DataCenter(){

    }

    public static DataCenter getInstance() {
        if (dataCenter==null){
            dataCenter = new DataCenter();
        }
        return dataCenter;
    }

    public Choice currentChoice;

    public List<ChoiceItem> currentChoiceItems;
    public List<String> currentItemsStr;

}
