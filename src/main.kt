import java.awt.Canvas
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import javax.swing.JFrame

class Renderer(private val width: Int, private val height: Int, private val canvas: Canvas) {
    private val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    private val framebuffer = (image.raster.dataBuffer as DataBufferInt).data

    private var frame = 0

    fun rgba(r: Int, g: Int, b: Int, a: Int = 255): Int {
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    fun render() {
        val color = rgba(frame++ % 256, 100, 200)

        for (y in 0 until height) {
            for (x in 0 until width) {
                framebuffer[y * width + x] = color
            }
        }
    }

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
    canvas.background = Color.BLACK
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

    while (true) {
        renderer.render()
        renderer.present()

        frames++

        val now = System.nanoTime()
        if (now - lastTime >= 1_000_000_000L) {
            println("FPS: $frames")
            frames = 0
            lastTime = now
        }
    }
}