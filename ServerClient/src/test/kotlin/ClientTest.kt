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
        server.startAsync()
        server.awaitRunning()
        client.connect()
    }

    @Test
    fun readMessage() {
        val client = (object: Client("127.0.0.1", 9000){
            override fun readData() {
                val data = InputStreamReader(socket.getInputStream()).readText()
                assertThat(data, `is`(equalTo("Hello")))
            }
        })
        val server = object: Server(9000) {
            override fun run() {
                clientSocket = serverSocket.accept()
                output = PrintWriter(clientSocket.getOutputStream())
                output.print("Hello")
                output.close()
            }
        }
        server.startAsync()
        client.connect()
        client.readData()
    }
}