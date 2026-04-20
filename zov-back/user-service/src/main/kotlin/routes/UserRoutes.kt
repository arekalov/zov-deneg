package zov.deneg.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import zov.deneg.data.UserRepository
import zov.deneg.models.*
import java.util.*

fun Routing.configureUserRoutes(userRepository: UserRepository) {
    
    // GET /users/me - Current user profile
    authenticate {
        get("/users/me") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.subject ?: run {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                    code = "UNAUTHORIZED",
                    message = "Требуется аутентификация"
                ))
                return@get
            }
            
            val user = userRepository.findById(UUID.fromString(userId))
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(
                    code = "NOT_FOUND",
                    message = "Пользователь не найден"
                ))
                return@get
            }
            
            call.respond(HttpStatusCode.OK, user)
        }
        
        // PUT /users/me - Update current user profile
        put("/users/me") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.subject ?: run {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                    code = "UNAUTHORIZED",
                    message = "Требуется аутентификация"
                ))
                return@put
            }
            
            val request = try {
                call.receive<UpdateProfileRequest>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                    code = "INVALID_REQUEST",
                    message = "Некорректный формат запроса"
                ))
                return@put
            }
            
            // Validate input
            val validationErrors = validateUpdateProfileRequest(request)
            if (validationErrors.isNotEmpty()) {
                call.respond(HttpStatusCode.BadRequest, ValidationErrorResponse(
                    code = "VALIDATION_ERROR",
                    message = "Ошибка валидации входных данных",
                    fields = validationErrors
                ))
                return@put
            }
            
            // Check if email or phone already exists (excluding current user)
            request.email?.let { email ->
                if (userRepository.existsByEmail(email, excludeId = UUID.fromString(userId))) {
                    call.respond(HttpStatusCode.Conflict, ErrorResponse(
                        code = "EMAIL_ALREADY_EXISTS",
                        message = "Этот email уже используется другим аккаунтом"
                    ))
                    return@put
                }
            }
            
            request.phone?.let { phone ->
                if (userRepository.existsByPhone(phone, excludeId = UUID.fromString(userId))) {
                    call.respond(HttpStatusCode.Conflict, ErrorResponse(
                        code = "PHONE_ALREADY_EXISTS",
                        message = "Этот телефон уже используется другим аккаунтом"
                    ))
                    return@put
                }
            }
            
            // Update user
            val updatedUser = userRepository.update(
                id = UUID.fromString(userId),
                firstName = request.firstName,
                lastName = request.lastName,
                email = request.email,
                phone = request.phone
            )
            
            if (updatedUser == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(
                    code = "NOT_FOUND",
                    message = "Пользователь не найден"
                ))
                return@put
            }
            
            call.respond(HttpStatusCode.OK, updatedUser)
        }
    }
    
    // GET /users - List all users (admin only)
    authenticate {
        get("/users") {
            val principal = call.principal<JWTPrincipal>()
            val role = principal?.payload?.getClaim("role")?.asString()?.let { 
                UserRole.valueOf(it.uppercase()) 
            }
            
            if (role != UserRole.ADMIN) {
                call.respond(HttpStatusCode.Forbidden, ErrorResponse(
                    code = "FORBIDDEN",
                    message = "Недостаточно прав для выполнения операции"
                ))
                return@get
            }
            
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
            val search = call.request.queryParameters["search"]
            val roleFilter = call.request.queryParameters["role"]?.let { 
                try { UserRole.valueOf(it.uppercase()) } catch (e: Exception) { null }
            }
            val isBlocked = call.request.queryParameters["isBlocked"]?.toBooleanStrictOrNull()
            
            val (users, total) = userRepository.findAll(
                page = page,
                pageSize = pageSize,
                search = search,
                role = roleFilter,
                isBlocked = isBlocked
            )
            
            val totalPages = (total + pageSize - 1) / pageSize
            
            call.respond(HttpStatusCode.OK, PaginatedResponse(
                data = users,
                pagination = Pagination(
                    page = page,
                    pageSize = pageSize,
                    totalItems = total,
                    totalPages = totalPages
                )
            ))
        }
    }
    
    // GET /users/{userId} - Get user by ID (admin only)
    authenticate {
        get("/users/{userId}") {
            val principal = call.principal<JWTPrincipal>()
            val role = principal?.payload?.getClaim("role")?.asString()?.let { 
                UserRole.valueOf(it.uppercase()) 
            }
            
            if (role != UserRole.ADMIN) {
                call.respond(HttpStatusCode.Forbidden, ErrorResponse(
                    code = "FORBIDDEN",
                    message = "Недостаточно прав для выполнения операции"
                ))
                return@get
            }
            
            val userId = call.parameters["userId"]?.let { 
                try { UUID.fromString(it) } catch (e: Exception) { null }
            }
            
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                    code = "INVALID_ID",
                    message = "Некорректный формат ID"
                ))
                return@get
            }
            
            val user = userRepository.findById(userId)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(
                    code = "NOT_FOUND",
                    message = "Пользователь не найден"
                ))
                return@get
            }
            
            call.respond(HttpStatusCode.OK, user)
        }
    }
    
    // PUT /users/{userId} - Update user (admin only)
    authenticate {
        put("/users/{userId}") {
            val principal = call.principal<JWTPrincipal>()
            val role = principal?.payload?.getClaim("role")?.asString()?.let { 
                UserRole.valueOf(it.uppercase()) 
            }
            
            if (role != UserRole.ADMIN) {
                call.respond(HttpStatusCode.Forbidden, ErrorResponse(
                    code = "FORBIDDEN",
                    message = "Недостаточно прав для выполнения операции"
                ))
                return@put
            }
            
            val userId = call.parameters["userId"]?.let { 
                try { UUID.fromString(it) } catch (e: Exception) { null }
            }
            
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                    code = "INVALID_ID",
                    message = "Некорректный формат ID"
                ))
                return@put
            }
            
            val request = try {
                call.receive<UpdateUserRequest>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                    code = "INVALID_REQUEST",
                    message = "Некорректный формат запроса"
                ))
                return@put
            }
            
            // Validate input
            val validationErrors = validateUpdateUserRequest(request)
            if (validationErrors.isNotEmpty()) {
                call.respond(HttpStatusCode.BadRequest, ValidationErrorResponse(
                    code = "VALIDATION_ERROR",
                    message = "Ошибка валидации входных данных",
                    fields = validationErrors
                ))
                return@put
            }
            
            // Check if email or phone already exists (excluding current user)
            request.email?.let { email ->
                if (userRepository.existsByEmail(email, excludeId = userId)) {
                    call.respond(HttpStatusCode.Conflict, ErrorResponse(
                        code = "EMAIL_ALREADY_EXISTS",
                        message = "Пользователь с таким email уже зарегистрирован"
                    ))
                    return@put
                }
            }
            
            request.phone?.let { phone ->
                if (userRepository.existsByPhone(phone, excludeId = userId)) {
                    call.respond(HttpStatusCode.Conflict, ErrorResponse(
                        code = "PHONE_ALREADY_EXISTS",
                        message = "Пользователь с таким телефоном уже зарегистрирован"
                    ))
                    return@put
                }
            }
            
            // Update user
            val updatedUser = userRepository.update(
                id = userId,
                firstName = request.firstName,
                lastName = request.lastName,
                email = request.email,
                phone = request.phone,
                role = request.role,
                isBlocked = request.isBlocked
            )
            
            if (updatedUser == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(
                    code = "NOT_FOUND",
                    message = "Пользователь не найден"
                ))
                return@put
            }
            
            call.respond(HttpStatusCode.OK, updatedUser)
        }
    }
    
    // DELETE /users/{userId} - Delete user (admin only)
    authenticate {
        delete("/users/{userId}") {
            val principal = call.principal<JWTPrincipal>()
            val currentUserId = principal?.payload?.subject
            val role = principal?.payload?.getClaim("role")?.asString()?.let { 
                UserRole.valueOf(it.uppercase()) 
            }
            
            if (role != UserRole.ADMIN) {
                call.respond(HttpStatusCode.Forbidden, ErrorResponse(
                    code = "FORBIDDEN",
                    message = "Недостаточно прав для выполнения операции"
                ))
                return@delete
            }
            
            val userId = call.parameters["userId"]?.let { 
                try { UUID.fromString(it) } catch (e: Exception) { null }
            }
            
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                    code = "INVALID_ID",
                    message = "Некорректный формат ID"
                ))
                return@delete
            }
            
            // Cannot delete self
            if (currentUserId == userId.toString()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                    code = "CANNOT_DELETE_SELF",
                    message = "Нельзя удалить собственный аккаунт"
                ))
                return@delete
            }
            
            // Check if user has active positions (placeholder - would need to call trading service)
            // For now, we'll skip this check
            
            val deleted = userRepository.delete(userId)
            if (!deleted) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(
                    code = "NOT_FOUND",
                    message = "Пользователь не найден"
                ))
                return@delete
            }
            
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

private fun validateUpdateProfileRequest(request: UpdateProfileRequest): List<FieldError> {
    val errors = mutableListOf<FieldError>()
    
    request.firstName?.let {
        if (it.isBlank() || it.length > 50) {
            errors.add(FieldError("firstName", "Имя должно быть от 1 до 50 символов"))
        }
    }
    
    request.lastName?.let {
        if (it.isBlank() || it.length > 50) {
            errors.add(FieldError("lastName", "Фамилия должна быть от 1 до 50 символов"))
        }
    }
    
    request.email?.let {
        if (!it.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)\$"))) {
            errors.add(FieldError("email", "Некорректный формат email"))
        }
    }
    
    request.phone?.let {
        if (!it.matches(Regex("^\\+7\\d{10}\$"))) {
            errors.add(FieldError("phone", "Некорректный формат номера телефона"))
        }
    }
    
    return errors
}

private fun validateUpdateUserRequest(request: UpdateUserRequest): List<FieldError> {
    val errors = validateUpdateProfileRequest(UpdateProfileRequest(
        firstName = request.firstName,
        lastName = request.lastName,
        email = request.email,
        phone = request.phone
    ))
    return errors
}
