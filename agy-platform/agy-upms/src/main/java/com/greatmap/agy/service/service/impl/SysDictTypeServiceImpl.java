package com.greatmap.agy.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greatmap.agy.api.dto.SysDictTypeSaveDTO;
import com.greatmap.agy.api.vo.PageResultVO;
import com.greatmap.agy.api.vo.SysDictTypeVO;
import com.greatmap.agy.common.exception.BusinessException;
import com.greatmap.agy.service.entity.SysDictData;
import com.greatmap.agy.service.entity.SysDictType;
import com.greatmap.agy.service.mapper.SysDictDataMapper;
import com.greatmap.agy.service.mapper.SysDictTypeMapper;
import com.greatmap.agy.service.service.SysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    private final SysDictDataMapper dictDataMapper;

    @Override
    public PageResultVO<SysDictTypeVO> getDictTypePage(int pageNo, int pageSize, String dictName, String dictType) {
        Page<SysDictType> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<SysDictType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(dictName), SysDictType::getDictName, dictName)
                .like(StringUtils.isNotBlank(dictType), SysDictType::getDictType, dictType)
                .eq(SysDictType::getDelFlag, 0)
                .orderByDesc(SysDictType::getCreateTime);
        Page<SysDictType> resultPage = this.page(page, queryWrapper);
        List<SysDictTypeVO> voList = BeanUtil.copyToList(resultPage.getRecords(), SysDictTypeVO.class);
        return PageResultVO.of(voList, resultPage.getTotal(), resultPage.getSize(), resultPage.getCurrent());
    }

    @Override
    public List<SysDictTypeVO> getAllDictTypes() {
        List<SysDictType> list = this.list(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getStatus, 1)
                .eq(SysDictType::getDelFlag, 0)
                .orderByAsc(SysDictType::getDictName));
        return BeanUtil.copyToList(list, SysDictTypeVO.class);
    }

    @Override
    public SysDictTypeVO getDictTypeVOById(Long id) {
        SysDictType dictType = this.getById(id);
        if (dictType == null) {
            return null;
        }
        return BeanUtil.copyProperties(dictType, SysDictTypeVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveDictType(SysDictTypeSaveDTO dto) {
        Long count = this.count(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dto.getDictType())
                .eq(SysDictType::getDelFlag, 0));
        if (count > 0) {
            throw new BusinessException("字典类型编码 [" + dto.getDictType() + "] 已存在");
        }
        SysDictType dictType = BeanUtil.copyProperties(dto, SysDictType.class);
        return this.save(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictType(SysDictTypeSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("更新字典类型 ID 不能为空");
        }
        SysDictType oldDictType = this.getById(dto.getId());
        if (oldDictType == null) {
            throw new BusinessException("字典类型不存在");
        }
        if (!oldDictType.getDictType().equals(dto.getDictType())) {
            Long count = this.count(new LambdaQueryWrapper<SysDictType>()
                    .eq(SysDictType::getDictType, dto.getDictType())
                    .ne(SysDictType::getId, dto.getId())
                    .eq(SysDictType::getDelFlag, 0));
            if (count > 0) {
                throw new BusinessException("字典类型编码 [" + dto.getDictType() + "] 已被使用");
            }
        }
        BeanUtil.copyProperties(dto, oldDictType);
        return this.updateById(oldDictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDictType(Long id) {
        if (id == null) {
            throw new BusinessException("字典类型 ID 不能为空");
        }
        SysDictType dictType = this.getById(id);
        if (dictType != null) {
            Long dataCount = dictDataMapper.selectCount(new LambdaQueryWrapper<SysDictData>()
                    .eq(SysDictData::getDictType, dictType.getDictType())
                    .eq(SysDictData::getDelFlag, 0));
            if (dataCount > 0) {
                throw new BusinessException("该字典类型 [" + dictType.getDictType() + "] 下存在关联的字典数据，请先删除字典数据项");
            }
        }
        return this.removeById(id);
    }
}
