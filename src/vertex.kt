data class Vertex(
    var position: Vector = Vector(0f, 0f, 0f),
    var normal: Vector = Vector(0f, 0f, 0f),
    var texture: Point = Point(0f, 0f),
    var color: Color = Color(0f, 0f, 0f)
) {
    fun Interpolate(other: Vertex, t: Float): Vertex {
        val s = 1f - t
        return Vertex(
            position * s + other.position * t,
            (normal * s + other.normal * t).Normalized(),
            texture * s + other.texture * t,
            color * s + other.color * t
        )
    }
}