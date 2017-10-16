import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.jmock.AbstractExpectations
import org.jmock.Expectations
import org.jmock.integration.junit4.JUnitRuleMockery
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.FileOutputStream



class DownloadAgentTest {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Rule @JvmField
    val context = JUnitRuleMockery()

    @Test
    fun downloadingFileThatWasServed() {
        val originalBytes = DownloadAgent::class.java.getResourceAsStream("picture.jpg").readBytes()
        val downloadedFile = tempFolder.newFile("picture.jpg")
        (object: DownloadAgent(){
            override fun downloadFile(url: String, destination: String){
                super.downloadFile(DownloadAgent::class.java.getResourceAsStream("picture.jpg"),
                        FileOutputStream(downloadedFile))
            }
        }).downloadFile("", "")
        assertThat(downloadedFile.readBytes(), `is`(equalTo(originalBytes)))
    }

    @Test
    fun progressUpdate(){
        val progress = context.mock(ProgressUpdate::class.java)
        val downloadedFile = tempFolder.newFile("picture.jpg")
        context.checking(object : Expectations() {
            init {
                allowing(progress).printProgress(with(AbstractExpectations.any(Double::class.java)))
            }
        })
        (object: DownloadAgent(){
            override fun setupProgressUpdate() {
                super.progress = progress
            }
            override fun downloadFile(url: String, destination: String){
                super.downloadFile(DownloadAgent::class.java.getResourceAsStream("picture.jpg"),
                        FileOutputStream(downloadedFile))
            }
        }).downloadFile("", "")
    }
}