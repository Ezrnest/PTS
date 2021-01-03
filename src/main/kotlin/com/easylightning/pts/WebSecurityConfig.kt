package com.easylightning.pts

import com.easylightning.pts.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder


/*
 * Created by liyicheng at 2020-05-09 21:28
 */
/**
 * @author liyicheng
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class GlobalWebSecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    lateinit var userService: UserService

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    //    override fun configure(http: HttpSecurity) {
//        http.authorizeRequests().antMatchers("/", "/repo").permitAll()
//        http.authorizeRequests().antMatchers("/manager/**").hasRole("manager")
//                .and().fo
//    }
//
//    override fun userDetailsService(): UserDetailsService {
//        return super.userDetailsService()
//    }
    @Bean
    fun daoAuthenticationProvider(): AuthenticationProvider {
        val p = DaoAuthenticationProvider()
        p.setPasswordEncoder(passwordEncoder())
        p.setUserDetailsService(userService)
        return p
    }


//    @Autowired
//    fun configureGlobal(auth: AuthenticationManagerBuilder) {
//
////        auth.userDetailsService()
//    }


    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userService)
//    val encoder = passwordEncoder()
//        auth.inMemoryAuthentication()
//                .withUser("lyc").password(encoder.encode("root")).roles("manager", "customer")
//                .and().withUser("test").password(encoder.encode("test")).roles("customer")
    }

    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
        http.authorizeRequests()
                .antMatchers("/", "/index","/registry", "/login*").permitAll()
                .antMatchers("/collect/**","/api/ios/**").permitAll()
                .antMatchers("/me", "/me/**").hasRole("user")
                .antMatchers("/manager/**").hasRole("manager")
                .anyRequest().authenticated()
                .and()
                .formLogin().loginPage("/login").defaultSuccessUrl("/me").permitAll()
                .and()
                .logout().clearAuthentication(true).invalidateHttpSession(true)
                .and()


    }


}