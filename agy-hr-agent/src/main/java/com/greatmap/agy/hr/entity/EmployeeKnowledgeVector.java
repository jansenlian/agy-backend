package com.greatmap.agy.hr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工全景档案向量知识库实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("employee_knowledge_vector")
public class EmployeeKnowledgeVector implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 员工工号
     */
    private String empId;

    /**
     * 员工姓名
     */
    private String empName;

    /**
     * 所属部门
     */
    private String department;

    /**
     * 职位
     */
    private String position;

    /**
     * 自然语言全景知识 Chunk
     */
    private String knowledgeChunk;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
