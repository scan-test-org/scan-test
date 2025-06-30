package com.alibaba.apiopenplatform.dto.params.portal;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zh
 */
@Data
public class UpdatePortalUiParam {

    @NotBlank(message = "门户ID不能为空")
    private String portalId;

    private String logo;

    private String icon;
}
