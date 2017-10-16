class ProgressUpdateImpl: ProgressUpdate {
    override fun printProgress(progress: Double) {
        print("\r${String.format("Downloading ... [%.2f%%]",progress)}")
    }
}