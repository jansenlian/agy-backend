package com.greatmap.agy.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greatmap.agy.api.dto.SysDictDataSaveDTO;
import com.greatmap.agy.api.vo.SysDictDataVO;
import com.greatmap.agy.common.exception.BusinessException;
import com.greatmap.agy.service.entity.SysDictData;
import com.greatmap.agy.service.mapper.SysDictDataMapper;
import com.greatmap.agy.service.service.SysDictDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    @Override
    public List<SysDictDataVO> getDictDataTree(String dictType) {
        List<SysDictData> list = this.list(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 1)
                .eq(SysDictData::getDelFlag, 0)
                .orderByAsc(SysDictData::getSortOrder));

        if (list.isEmpty()) {
            return new ArrayList<>();
        }

        List<SysDictDataVO> voList = BeanUtil.copyToList(list, SysDictDataVO.class);

        Map<Long, List<SysDictDataVO>> childrenMap = voList.stream()
                .filter(node -> node.getParentId() != null)
                .collect(Collectors.groupingBy(SysDictDataVO::getParentId));

        voList.forEach(node -> node.setChildren(childrenMap.getOrDefault(node.getId(), new ArrayList<>())));

        return voList.stream()
                .filter(node -> node.getParentId() == null || node.getParentId() == 0L)
                .collect(Collectors.toList());
    }

    @Override
    public List<SysDictDataVO> getDictDataList(String dictType) {
        List<SysDictData> list = this.list(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 1)
                .eq(SysDictData::getDelFlag, 0)
                .orderByAsc(SysDictData::getSortOrder));
        return BeanUtil.copyToList(list, SysDictDataVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveDictData(SysDictDataSaveDTO dto) {
        SysDictData data = BeanUtil.copyProperties(dto, SysDictData.class);
        if (data.getParentId() == null) {
            data.setParentId(0L);
        }
        return this.save(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictData(SysDictDataSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("更新字典数据项 ID 不能为空");
        }
        SysDictData oldData = this.getById(dto.getId());
        if (oldData == null) {
            throw new BusinessException("字典数据项不存在");
        }
        BeanUtil.copyProperties(dto, oldData);
        return this.updateById(oldData);
    }
}
