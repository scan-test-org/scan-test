package com.alibaba.apiopenplatform.dto.params.portal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdatePortalParam extends CreatePortalParam {

    @NotBlank(message = "门户ID不能为空")
    private String portalId;

}
