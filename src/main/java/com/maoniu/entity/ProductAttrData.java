package com.maoniu.entity;

import lombok.Data;

import java.util.Set;

/**
 * Created by Administrator on 2018/4/10.
 */
@Data
public class ProductAttrData {
    private String model;//型号
    private int releasedCount;//已发布产品数
    private String classify;//品类
    private Set<String> compositeSet;//特征词组合(特征词一+特征词二+特征词三（相关词）)
}
