package com.maoniu.entity;

import lombok.Data;

/**
 * Created by Administrator on 2018/4/9.
 * 待处理数据，原则上待数据只需要包含id和name就够了
 */
@Data
public class PendingData {
    private String id;
    private String origin;
    private String target;

    @Override
    public String toString() {
        return "PendingData{" +
                "id='" + id + '\'' +
                ", origin='" + origin + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
