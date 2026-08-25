package com.greatmap.agy.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.greatmap.agy.api.dto.SysDictTypeSaveDTO;
import com.greatmap.agy.api.vo.PageResultVO;
import com.greatmap.agy.api.vo.SysDictTypeVO;
import com.greatmap.agy.service.entity.SysDictType;

import java.util.List;

/**
 * 字典类型 Service
 */
public interface SysDictTypeService extends IService<SysDictType> {

    PageResultVO<SysDictTypeVO> getDictTypePage(int pageNo, int pageSize, String dictName, String dictType);

    List<SysDictTypeVO> getAllDictTypes();

    SysDictTypeVO getDictTypeVOById(Long id);

    boolean saveDictType(SysDictTypeSaveDTO dto);

    boolean updateDictType(SysDictTypeSaveDTO dto);

    boolean deleteDictType(Long id);
}
