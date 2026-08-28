data class Triangle(
    var v1: Vertex = Vertex(),
    var v2: Vertex = Vertex(),
    var v3: Vertex = Vertex()
) {
    fun Apply(shader: Shader) = RasterTriangle(
        this,
        shader.Vertex(v1),
        shader.Vertex(v2),
        shader.Vertex(v3)
    )

    fun Normal() = (v2.position - v1.position).Cross(v3.position - v1.position).Normalized()

    fun Transform(matrix: Matrix) {
        v1.position = matrix * v1.position
        v2.position = matrix * v2.position
        v3.position = matrix * v3.position

        // No translation for normals, only direction transform
        v1.normal = (matrix * Tensor(v1.normal, 0f)).Vector().Normalized()
        v2.normal = (matrix * Tensor(v2.normal, 0f)).Vector().Normalized()
        v3.normal = (matrix * Tensor(v3.normal, 0f)).Vector().Normalized()
    }
}

data class RasterTriangle(
    val triangle: Triangle,
    var t1: Tensor,
    var t2: Tensor,
    var t3: Tensor
) {
    fun DeepCopy() = RasterTriangle(
        Triangle(triangle.v1.copy(), triangle.v2.copy(), triangle.v3.copy()),
        t1,
        t2,
        t3
    )

    fun Clip(plane: Plane): Array<RasterTriangle> {
        data class Pair(
            val tensor: Tensor,
            val vertex: Vertex
        )

        val inside = mutableListOf<Pair>()
        val outside = mutableListOf<Pair>()

        if (plane.Front(t1)) inside.add(Pair(t1, triangle.v1)) else outside.add(Pair(t1, triangle.v1))
        if (plane.Front(t2)) inside.add(Pair(t2, triangle.v2)) else outside.add(Pair(t2, triangle.v2))
        if (plane.Front(t3)) inside.add(Pair(t3, triangle.v3)) else outside.add(Pair(t3, triangle.v3))

        return when (inside.size) {
            0 -> emptyArray()

            1 -> {
                val clipped = DeepCopy()
                val (x1, a) = plane.Intersect(inside[0].tensor, outside[0].tensor)
                val (x2, b) = plane.Intersect(inside[0].tensor, outside[1].tensor)

                clipped.t1 = inside[0].tensor
                clipped.t2 = x1
                clipped.t3 = x2

                clipped.triangle.v1 = inside[0].vertex
                clipped.triangle.v2 = inside[0].vertex.Interpolate(outside[0].vertex, a)
                clipped.triangle.v3 = inside[0].vertex.Interpolate(outside[1].vertex, b)

                arrayOf(clipped)
            }

            2 -> {
                val clipped1 = DeepCopy()
                val clipped2 = DeepCopy()

                val (x1, a) = plane.Intersect(inside[0].tensor, outside[0].tensor)
                val (x2, b) = plane.Intersect(inside[1].tensor, outside[0].tensor)

                clipped1.t1 = inside[0].tensor
                clipped1.t2 = inside[1].tensor
                clipped1.t3 = x1

                clipped2.t1 = inside[1].tensor
                clipped2.t2 = x1
                clipped2.t3 = x2

                val vertex1 = inside[0].vertex.Interpolate(outside[0].vertex, a)
                val vertex2 = inside[1].vertex.Interpolate(outside[0].vertex, b)

                clipped1.triangle.v1 = inside[0].vertex
                clipped1.triangle.v2 = inside[1].vertex
                clipped1.triangle.v3 = vertex1

                clipped2.triangle.v1 = inside[1].vertex
                clipped2.triangle.v2 = vertex1
                clipped2.triangle.v3 = vertex2

                arrayOf(clipped1, clipped2)
            }

            3 -> arrayOf(this)

            else -> emptyArray()
        }
    }
}