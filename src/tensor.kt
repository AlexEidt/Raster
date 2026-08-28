data class Tensor(val x: Float, val y: Float, val z: Float, val w: Float) {
    constructor(v: Vector) : this(v.x, v.y, v.z, 0f)

    operator fun plus(t: Tensor) = Tensor(x + t.x, y + t.y, z + t.z, w + t.w)
    operator fun minus(t: Tensor) = Tensor(x - t.x, y - t.y, z - t.z, w - t.w)
    operator fun times(t: Tensor) = Tensor(x * t.x, y * t.y, z * t.z, w * t.w)
    operator fun div(t: Tensor) = Tensor(x / t.x, y / t.y, z / t.z, w / t.w)

    operator fun plus(s: Float) = Tensor(x + s, y + s, z + s, w + s)
    operator fun minus(s: Float) = Tensor(x - s, y - s, z - s, w - s)
    operator fun times(s: Float) = Tensor(x * s, y * s, z * s, w * s)
    operator fun div(s: Float) = Tensor(x / s, y / s, z / s, w / s)

    operator fun unaryMinus() = Tensor(-x, -y, -z, -w)

    fun Behind(): Boolean = z < -w

    fun Dot(v: Tensor) = x * v.x + y * v.y + z * v.z + w * v.w

    fun Vector() = Vector(x, y, z)
}