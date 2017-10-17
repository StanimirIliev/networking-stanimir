package com.clouway.clientsinfo

import java.net.ConnectException
import java.net.Socket
import java.nio.charset.Charset
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.io.*


open class Client(val host: String, val port: Int): Runnable {

    private inner class ConsoleInput(val pipe: BlockingQueue<String>) : Runnable {
        override fun run() {
            while (true) {
                try {
                    pipe.put(readLine())
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }

    private inner class Reader(val stream: InputStream) : Runnable {
        override fun run() {
            val reader = stream.bufferedReader(Charset.defaultCharset())
            while (true) {
                try {
                    println(reader.readLine())
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }

    private inner class Writer(val pipe: BlockingQueue<String>, val stream: OutputStream) : Runnable {
        override fun run() {
            while (true) {
                try {
                    val msg = pipe.poll() ?: continue
                    stream.write((msg + "\n").toByteArray())
                    stream.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }

    private lateinit var clientSocket: Socket

    override fun run() {
        println("Waiting the server to setup...")
        while (true) {
            try {
                clientSocket = Socket(host, port)
                break
            } catch (e: ConnectException) {

            }
        }
        println("Connected to $host:$port")
        val pipe = ArrayBlockingQueue<String>(5)
        Thread(Reader(clientSocket.getInputStream())).start()
        Thread(Writer(pipe, clientSocket.getOutputStream())).start()
        Thread(ConsoleInput(pipe)).start()
    }
}