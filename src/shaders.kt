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
        return matrix * Tensor(vertex.position.x, vertex.position.y, vertex.position.z, 1f)
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
        return matrix * Tensor(vertex.position.x, vertex.position.y, vertex.position.z, 1f)
    }

    override fun Fragment(vertex: Vertex): Color {
        return texture.BilinearSample(vertex.texture) * color
    }
}