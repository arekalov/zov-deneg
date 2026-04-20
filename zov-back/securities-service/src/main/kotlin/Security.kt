package zov.deneg

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String
)

fun Application.configureSecurity() {
    // Get JWT config from environment variables with defaults from application.yaml
    val jwtConfig = JwtConfig(
        secret = System.getenv("JWT_SECRET") ?: environment.config.property("jwt.secret").getString(),
        issuer = System.getenv("JWT_ISSUER") ?: environment.config.property("jwt.issuer").getString(),
        audience = System.getenv("JWT_AUDIENCE") ?: environment.config.property("jwt.audience").getString(),
        realm = System.getenv("JWT_REALM") ?: environment.config.property("jwt.realm").getString()
    )
    
    authentication {
        jwt {
            realm = jwtConfig.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtConfig.audience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
