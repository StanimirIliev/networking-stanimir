import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

open class DownloadAgent {

    lateinit var progress: ProgressUpdate

    init{
        setupProgressUpdate()
    }

    open fun setupProgressUpdate(){
        progress = ProgressUpdateImpl()
    }

    open fun downloadFile(url: String, destination: String){
        downloadFile(URL(url).openStream(), FileOutputStream(destination))
    }

    fun downloadFile(inputStream: InputStream, destination: FileOutputStream) {
        val allBytes = inputStream.available()
        val bis = BufferedInputStream(inputStream)
        var count: Int
        var downloadedBytes = 0
        val buffer = ByteArray(1024)
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