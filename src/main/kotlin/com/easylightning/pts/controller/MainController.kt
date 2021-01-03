package com.easylightning.pts.controller

import com.easylightning.pts.pojo.UserRegistryForm
import com.easylightning.pts.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import javax.validation.Valid


/**
 * 负责处理登录等基本流程。
 *
 * Created by liyicheng at 2020-11-02 18:46
 */
@Controller
class MainController {

    @Autowired
    lateinit var userService: UserService

    @RequestMapping("/", "/index")
    fun indexPage(): String {
//        println("Index page")
        return "index"
    }

//    @RequestMapping("/error")
//    fun errorPage(): String{
//        return "error"
//    }

    @RequestMapping("/logout")
    fun logoutPage(): String {
        return "logout"
    }

    @RequestMapping("/login", method = [RequestMethod.GET])
    fun loginPage(@RequestParam(value = "error", required = false) error: String?,
                  @RequestParam(value = "logout", required = false) logout: String?
    ): ModelAndView {
        val mv = ModelAndView("login")
        if (error != null) {
            mv.addObject("error", "用户名或者密码不正确")
        }
        return mv
    }

    @RequestMapping("/registry", method = [RequestMethod.GET])
    fun registryPage(userRegistryForm: UserRegistryForm): String {
//        model.addAttribute("registryForm", UserRegistryForm())
        return "registry"
    }

    @RequestMapping("/registry", method = [RequestMethod.POST])
    fun doRegistry(
            @Valid
            userRegistryForm: UserRegistryForm,
            bindingResult: BindingResult
    ): String {
//        println(userRegistryForm)
        if (bindingResult.hasErrors()) {
            return "registry"
        }
        if (userRegistryForm.password != userRegistryForm.password2) {
            bindingResult.rejectValue("password", "", "两次输入的密码不一致")
            return "registry"
        }
        val succeeded = userService.registerUser(userRegistryForm.username!!,userRegistryForm.password!!)
        if (!succeeded) {
            bindingResult.rejectValue("username", "", "用户名重复")
            return "registry"
        }


        return "registrySuccess"
    }

    @GetMapping("/registrySuccess")
    fun registrySuccessPage(): String {
        return "registrySuccess"
    }
}

