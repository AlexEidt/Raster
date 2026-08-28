import kotlin.math.pow

abstract class Shader {
    abstract fun Vertex(vertex: Vertex): Tensor
    abstract fun Fragment(vertex: Vertex): Color
}

class BasicShader(
    val matrix: Matrix,
    val color: Color
) : Shader() {
    override fun Vertex(vertex: Vertex): Tensor {
        return matrix * Tensor(vertex.position, 1f)
    }

    override fun Fragment(vertex: Vertex): Color {
        return color
    }
}

class TextureShader(
    val matrix: Matrix,
    val texture: Image,
    val color: Color
) : Shader() {
    override fun Vertex(vertex: Vertex): Tensor {
        return matrix * Tensor(vertex.position, 1f)
    }

    override fun Fragment(vertex: Vertex): Color {
        return texture.BilinearSample(vertex.texture) * color
    }
}

class PhongShader(
    private val matrix: Matrix,
    private val light: Vector = Vector(3f, 4f, 3f),
    private val color: Color = Color(1f, 1f, 1f)
) : Shader() {
    override fun Vertex(vertex: Vertex): Tensor {
        return matrix * Tensor(vertex.position, 1f)
    }

    override fun Fragment(vertex: Vertex): Color {
        val position = vertex.position
        val normal = vertex.normal.Normalized()

        val lightDirection = (light - position).Normalized()
        val diffuse = normal.Dot(lightDirection).coerceAtLeast(0f)

        val lighting = 0.1f + diffuse

        return Color(
            (color.r * lighting).coerceAtMost(1f),
            (color.g * lighting).coerceAtMost(1f),
            (color.b * lighting).coerceAtMost(1f),
            color.a
        )
    }
}