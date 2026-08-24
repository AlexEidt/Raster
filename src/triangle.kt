data class Triangle(
    var v1: Vertex = Vertex(),
    var v2: Vertex = Vertex(),
    var v3: Vertex = Vertex()
) {
    fun Normal(): Vector {
        return (v2.position - v1.position).cross(v3.position - v1.position).normalized()
    }
}

data class RasterTriangle(
    val triangle: Triangle,
    val t1: Tensor,
    val t2: Tensor,
    val t3: Tensor
)