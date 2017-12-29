/** Генерация css для анимации перемещения между [points] **/
@JsName("generateCSSAnimation")
fun generateCSSAnimation(points: Array<String>): String {
    val letters = listOf("a", "b", "c", "d", "e", "f", "g", "h")
    val translate = {from: String, to: String ->
        val getCoordinates = { pos: String ->
            letters.indexOf(pos.substring(0, 1)) to pos.substring(1, 2).toInt() - 1 }
        val coordFrom = getCoordinates(from)
        val coordTo = getCoordinates(to)
        val dx = (-coordFrom.first + coordTo.first)*41
        val dy = (coordFrom.second - coordTo.second)*41
        dx to dy
    }
    return (0 until points.size)
            .map { it to translate(points[0], points[it]) }
            .map { it.first * 100 / (points.size - 1) to it.second }
            .map { "${it.first}% { transform: translate(${it.second.first}px,${it.second.second}px); }" }
            .reduce { s1, s2 -> "$s1\n$s2" }
}