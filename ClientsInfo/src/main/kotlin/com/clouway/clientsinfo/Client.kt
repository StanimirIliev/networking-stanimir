package com.clouway.clientsinfo

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ConnectException
import java.net.Socket
import java.nio.charset.Charset
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue


open class Client(val host: String, val port: Int, val display: Display) : Runnable {

    private inner class ConsoleInput(val pipe: BlockingQueue<String>) : Runnable {
        override fun run() {
            while (true) {
                try {
                    pipe.put(readLine())
                } catch (e: IOException) {
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
                    display.print(reader.readLine())
                } catch (e: IOException) {
                    break
                }
            }
        }
    }

    private inner class Writer(val pipe: BlockingQueue<String>, val stream: OutputStream) : Runnable {
        override fun run() {
            try {
                while (true) {
                    val msg = pipe.poll() ?: continue
                    stream.write((msg + "\n").toByteArray())
                    stream.flush()
                }
            } catch (e: IOException) {

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
        val pipe = ArrayBlockingQueue<String>(5)
        Thread(Reader(clientSocket.getInputStream())).start()
        Thread(ConsoleInput(pipe)).start()
        Thread(Writer(pipe, clientSocket.getOutputStream())).start()
    }
}