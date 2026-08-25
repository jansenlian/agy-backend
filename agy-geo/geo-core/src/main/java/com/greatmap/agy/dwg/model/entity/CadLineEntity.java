package com.greatmap.agy.dwg.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.locationtech.jts.geom.Coordinate;

@Data
@EqualsAndHashCode(callSuper = true)
public class CadLineEntity extends CadEntity {

    /**
     * 起点坐标
     */
    private Coordinate startPoint;

    /**
     * 终点坐标
     */
    private Coordinate endPoint;
}
