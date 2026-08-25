import kotlin.math.sqrt

data class Vector(val x: Float, val y: Float, val z: Float) {

    operator fun plus(v: Vector) = Vector(x + v.x, y + v.y, z + v.z)
    operator fun minus(v: Vector) = Vector(x - v.x, y - v.y, z - v.z)
    operator fun times(v: Vector) = Vector(x * v.x, y * v.y, z * v.z)
    operator fun div(v: Vector) = Vector(x / v.x, y / v.y, z / v.z)

    operator fun plus(s: Float) = Vector(x + s, y + s, z + s)
    operator fun minus(s: Float) = Vector(x - s, y - s, z - s)
    operator fun times(s: Float) = Vector(x * s, y * s, z * s)
    operator fun div(s: Float) = Vector(x / s, y / s, z / s)

    operator fun unaryMinus() = Vector(-x, -y, -z)

    fun Dot(v: Vector) = x * v.x + y * v.y + z * v.z

    fun Cross(v: Vector) = Vector(
        y * v.z - z * v.y,
        z * v.x - x * v.z,
        x * v.y - y * v.x
    )

    fun Length() = sqrt(x * x + y * y + z * z)

    fun Normalized(): Vector {
        val length = Length()
        return if (length != 0f) this / length else this
    }
}