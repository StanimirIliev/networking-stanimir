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



class DownloadAgentImplTest {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Rule @JvmField
    val context = JUnitRuleMockery()

    @Test
    fun downloadingFileThatWasServed() {
        val originalBytes = DownloadAgentImpl::class.java.getResourceAsStream("picture.jpg").readBytes()
        val downloadedFile = tempFolder.newFile("picture.jpg")
        DownloadAgentImpl(DownloadAgent::class.java.getResourceAsStream("picture.jpg"),
                FileOutputStream(downloadedFile), ProgressUpdateImpl()).downloadFile()
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
        DownloadAgentImpl(DownloadAgentImplTest::class.java.getResourceAsStream("picture.jpg"),
                FileOutputStream(downloadedFile), progress).downloadFile()
    }
}