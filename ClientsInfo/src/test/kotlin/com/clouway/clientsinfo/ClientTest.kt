package com.clouway.clientsinfo

import com.google.common.util.concurrent.AbstractExecutionThreadService
import junit.framework.Assert.fail
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset

class ClientTest {

    private val defaultPort = 9000
    private val defaultHost = "127.0.0.1"

    @Test
    fun establishConnection() {
        val client = Thread(Client(defaultHost, defaultPort, ConsoleDisplay()))
        client.start()

        val server = object : Server(9000) {
            override fun run() {
                val serverSocket = ServerSocket(9000)
                val clientSocket = serverSocket.accept()
                assertThat(clientSocket.isConnected, `is`(equalTo(true)))
                clientSocket.close()
                serverSocket.close()
            }
        }
        server.run()
        client.interrupt()
    }

    @Test
    fun readDataFromServer() {
        val server = object : AbstractExecutionThreadService() {
            val serverSocket = ServerSocket(defaultPort)
            lateinit var outputStream: OutputStream
            lateinit var clientSocket: Socket
            override fun run() {
                clientSocket = serverSocket.accept()
                outputStream = clientSocket.getOutputStream()
                outputStream.write("Hello".toByteArray(Charset.defaultCharset()))
                outputStream.flush()
                outputStream.close()
            }

            override fun shutDown() {
                clientSocket.close()
                serverSocket.close()
            }
        }
        server.startAsync().awaitRunning()
        val fakeDisplay = object : Display {
            val content = ArrayList<String?>()
            override fun print(content: String?) {
                if (content != null) {
                    this.content.add(content)
                }
            }
        }
        val client = Client(defaultHost, defaultPort, fakeDisplay)
        client.run()
        server.stopAsync().awaitTerminated()
        assertThat(fakeDisplay.content.contains("Hello"), `is`(equalTo(true)))
    }

    @Test
    fun writeDataToServer() {
        val server = Server(defaultPort)
        Thread(server).start()

        val client = object : Client(defaultHost, defaultPort, ConsoleDisplay()) {
            private inner class Writer(val stream: OutputStream) : Runnable {
                override fun run() {
                    while (true) {
                        try {
                            val msg = "Hello"
                            stream.write((msg + "\n").toByteArray())
                            stream.flush()
                            break
                        } catch (e: IOException) {
                            e.printStackTrace()
                            fail("Exception has been thrown")
                        }
                    }
                }
            }

            private inner class Reader(val stream: InputStream) : Runnable {
                override fun run() {
                    val reader = stream.bufferedReader(Charset.defaultCharset())
                    while (true) {
                        try {
                            reader.readLine()// skip the first red line
                            assertThat(reader.readLine(), `is`(equalTo("[Client #1]: Hello")))
                            break
                        } catch (e: IOException) {
                            e.printStackTrace()
                            fail("Exception has been thrown")
                        }
                    }
                }
            }

            override fun run() {
                while (true) {
                    try {
                        val socket = Socket(host, port)
                        Writer(socket.getOutputStream()).run()
                        Reader(socket.getInputStream()).run()
                        break
                    } catch (e: IOException) {

                    }
                }
            }
        }
        client.run()
        server.close()
    }
}