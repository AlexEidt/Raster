import java.awt.image.Raster

class Context(private val width: Int, private val height: Int) {
    private val framebuffer = IntArray(width * height)
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

        if (t1.Outside() || t2.Outside() || t3.Outside()) {
            val triangles = Clip(RasterTriangle(tri, t1, t2, t3))
            for (triangle in triangles) {
                Draw(triangle)
            }
        } else {
            Draw(RasterTriangle(tri, t1, t2, t3))
        }
    }

    fun Draw(tri: RasterTriangle) {
        val ndc0 = tri.t1.Vector() / tri.t1.w
        val ndc1 = tri.t2.Vector() / tri.t2.w
        val ndc2 = tri.t3.Vector() / tri.t3.w
    }
}