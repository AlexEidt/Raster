import java.awt.Canvas
import java.awt.Color
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.datatransfer.DataFlavor
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import javax.swing.JFrame

class Window(width: Int, height: Int, var mesh: Mesh = Mesh(emptyArray())) {
    val canvas = Canvas()
    val window = JFrame("Raster")

    private val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    private val framebuffer = (image.raster.dataBuffer as DataBufferInt).data

    val context = Context(width, height, framebuffer)

    var texture: Image? = null

    var rotationX = 0f
    var rotationY = 0f
    var rotationZ = 0f

    var positionX = 0f
    var positionY = 0f
    var positionZ = 0f

    var dirty = true

    private var lastX = 0
    private var lastY = 0

    init {
        canvas.background = Color.BLACK
        canvas.ignoreRepaint = true

        window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        window.isResizable = false
        window.add(canvas)
        window.setSize(width, height)
        window.setLocationRelativeTo(null)
        window.isVisible = true

        canvas.createBufferStrategy(2)

        canvas.isFocusable = true
        canvas.requestFocus()

        canvas.dropTarget = DropTarget().apply {
            addDropTargetListener(object : java.awt.dnd.DropTargetAdapter() {
                override fun drop(event: java.awt.dnd.DropTargetDropEvent) {
                    try {
                        if (!event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                            event.rejectDrop()
                            return
                        }

                        event.acceptDrop(DnDConstants.ACTION_COPY)

                        val files = event.transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>

                        var success = false

                        for (file in files.filterIsInstance<java.io.File>()) {
                            when (file.extension.lowercase()) {
                                "obj" -> {
                                    mesh = Mesh.ReadOBJ(file.path)
                                    dirty = true
                                    success = true
                                }

                                "png", "jpg", "jpeg" -> {
                                    texture = Image(file.path)
                                    dirty = true
                                    success = true
                                }
                            }
                        }

                        if (success)
                            canvas.requestFocus()

                        event.dropComplete(success)
                    } catch (e: Exception) {
                        event.dropComplete(false)
                        e.printStackTrace()
                    }
                }
            })
        }

        canvas.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                lastX = e.x
                lastY = e.y
            }
        })

        canvas.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                val dx = e.x - lastX
                val dy = e.y - lastY

                when {
                    e.isControlDown && (e.modifiersEx and MouseEvent.BUTTON3_DOWN_MASK) != 0 -> {
                        rotationZ += dx * 0.5f
                    }

                    (e.modifiersEx and MouseEvent.BUTTON3_DOWN_MASK) != 0 -> {
                        rotationY += dx * 0.5f
                        rotationX -= dy * 0.5f
                    }

                    (e.modifiersEx and MouseEvent.BUTTON1_DOWN_MASK) != 0 -> {
                        positionX += dx * 0.01f
                        positionY -= dy * 0.01f
                    }
                }

                lastX = e.x
                lastY = e.y

                dirty = true
            }
        })

        canvas.addMouseWheelListener { e: MouseWheelEvent ->
            positionZ -= e.preciseWheelRotation.toFloat() * 0.5f
            dirty = true
        }

        canvas.addKeyListener(object : java.awt.event.KeyAdapter() {
            override fun keyPressed(e: java.awt.event.KeyEvent) {
                if (e.keyCode == java.awt.event.KeyEvent.VK_SPACE) {
                    context.wireframe = !context.wireframe
                    dirty = true
                }
            }
        })
    }

    fun Present() {
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