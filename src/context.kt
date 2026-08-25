class Context(val width: Int, val height: Int, val framebuffer: IntArray) {
    init {
        require(framebuffer.size == width * height) {
            "Framebuffer size ${framebuffer.size} does not match dimensions ${width}x${height}"
        }
    }

    private val depthbuffer = FloatArray(width * height)

    var shader: Shader = BasicShader(Matrix.Identity(), Color(1f, 1f, 1f))

    fun Draw(mesh: Mesh) {
        Parallel { cpu, cpus ->
            for (i in cpu until mesh.triangles.size step cpus) {
                Draw(mesh.triangles[i])
            }
        }
    }

    fun Draw(tri: Triangle) {
        val t1 = shader.Vertex(tri.v1)
        val t2 = shader.Vertex(tri.v2)
        val t3 = shader.Vertex(tri.v3)

        val t = RasterTriangle(tri, t1, t2, t3)

        // Is any vertex behind the near plane?
        if (t1.Behind() || t2.Behind() || t3.Behind()) {
            val triangles = t.Clip()
            for (triangle in triangles) {
                Draw(triangle)
            }
        } else {
            Draw(t)
        }
    }

    fun Draw(tri: RasterTriangle) {
        val ndc0 = tri.t1.Vector() / tri.t1.w
        val ndc1 = tri.t2.Vector() / tri.t2.w
        val ndc2 = tri.t3.Vector() / tri.t3.w
    }

    fun Rasterize() {

    }
}