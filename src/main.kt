import java.awt.Canvas
import java.awt.Color as AWTColor
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import javax.swing.JFrame

class Renderer(private val width: Int, private val height: Int, private val canvas: Canvas) {
    private val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    private val framebuffer = (image.raster.dataBuffer as DataBufferInt).data

    val context = Context(width, height, framebuffer)

    fun present() {
        do {
            val g = canvas.bufferStrategy.drawGraphics

            if (!canvas.bufferStrategy.contentsRestored()) {
                g.drawImage(image, 0, 0, null)
            }

            g.dispose()
            canvas.bufferStrategy.show()
        } while (canvas.bufferStrategy.contentsLost())
    }
}

fun main() {
    val width = 800
    val height = 600
    val canvas = Canvas()
    canvas.background = AWTColor.BLACK
    canvas.ignoreRepaint = true

    val window = JFrame("Raster")
    window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    window.isResizable = false
    window.add(canvas)
    window.setSize(width, height)
    window.setLocationRelativeTo(null)
    window.isVisible = true

    canvas.createBufferStrategy(2)

    val renderer = Renderer(width, height, canvas)

    var frames = 0
    var lastTime = System.nanoTime()

    val cube = Mesh.LoadOBJ("cube.obj")

    val projection = Matrix.Perspective(90f, width.toFloat() / height, 0.1f, 100f)
    val view = Matrix.LookAt(
        Vector(0f, 0f, 5f),
        Vector(0f, 0f, 0f),
        Vector(0f, 1f, 0f)
    )

    val mat = projection * view
    var angle = 0f

    while (true) {
        val rotation = Matrix.Rotate(Vector(0f, 1f, 1f), angle)
        renderer.context.shader = BasicShader(mat * rotation, Color(0f, 1f, 0f))

        renderer.context.Clear(Color(0f, 0f, 0f))
        renderer.context.Draw(cube)

        renderer.present()

        angle += 0.2f
        frames++

        val now = System.nanoTime()
        if (now - lastTime >= 1_000_000_000L) {
            println("FPS: $frames")
            frames = 0
            lastTime = now
        }
    }
}