package zov.deneg.security

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordHasher {
    
    fun hash(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }
    
    fun verify(password: String, hash: String): Boolean {
        return try {
            BCrypt.verifyer().verify(password.toCharArray(), hash.toCharArray()).verified
        } catch (e: Exception) {
            false
        }
    }
}
