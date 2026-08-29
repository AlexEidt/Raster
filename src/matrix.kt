import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

data class Matrix(
    val m00: Float, val m01: Float, val m02: Float, val m03: Float,
    val m10: Float, val m11: Float, val m12: Float, val m13: Float,
    val m20: Float, val m21: Float, val m22: Float, val m23: Float,
    val m30: Float, val m31: Float, val m32: Float, val m33: Float
) {

    operator fun times(m: Matrix) = Matrix(
        m00 * m.m00 + m01 * m.m10 + m02 * m.m20 + m03 * m.m30,
        m00 * m.m01 + m01 * m.m11 + m02 * m.m21 + m03 * m.m31,
        m00 * m.m02 + m01 * m.m12 + m02 * m.m22 + m03 * m.m32,
        m00 * m.m03 + m01 * m.m13 + m02 * m.m23 + m03 * m.m33,

        m10 * m.m00 + m11 * m.m10 + m12 * m.m20 + m13 * m.m30,
        m10 * m.m01 + m11 * m.m11 + m12 * m.m21 + m13 * m.m31,
        m10 * m.m02 + m11 * m.m12 + m12 * m.m22 + m13 * m.m32,
        m10 * m.m03 + m11 * m.m13 + m12 * m.m23 + m13 * m.m33,

        m20 * m.m00 + m21 * m.m10 + m22 * m.m20 + m23 * m.m30,
        m20 * m.m01 + m21 * m.m11 + m22 * m.m21 + m23 * m.m31,
        m20 * m.m02 + m21 * m.m12 + m22 * m.m22 + m23 * m.m32,
        m20 * m.m03 + m21 * m.m13 + m22 * m.m23 + m23 * m.m33,

        m30 * m.m00 + m31 * m.m10 + m32 * m.m20 + m33 * m.m30,
        m30 * m.m01 + m31 * m.m11 + m32 * m.m21 + m33 * m.m31,
        m30 * m.m02 + m31 * m.m12 + m32 * m.m22 + m33 * m.m32,
        m30 * m.m03 + m31 * m.m13 + m32 * m.m23 + m33 * m.m33
    )

    operator fun times(v: Vector) = Vector(
        m00 * v.x + m01 * v.y + m02 * v.z + m03,
        m10 * v.x + m11 * v.y + m12 * v.z + m13,
        m20 * v.x + m21 * v.y + m22 * v.z + m23
    )

    operator fun times(t: Tensor) = Tensor(
        m00 * t.x + m01 * t.y + m02 * t.z + m03 * t.w,
        m10 * t.x + m11 * t.y + m12 * t.z + m13 * t.w,
        m20 * t.x + m21 * t.y + m22 * t.z + m23 * t.w,
        m30 * t.x + m31 * t.y + m32 * t.z + m33 * t.w
    )

    fun Transpose() = Matrix(
        m00, m10, m20, m30,
        m01, m11, m21, m31,
        m02, m12, m22, m32,
        m03, m13, m23, m33
    )

    companion object {

        fun Identity() = Matrix(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )

        fun Orthographic(
            left: Float, right: Float,
            bottom: Float, top: Float,
            near: Float, far: Float
        ) = Matrix(
            2f / (right - left), 0f, 0f, -(right + left) / (right - left),
            0f, 2f / (top - bottom), 0f, -(top + bottom) / (top - bottom),
            0f, 0f, -2f / (far - near), -(far + near) / (far - near),
            0f, 0f, 0f, 1f
        )

        fun Perspective(fov: Float, aspect: Float, near: Float, far: Float): Matrix {
            val radians = fov * Math.PI.toFloat() / 180f
            val f = 1f / tan(radians / 2f)

            return Matrix(
                f / aspect, 0f, 0f, 0f,
                0f, f, 0f, 0f,
                0f, 0f, (far + near) / (near - far), (2f * far * near) / (near - far),
                0f, 0f, -1f, 0f
            )
        }

        fun Translate(v: Vector) = Matrix(
                1f, 0f, 0f, v.x,
                0f, 1f, 0f, v.y,
                0f, 0f, 1f, v.z,
                0f, 0f, 0f, 1f
        )

        fun Scale(v: Vector) = Matrix(
                v.x, 0f, 0f, 0f,
                0f, v.y, 0f, 0f,
                0f, 0f, v.z, 0f,
                0f, 0f, 0f, 1f
        )

        fun Rotate(axis: Vector, angle: Float): Matrix {
            val radians = angle * Math.PI.toFloat() / 180f
            val a = axis.Normalized()
            val c = cos(radians)
            val s = sin(radians)
            val t = 1f - c

            return Matrix(
                t * a.x * a.x + c,       t * a.x * a.y - s * a.z, t * a.x * a.z + s * a.y, 0f,
                t * a.x * a.y + s * a.z, t * a.y * a.y + c,       t * a.y * a.z - s * a.x, 0f,
                t * a.x * a.z - s * a.y, t * a.y * a.z + s * a.x, t * a.z * a.z + c,       0f,
                0f,                       0f,                       0f,                       1f
            )
        }

        fun LookAt(eye: Vector, target: Vector, up: Vector): Matrix {
            val f = (target - eye).Normalized()
            val s = f.Cross(up).Normalized()
            val u = s.Cross(f)

            return Matrix(
                s.x,  s.y,  s.z,  -s.Dot(eye),
                u.x,  u.y,  u.z,  -u.Dot(eye),
                -f.x, -f.y, -f.z, f.Dot(eye),
                0f,   0f,   0f,   1f
            )
        }
    }
}