package com.greatmap.agy.dwg.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CadPolylineEntity extends CadEntity {

    /**
     * 顶点坐标序列
     */
    private List<Coordinate> vertices = new ArrayList<>();

    /**
     * 是否闭合
     */
    private boolean closed;

    /**
     * 凸度列表(用于圆弧段)
     */
    private List<Double> bulges = new ArrayList<>();
}
