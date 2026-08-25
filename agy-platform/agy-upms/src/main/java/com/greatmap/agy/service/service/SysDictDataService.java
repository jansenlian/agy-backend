package com.greatmap.agy.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.greatmap.agy.api.dto.SysDictDataSaveDTO;
import com.greatmap.agy.api.vo.SysDictDataVO;
import com.greatmap.agy.service.entity.SysDictData;

import java.util.List;

/**
 * 字典数据 Service (支持无限层级多级树形结构)
 */
public interface SysDictDataService extends IService<SysDictData> {

    List<SysDictDataVO> getDictDataTree(String dictType);

    List<SysDictDataVO> getDictDataList(String dictType);

    boolean saveDictData(SysDictDataSaveDTO dto);

    boolean updateDictData(SysDictDataSaveDTO dto);
}
