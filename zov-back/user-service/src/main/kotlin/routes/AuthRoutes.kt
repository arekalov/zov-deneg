package zov.deneg.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import zov.deneg.data.UserRepository
import zov.deneg.models.*
import zov.deneg.security.JwtConfig
import zov.deneg.security.PasswordHasher
import java.util.*

fun Routing.configureAuthRoutes(userRepository: UserRepository, jwtConfig: JwtConfig) {
    
    // POST /auth/register
    post("/auth/register") {
        val request = try {
            call.receive<RegisterRequest>()
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ValidationErrorResponse(
                    code = "VALIDATION_ERROR",
                    message = "Ошибка валидации входных данных",
                    fields = listOf(FieldError(field = "body", message = "Некорректный формат запроса"))
                )
            )
            return@post
        }
        
        // Validate input
        val validationErrors = validateRegisterRequest(request)
        if (validationErrors.isNotEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ValidationErrorResponse(
                code = "VALIDATION_ERROR",
                message = "Ошибка валидации входных данных",
                fields = validationErrors
            ))
            return@post
        }
        
        // Check if email or phone already exists
        if (userRepository.findByEmail(request.email) != null) {
            call.respond(
                HttpStatusCode.Conflict,
                ErrorResponse(
                    code = "EMAIL_ALREADY_EXISTS",
                    message = "Пользователь с таким email уже зарегистрирован"
                )
            )
            return@post
        }
        
        if (userRepository.findByPhone(request.phone) != null) {
            call.respond(
                HttpStatusCode.Conflict,
                ErrorResponse(
                    code = "PHONE_ALREADY_EXISTS",
                    message = "Пользователь с таким номером телефона уже зарегистрирован"
                )
            )
            return@post
        }
        
        // Hash password and create user
        val passwordHash = PasswordHasher.hash(request.password)
        val user = userRepository.create(
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            phone = request.phone,
            passwordHash = passwordHash
        )
        
        if (user == null) {
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                code = "INTERNAL_ERROR",
                message = "Внутренняя ошибка сервера"
            ))
            return@post
        }
        
        // Generate tokens
        val tokens = generateTokens(user.id, user.role, userRepository, jwtConfig)
        
        call.respond(HttpStatusCode.Created, AuthResponse(user, tokens))
    }
    
    // POST /auth/login
    post("/auth/login") {
        val request = try {
            call.receive<LoginRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                code = "INVALID_REQUEST",
                message = "Некорректный формат запроса"
            ))
            return@post
        }
        
        // Find user by phone
        val userResult = userRepository.findByPasswordHash(request.phone)
        if (userResult == null) {
            call.respond(HttpStatusCode.NotFound, ErrorResponse(
                code = "USER_NOT_FOUND",
                message = "Пользователь не найден"
            ))
            return@post
        }
        
        val (user, passwordHash) = userResult
        
        // Verify password
        if (!PasswordHasher.verify(request.password, passwordHash)) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                code = "INVALID_CREDENTIALS",
                message = "Неверный логин или пароль"
            ))
            return@post
        }
        
        // Check if user is blocked
        if (user.isBlocked) {
            call.respond(HttpStatusCode.Forbidden, ErrorResponse(
                code = "USER_BLOCKED",
                message = "Пользователь заблокирован"
            ))
            return@post
        }
        
        // Generate tokens
        val tokens = generateTokens(user.id, user.role, userRepository, jwtConfig)
        
        call.respond(HttpStatusCode.OK, AuthResponse(user, tokens))
    }
    
    // POST /auth/token/refresh
    post("/auth/token/refresh") {
        val request = try {
            call.receive<RefreshTokenRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                code = "INVALID_REQUEST",
                message = "Некорректный формат запроса"
            ))
            return@post
        }
        
        // Find refresh token
        val tokenRow = userRepository.findRefreshToken(request.refreshToken)
        if (tokenRow == null) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                code = "INVALID_REFRESH_TOKEN",
                message = "Refresh token недействителен"
            ))
            return@post
        }
        
        // Check if token is revoked
        if (tokenRow.isRevoked) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                code = "REFRESH_TOKEN_EXPIRED",
                message = "Refresh token истёк, необходима повторная авторизация"
            ))
            return@post
        }
        
        // Check if token is expired
        if (tokenRow.expiresAt.isBefore(java.time.Instant.now())) {
            userRepository.revokeRefreshToken(request.refreshToken)
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                code = "REFRESH_TOKEN_EXPIRED",
                message = "Refresh token истёк, необходима повторная авторизация"
            ))
            return@post
        }
        
        // Get user
        val user = userRepository.findById(tokenRow.userId)
        if (user == null) {
            call.respond(HttpStatusCode.NotFound, ErrorResponse(
                code = "USER_NOT_FOUND",
                message = "Пользователь не найден"
            ))
            return@post
        }
        
        // Revoke old token (rotation)
        userRepository.revokeRefreshToken(request.refreshToken)
        
        // Generate new tokens
        val tokens = generateTokens(user.id, user.role, userRepository, jwtConfig)
        
        call.respond(HttpStatusCode.OK, tokens)
    }
}

private fun validateRegisterRequest(request: RegisterRequest): List<FieldError> {
    val errors = mutableListOf<FieldError>()
    
    if (request.firstName.isBlank() || request.firstName.length > 50) {
        errors.add(FieldError("firstName", "Имя должно быть от 1 до 50 символов"))
    }
    
    if (request.lastName.isBlank() || request.lastName.length > 50) {
        errors.add(FieldError("lastName", "Фамилия должна быть от 1 до 50 символов"))
    }
    
    if (!request.email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)\$"))) {
        errors.add(FieldError("email", "Некорректный формат email"))
    }
    
    if (!request.phone.matches(Regex("^\\+7\\d{10}\$"))) {
        errors.add(FieldError("phone", "Некорректный формат номера телефона"))
    }
    
    if (request.password.length < 8) {
        errors.add(FieldError("password", "Пароль должен содержать минимум 8 символов"))
    }
    
    return errors
}

private fun generateTokens(
    userId: String,
    role: UserRole,
    userRepository: UserRepository,
    jwtConfig: JwtConfig
): Tokens {
    val accessToken = jwtConfig.generateAccessToken(userId, role)
    val refreshToken = jwtConfig.generateRefreshToken()
    val refreshTokenExpiry = jwtConfig.getRefreshTokenExpiry()
    
    // Save refresh token to database
    userRepository.saveRefreshToken(
        userId = UUID.fromString(userId),
        token = refreshToken,
        expiresAt = refreshTokenExpiry.toInstant()
    )
    
    return Tokens(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = jwtConfig.accessTokenTtlSeconds.toInt()
    )
}
