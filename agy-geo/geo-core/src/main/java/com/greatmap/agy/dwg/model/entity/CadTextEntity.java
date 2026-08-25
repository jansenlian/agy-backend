package com.greatmap.agy.dwg.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.locationtech.jts.geom.Coordinate;

@Data
@EqualsAndHashCode(callSuper = true)
public class CadTextEntity extends CadEntity {

    /**
     * 注记文字内容
     */
    private String text;

    /**
     * 文字插入点坐标(X, Y, Z)
     */
    private Coordinate position;

    /**
     * 字高
     */
    private double height;

    /**
     * 旋转角(度)
     */
    private double rotation;
}
