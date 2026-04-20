package zov.deneg.data

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import zov.deneg.models.TransactionType
import zov.deneg.models.UserRole
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.test.*

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class RepositoryTest {

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:15-alpine").apply {
            withDatabaseName("test_repos")
            withUsername("test")
            withPassword("test")
            withStartupTimeout(Duration.ofMinutes(2))
        }

        lateinit var database: org.jetbrains.exposed.sql.Database

        @JvmStatic
        @BeforeAll
        fun setup() {
            val jdbcUrl = postgres.jdbcUrl
            val dbUser = postgres.username
            val dbPassword = postgres.password

            database = org.jetbrains.exposed.sql.Database.connect(
                url = jdbcUrl,
                driver = "org.postgresql.Driver",
                user = dbUser,
                password = dbPassword
            )

            // Create tables
            transaction(database) {
                SchemaUtils.create(UsersTable, RefreshTokensTable, UserBalancesTable, TransactionsTable)
            }
        }

        @JvmStatic
        @AfterAll
        fun teardown() {
            postgres.stop()
        }
    }

    private lateinit var userRepository: UserRepository
    private lateinit var balanceRepository: BalanceRepository
    private lateinit var transactionRepository: TransactionRepository

    @BeforeEach
    fun setUp() {
        userRepository = UserRepository(database)
        balanceRepository = BalanceRepository(database)
        transactionRepository = TransactionRepository(database)

        // Clean up tables before each test
        transaction(database) {
            SchemaUtils.drop(UsersTable, RefreshTokensTable, UserBalancesTable, TransactionsTable)
            SchemaUtils.create(UsersTable, RefreshTokensTable, UserBalancesTable, TransactionsTable)
        }
    }

    // ==================== USER REPOSITORY TESTS ====================

    @org.junit.jupiter.api.Test
    fun `test create user`() {
        val user = userRepository.create(
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            phone = "+79001234567",
            passwordHash = "hashed_password"
        )

        assertNotNull(user)
        assertEquals("John", user.firstName)
        assertEquals("Doe", user.lastName)
        assertEquals("john@example.com", user.email)
        assertEquals("+79001234567", user.phone)
        assertEquals(UserRole.USER, user.role)
        assertFalse(user.isBlocked)
    }

    @org.junit.jupiter.api.Test
    fun `test create user with admin role`() {
        val user = userRepository.create(
            firstName = "Admin",
            lastName = "User",
            email = "admin@example.com",
            phone = "+79009998877",
            passwordHash = "hashed_password",
            role = UserRole.ADMIN
        )

        assertNotNull(user)
        assertEquals(UserRole.ADMIN, user.role)
    }

    @org.junit.jupiter.api.Test
    fun `test find user by email`() {
        val created = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        val found = userRepository.findByEmail("test@example.com")

        assertNotNull(found)
        assertEquals(created.id, found.id)
    }

    @org.junit.jupiter.api.Test
    fun `test find user by phone`() {
        userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )

        val found = userRepository.findByPhone("+79001112233")

        assertNotNull(found)
        assertEquals("+79001112233", found.phone)
    }

    @org.junit.jupiter.api.Test
    fun `test find user by id`() {
        val created = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        val found = userRepository.findById(UUID.fromString(created.id))

        assertNotNull(found)
        assertEquals(created.id, found.id)
    }

    @org.junit.jupiter.api.Test
    fun `test find user by id - not found`() {
        val found = userRepository.findById(UUID.randomUUID())
        assertNull(found)
    }

    @org.junit.jupiter.api.Test
    fun `test update user`() {
        val created = userRepository.create(
            firstName = "Original",
            lastName = "Name",
            email = "original@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        val updated = userRepository.update(
            id = UUID.fromString(created.id),
            firstName = "Updated",
            email = "updated@example.com"
        )

        assertNotNull(updated)
        assertEquals("Updated", updated.firstName)
        assertEquals("updated@example.com", updated.email)
        assertEquals("Name", updated.lastName) // unchanged
    }

    @org.junit.jupiter.api.Test
    fun `test update user role`() {
        val created = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        val updated = userRepository.update(
            id = UUID.fromString(created.id),
            role = UserRole.ADMIN
        )

        assertNotNull(updated)
        assertEquals(UserRole.ADMIN, updated.role)
    }

    @org.junit.jupiter.api.Test
    fun `test update user block status`() {
        val created = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        val updated = userRepository.update(
            id = UUID.fromString(created.id),
            isBlocked = true
        )

        assertNotNull(updated)
        assertTrue(updated.isBlocked)
    }

    @org.junit.jupiter.api.Test
    fun `test delete user`() {
        val created = userRepository.create(
            firstName = "To Delete",
            lastName = "User",
            email = "delete@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        val deleted = userRepository.delete(UUID.fromString(created.id))
        assertTrue(deleted)

        val found = userRepository.findById(UUID.fromString(created.id))
        assertNull(found)
    }

    @org.junit.jupiter.api.Test
    fun `test exists by email`() {
        userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "exists@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )

        assertTrue(userRepository.existsByEmail("exists@example.com"))
        assertFalse(userRepository.existsByEmail("notexists@example.com"))
    }

    @org.junit.jupiter.api.Test
    fun `test exists by email with exclude`() {
        val created = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "exists@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        // Should return false when excluding the same user
        assertFalse(userRepository.existsByEmail("exists@example.com", excludeId = UUID.fromString(created.id)))
        // Should return true for different user
        assertTrue(userRepository.existsByEmail("exists@example.com"))
    }

    @org.junit.jupiter.api.Test
    fun `test find all users`() {
        // Create multiple users
        repeat(5) { i ->
            userRepository.create(
                firstName = "User$i",
                lastName = "Test",
                email = "user$i@example.com",
                phone = "+7900111000$i",
                passwordHash = "hash"
            )
        }

        val (users, total) = userRepository.findAll(page = 1, pageSize = 10)

        assertEquals(5, users.size)
        assertEquals(5, total)
    }

    @org.junit.jupiter.api.Test
    fun `test find all with pagination`() {
        repeat(25) { i ->
            userRepository.create(
                firstName = "User$i",
                lastName = "Test",
                email = "user$i@example.com",
                phone = "+790011100$i",
                passwordHash = "hash"
            )
        }

        val (page1, total1) = userRepository.findAll(page = 1, pageSize = 10)
        val (page2, total2) = userRepository.findAll(page = 2, pageSize = 10)

        assertEquals(10, page1.size)
        assertEquals(10, page2.size)
        assertEquals(25, total1)
        assertEquals(25, total2)
    }

    @org.junit.jupiter.api.Test
    fun `test find all with role filter`() {
        userRepository.create(
            firstName = "User",
            lastName = "One",
            email = "user@example.com",
            phone = "+79001111111",
            passwordHash = "hash"
        )
        userRepository.create(
            firstName = "Admin",
            lastName = "One",
            email = "admin@example.com",
            phone = "+79002222222",
            passwordHash = "hash",
            role = UserRole.ADMIN
        )

        val (users, _) = userRepository.findAll(role = UserRole.ADMIN)
        assertEquals(1, users.size)
        assertEquals(UserRole.ADMIN, users[0].role)
    }

    @org.junit.jupiter.api.Test
    fun `test save and find refresh token`() {
        val user = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        val tokenId = userRepository.saveRefreshToken(
            userId = UUID.fromString(user.id),
            token = "test-refresh-token",
            expiresAt = Instant.now().plusSeconds(3600)
        )

        assertNotNull(tokenId)

        val found = userRepository.findRefreshToken("test-refresh-token")
        assertNotNull(found)
        assertEquals("test-refresh-token", found.token)
        assertFalse(found.isRevoked)
    }

    @org.junit.jupiter.api.Test
    fun `test revoke refresh token`() {
        val user = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        userRepository.saveRefreshToken(
            userId = UUID.fromString(user.id),
            token = "test-token",
            expiresAt = Instant.now().plusSeconds(3600)
        )

        val revoked = userRepository.revokeRefreshToken("test-token")
        assertTrue(revoked)

        val found = userRepository.findRefreshToken("test-token")
        assertNotNull(found)
        assertTrue(found.isRevoked)
    }

    // ==================== BALANCE REPOSITORY TESTS ====================

    @org.junit.jupiter.api.Test
    fun `test get balance - creates if not exists`() {
        val user = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        val balance = balanceRepository.getBalance(UUID.fromString(user.id))

        assertEquals("0.00", balance.available)
        assertEquals("0.00", balance.total)
        assertEquals("0.00", balance.blocked)
    }

    @org.junit.jupiter.api.Test
    fun `test deposit`() {
        val user = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        val balance = balanceRepository.deposit(
            userId = UUID.fromString(user.id),
            amount = BigDecimal("1000.50")
        )

        assertEquals("1000.50", balance.available)
        assertEquals("1000.50", balance.total)
        assertEquals("0.00", balance.blocked)
    }

    @org.junit.jupiter.api.Test
    fun `test multiple deposits`() {
        val user = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        balanceRepository.deposit(UUID.fromString(user.id), BigDecimal("1000"))
        balanceRepository.deposit(UUID.fromString(user.id), BigDecimal("500"))
        val balance = balanceRepository.deposit(UUID.fromString(user.id), BigDecimal("250.75"))

        assertEquals("1750.75", balance.available)
        assertEquals("1750.75", balance.total)
    }

    @org.junit.jupiter.api.Test
    fun `test withdraw - success`() {
        val user = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        balanceRepository.deposit(UUID.fromString(user.id), BigDecimal("1000"))
        val balance = balanceRepository.withdraw(UUID.fromString(user.id), BigDecimal("400"))

        assertNotNull(balance)
        assertEquals("600.00", balance.available)
        assertEquals("600.00", balance.total)
    }

    @org.junit.jupiter.api.Test
    fun `test withdraw - insufficient funds`() {
        val user = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        balanceRepository.deposit(UUID.fromString(user.id), BigDecimal("100"))
        val balance = balanceRepository.withdraw(UUID.fromString(user.id), BigDecimal("500"))

        assertNull(balance)
    }

    @org.junit.jupiter.api.Test
    fun `test block and unblock funds`() {
        val user = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        balanceRepository.deposit(UUID.fromString(user.id), BigDecimal("1000"))

        val blocked = balanceRepository.blockFunds(UUID.fromString(user.id), BigDecimal("300"))
        assertTrue(blocked)

        var balance = balanceRepository.getBalance(UUID.fromString(user.id))
        assertEquals("700.00", balance.available)
        assertEquals("1000.00", balance.total)
        assertEquals("300.00", balance.blocked)

        val unblocked = balanceRepository.unblockFunds(UUID.fromString(user.id), BigDecimal("300"))
        assertTrue(unblocked)

        balance = balanceRepository.getBalance(UUID.fromString(user.id))
        assertEquals("1000.00", balance.available)
        assertEquals("1000.00", balance.total)
        assertEquals("0.00", balance.blocked)
    }

    @org.junit.jupiter.api.Test
    fun `test block funds - insufficient`() {
        val user = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        balanceRepository.deposit(UUID.fromString(user.id), BigDecimal("100"))
        val blocked = balanceRepository.blockFunds(UUID.fromString(user.id), BigDecimal("500"))

        assertFalse(blocked)
    }

    // ==================== TRANSACTION REPOSITORY TESTS ====================

    @org.junit.jupiter.api.Test
    fun `test create transaction`() {
        val user = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        val transaction = transactionRepository.create(
            userId = UUID.fromString(user.id),
            type = TransactionType.DEPOSIT,
            amount = BigDecimal("1000"),
            commission = BigDecimal("10")
        )

        assertNotNull(transaction)
        assertEquals(TransactionType.DEPOSIT, transaction.type)
        assertEquals("1000", transaction.amount)
        assertEquals("10", transaction.commission)
    }

    @org.junit.jupiter.api.Test
    fun `test create transaction with security`() {
        val user = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        val securityId = UUID.randomUUID()
        val transaction = transactionRepository.create(
            userId = UUID.fromString(user.id),
            type = TransactionType.BUY,
            amount = BigDecimal("5000"),
            securityId = securityId,
            ticker = "GAZP",
            securityName = "Gazprom",
            quantity = 10,
            price = BigDecimal("500"),
            commission = BigDecimal("25")
        )

        assertNotNull(transaction)
        assertEquals(TransactionType.BUY, transaction.type)
        assertEquals(securityId.toString(), transaction.securityId)
        assertEquals("GAZP", transaction.ticker)
        assertEquals(10, transaction.quantity)
        assertEquals("500", transaction.price)
    }

    @org.junit.jupiter.api.Test
    fun `test find transaction by id`() {
        val user = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        val created = transactionRepository.create(
            userId = UUID.fromString(user.id),
            type = TransactionType.DEPOSIT,
            amount = BigDecimal("1000")
        )

        val found = transactionRepository.findById(UUID.fromString(created!!.id))

        assertNotNull(found)
        assertEquals(created.id, found.id)
    }

    @org.junit.jupiter.api.Test
    fun `test find transactions by user id`() {
        val user = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        // Create multiple transactions
        transactionRepository.create(UUID.fromString(user.id), TransactionType.DEPOSIT, BigDecimal("1000"))
        transactionRepository.create(UUID.fromString(user.id), TransactionType.WITHDRAWAL, BigDecimal("100"))
        transactionRepository.create(UUID.fromString(user.id), TransactionType.DEPOSIT, BigDecimal("500"))

        val (transactions, total) = transactionRepository.findByUserId(UUID.fromString(user.id))

        assertEquals(3, transactions.size)
        assertEquals(3, total)
    }

    @org.junit.jupiter.api.Test
    fun `test find transactions with pagination`() {
        val user = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        repeat(25) { i ->
            transactionRepository.create(
                UUID.fromString(user.id),
                TransactionType.DEPOSIT,
                BigDecimal(i + 1)
            )
        }

        val (page1, total1) = transactionRepository.findByUserId(
            UUID.fromString(user.id),
            page = 1,
            pageSize = 10
        )
        val (page2, total2) = transactionRepository.findByUserId(
            UUID.fromString(user.id),
            page = 2,
            pageSize = 10
        )

        assertEquals(10, page1.size)
        assertEquals(10, page2.size)
        assertEquals(25, total1)
        assertEquals(25, total2)
    }

    @org.junit.jupiter.api.Test
    fun `test find transactions by type`() {
        val user = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        transactionRepository.create(UUID.fromString(user.id), TransactionType.DEPOSIT, BigDecimal("1000"))
        transactionRepository.create(UUID.fromString(user.id), TransactionType.WITHDRAWAL, BigDecimal("100"))
        transactionRepository.create(UUID.fromString(user.id), TransactionType.DEPOSIT, BigDecimal("500"))

        val (transactions, total) = transactionRepository.findByUserId(
            UUID.fromString(user.id),
            type = TransactionType.DEPOSIT
        )

        assertEquals(2, transactions.size)
        assertEquals(2, total)
        transactions.forEach { assertEquals(TransactionType.DEPOSIT, it.type) }
    }

    @org.junit.jupiter.api.Test
    fun `test find transactions by security id`() {
        val user = userRepository.create(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+79001112233",
            passwordHash = "hash"
        )!!

        val securityId1 = UUID.randomUUID()
        val securityId2 = UUID.randomUUID()

        transactionRepository.create(
            UUID.fromString(user.id),
            TransactionType.BUY,
            BigDecimal("1000"),
            securityId = securityId1,
            ticker = "GAZP"
        )
        transactionRepository.create(
            UUID.fromString(user.id),
            TransactionType.BUY,
            BigDecimal("2000"),
            securityId = securityId2,
            ticker = "SBER"
        )
        transactionRepository.create(
            UUID.fromString(user.id),
            TransactionType.BUY,
            BigDecimal("1500"),
            securityId = securityId1,
            ticker = "GAZP"
        )

        val (transactions, total) = transactionRepository.findByUserId(
            UUID.fromString(user.id),
            securityId = securityId1
        )

        assertEquals(2, transactions.size)
        assertEquals(2, total)
        transactions.forEach { assertEquals(securityId1.toString(), it.securityId) }
    }
}
