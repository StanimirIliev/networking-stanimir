open class ProgressUpdateImpl: ProgressUpdate {
    override fun printProgress(process: Double) {
        print("\r${String.format("Downloading ... [%.2f%%]",process)}")
    }
}