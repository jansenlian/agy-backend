package com.greatmap.agy.web.controller;

import com.greatmap.agy.api.dto.SysDictTypeSaveDTO;
import com.greatmap.agy.api.vo.PageResultVO;
import com.greatmap.agy.api.vo.SysDictTypeVO;
import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.service.service.SysDictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "数据字典类型管理接口")
@RestController
@RequestMapping("/api/dict/type")
@RequiredArgsConstructor
public class SysDictTypeController {

    private final SysDictTypeService dictTypeService;

    @Operation(summary = "分页获取字典类型")
    @GetMapping("/page")
    @PreAuthorize("@ss.hasPerm('sys:dict:list')")
    public Result<PageResultVO<SysDictTypeVO>> getDictTypePage(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String dictName,
            @RequestParam(required = false) String dictType) {
        return Result.ok(dictTypeService.getDictTypePage(pageNo, pageSize, dictName, dictType));
    }

    @Operation(summary = "获取所有激活的字典类型下拉列表")
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPerm('sys:dict:list')")
    public Result<List<SysDictTypeVO>> getAllDictTypes() {
        return Result.ok(dictTypeService.getAllDictTypes());
    }

    @Operation(summary = "获取字典类型详情")
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('sys:dict:list')")
    public Result<SysDictTypeVO> getDictTypeById(@PathVariable Long id) {
        return Result.ok(dictTypeService.getDictTypeVOById(id));
    }

    @Operation(summary = "新增字典类型")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:dict:type:add')")
    public Result<Boolean> addDictType(@jakarta.validation.Valid @RequestBody com.greatmap.agy.api.dto.SysDictTypeSaveDTO dto) {
        return Result.ok(dictTypeService.saveDictType(dto));
    }

    @Operation(summary = "修改字典类型")
    @PutMapping
    @PreAuthorize("@ss.hasPerm('sys:dict:type:edit')")
    public Result<Boolean> updateDictType(@jakarta.validation.Valid @RequestBody com.greatmap.agy.api.dto.SysDictTypeSaveDTO dto) {
        return Result.ok(dictTypeService.updateDictType(dto));
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('sys:dict:type:delete')")
    public Result<Boolean> deleteDictType(@PathVariable Long id) {
        return Result.ok(dictTypeService.deleteDictType(id));
    }
}
