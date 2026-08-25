package com.greatmap.agy.business.web.controller;

import com.greatmap.agy.business.api.dto.BusinessDemoDTO;
import com.greatmap.agy.business.service.service.BusinessDemoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessDemoController 业务演示控制器 MockMvc 集成测试")
class BusinessDemoControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private BusinessDemoService businessDemoService;

    @InjectMocks
    private BusinessDemoController businessDemoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(businessDemoController).build();
    }

    @Test
    @DisplayName("测试创建业务数据接口返回标准 Result 格式")
    void testCreateDemoSuccess() throws Exception {
        when(businessDemoService.createDemo(any(BusinessDemoDTO.class))).thenReturn(100L);

        mockMvc.perform(post("/api/business/demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"测试项目\",\"description\":\"描述\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(100));
    }
}
