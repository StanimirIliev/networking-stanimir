import com.google.common.util.concurrent.AbstractExecutionThreadService
import java.io.IOException
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.Socket

open class Client(val host: String, val port: Int) {

    private val prefix = "\u001B[33m"// yellow
    lateinit var socket: Socket

    open fun connect() {
        print("[Client]\tWaiting for server to setup")
        while(true){
            try{
                socket = Socket(host, port)
                break
            }
            catch(e: ConnectException){

            }
        }
        print("[Client]\tConnected to $host:$port")
    }

    open fun readData() {
        val data = InputStreamReader(socket.getInputStream()).readText()
        print("[Client]\tData red from server:\t$data")
    }

    fun close(){
        try{
            socket.close()
            print("[Client]\tClosed")
        }
        catch(e: IOException){
            print("[Client]\tError occurred while shutting down")
        }
    }

    private fun print(str: String){
        println(prefix + str)
    }
}