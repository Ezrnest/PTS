package com.easylightning.pts.service

import com.easylightning.pts.dao.iotdb.SensorDataDAO
import com.easylightning.pts.dao.mysql.UserDAO
import com.easylightning.pts.dao.mysql.UserDeviceDAO
import com.easylightning.pts.entity.UserDevice
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.easylightning.pts.entity.User
import java.time.Instant

/*
 * Created by liyicheng at 2020-11-02 20:25
 */


/**
 * @author liyicheng
 */
@Transactional
@Service
class UserService(

) : UserDetailsService {

    @Autowired
    lateinit var userDAO: UserDAO

    @Autowired
    lateinit var encoder: PasswordEncoder

    @Autowired
    lateinit var deviceDAO: UserDeviceDAO

    @Autowired
    lateinit var dataService: DataService

    companion object {
        val logger = LogManager.getLogger()
    }



    override fun loadUserByUsername(username: String): UserDetails {
        val re = userDAO.findByUsername(username)
        logger.info("Loading user $username")
        if (re.isEmpty) {
            throw UsernameNotFoundException("User $username does not exist")
        }
        val u = re.get()
        val auth = arrayListOf<GrantedAuthority>(SimpleGrantedAuthority("ROLE_user"))
        return org.springframework.security.core.userdetails.User(u.username, u.password, auth)
    }


    @Transactional
    fun registerUser(name: String, pass: String): Boolean {
//        val name = form.username!!
//        val pass = form.password!!
        if (userDAO.existsByUsername(name)) {
            logger.debug("Duplicated user: $name")
            return false
        }
        val encodedPassword = encoder.encode(pass)
        val user = User()
        user.username = name
        user.password = encodedPassword
        userDAO.save(user)
        return true
    }

//    fun register

    fun checkAuthentication(username: String, password: String): User? {
        val op = userDAO.findByUsername(username)
        if (op.isEmpty) {
            logger.debug("User does not exist: $username")
            return null
        }
        val u = op.get()
        if (!encoder.matches(password, u.password)) {
            logger.debug("Incorrect password for user: $username")
            return null
        }
        return u
    }

    fun checkDevice(username: String, password: String, imei : String) : Pair<User,UserDevice>?{
        val user = checkAuthentication(username, password) ?: return null
        val op = deviceDAO.findByUidAndImei(user.uid!!,imei)
        if (op.isEmpty) {
            return null
        }
        return user to op.get()
    }

    fun activeDevice(uid :Long): Pair<UserDevice,Instant>?{
        val devices = deviceDAO.findAllByUid(uid)
        return devices.map {
            it to dataService.lastUpdateTime(uid,it.tid!!)
        }.maxBy { it.second }
    }

//    fun




}
