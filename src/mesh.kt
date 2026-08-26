import java.io.File

class Mesh(val triangles: Array<Triangle>) {

    companion object {
        fun LoadOBJ(path: String): Mesh {
            val file = File(path)

            require(file.exists()) { "File does not exist: $path" }
            require(file.extension.lowercase() == "obj") { "File is not an OBJ: $path" }

            val vertices = mutableListOf<Vector>()
            val texcoords = mutableListOf<Point>()
            val normals = mutableListOf<Vector>()
            val triangles = mutableListOf<Triangle>()

            file.forEachLine { line ->
                val fields = line.trim().split(Regex("\\s+"))

                if (fields.isEmpty() || fields[0].startsWith("#")) {
                    return@forEachLine // continue
                }

                when (fields[0]) {
                    "v" -> {
                        vertices.add(Vector(fields[1].toFloat(), fields[2].toFloat(), fields[3].toFloat()))
                    }

                    "vt" -> {
                        texcoords.add(Point(fields[1].toFloat(), fields[2].toFloat()))
                    }

                    "vn" -> {
                        normals.add(Vector(fields[1].toFloat(), fields[2].toFloat(), fields[3].toFloat()))
                    }

                    "f" -> {
                        val face = fields.drop(1)

                        val vertexIndices = IntArray(face.size)
                        val textureIndices = IntArray(face.size)
                        val normalIndices = IntArray(face.size)
                        var hasNormals = true

                        for (i in face.indices) {
                            val indices = face[i].split("/")

                            vertexIndices[i] = indices[0].toInt() - 1

                            if (indices.size > 1 && indices[1].isNotEmpty()) {
                                textureIndices[i] = indices[1].toInt() - 1
                            }

                            if (indices.size > 2 && indices[2].isNotEmpty()) {
                                normalIndices[i] = indices[2].toInt() - 1
                            }
                            else {
                                hasNormals = false
                            }
                        }

                        // Triangle fan:
                        // (0,1,2), (0,2,3), (0,3,4), ...
                        for (i in 1 until vertexIndices.size - 1) {
                            val triangle = Triangle()

                            triangle.v1.position = vertices[vertexIndices[0]]
                            triangle.v2.position = vertices[vertexIndices[i]]
                            triangle.v3.position = vertices[vertexIndices[i + 1]]

                            if (normals.isNotEmpty() && hasNormals) {
                                triangle.v1.normal = normals[normalIndices[0]]
                                triangle.v2.normal = normals[normalIndices[i]]
                                triangle.v3.normal = normals[normalIndices[i + 1]]
                            }
                            else {
                                val normal = triangle.Normal()
                                triangle.v1.normal = normal
                                triangle.v2.normal = normal
                                triangle.v3.normal = normal
                            }

                            if (texcoords.isNotEmpty()) {
                                triangle.v1.texture = texcoords[textureIndices[0]]
                                triangle.v2.texture = texcoords[textureIndices[i]]
                                triangle.v3.texture = texcoords[textureIndices[i + 1]]
                            }

                            triangles.add(triangle)
                        }
                    }
                }
            }

            return Mesh(triangles.toTypedArray())
        }
    }
}