package cl.duoc.mastermartini.data.repository

import java.security.MessageDigest

/** Cuenta de demostración local: no equivale a autenticación empresarial. */
object DemoAdminAccount {
    const val EMAIL = "admin@gmail.com"
    private const val DEMO_SALT = "mastermartini-ep2:"
    const val SEEDED_SALT = "mm_ep2_preconfigured_admin_v2"
    const val SEEDED_HASH = "toXIOhuSS3C0ZAfJ8sOQi16shaWiBtK6JIMUlKqdQWU="
    private const val PASSWORD_SHA256 = "d4ee0b4af0fcfdbce3e4c3353dcd08c603daa90fb054ec9a448cc02572985d6f"

    fun isReservedEmail(email: String): Boolean = email.trim().equals(EMAIL, ignoreCase = true)

    fun isSeededAccount(salt: String, passwordHash: String): Boolean =
        salt == SEEDED_SALT && passwordHash == SEEDED_HASH

    fun matches(email: String, password: String): Boolean {
        if (!isReservedEmail(email)) return false
        val digest = MessageDigest.getInstance("SHA-256")
            .digest((DEMO_SALT + password).toByteArray(Charsets.UTF_8))
        val expected = PASSWORD_SHA256.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        return MessageDigest.isEqual(digest, expected)
    }
}
