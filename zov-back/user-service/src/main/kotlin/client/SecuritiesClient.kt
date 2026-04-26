package zov.deneg.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import zov.deneg.models.Security
import java.util.UUID

/**
 * Client for calling the Securities Service API
 */
class SecuritiesClient(private val httpClient: HttpClient, private val baseUrl: String = "") {

    /**
     * Get security details by ID
     */
    suspend fun getSecurityById(securityId: UUID): Security? {
        return try {
            httpClient.get("$baseUrl/securities/$securityId").body()
        } catch (e: Exception) {
            println("Error fetching security $securityId: ${e.message}")
            null
        }
    }

    /**
     * Get security details by ID with error handling
     */
    suspend fun getSecurityByIdOrNull(securityId: UUID): Result<Security> {
        return try {
            val response = httpClient.get("$baseUrl/securities/$securityId")
            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch security: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
