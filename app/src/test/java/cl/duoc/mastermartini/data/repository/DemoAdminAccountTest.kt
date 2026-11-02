package cl.duoc.mastermartini.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoAdminAccountTest {
    @Test fun credencialesAdminCorrectas() {
        assertTrue(DemoAdminAccount.matches("admin@gmail.com", "admin123"))
    }

    @Test fun contrasenaIncorrectaNoIniciaSesion() {
        assertFalse(DemoAdminAccount.matches("admin@gmail.com", "admin124"))
    }

    @Test fun usuarioClienteNoEsAdministrador() {
        assertFalse(DemoAdminAccount.matches("cliente@gmail.com", "admin123"))
    }

    @Test fun correoReservadoNoBastaParaElRolAdmin() {
        assertTrue(DemoAdminAccount.isReservedEmail("ADMIN@gmail.com"))
        assertFalse(DemoAdminAccount.isSeededAccount("sal-cliente", DemoAdminAccount.SEEDED_HASH))
    }
}
