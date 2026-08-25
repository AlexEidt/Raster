data class Color(var r: Float, var g: Float, var b: Float, var a: Float = 1f) {

    constructor(rgba: Int) : this(
        ((rgba shr 16) and 0xFF) / 255f,
        ((rgba shr 8) and 0xFF) / 255f,
        (rgba and 0xFF) / 255f,
        ((rgba ushr 24) and 0xFF) / 255f
    )

    operator fun plus(c: Color) = Color(r + c.r, g + c.g, b + c.b, a + c.a)
    operator fun minus(c: Color) = Color(r - c.r, g - c.g, b - c.b, a - c.a)
    operator fun times(c: Color) = Color(r * c.r, g * c.g, b * c.b, a * c.a)
    operator fun div(c: Color) = Color(r / c.r, g / c.g, b / c.b, a / c.a)

    operator fun plus(s: Float) = Color(r + s, g + s, b + s, a + s)
    operator fun minus(s: Float) = Color(r - s, g - s, b - s, a - s)
    operator fun times(s: Float) = Color(r * s, g * s, b * s, a * s)
    operator fun div(s: Float) = Color(r / s, g / s, b / s, a / s)

    fun Clamp() {
        r = r.coerceIn(0f, 1f)
        g = g.coerceIn(0f, 1f)
        b = b.coerceIn(0f, 1f)
        a = a.coerceIn(0f, 1f)
    }

    fun RGBA(): Int {
        val r = (r * 255f).toInt()
        val g = (g * 255f).toInt()
        val b = (b * 255f).toInt()
        val a = (a * 255f).toInt()
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}