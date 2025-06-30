package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.dto.params.portal.CreatePortalParam;
import com.alibaba.apiopenplatform.dto.params.portal.UpdatePortalParam;
import com.alibaba.apiopenplatform.dto.params.portal.UpdatePortalSettingParam;
import com.alibaba.apiopenplatform.dto.params.portal.UpdatePortalUiParam;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.PortalResult;
import com.alibaba.apiopenplatform.service.PortalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author zh
 */
@RestController
@RequestMapping("/portal")
@Slf4j
@Validated
public class PortalController {

    private final PortalService portalService;

    public PortalController(PortalService portalService) {
        this.portalService = portalService;
    }

    @PostMapping("/create")
    public PortalResult createPortal(@Valid @RequestBody CreatePortalParam param) {
        return portalService.createPortal(param);
    }

    @GetMapping("/{portalId}")
    public PortalResult getPortal(@PathVariable String portalId) {
        return portalService.getPortal(portalId);
    }

    @GetMapping("/list")
    public PageResult<PortalResult> listPortals(@RequestParam(defaultValue = "1") int pageNumber,
                                                @RequestParam(defaultValue = "10") int pageSize) {
        return portalService.listPortals(pageNumber - 1, pageSize);
    }

    @PostMapping("/update")
    public PortalResult updatePortal(@Valid @RequestBody UpdatePortalParam param) {
        return portalService.updatePortal(param);
    }

    @PostMapping("/setting")
    public PortalResult updatePortalSetting(@Valid @RequestBody UpdatePortalSettingParam param) {
        return portalService.updatePortalSetting(param);
    }

    @PostMapping("/ui")
    public PortalResult updatePortalUi(@Valid @RequestBody UpdatePortalUiParam param) {
        return portalService.updatePortalUi(param);
    }

    @DeleteMapping("/{portalId}")
    public void deletePortal(@PathVariable String portalId) {
        portalService.deletePortal(portalId);
    }
}
