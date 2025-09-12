package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.dto.result.AuthResult;
import com.alibaba.apiopenplatform.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author zh
 */
@Controller
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    @PostMapping("/oauth2/token")
    public AuthResult authenticate(String jwtToken) {
        return oAuth2Service.authenticate(jwtToken);
    }
}
