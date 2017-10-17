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
        val client = object : Client(defaultHost, defaultPort) {
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
        val client = object : Client(defaultHost, defaultPort) {
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
        val client = object : Client(defaultHost, defaultPort) {
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
    }

    @Test
    fun readDataFromClient() {

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
    }

    @Test
    fun sendDataToAllClients() {
        val client1 = object : Client(defaultHost, defaultPort) {
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
        val client2 = object : Client(defaultHost, defaultPort) {
            private inner class Reader(val stream: InputStream) : Runnable {
                override fun run() {
                    val reader = stream.bufferedReader(Charset.defaultCharset())
                    while (true) {
                        try {
                            val msg = reader.readLine()
                            assertThat(msg, `is`(equalTo("You are client #2")))
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
        client1.run()
        client2.run()
    }
}