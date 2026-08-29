import kotlin.math.abs

class Context(val width: Int, val height: Int, val framebuffer: IntArray) {
    init {
        require(framebuffer.size == width * height) {
            "Framebuffer size ${framebuffer.size} does not match dimensions ${width}x${height}"
        }
    }

    private val depthbuffer = FloatArray(width * height) {
        Float.POSITIVE_INFINITY
    }

    private val locks = Array(256) { Any() }

    var shader: Shader = BasicShader(Matrix.Identity(), Color(1f, 1f, 1f))

    var wireframe = false

    fun Clear(color: Color) {
        framebuffer.fill(color.RGBA())
        depthbuffer.fill(Float.POSITIVE_INFINITY)
    }

    fun Draw(mesh: Mesh) {
        Parallel { cpu, cpus ->
            for (i in cpu until mesh.triangles.size step cpus) {
                val t = mesh.triangles[i].Apply(shader)

                // Is any vertex behind the near plane?
                if (t.t1.Behind() || t.t2.Behind() || t.t3.Behind()) {
                    val near = Plane(Tensor(0f, 0f, -1f, 1f), Tensor(0f, 0f, 1f, 1f))

                    val triangles = t.Clip(near)
                    for (triangle in triangles) {
                        val (s1, s2, s3) = Rasterize(triangle) ?: continue
                        if (wireframe)
                            Wireframe(s1, s2, s3)
                    }
                } else {
                    val (s1, s2, s3) = Rasterize(t) ?: continue
                    if (wireframe)
                        Wireframe(s1, s2, s3)
                }
            }
        }
    }

    fun Screen(t: Tensor): Vector {
        val x = t.x / t.w
        val y = t.y / t.w
        val z = t.z / t.w

        return Vector(
            x * width / 2f + width / 2f,
            y * height / 2f + height / 2f,
            z * 0.5f + 0.5f
        )
    }

    fun Rasterize(tri: RasterTriangle): Triple<Vector, Vector, Vector>? {
        val s1 = Screen(tri.t1)
        val s2 = Screen(tri.t2)
        val s3 = Screen(tri.t3)

        val v1 = tri.triangle.v1
        val v2 = tri.triangle.v2
        val v3 = tri.triangle.v3

        // Back-face culling
        val area = (s2.x - s1.x) * (s3.y - s1.y) - (s3.x - s1.x) * (s2.y - s1.y)

        if (area <= 0f) {
            return null
        }

        // Bounding box
        val minX = maxOf(0, minOf(s1.x, s2.x, s3.x).toInt())
        val maxX = minOf(width - 1, maxOf(s1.x, s2.x, s3.x).toInt())
        val minY = maxOf(0, minOf(s1.y, s2.y, s3.y).toInt())
        val maxY = minOf(height - 1, maxOf(s1.y, s2.y, s3.y).toInt())

        val inverseArea = 1f / area

        for (y in minY..maxY) {
            var wasInside = false

            for (x in minX..maxX) {
                val p = Point(x + 0.5f, y + 0.5f)

                // Barycentric
                val b0 = ((s2.y - s3.y) * (p.x - s3.x) + (s3.x - s2.x) * (p.y - s3.y)) * inverseArea
                val b1 = ((s3.y - s1.y) * (p.x - s3.x) + (s1.x - s3.x) * (p.y - s3.y)) * inverseArea
                val b2 = 1f - b0 - b1

                val outside = b0 < 0f || b1 < 0f || b2 < 0f
                if (!outside) {
                    wasInside = true
                } else if (wasInside) {
                    break
                } else {
                    continue
                }

                // Depth
                val z = b0 * s1.z + b1 * s2.z + b2 * s3.z
                val index = y * width + x

                synchronized(locks[index and 255]) {
                    if (z >= depthbuffer[index]) {
                        continue
                    }

                    depthbuffer[index] = z

                    // Perspective-correct interpolation
                    val w0 = 1f / tri.t1.w
                    val w1 = 1f / tri.t2.w
                    val w2 = 1f / tri.t3.w

                    val pw0 = b0 * w0
                    val pw1 = b1 * w1
                    val pw2 = b2 * w2

                    val sum = pw0 + pw1 + pw2
                    val weights = Vector(pw0, pw1, pw2) / sum

                    val vertex = Vertex(
                        position = v1.position * weights.x + v2.position * weights.y + v3.position * weights.z,
                        normal = (v1.normal * weights.x + v2.normal * weights.y + v3.normal * weights.z).Normalized(),
                        texture = v1.texture * weights.x + v2.texture * weights.y + v3.texture * weights.z,
                        color = v1.color * weights.x + v2.color * weights.y + v3.color * weights.z
                    )

                    framebuffer[index] = shader.Fragment(vertex).RGBA()
                }
            }
        }

        return Triple(s1, s2, s3)
    }

    fun Wireframe(s1: Vector, s2: Vector, s3: Vector) {
        Line(s1, s2)
        Line(s2, s3)
        Line(s3, s1)
    }

    private fun Line(a: Vector, b: Vector) {
        var x0 = a.x.toInt()
        var y0 = a.y.toInt()

        val x1 = b.x.toInt()
        val y1 = b.y.toInt()

        val dx = abs(x1 - x0)
        val dy = abs(y1 - y0)

        val sx = if (x0 < x1) 1 else -1
        val sy = if (y0 < y1) 1 else -1

        val length = maxOf(dx, dy)
        var step = 0

        var error = dx - dy

        while (true) {
            if (x0 in 0 until width && y0 in 0 until height) {
                val t = if (length == 0) 0f else step.toFloat() / length
                val z = a.z + (b.z - a.z) * t
                val index = y0 * width + x0

                synchronized(locks[index and 255]) {
                    if (z <= depthbuffer[index] + 0.001f) {
                        framebuffer[index] = Color(1f, 1f, 1f).RGBA()
                    }
                }
            }

            if (x0 == x1 && y0 == y1) {
                break
            }

            val e2 = 2 * error

            if (e2 > -dy) {
                error -= dy
                x0 += sx
            }

            if (e2 < dx) {
                error += dx
                y0 += sy
            }

            step++
        }
    }
}