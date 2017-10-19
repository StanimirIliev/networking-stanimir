package com.clouway.clientsinfo

import junit.framework.Assert.fail
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ConnectException
import java.net.Socket
import java.nio.charset.Charset

class ServerTest {

    private val defaultPort = 9000
    private val defaultHost = "127.0.0.1"
    private val server = Server(defaultPort)

    @Before
    fun startServer() {
        Thread(server).start()
    }

    @After
    fun closeServer() {
        server.close()
    }

    @Test
    fun establishConnection() {
        val client = object : Client(defaultHost, defaultPort, ConsoleDisplay()) {
            override fun run() {
                while (true) {
                    try {
                        val socket = Socket(host, port)
                        assertThat(socket.isConnected, `is`(equalTo(true)))
                        break
                    } catch (e: IOException) {

                    }
                }
            }
        }
        client.run()
    }

    @Test
    fun establishConnectionWithManyClients() {
        val client = object : Client(defaultHost, defaultPort, ConsoleDisplay()) {
            override fun run() {
                while (true) {
                    try {
                        val socket = Socket(host, port)
                        assertThat(socket.isConnected, `is`(equalTo(true)))
                        break
                    } catch (e: IOException) {

                    }
                }
            }
        }
        client.run()
        client.run()
        client.run()
        client.run()
    }

    @Test
    fun sendDataToClient() {
        val fakeDisplay = object : Display {
            val content = ArrayList<String>()
            override fun print(content: String?) {
                if (content != null) {
                    this.content.add(content)
                }
            }
        }
        object : Client(defaultHost, defaultPort, fakeDisplay) {
            private inner class Reader(val stream: InputStream) {
                init {
                    val reader = stream.bufferedReader(Charset.defaultCharset())
                    try {
                        display.print(reader.readLine())

                    } catch (e: IOException) {
                        fail("Exception has been thrown")
                        e.printStackTrace()
                    }
                }
            }

            private lateinit var clientSocket: Socket

            override fun run() {
                display.print("Waiting the server to setup...")
                while (true) {
                    try {
                        clientSocket = Socket(host, port)
                        break
                    } catch (e: ConnectException) {

                    }
                }
                display.print("Connected to $host:$port")
                Reader(clientSocket.getInputStream())
            }
        }.run()
        assertThat(fakeDisplay.content.contains("You are client #1"), `is`(equalTo(true)))
    }

    @Test
    fun readDataFromClient() {
        val fakeDisplay = object : Display {
            val content = ArrayList<String>()
            override fun print(content: String?) {
                if (content != null) {
                    this.content.add(content)
                }
            }
        }
        object : Client(defaultHost, defaultPort, fakeDisplay) {
            private inner class Reader(val stream: InputStream) {
                init {
                    val reader = stream.bufferedReader(Charset.defaultCharset())
                    try {
                        display.print(reader.readLine())
                        display.print(reader.readLine())

                    } catch (e: IOException) {
                        fail("Exception has been thrown")
                        e.printStackTrace()
                    }
                }
            }

            private inner class Writer(val stream: OutputStream) {
                init {
                    try {
                        stream.write(("Hi" + "\n").toByteArray())
                        stream.flush()
                    } catch (e: IOException) {
                        fail("Exception has been thrown")
                        e.printStackTrace()
                    }
                }
            }

            private lateinit var clientSocket: Socket

            override fun run() {
                display.print("Waiting the server to setup...")
                while (true) {
                    try {
                        clientSocket = Socket(host, port)
                        break
                    } catch (e: ConnectException) {

                    }
                }
                display.print("Connected to $host:$port")
                Writer(clientSocket.getOutputStream())
                Reader(clientSocket.getInputStream())
            }
        }.run()
        assertThat(fakeDisplay.content.contains("[Client #1]: Hi"), `is`(equalTo(true)))
    }

    @Test
    fun sendDataToAllClients() {
        val fakeDisplay = object : Display {
            val content = ArrayList<String>()
            override fun print(content: String?) {
                if (content != null) {
                    this.content.add(content)
                }
            }
        }
        val client1 = object : Client(defaultHost, defaultPort, fakeDisplay) {
            private inner class Reader(val stream: InputStream) {
                init {
                    val reader = stream.bufferedReader(Charset.defaultCharset())
                    try {
                        val msg = reader.readLine()
                        display.print(msg)
                    } catch (e: IOException) {
                        fail("Exception has been thrown")
                        e.printStackTrace()
                    }
                }
            }

            override fun run() {
                while (true) {
                    try {
                        val socket = Socket(host, port)
                        Reader(socket.getInputStream())
                        break
                    } catch (e: IOException) {

                    }
                }
            }
        }
        val client2 = object : Client(defaultHost, defaultPort, fakeDisplay) {
            private inner class Reader(val stream: InputStream) {
                init {
                    val reader = stream.bufferedReader(Charset.defaultCharset())
                    try {
                        val msg = reader.readLine()
                        display.print(msg)//assertThat(msg, `is`(equalTo("You are client #2")))
                    } catch (e: IOException) {
                        fail("Exception has been thrown")
                        e.printStackTrace()
                    }
                }
            }

            override fun run() {
                while (true) {
                    try {
                        val socket = Socket(host, port)
                        Reader(socket.getInputStream())
                        break
                    } catch (e: IOException) {

                    }
                }
            }
        }
        client1.run()
        client2.run()
        assertThat(fakeDisplay.content.contains("You are client #1"), `is`(equalTo(true)))
        assertThat(fakeDisplay.content.contains("You are client #2"), `is`(equalTo(true)))
    }
}