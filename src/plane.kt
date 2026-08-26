data class Plane(val p: Tensor, val n: Tensor) {
    fun Front(v: Tensor): Boolean {
        return (v - p).Dot(n) > 0f
    }

    fun Intersect(v0: Tensor, v1: Tensor): Pair<Tensor, Float> {
        val u = v1 - v0
        val w = v0 - p
        val d = n.Dot(u)
        val t = -n.Dot(w) / d

        return v0 + u * t to t
    }
}