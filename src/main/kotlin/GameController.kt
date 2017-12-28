class GameController(initboard: Checkerboard? = null) {
    private val board: Checkerboard
    var currentColor = 0
    val checkerboard: Checkerboard
        get() = board.clone()
    private val bot = Player(NetworkIO().load(), 2)
    var botColor = 1

    init {
        board = initboard ?: Checkerboard().also {
            val whiteCheckers = listOf("a1", "c1", "e1", "g1", "b2", "d2", "f2", "h2", "a3", "c3", "e3", "g3")
            val blackCheckers = listOf("b8", "d8", "f8", "h8", "a7", "c7", "e7", "g7", "b6", "d6", "f6", "h6")
            it.init(whiteCheckers, blackCheckers)
        }
    }

    /**
     * Отображение доски в консоле
     */
    fun print() = board.print()

    /**
     * Перемещение шашки с позиции [from] в позицию [to]
     */
    private fun move(from: String, to: String) {
        board.move(from, to)
    }

    /**
     * Атака шашки из позции [from] в позиции [to]
     *
     * Удаление атакованных шашек с доски, перемещение атакующей шашки на заданную позицию
     */
    private fun kill(from: String, to: String) {
        val checker = board.get(from)?.checker
        board.remove(from, to)
        board.place(to, checker)
    }

    /**
     * Выполнить ход
     * Ход задаётся командой по шаблону c3-b2 (перемещение шашки с позиции c3 в позицю b2)
     * или f4:h6:f8:e7 - атака
     */
    @JsName("go")
    fun go(command: String) {
        val moveTemplate = Regex("([a-z]\\d)-([a-z]\\d)")
        val killTemplate = Regex("([a-z]\\d):([a-z]\\d).*")
        moveTemplate.matchEntire(command)?.let {
            val from = it.groups[1]!!.value
            val to = it.groups[2]!!.value
            move(from, to)
            return
        }
        killTemplate.matchEntire(command)?.let {
            val positions = it.value.split(":")
            for (i in 1 until positions.size) {
                val from = positions[i-1]
                val to = positions[i]
                kill(from, to)
            }
        }
    }

    /**
     * Назначает дамку в позиции pos
     */
    fun queen(pos: String) {
        board.get(pos)?.checker?.type = 1
    }

    fun nextMoves() = MoveSearcher(currentColor, board).nextMoves()

    /** инициализация шашек на доске **/
    @JsName("init")
    fun init(whiteCheckers: Array<String>, blackCheckers: Array<String>, queens: Array<String>) {
        board.init(whiteCheckers.toList(), blackCheckers.toList())
        queens.forEach { queen(it) }
    }

    /** Возвращает список позиций шашек, которые могут ходить **/
    @JsName("extractActiveFields")
    fun extractActiveFields(moves: List<String>) = moves.map { it.substring(0, 2) }.toSet().toTypedArray()

    /** Возвращает список позиций полей на которых оканчиваются ходы шашки с позицией [position] **/
    @JsName("getCheckerMoveFields")
    fun getCheckerMoveFields(moves: List<String>, position: String) = moves
            .filter { it.substring(0, 2) == position }
            .map { it.substring(it.length-2, it.length) }.toSet().toTypedArray()

    /** Возвращает команду для шашки с позиции [from], которая перемещается на позицию [to] **/
    @JsName("getCommand")
    fun getCommand(moves: List<String>, from: String, to: String) = moves
            .filter { it.substring(0, 2) == from }.first { it.substring(it.length - 2, it.length) == to }

    /** Возвращает позиции шашек белого цвета **/
    fun getWhiteCheckers() = checkerboard.getCheckers(0).map { it.toString() }.toTypedArray()

    /** Возвращает позиции шашек черного цвета **/
    fun getBlackCheckers() = checkerboard.getCheckers(1).map { it.toString() }.toTypedArray()

    /** Возвращает позиции дамок **/
    fun getQueens() = checkerboard.board
            .filter { it.checker != null && it.checker!!.type == 1 }
            .map { Position(it.x, it.y) }.map { it.toString() }.toTypedArray()

    /** Получить ход бота **/
    fun getBotStep(): String {
        val moves = nextMoves()
        if (moves.isEmpty()) return "lose"
        return bot.selectMove(checkerboard, botColor,moves)
    }
}