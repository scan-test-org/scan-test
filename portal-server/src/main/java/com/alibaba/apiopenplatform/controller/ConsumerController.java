package com.alibaba.apiopenplatform.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.apiopenplatform.dto.params.consumer.CreateConsumerParam;
import com.alibaba.apiopenplatform.dto.result.ConsumerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.service.ConsumerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * @author zh
 */
@Tag(name = "消费者管理", description = "提供消费者注册、审批等管理功能")
@RestController
@RequestMapping("/consumer")
@RequiredArgsConstructor
@Validated
public class ConsumerController {

    private final ConsumerService consumerService;

    @Operation(summary = "获取门户或开发者的消费者列表")
    @GetMapping("/list")
    public PageResult<ConsumerResult> listPortalConsumers(@RequestParam String portalId,
                                                          @RequestParam String developerId,
                                                          @PageableDefault(sort = "gmt_create", direction = DESC) Pageable pageable) {
        return StrUtil.isBlank(portalId) ?
                consumerService.listConsumers(developerId, pageable) : consumerService.listConsumers(portalId, pageable);
    }

    @Operation(summary = "注册消费者",
            description = "使用提供的信息注册新的消费者")
    @PostMapping("/register")
    public ConsumerResult registerConsumer(
            @RequestBody @Valid CreateConsumerParam param) {
        return consumerService.registerConsumer(param);
    }

    @Operation(summary = "审批消费者",
            description = "根据消费者ID进行审批")
    @PostMapping("/{consumerId}/approve")
    public void approveConsumer(
            @Parameter(description = "消费者ID", required = true)
            @PathVariable @NotBlank(message = "消费者ID不能为空") String consumerId) {
        consumerService.approveConsumer(consumerId);
    }

    @Operation(summary = "删除消费者",
            description = "根据消费者ID删除指定消费者")
    @DeleteMapping("/{consumerId}")
    public void deleteConsumer(
            @Parameter(description = "消费者ID", required = true)
            @PathVariable @NotBlank(message = "消费者ID不能为空") String consumerId) {
        consumerService.deleteConsumer(consumerId);
    }
}
