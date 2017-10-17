package com.clouway.clientsinfo

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
        Thread(Client(defaultHost, defaultPort)).start()

        val server = object : Server(9000) {
            override fun run() {
                val serverSocket = ServerSocket(9000)
                val clientSocket = serverSocket.accept()
                assertThat(clientSocket.isConnected, `is`(equalTo(true)))
            }
        }
        server.run()
    }

    @Test
    fun readDataFromServer() {
        val server = Server(defaultPort)
        Thread(server).start()

        val client = object : com.clouway.clientsinfo.Client(defaultHost, defaultPort) {
            private inner class Reader(val stream: InputStream) : Runnable {
                override fun run() {
                    val reader = stream.bufferedReader(Charset.defaultCharset())
                    while (true) {
                        try {
                            val msg = reader.readLine()
                            assertThat(msg, `is`(equalTo("You are client #1")))
                            return
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

    @Test
    fun writeDataToServer() {
        val server = Server(defaultPort)
        Thread(server).start()

        val client = object : Client(defaultHost, defaultPort) {
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