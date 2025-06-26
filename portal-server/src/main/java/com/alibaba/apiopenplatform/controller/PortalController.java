package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.dto.params.PortalParam;
import com.alibaba.apiopenplatform.dto.result.PortalResult;
import com.alibaba.apiopenplatform.service.PortalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public PortalResult createPortal(@Valid @RequestBody PortalParam portalParam) {
        return portalService.createPortal(portalParam);
    }
}
