package com.pathwayfit.app.data.repository

import com.pathwayfit.app.core.model.EducationSystem
import com.pathwayfit.app.core.model.StoredAccount
import com.pathwayfit.app.core.model.User
import com.pathwayfit.app.data.local.LocalStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun register(name: String, email: String, password: String, consentAccepted: Boolean = false): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout()
    suspend fun setEducationSystem(system: EducationSystem): Result<User>
    suspend fun updateName(name: String)
}

/**
 * Prototype, on-device authentication. Passwords are salted and hashed; accounts never
 * leave the device. Replace with a real auth backend in the "Authentication" phase.
 */
class LocalAuthRepository(private val store: LocalStore) : AuthRepository {

    private companion object {
        const val KEY_ACCOUNTS = "accounts"
        const val KEY_CURRENT = "current_user_id"
    }

    private val accountsFlow = store.objectFlow<Map<String, StoredAccount>>(KEY_ACCOUNTS, emptyMap())

    override val currentUser: Flow<User?> =
        combine(accountsFlow, store.stringFlow(KEY_CURRENT)) { accounts, id ->
            id?.let { uid -> accounts.values.firstOrNull { it.user.userId == uid }?.user }
        }

    private suspend fun accounts() = store.getObject<Map<String, StoredAccount>>(KEY_ACCOUNTS, emptyMap())

    override suspend fun register(name: String, email: String, password: String, consentAccepted: Boolean): Result<User> {
        if (!consentAccepted) return Result.failure(IllegalArgumentException("Please read and agree to the data use and consent notice."))
        val key = email.trim().lowercase()
        if (name.isBlank()) return Result.failure(IllegalArgumentException("Please enter your name."))
        if (!key.contains('@') || !key.contains('.')) return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        if (password.length < 6) return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        val all = accounts()
        if (all.containsKey(key)) return Result.failure(IllegalStateException("An account with this email already exists. Try logging in."))
        val salt = newSalt()
        val user = User(userId = UUID.randomUUID().toString(), name = name.trim(), email = key, dataConsentVersion = com.pathwayfit.app.core.model.DataConsent.VERSION, dataConsentAcceptedAt = System.currentTimeMillis())
        store.putObject(KEY_ACCOUNTS, all + (key to StoredAccount(user, hash(password, salt), salt)))
        store.putString(KEY_CURRENT, user.userId)
        return Result.success(user)
    }

    override suspend fun login(email: String, password: String): Result<User> {
        val account = accounts()[email.trim().lowercase()]
            ?: return Result.failure(IllegalArgumentException("No account found for this email."))
        if (account.passwordHash != hash(password, account.salt))
            return Result.failure(IllegalArgumentException("Incorrect password."))
        store.putString(KEY_CURRENT, account.user.userId)
        return Result.success(account.user)
    }

    override suspend fun logout() = store.putString(KEY_CURRENT, null)

    override suspend fun setEducationSystem(system: EducationSystem): Result<User> =
        mutateCurrent { it.copy(educationSystem = system) }

    override suspend fun updateName(name: String) {
        if (name.isNotBlank()) mutateCurrent { it.copy(name = name.trim()) }
    }

    private suspend fun mutateCurrent(change: (User) -> User): Result<User> {
        val uid = store.getString(KEY_CURRENT) ?: return Result.failure(IllegalStateException("Not signed in"))
        val all = accounts()
        val entry = all.entries.firstOrNull { it.value.user.userId == uid }
            ?: return Result.failure(IllegalStateException("Account missing"))
        val updated = change(entry.value.user)
        store.putObject(KEY_ACCOUNTS, all + (entry.key to entry.value.copy(user = updated)))
        return Result.success(updated)
    }

    private fun newSalt(): String = ByteArray(16).also { SecureRandom().nextBytes(it) }
        .let { Base64.getEncoder().encodeToString(it) }

    private fun hash(password: String, salt: String): String =
        MessageDigest.getInstance("SHA-256").digest((salt + password).toByteArray())
            .let { Base64.getEncoder().encodeToString(it) }
}
