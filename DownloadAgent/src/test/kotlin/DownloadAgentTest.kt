import junit.framework.Assert.fail
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.FileOutputStream


class DownloadAgentTest {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Test
    fun downloadingFileThatWasServed() {
        val originalBytes = DownloadAgent::class.java.getResourceAsStream("picture.jpg").readBytes()
        val downloadedFile = tempFolder.newFile("downloadedPicture.jpg")
        val downloadAgent = DownloadAgent()
        downloadAgent.downloadFile(DownloadAgent::class.java.getResourceAsStream("picture.jpg"),
                FileOutputStream(downloadedFile))
        assertThat(downloadedFile.readBytes(), `is`(equalTo(originalBytes)))
    }

    @Test
    fun progressUpdate() {
        val downloadedFile = tempFolder.newFile("picture.jpg")
        val progress = object : ProgressUpdate {
            override fun printProgress(progress: Double) {
                if (progress < 0.0 || progress > 100.0) {
                    fail("Progress out of range")
                }
            }
        }
        (object : DownloadAgent() {
            override fun setupProgressUpdate() {
                super.progress = progress
            }
        }).downloadFile(DownloadAgent::class.java.getResourceAsStream("picture.jpg"),
                FileOutputStream(downloadedFile))
    }
}