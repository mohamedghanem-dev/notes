package com.example.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object SecurityUtils {

    /**
     * Generates a random 16-byte salt for password hashing
     */
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    /**
     * Hashes a password using SHA-256 with salt
     */
    fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt.toByteArray(Charsets.UTF_8))
        val hashedBytes = md.digest(password.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashedBytes)
    }

    /**
     * Verifies if input password matches stored salt and hash
     */
    fun verifyPassword(inputPassword: String, storedSalt: String, storedHash: String): Boolean {
        if (inputPassword.isBlank() || storedSalt.isBlank() || storedHash.isBlank()) return false
        val computedHash = hashPassword(inputPassword, storedSalt)
        return computedHash == storedHash
    }
}
