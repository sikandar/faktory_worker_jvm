package me.carlosribeiro.kotlin.faktory_worker

import io.mockk.every
import io.mockk.mockkClass
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.Rule
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.Socket

class FaktoryClientTest {
    private val client = FaktoryClient()
    private val mockedSocket = mockkClass(Socket::class)

    @Rule @JvmField
    val environmentVariables = EnvironmentVariables()

    @Test
    fun initializeClientWithoutUriAndWithoutEnvVars() {
        Assert.assertEquals("tcp://localhost:7419", client.uri.toString())
    }

    @Test
    fun initializeClientWithoutUriButWithEnvVars() {
        environmentVariables.set("FAKTORY_URL", "tcp://192.168.0.2:7419")
        val anotherClient = FaktoryClient()

        Assert.assertEquals("tcp://192.168.0.2:7419", anotherClient.uri.toString())
    }

    @Test
    fun initializeClientWithACustomUri() {
        val client = FaktoryClient("tcp://192.168.0.1:7419")

        Assert.assertEquals("tcp://192.168.0.1:7419", client.uri.toString())
    }

    @Test
    fun whenFaktorySendsHiClientShouldSendHello() {
        val outFromServer = ByteArrayOutputStream()
        val faktoryResponse = ByteArrayInputStream("+HI {\"v\":2}".toByteArray())
        every { mockedSocket.getInputStream() } returns faktoryResponse
        every { mockedSocket.getOutputStream() } returns outFromServer
        client.socket = mockedSocket

        client.connect()

        Assert.assertThat(outFromServer.toString(), CoreMatchers.containsString("HELLO"))
    }

    @Test(expected = FaktoryConnectionError::class)
    fun raisesErrorWhenFaktoryDoesNotSendHi() {
        val outFromServer = ByteArrayOutputStream()
        val faktoryResponse = ByteArrayInputStream("NOT HI".toByteArray())
        every { mockedSocket.getInputStream() } returns faktoryResponse
        every { mockedSocket.getOutputStream() } returns outFromServer
        client.socket = mockedSocket

        client.connect()
    }
}