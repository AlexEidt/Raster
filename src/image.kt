import java.io.File
import javax.imageio.ImageIO
import kotlin.math.floor

class Image(val width: Int, val height: Int, private val pixels: IntArray = IntArray(width * height)) {
    constructor(path: String) : this(Read(path))

    private constructor(image: Image) : this(image.width, image.height, image.pixels)

    operator fun get(x: Int, y: Int) = pixels[y * width + x]

    operator fun get(uv: Point) = Sample(uv)

    operator fun set(x: Int, y: Int, rgba: Int) {
        pixels[y * width + x] = rgba
    }

    operator fun set(uv: Point, color: Color) {
        val x = (uv.x * width).toInt().coerceIn(0, width - 1)
        val y = (uv.y * height).toInt().coerceIn(0, height - 1)
        this[x, y] = color.RGBA()
    }

    fun Sample(uv: Point): Color {
        val x = (uv.x * width).toInt().coerceIn(0, width - 1)
        val y = (uv.y * height).toInt().coerceIn(0, height - 1)
        return Color(this[x, y])
    }

    fun BilinearSample(uv: Point): Color {
        val x = (uv.x * (width - 1)).coerceIn(0f, (width - 1).toFloat())
        val y = (uv.y * (height - 1)).coerceIn(0f, (height - 1).toFloat())

        val x0 = floor(x).toInt()
        val y0 = floor(y).toInt()
        val x1 = (x0 + 1).coerceAtMost(width - 1)
        val y1 = (y0 + 1).coerceAtMost(height - 1)

        val tx = x - x0
        val ty = y - y0

        val c00 = Color(this[x0, y0])
        val c10 = Color(this[x1, y0])
        val c01 = Color(this[x0, y1])
        val c11 = Color(this[x1, y1])

        val top = c00 * (1f - tx) + c10 * tx
        val bottom = c01 * (1f - tx) + c11 * tx

        return top * (1f - ty) + bottom * ty
    }

    companion object {
        fun Write(path: String, width: Int, height: Int, pixels: IntArray) {
            require(width * height == pixels.size) {
                "Pixel array size ${pixels.size} does not match image dimensions ${width}x${height}"
            }

            val file = File(path)
            val extension = file.extension.lowercase()

            require(extension == "png" || extension == "jpg" || extension == "jpeg") {
                "Unsupported image format: .$extension"
            }

            val image = java.awt.image.BufferedImage(
                width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB
            )

            image.setRGB(0, 0, width, height, pixels, 0, width)
            ImageIO.write(image, if (extension == "jpeg") "jpg" else extension, file)
        }

        private fun Read(path: String): Image {
            val extension = File(path).extension.lowercase()

            require(extension == "png" || extension == "jpg" || extension == "jpeg") {
                "Unsupported image format: .$extension"
            }

            val image = ImageIO.read(File(path))
                ?: throw IllegalArgumentException("Unable to load image: $path")

            val pixels = IntArray(image.width * image.height)
            image.getRGB(0, 0, image.width, image.height, pixels, 0, image.width)

            return Image(image.width, image.height, pixels)
        }
    }
}