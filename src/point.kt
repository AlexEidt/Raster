data class Point(val x: Float, val y: Float) {
    operator fun plus(p: Point) = Point(x + p.x, y + p.y)
    operator fun minus(p: Point) = Point(x - p.x, y - p.y)
    operator fun times(p: Point) = Point(x * p.x, y * p.y)
    operator fun div(p: Point) = Point(x / p.x, y / p.y)

    operator fun plus(s: Float) = Point(x + s, y + s)
    operator fun minus(s: Float) = Point(x - s, y - s)
    operator fun times(s: Float) = Point(x * s, y * s)
    operator fun div(s: Float) = Point(x / s, y / s)
}