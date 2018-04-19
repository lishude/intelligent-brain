package com.maoniu.debug;

import lombok.Data;

/**
 * Created by Administrator on 2018/4/18.
 */
@Data
public class DebugProductAttrData {
    private String model;//型号
    private String releasedCount;//已发布产品数
    private String classify;//品类
    private String compositeSet;//特征词组合(特征词一+特征词二+特征词三（相关词）)
    private String featureOnes;//特征词一
    private String featureTwos;//特征词二
    private String featureThrees;//特征词三（相关词）
}
