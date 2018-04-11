package com.maoniu.core;

import com.maoniu.entity.MaoniuPendingData;

import java.util.Comparator;

/**
 * Created by Administrator on 2018/4/9.
 */
public class MaoNiuComparator implements Comparator<MaoniuPendingData> {


    @Override
    public int compare(MaoniuPendingData o1, MaoniuPendingData o2) {
        if(o1.getOrigin().split("\\s+").length == o2.getOrigin().split("\\s+").length){//如果长度相等，则按热度降序
            return -(o1.getHeat().intValue() - o2.getHeat().intValue());
        }else{
            return o1.getOrigin().split("\\s+").length - o2.getOrigin().split("\\s+").length;
        }
    }
}
