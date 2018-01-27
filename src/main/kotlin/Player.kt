@file:Suppress("DEPRECATION")

import kotlin.js.Math.random

class Player(private val nw: Network, private val predicateMoves: Int = 4,
             val error: Double=3.0) {
    /**
     * Выбор ИИ наилучшего хода
     * [checkerboard] - доска на момент хода
     * [color] - цвет фигур ИИ
     * [steps] - набор возможных ходов
     *
     * Оценка каждого хода производится в паралельных потоках
     * Оценка осуществляется путём проигрывания ходов вперёд в количестве [predicateMoves]
     * В итоге получается список пар {ход - счёт}
     * Выбирается ход с максимальным счётом
     */
    fun selectMove(checkerboard: Checkerboard, color: Int, steps: List<String>): String {
        val list = steps.map { it to play(checkerboard, color, predicateMoves, it) }.toList() // оценка каждого ходв
        val max = list.maxBy { it.second }!!.second
        val l = list.filter { it.second == max }.map { it.first }
        val step = selectBestStep(checkerboard, color, l)
        return step
    }

    /**
     * Проигрывание ИИ партии с позиций доски [checkerboard]
     * [initColor] - цвет ИИ
     * [count] - количество проигрываемых ИИ ходов
     * [initStep] - рассматриваемый ход
     *
     * ИИ играет сам с собой за чёрных и белых
     * Начинает с заданного хода
     * По окончанию ходов или по результатам партии (если до выделенного количество ходов игра окончена)
     * происходит подсчёт очков
     * За выйгранную партию начисляется 100 очков, за проигрыш - минус 100
     * Чтобы выбрать, который приблежает победу или оттягивает поражение, из 100 вычтим количество ходов, затраченных в игре
     * Если результат не определен, очки начисляются за оставшееся количество шашек
     * стоимость шашки - 1 очко, дамки - 3
     * Результат определяется как разность очков между белыми и чёрными
     */
    private fun play(checkerboard: Checkerboard, initColor: Int, count: Int, initStep: String): Int {
        val game = GameController(checkerboard.clone())
        game.currentColor = initColor
        var steps = game.nextMoves()
        for (i in 0 until count * 2) {
            val step = if (i == 0) initStep else selectBestStep(game.checkerboard, game.currentColor, steps)
            game.go(step)
            game.currentColor = 1 - game.currentColor
            steps = game.nextMoves()
            if (steps.isEmpty()) {
                return if (game.currentColor != initColor) 100 - i
                else -100 + i
            }
        }
        val whiteCount = game.checkerboard.encodeToVector().filter { it > 0 }.count()
        val blackCount = game.checkerboard.encodeToVector().filter { it < 0 }.count()
        return if (initColor == 0) whiteCount - blackCount
        else blackCount - whiteCount
    }

    /**
     * Выбор ИИ наилучшего хода в процессе оценки,
     * если выберать неизчего возвращаем результат
     *
     * Алгоритм
     * Для каждого хода из заданного списка
     * Положение шашек на доске кодируется в вектор действительных чисел.
     * Размер вектора соответсвует игровым полям доски, т.е. 32
     * Этот вектор дальше преобразуется при помощи инкодера в вектор с размерностью 91.
     * Инкодер составляет вектор из фрагментов доски по аналогии с принципом работы свёрточных сетей.
     * Это позволяет нейронной сети, как бы рассматривать доску с разных точек зрения, а нейроны её первого слоя,
     * тогда играют роль детектора фич
     * При помощи мультиактивации, каждый вектор массива сопоставляется с одним нейроном сети первого слоя
     * Каждый нейрон выступает своего рода рецептором, который реагирует на ситуацию на доске в общем или какойто ее части
     * Дальше нейронная сеть вычисляет число от -1 до 1, где 1 соответствуют лучшему с её точки зрения ходу, а -1 - худшему
     *
     * Чтобы нейронная сеть не зависила от цвета фигур за которые она играет, оценка идёт со стороны белых
     * Если нейронная сеть играет за чёрных, то выбирается ход наиболее худший для белых (т.е. минимальное значение)
     * Если за белых, то соответственно максимальное значение
     **/
    private fun selectBestStep(checkerboard: Checkerboard, color: Int, steps: List<String>): String {
        if (steps.isEmpty()) return ""
        if (steps.size == 1) return steps[0]
        val list = steps.map { it to GameController(checkerboard.clone()) }.map { (command, game) ->
            // может стоить запаралелить
            game.go(command)
            val vector = game.checkerboard.encodeToVector()
            val o = nw.multiActivate(InputEncoder().encode(vector))
            command to o[0]
        }.toList().map {
            it.first to it.second * if (error > 0) (1 + error/100 * (1 - 2*random())) else 1.0
        }
        val step = (if (color == 0) {
            list.maxBy { it -> it.second }!!
        } else list.minBy { it.second }!!)
        return step.first
    }
}