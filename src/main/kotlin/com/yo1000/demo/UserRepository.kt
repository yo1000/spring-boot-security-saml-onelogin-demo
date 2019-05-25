package com.yo1000.demo

import org.springframework.stereotype.Repository

/**
 *
 * @author yo1000
 */
interface UserRepository {
    fun findByUsername(username: String): User?
}

@Repository
class UserRepositoryImpl : UserRepository {
    override fun findByUsername(username: String): User? = User(
            username = username,
            authorityValues = listOf("USER")
    )
}
