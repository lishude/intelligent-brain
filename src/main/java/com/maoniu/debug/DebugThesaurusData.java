package com.maoniu.debug;

import lombok.Data;

/**
 * Created by Administrator on 2018/4/18.
 */
@Data
public class DebugThesaurusData {
    private String classify;//品类
    private String characteristicWords;//特征词
    private String commonWords;//通用词
    private String synonymWords;//同义词
    private String wordGroups;//词组
}
