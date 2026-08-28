fun main() {
    val width = 800
    val height = 600
    val window = Window(width, height, Mesh.ReadOBJ("cube.obj"))

    val projection = Matrix.Perspective(90f, width.toFloat() / height, 0.1f, 100f)
    val view = Matrix.LookAt(Vector(0f, 0f, 5f), Vector(0f, 0f, 0f), Vector(0f, 1f, 0f))
    val mat = projection * view

    var frames = 0
    var lastTime = System.nanoTime()

    while (true) {
        if (window.dirty) {
            val rotationX = Matrix.Rotate(Vector(1f, 0f, 0f), window.rotationX)
            val rotationY = Matrix.Rotate(Vector(0f, 1f, 0f), window.rotationY)
            val rotationZ = Matrix.Rotate(Vector(0f, 0f, 1f), window.rotationZ)

            val translation = Matrix.Translate(Vector(window.positionX, -window.positionY, window.positionZ))

            val model = translation * rotationX * rotationY * rotationZ

            window.context.shader =
                if (window.texture != null) {
                    TextureShader(mat * model, window.texture!!, Color(1f, 1f, 1f))
                } else {
                    PhongShader(mat * model)
                }

            window.context.Clear(Color(0f, 0f, 0f))
            window.context.Draw(window.mesh)

            window.dirty = false
        }

        window.Present()

        frames++
        val now = System.nanoTime()

        if (now - lastTime >= 1_000_000_000L) {
            window.window.title = "Raster — $frames FPS"
            frames = 0
            lastTime = now
        }
    }
}