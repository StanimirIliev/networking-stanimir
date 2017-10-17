package com.clouway.clientsinfo

import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

open class Server(val port: Int) : Runnable {

    private inner class Client(val pipe: BlockingQueue<String>, val socket: Socket, val number: Int, val server: Server) {
        fun start() {
            pipe.put("You are client #$number\n")
            Thread(Writer(this)).start()
            Thread(Reader(this, server)).start()
        }
    }

    private inner class Reader(val client: Client, val server: Server) : Runnable {
        override fun run() {
            try {
                val inputStream = client.socket.getInputStream()
                val reader = inputStream.bufferedReader(Charset.defaultCharset())
                while (true) {
                    val content = reader.readLine()
                    server.dispatchToAll(content, client.number)
                }
            } catch (e: IOException) {

            }
        }
    }

    private inner class Writer(val client: Client) : Runnable {
        override fun run() {
            try {
                val outputStream = client.socket.getOutputStream()
                while (true) {
                    val msg = client.pipe.poll() ?: continue
                    outputStream.write(msg.toByteArray(Charset.defaultCharset()))
                    outputStream.flush()
                }
            } catch (e: IOException) {

            }
        }
    }

    private val clients = ArrayList<Client>()
    private lateinit var serverSocket: ServerSocket

    override fun run() {
        serverSocket = ServerSocket(port)
        while (true) {
            try {
                val newClientSocket = serverSocket.accept()
                val newClient = Client(
                        ArrayBlockingQueue<String>(5),
                        newClientSocket,
                        clients.size + 1,
                        this
                )
                newClient.start()
                clients.add(newClient)
                for (client in clients.minusElement(clients.last())) {
                    client.pipe.put("Client #${clients.last().number} joined the server\n")
                }
            } catch (e: IOException) {
                break
            }
        }
    }

    open fun close() {
        try {
            for (client in clients) {
                client.socket.close()
            }
            serverSocket.close()
        } catch (e: IOException) {
            System.err.print("Server has not stop correctly")
            e.printStackTrace()
        }
    }

    private fun dispatchToAll(data: String, senderNumber: Int) {
        synchronized(clients, {
            for (client in clients) {
                client.pipe.put("[Client #$senderNumber]: $data\n")
            }
        })
    }
}