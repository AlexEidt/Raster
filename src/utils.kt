fun Parallel(func: (cpu: Int, cpus: Int) -> Unit) {
    val cpus = Runtime.getRuntime().availableProcessors()
    val threads = Array(cpus) {
        Thread { func(it, cpus) }
    }

    for (thread in threads)
        thread.start()

    try {
        for (thread in threads) thread.join()
    } catch (ie: InterruptedException) {
        Thread.currentThread().interrupt()
    }
}