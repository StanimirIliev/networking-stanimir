import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.InputStream

open class DownloadAgentImpl(val inputStream: InputStream,
                        val destination: FileOutputStream,
                        val progress: ProgressUpdate): DownloadAgent {

    override fun downloadFile() {
        val allBytes = inputStream.available()
        val bis = BufferedInputStream(inputStream)
        var count: Int
        var downloadedBytes = 0
        var buffer = ByteArray(1024)
        do{
            count = bis.read(buffer, 0, 1024)
            if(count == -1){
                break
            }
            downloadedBytes += count
            destination.write(buffer, 0, count)
            progress.printProgress((downloadedBytes.toDouble() / allBytes) * 100)
        } while(true)
        println("\nDone!")
    }
}