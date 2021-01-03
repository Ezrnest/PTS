package com.easylightning.pts.dao.mysql

import com.easylightning.pts.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.relational.core.mapping.Table
import java.util.*


/*
 * Created by liyicheng at 2020-11-02 19:34
 */


@Table("user")
interface UserDAO : JpaRepository<User, Long> {
    fun findByUsername(username: String) : Optional<User>

    fun existsByUsername(username: String) : Boolean

}