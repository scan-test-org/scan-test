package com.alibaba.apiopenplatform.dto.params.admin;

public class ChangePasswordParam {
    private String oldPassword;
    private String newPassword;
    // getter/setter
    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
} 