package com.gcp.common.core.base;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * token生成
 * @author Admin
 */
@ApiModel(value = "登录出参")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@NoArgsConstructor
public class LoginResponseDto {

    @ApiModelProperty(value = "token")
    private String token;

    @ApiModelProperty(value = "用户名称")
    private String name;

    @ApiModelProperty(value = "用户id")
    private String id;

    @ApiModelProperty(value = "角色id")
    private String roleId;

    @ApiModelProperty(value = "是否管理员")
    private Boolean admin;

    /**
     * 格式化token
     * @param token
     */
    public void setToken(String token) {
        this.token = token+'|'+ LocalDateTime.now().plusMinutes(5L).toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }
}
