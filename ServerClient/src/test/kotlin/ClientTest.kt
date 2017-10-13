import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.time.LocalDate
import java.time.LocalTime

class ClientTest {

    @Test
    fun establishConnection() {
        val client = Client("127.0.0.1", 9000)
        val server = (object: Server(9000){
            override fun startUp() {
                serverSocket = ServerSocket(port)
            }

            override fun run() {
                clientSocket = serverSocket.accept()
                assertThat(clientSocket.isConnected, `is`(equalTo(true)))
            }
        })
        client.addServer(server)
        client.startAsync()
        server.startAsync()
    }

    @Test
    fun readMessage() {
        val client = (object: Client("127.0.0.1", 9000){
            override fun startUp() {
                print("[Client]\tWaiting for server to setup")
                server.awaitRunning()
                socket = Socket(host, port)
                print("[Client]\tConnected to $host:$port")
                val data = InputStreamReader(socket.getInputStream()).readText()
                assertThat(data, `is`(equalTo("Hello\tDate: ${LocalDate.now()}, Time: ${LocalTime.now()}")))
            }
        })
        val server = (object: Server(9000){
            override fun startUp() {
                serverSocket = ServerSocket(port)
            }

            override fun run() {
                clientSocket = serverSocket.accept()
                output = PrintWriter(clientSocket.getOutputStream())
                output.print("Hello\tDate: ${LocalDate.now()}, Time: ${LocalTime.now()}")
                output.close()
            }
        })
        client.addServer(server)
        client.startAsync()
        server.startAsync()
    }
}