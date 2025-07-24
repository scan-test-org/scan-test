package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.core.annotation.AdminAuth;
import com.alibaba.apiopenplatform.core.annotation.DeveloperAuth;
import com.alibaba.apiopenplatform.dto.params.consumer.QueryConsumerParam;
import com.alibaba.apiopenplatform.dto.params.consumer.CreateConsumerParam;
import com.alibaba.apiopenplatform.dto.result.ConsumerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.service.ConsumerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

/**
 * @author zh
 */
@Tag(name = "Consumer管理", description = "提供Consumer注册、审批等管理功能")
@RestController
@RequestMapping("/consumers")
@RequiredArgsConstructor
@Validated
public class ConsumerController {

    private final ConsumerService consumerService;

    @Operation(summary = "获取Consumer列表")
    @GetMapping
    public PageResult<ConsumerResult> listConsumers(QueryConsumerParam param,
                                                    Pageable pageable) {
        return consumerService.listConsumers(param, pageable);
    }

    @Operation(summary = "注册Consumer")
    @PostMapping
    @DeveloperAuth
    public ConsumerResult registerConsumer(@RequestBody @Valid CreateConsumerParam param) {
        return consumerService.registerConsumer(param);
    }

    @Operation(summary = "审批Consumer")
    @PatchMapping("/{consumerId}/status")
    @AdminAuth
    public void approveConsumer(
            @PathVariable @NotBlank(message = "Consumer ID不能为空") String consumerId) {
        consumerService.approveConsumer(consumerId);
    }

    @Operation(summary = "删除Consumer")
    @DeleteMapping("/{consumerId}")
    public void deleteDevConsumer(
            @PathVariable @NotBlank(message = "Consumer ID不能为空") String consumerId) {
        consumerService.deleteConsumer(consumerId);
    }
}
