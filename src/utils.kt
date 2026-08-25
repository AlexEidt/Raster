fun Parallel(func: (cpu: Int, cpus: Int) -> Unit) {
    val cpus = Runtime.getRuntime().availableProcessors()
    val threads = Array(cpus) {
        Thread { func(it, cpus) }
    }

    for (thread in threads) thread.start()

    try {
        for (thread in threads) thread.join()
    } catch (ie: InterruptedException) {
        Thread.currentThread().interrupt()
    }
}

fun Barycentric(p1: Vector, p2: Vector, p3: Vector, p: Vector): Vector {
    val v0 = p2 - p1
    val v1 = p3 - p1
    val v2 = p - p1

    val d00 = v0.Dot(v0)
    val d01 = v0.Dot(v1)
    val d11 = v1.Dot(v1)
    val d20 = v2.Dot(v0)
    val d21 = v2.Dot(v1)

    val d = d00 * d11 - d01 * d01

    val v = (d11 * d20 - d01 * d21) / d
    val w = (d00 * d21 - d01 * d20) / d
    val u = 1f - v - w

    return Vector(u, v, w)
}