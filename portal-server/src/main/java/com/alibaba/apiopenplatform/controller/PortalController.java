package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.dto.params.portal.CreatePortalParam;
import com.alibaba.apiopenplatform.dto.params.portal.UpdatePortalParam;
import com.alibaba.apiopenplatform.dto.params.portal.UpdatePortalSettingParam;
import com.alibaba.apiopenplatform.dto.params.portal.UpdatePortalUiParam;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.PortalResult;
import com.alibaba.apiopenplatform.service.PortalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * @author zh
 */
@RestController
@RequestMapping("/portal")
@Slf4j
@Validated
@Tag(name = "门户管理")
public class PortalController {

    private final PortalService portalService;

    public PortalController(PortalService portalService) {
        this.portalService = portalService;
    }

    @Operation(summary = "创建门户")
    @PostMapping("/create")
    public PortalResult createPortal(@Valid @RequestBody CreatePortalParam param) {
        return portalService.createPortal(param);
    }

    @Operation(summary = "获取门户详情")
    @GetMapping("/{portalId}")
    public PortalResult getPortal(@PathVariable String portalId) {
        return portalService.getPortal(portalId);
    }

    @Operation(summary = "获取门户列表")
    @GetMapping("/list")
    public PageResult<PortalResult> listPortals(@PageableDefault(sort = "gmt_create", direction = DESC) Pageable pageable) {
        return portalService.listPortals(pageable);
    }

    @Operation(summary = "更新门户的基础信息")
    @PostMapping("/update")
    public PortalResult updatePortal(@Valid @RequestBody UpdatePortalParam param) {
        return portalService.updatePortal(param);
    }

    @Operation(summary = "更新门户的配置信息")
    @PostMapping("/setting")
    public PortalResult updatePortalSetting(@Valid @RequestBody UpdatePortalSettingParam param) {
        return portalService.updatePortalSetting(param);
    }

    @Operation(summary = "更新门户的UI配置")
    @PostMapping("/ui")
    public PortalResult updatePortalUi(@Valid @RequestBody UpdatePortalUiParam param) {
        return portalService.updatePortalUi(param);
    }

    @Operation(summary = "删除门户")
    @DeleteMapping("/{portalId}")
    public void deletePortal(@PathVariable String portalId) {
        portalService.deletePortal(portalId);
    }
}
