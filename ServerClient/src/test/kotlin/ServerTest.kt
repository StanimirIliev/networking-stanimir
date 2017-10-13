import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.InputStreamReader
import java.net.PortUnreachableException
import java.net.ServerSocket
import java.net.Socket
import java.time.LocalDate
import java.time.LocalTime

class ServerTest {

    @Test
    fun establishConnection() {
        val server = Server(9000)
        val client = (object: Client("",1){
            override fun run() {
                server.awaitRunning()
                val socket = Socket("127.0.0.1", 9000)
                assertThat(socket.isConnected, `is`(equalTo(true)))
            }
        })
        server.addClient(client)
        server.startAsync()
        client.startAsync()
    }

    @Test
    fun readMessage() {
        val server = Server(9000)
        val client = (object: Client("127.0.0.1",9000){
            override fun run() {
                server.awaitRunning()
                val socket = Socket(host, port)
                val data = InputStreamReader(socket.getInputStream()).readText()
                assertThat(data, `is`(equalTo("Hello\tDate: ${LocalDate.now()}, Time: ${LocalTime.now()}")))
            }
        })
        server.addClient(client)
        server.startAsync()
        client.startAsync()
    }

    @Test(expected = PortUnreachableException::class)
    fun busyPort(){
        val server = Server(443)
        server.startUp()
    }
}