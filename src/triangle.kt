data class Triangle(
    var v1: Vertex = Vertex(),
    var v2: Vertex = Vertex(),
    var v3: Vertex = Vertex()
) {
    fun Normal(): Vector {
        return (v2.position - v1.position).Cross(v3.position - v1.position).Normalized()
    }

    fun Interpolate(vector: Vector): Vertex {
        val v = Vertex()
        v.position = v1.position * vector.x + v2.position * vector.y + v3.position * vector.z
        v.normal = (v1.normal * vector.x + v2.normal * vector.y + v3.normal * vector.z).Normalized()
        v.texture = v1.texture * vector.x + v2.texture * vector.y + v3.texture * vector.z
        v.color = v1.color * vector.x + v2.color * vector.y + v3.color * vector.z

        return v
    }
}

data class RasterTriangle(
    val triangle: Triangle,
    var t1: Tensor,
    var t2: Tensor,
    var t3: Tensor
) {
    fun Clip(): Array<RasterTriangle> {
        val near = Plane(
            Tensor(0f, 0f, -1f, 1f),
            Tensor(0f, 0f, 1f, 1f)
        )

        val inside = mutableListOf<Tensor>()
        val outside = mutableListOf<Tensor>()

        if (near.Front(t1)) inside.add(t1) else outside.add(t1)
        if (near.Front(t2)) inside.add(t2) else outside.add(t2)
        if (near.Front(t3)) inside.add(t3) else outside.add(t3)

        return when (inside.size) {
            0 -> emptyArray()

            1 -> {
                val clipped = copy()
                clipped.t1 = inside[0]
                clipped.t2 = near.Intersect(inside[0], outside[0])
                clipped.t3 = near.Intersect(inside[0], outside[1])

                val v1 = t1.Vector()
                val v2 = t2.Vector()
                val v3 = t3.Vector()

                clipped.triangle.v2 = triangle.Interpolate(Barycentric(v1, v2, v3, clipped.t2.Vector()))
                clipped.triangle.v3 = triangle.Interpolate(Barycentric(v1, v2, v3, clipped.t3.Vector()))

                arrayOf(clipped)
            }

            2 -> {
                val clipped1 = copy()
                val clipped2 = copy()

                val x1 = near.Intersect(inside[0], outside[0])
                val x2 = near.Intersect(inside[1], outside[0])

                clipped1.t1 = inside[0]
                clipped1.t2 = inside[1]
                clipped1.t3 = x1

                clipped2.t1 = inside[1]
                clipped2.t2 = x1
                clipped2.t3 = x2

                val v1 = t1.Vector()
                val v2 = t2.Vector()
                val v3 = t3.Vector()

                clipped1.triangle.v3 = triangle.Interpolate(Barycentric(v1, v2, v3, x1.Vector()))
                clipped2.triangle.v2 = clipped1.triangle.v3
                clipped2.triangle.v3 = triangle.Interpolate(Barycentric(v1, v2, v3, x2.Vector()))

                arrayOf(clipped1, clipped2)
            }

            3 -> arrayOf(this)

            else -> emptyArray()
        }
    }
}