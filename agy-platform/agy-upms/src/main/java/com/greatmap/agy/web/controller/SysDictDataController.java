package com.greatmap.agy.web.controller;

import com.greatmap.agy.api.vo.SysDictDataVO;
import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.service.service.SysDictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "数据字典数据项管理接口")
@RestController
@RequestMapping("/api/dict/data")
@RequiredArgsConstructor
public class SysDictDataController {

    private final SysDictDataService dictDataService;

    @Operation(summary = "根据字典类型获取树形字典数据列表 (支持多级层级结构)")
    @GetMapping("/tree/{dictType}")
    @PreAuthorize("isAuthenticated()")
    public Result<List<SysDictDataVO>> getDictDataTree(@PathVariable String dictType) {
        return Result.ok(dictDataService.getDictDataTree(dictType));
    }

    @Operation(summary = "根据字典类型获取激活的扁平字典数据列表 (供下拉选择)")
    @GetMapping("/list/{dictType}")
    @PreAuthorize("isAuthenticated()")
    public Result<List<SysDictDataVO>> getDictDataList(@PathVariable String dictType) {
        return Result.ok(dictDataService.getDictDataList(dictType));
    }

    @Operation(summary = "新增字典数据项")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:dict:data:add')")
    public Result<Boolean> addDictData(@jakarta.validation.Valid @RequestBody com.greatmap.agy.api.dto.SysDictDataSaveDTO dto) {
        return Result.ok(dictDataService.saveDictData(dto));
    }

    @Operation(summary = "修改字典数据项")
    @PutMapping
    @PreAuthorize("@ss.hasPerm('sys:dict:data:edit')")
    public Result<Boolean> updateDictData(@jakarta.validation.Valid @RequestBody com.greatmap.agy.api.dto.SysDictDataSaveDTO dto) {
        return Result.ok(dictDataService.updateDictData(dto));
    }

    @Operation(summary = "删除字典数据项")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('sys:dict:data:delete')")
    public Result<Boolean> deleteDictData(@PathVariable Long id) {
        return Result.ok(dictDataService.removeById(id));
    }
}
