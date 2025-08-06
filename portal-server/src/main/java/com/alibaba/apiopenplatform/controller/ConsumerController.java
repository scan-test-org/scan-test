package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.core.annotation.AdminAuth;
import com.alibaba.apiopenplatform.core.annotation.DeveloperAuth;
import com.alibaba.apiopenplatform.dto.params.consumer.CreateCredentialParam;
import com.alibaba.apiopenplatform.dto.params.consumer.QueryConsumerParam;
import com.alibaba.apiopenplatform.dto.params.consumer.CreateConsumerParam;
import com.alibaba.apiopenplatform.dto.params.consumer.UpdateCredentialParam;
import com.alibaba.apiopenplatform.dto.params.consumer.CreateSubscriptionParam;
import com.alibaba.apiopenplatform.dto.params.consumer.QuerySubscriptionParam;
import com.alibaba.apiopenplatform.dto.result.ConsumerCredentialResult;
import com.alibaba.apiopenplatform.dto.result.ConsumerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.SubscriptionResult;
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
@Tag(name = "Consumer管理", description = "提供Consumer注册、审批、产品订阅等管理功能")
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

    @Operation(summary = "获取Consumer")
    @GetMapping("/{consumerId}")
    public ConsumerResult getConsumer(@PathVariable String consumerId) {
        return consumerService.getConsumer(consumerId);
    }

    @Operation(summary = "注册Consumer")
    @PostMapping
    @DeveloperAuth
    public ConsumerResult createConsumer(@RequestBody @Valid CreateConsumerParam param) {
        return consumerService.createConsumer(param);
    }

//    @Operation(summary = "审批Consumer")
//    @PatchMapping("/{consumerId}/status")
//    @AdminAuth
//    public void approveConsumer(
//            @PathVariable @NotBlank(message = "Consumer ID不能为空") String consumerId) {
//        consumerService.approveConsumer(consumerId);
//    }

    @Operation(summary = "删除Consumer")
    @DeleteMapping("/{consumerId}")
    public void deleteDevConsumer(
            @PathVariable @NotBlank(message = "Consumer ID不能为空") String consumerId) {
        consumerService.deleteConsumer(consumerId);
    }

    @Operation(summary = "生成Consumer凭证")
    @PostMapping("/{consumerId}/credentials")
    @DeveloperAuth
    public ConsumerCredentialResult createCredential(@PathVariable String consumerId,
                                                     @RequestBody @Valid CreateCredentialParam param) {
        return null;
    }

    @Operation(summary = "获取Consumer凭证信息")
    @GetMapping("/{consumerId}/credentials")
    @DeveloperAuth
    public ConsumerCredentialResult getCredential(@PathVariable String consumerId) {
        return null;
    }

    @Operation(summary = "更新Consumer凭证")
    @PutMapping("/{consumerId}/credentials")
    @DeveloperAuth
    public ConsumerCredentialResult updateCredential(@PathVariable String consumerId,
                                                     @RequestBody @Valid UpdateCredentialParam param) {
        return null;
    }

    @Operation(summary = "删除Consumer凭证")
    @DeleteMapping("/{consumerId}/credentials")
    @DeveloperAuth
    public void deleteCredential(@PathVariable String consumerId) {

    }

    @Operation(summary = "订阅API产品")
    @PostMapping("/{consumerId}/subscriptions")
    @DeveloperAuth
    public SubscriptionResult subscribeProduct(@PathVariable String consumerId,
                                               @RequestBody @Valid CreateSubscriptionParam param) {
        return null;
    }

    @Operation(summary = "获取Consumer的订阅列表")
    @GetMapping("/{consumerId}/subscriptions")
    @DeveloperAuth
    public PageResult<SubscriptionResult> listSubscriptions(@PathVariable String consumerId,
                                                                   QuerySubscriptionParam param,
                                                                   Pageable pageable) {
        return null;
    }

    @Operation(summary = "取消订阅")
    @DeleteMapping("/{consumerId}/subscriptions/{productId}")
    public void deleteSubscription(@PathVariable String consumerId, @PathVariable String productId) {

    }

    @Operation(summary = "审批订阅申请")
    @PatchMapping("/{consumerId}/subscriptions/{productId}")
    @AdminAuth
    public SubscriptionResult approveSubscription(@PathVariable String consumerId, @PathVariable String productId) {
        return null;
    }
}
