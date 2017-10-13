import com.google.common.util.concurrent.AbstractExecutionThreadService
import java.io.IOException
import java.io.PrintWriter
import java.net.PortUnreachableException
import java.net.ServerSocket
import java.net.Socket
import java.time.LocalDate
import java.time.LocalTime

open class Server(val port:Int): AbstractExecutionThreadService() {

    private val prefix = "\u001B[34m"// blue
    lateinit var serverSocket: ServerSocket
    lateinit var output: PrintWriter
    lateinit var clientSocket: Socket
    lateinit var client: AbstractExecutionThreadService

    public override fun startUp() {
        try{
            serverSocket = ServerSocket(port)
            print("[Server]\tStart listening on port $port")
        }
        catch(e: IOException){
            print("[Server]\tCannot listen to port $port")
            throw PortUnreachableException("Cannot listen to port $port")
        }
    }

    override fun run() {
        clientSocket = serverSocket.accept()
        print("[Server]\tConnection is established")
        print("[Server]\tSending greeter message")
        output = PrintWriter(clientSocket.getOutputStream())
        output.print("Hello\tDate: ${LocalDate.now()}, Time: ${LocalTime.now()}")
        output.close()
        client.awaitRunning()
    }

    override fun shutDown() {
        try{
            output.close()
            clientSocket.close()
            serverSocket.close()
            print("[Server]\tClosed")
        }
        catch(e: IOException){
            print("[Server]\tError occurred while shutting down")
        }
    }

    fun addClient(client: Client){
        this.client = client
    }

    private fun print(str: String){
        println(prefix + str)
    }
}