package cl.duoc.mastermartini.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ApiClientTest {
    private val server = MockWebServer()
    private val tokens = MemoryTokenStorage()
    private var expired = 0
    private lateinit var client: ApiClient

    @Before
    fun setUp() {
        server.start()
        client = ApiClient(server.url("/").toString(), tokens) { expired++ }
    }

    @After
    fun tearDown() = server.shutdown()

    private fun json(body: String, code: Int = 200) =
        MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json").setBody(body)

    @Test
    fun agregaElTokenYLoRenuevaUnaVezSiVence() = runBlocking {
        tokens.save("viejo", "refresh-1")
        server.enqueue(json("""{"message":"Token vencido"}""", 401))
        server.enqueue(json("""{"accessToken":"nuevo","expiresIn":900,"refreshToken":"refresh-2","usuario":{"id":1,"nombre":"Ana","email":"a@b.cl","rol":"CLIENTE"}}"""))
        server.enqueue(json("[]"))

        val pedidos = client.llamar { misPedidos() }

        assertTrue(pedidos.isEmpty())
        assertEquals("Bearer viejo", server.takeRequest().getHeader("Authorization"))
        val refresh = server.takeRequest()
        assertEquals("/api/v1/auth/refresh", refresh.path)
        assertTrue(refresh.body.readUtf8().contains("refresh-1"))
        assertEquals("Bearer nuevo", server.takeRequest().getHeader("Authorization"))
        assertEquals("refresh-2", tokens.refreshToken())
        assertEquals(0, expired)
    }

    @Test
    fun cierraLaSesionSiElRefreshFueRevocado() = runBlocking {
        tokens.save("viejo", "revocado")
        server.enqueue(json("""{"message":"Token vencido"}""", 401))
        server.enqueue(json("""{"message":"Sesión inválida"}""", 401))

        try {
            client.llamar { misPedidos() }
            fail("Debía lanzar ApiException")
        } catch (e: ApiException) {
            assertEquals(401, e.status)
        }
        assertNull(tokens.refreshToken())
        assertEquals(1, expired)
    }

    @Test
    fun elLoginNoLlevaTokenNiIntentaRenovar() = runBlocking {
        tokens.save("viejo", "refresh-1")
        server.enqueue(json("""{"message":"Correo o contraseña incorrectos"}""", 401))

        try {
            client.llamar { login(LoginBody("a@b.cl", "mala")) }
            fail("Debía lanzar ApiException")
        } catch (e: ApiException) {
            assertEquals("Correo o contraseña incorrectos", e.message)
        }
        assertNull(server.takeRequest().getHeader("Authorization"))
        assertEquals(1, server.requestCount)
    }

    @Test
    fun traduceLosErroresPorCampo() = runBlocking {
        server.enqueue(json("""{"message":"Datos inválidos","fields":{"run":"Revisa este campo: RUN inválido"}}""", 400))
        try {
            client.llamar { registro(RegistroBody("Ana Pérez", "a@b.cl", "clave12345", "1-9", "+56911112222", "R", "C", "Calle 123")) }
            fail("Debía lanzar ApiException")
        } catch (e: ApiException) {
            assertEquals(400, e.status)
            assertEquals("RUN inválido", e.fields["run"])
        }
    }

    @Test
    fun urlsDeImagenes() {
        val base = server.url("/").toString()
        assertEquals(base + "assets/images/pan.jpg", client.urlImagen("/assets/images/pan.jpg"))
        assertEquals(base + "mm-media/imagenes/a.png", client.urlImagen("http://localhost:8095/mm-media/imagenes/a.png"))
        assertEquals("https://mastermartini.cl/x.jpg", client.urlImagen("https://mastermartini.cl/x.jpg"))
        assertNull(client.urlImagen(""))
    }
}
