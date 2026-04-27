package zov.deneg.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT as JwtDecodedJWT
import io.ktor.server.application.*
import io.ktor.server.util.*
import zov.deneg.models.UserRole
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class JwtConfig(private val environment: ApplicationEnvironment) {
    
    val secret: String = environment.config.property("jwt.secret").getString()
    val audience: String = environment.config.property("jwt.audience").getString()
    val issuer: String = environment.config.property("jwt.issuer").getString()
    val realm: String = environment.config.property("jwt.realm").getString()
    val accessTokenTtlSeconds: Long = environment.config.property("jwt.accessTokenTtlSeconds").getString().toLong()
    val refreshTokenTtlDays: Long = environment.config.property("jwt.refreshTokenTtlDays").getString().toLong()
    
    private val algorithm = Algorithm.HMAC256(secret)
    
    fun generateAccessToken(userId: String, role: UserRole): String {
        val now = Date()
        val expiresAt = Date.from(now.toInstant().plusSeconds(accessTokenTtlSeconds))
        
        return JWT.create()
            .withSubject(userId)
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("role", role.name.lowercase())
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }
    
    fun generateRefreshToken(): String {
        return UUID.randomUUID().toString()
    }
    
    fun getRefreshTokenExpiry(): Date {
        return Date.from(LocalDateTime.now().plusDays(refreshTokenTtlDays).toInstant(ZoneOffset.UTC))
    }
    
    fun verifyToken(token: String): DecodedJWT? {
        return try {
            val jwt = JWT.require(algorithm)
                .withAudience(audience)
                .withIssuer(issuer)
                .build()
                .verify(token)
            jwt.toDecodedJWT()
        } catch (e: Exception) {
            null
        }
    }
}

data class DecodedJWT(
    val subject: String,
    val role: UserRole,
    val expiresAt: Date
)

fun JwtDecodedJWT.toDecodedJWT(): DecodedJWT {
    return DecodedJWT(
        subject = this.subject,
        role = UserRole.valueOf(this.getClaim("role").asString().uppercase()),
        expiresAt = this.expiresAt
    )
}
