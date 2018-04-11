package com.maoniu.entity;

import lombok.Data;

import java.util.List;

/**
 * Created by Administrator on 2018/4/9.
 */
@Data
public class MaoniuPendingData extends PendingData{
    private Integer heat;//热度
    private String keyword;//核心词，词组或者单个词
    private String classify;//归类
    private List<String> samePendingDataList;//待处理相同的数据集合
}
