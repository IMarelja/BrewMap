package hr.algebra.mobileapp.service.auth

import hr.algebra.mobileapp.models.AuthResponse

/**
 * Contract for authentication operations.
 *
 * Two implementations are available:
 *  - [AuthServiceApi]      — real HTTP calls to the BrewMap backend
 *  - [AuthServiceHardCode] — in-memory stub with seeded users, no network needed
 *
 * Switch between them in [hr.algebra.mobileapp.service.AuthServiceProvider].
 */
interface IAuthService {
    /**
     * Attempt to sign in.
     *
     * @param user       username **or** e-mail address
     * @param password   plain-text password
     * @param rememberMe whether the server should issue a long-lived token
     */
    suspend fun login(user: String, password: String, rememberMe: Boolean): AuthResponse

    /**
     * Create a new account.
     *
     * @param email    new user's e-mail
     * @param username desired display name
     * @param password plain-text password chosen by the user
     */
    suspend fun register(email: String, username: String, password: String): AuthResponse
}
