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

    fun Draw(mesh: Mesh) {
        Parallel { cpu, cpus ->
            for (i in cpu until mesh.triangles.size step cpus) {
                Draw(mesh.triangles[i])
            }
        }
    }

    fun Draw(tri: Triangle) {
        val t = tri.Apply(shader)

        // Is any vertex behind the near plane?
        if (t.t1.Behind() || t.t2.Behind() || t.t3.Behind()) {
            val near = Plane(Tensor(0f, 0f, -1f, 1f), Tensor(0f, 0f, 1f, 1f))

            val triangles = t.Clip(near)
            for (triangle in triangles) {
                Rasterize(triangle)
            }
        } else {
            Rasterize(t)
        }
    }

    fun Screen(t: Tensor): Vector {
        val x = t.x / t.w
        val y = t.y / t.w
        val z = t.z / t.w

        return Vector(
            x * width / 2f + width / 2f,
            -y * height / 2f + height / 2f,
            z * 0.5f + 0.5f
        )
    }

    fun Clear(color: Color) {
        framebuffer.fill(color.RGBA())
        depthbuffer.fill(Float.POSITIVE_INFINITY)
    }

    fun Rasterize(tri: RasterTriangle) {
        val s1 = Screen(tri.t1)
        val s2 = Screen(tri.t2)
        val s3 = Screen(tri.t3)

        val v1 = tri.triangle.v1
        val v2 = tri.triangle.v2
        val v3 = tri.triangle.v3

        // Back-face culling
        val area =
            (s2.x - s1.x) * (s3.y - s1.y) -
                    (s3.x - s1.x) * (s2.y - s1.y)

        if (area <= 0f) {
            return
        }

        // Bounding box
        val minX = maxOf(0, minOf(s1.x, s2.x, s3.x).toInt())
        val maxX = minOf(width - 1, maxOf(s1.x, s2.x, s3.x).toInt())
        val minY = maxOf(0, minOf(s1.y, s2.y, s3.y).toInt())
        val maxY = minOf(height - 1, maxOf(s1.y, s2.y, s3.y).toInt())

        val inverseArea = 1f / area

        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val p = Vector(x + 0.5f, y + 0.5f, 0f)

                val b0 =
                    ((s2.y - s3.y) * (p.x - s3.x) +
                            (s3.x - s2.x) * (p.y - s3.y)) * inverseArea

                val b1 =
                    ((s3.y - s1.y) * (p.x - s3.x) +
                            (s1.x - s3.x) * (p.y - s3.y)) * inverseArea

                val b2 = 1f - b0 - b1

                if (b0 < 0f || b1 < 0f || b2 < 0f) {
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

                    val weights = Vector(
                        pw0 / sum,
                        pw1 / sum,
                        pw2 / sum
                    )

                    val vertex = Vertex(
                        position =
                            v1.position * weights.x +
                                    v2.position * weights.y +
                                    v3.position * weights.z,

                        normal =
                            (v1.normal * weights.x +
                                    v2.normal * weights.y +
                                    v3.normal * weights.z).Normalized(),

                        texture =
                            v1.texture * weights.x +
                                    v2.texture * weights.y +
                                    v3.texture * weights.z,

                        color =
                            v1.color * weights.x +
                                    v2.color * weights.y +
                                    v3.color * weights.z
                    )

                    framebuffer[index] = shader.Fragment(vertex).RGBA()
                }
            }
        }
    }
}