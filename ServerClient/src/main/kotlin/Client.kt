import com.google.common.util.concurrent.AbstractExecutionThreadService
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket

open class Client(val host: String, val port: Int): AbstractExecutionThreadService() {

    private val prefix = "\u001B[33m"// yellow
    lateinit var server: AbstractExecutionThreadService
    lateinit var socket: Socket

    override fun startUp() {
        print("[Client]\tWaiting for server to setup")
        server.awaitRunning()
        socket = Socket(host, port)
        print("[Client]\tConnected to $host:$port")
        val data = InputStreamReader(socket.getInputStream()).readText()
        print("[Client]\tData red from server:\t$data")
    }

    override fun run() {
    }

    override fun shutDown() {
        try{
            server.stopAsync()
            server.awaitTerminated()
            socket.close()
            this.stopAsync()
            print("[Client]\tClosed")
        }
        catch(e: IOException){
            print("[Client]\tError occurred while shutting down")
        }
    }

    fun addServer(server: Server){
        this.server = server
    }

    private fun print(str: String){
        println(prefix + str)
    }
}