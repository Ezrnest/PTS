package com.easylightning.pts.pojo

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


/*
 * Created by liyicheng at 2020-05-11 18:50
 */
/**
 * @author liyicheng
 */
open class UserRegistryForm {
    @NotBlank(message = "用户名不能为空")
    @Size(max = 40)
    var username: String? = null

    @NotEmpty(message = "密码不能为空")
    @Size(max = 40)
    var password: String? = null

    @NotEmpty(message = "请重复一遍密码")
    @Size(max = 40)
    var password2: String? = null

    override fun toString(): String {
        return "UserRegistryForm(username=$username, password=$password, password2=$password2)"
    }
}
