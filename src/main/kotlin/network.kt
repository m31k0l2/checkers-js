import kotlin.math.tanh

class Neuron {
    private val a = 1.0
    private val b = 2.0/3.0
    var weights = mutableListOf<Double>()

    // активационная функция
    fun activate(input: List<Double>): Double {
        val x = listOf(1.0, *input.toTypedArray()) // добавляем вход 1 для смещения
        return a*tanh(b*sum(x))
    }

    // сумматор
    private fun sum(input: List<Double>) = weights.mapIndexed { i, w -> input[i] * w }.sum()
}

class Layer(size: Int=0) {
    val neurons = MutableList(size, { Neuron() })
    fun activate(input: List<Double>) = neurons.map { it.activate(input) }
}

class Network(vararg layerSize: Int) {
    val layers = MutableList(layerSize.size, { i -> Layer(layerSize[i]) })

    fun activate(input: List<Double>): List<Double> {
        var y = input
        // последовательно активируем все слои
        for (i in 0 until layers.size) {
            y = layers[i].activate(y)
        }
        return y
    }

    fun multiActivate(x: List<List<Double>>): List<Double> {
        synchronized(this) {
            layers[0].neurons.apply {
                if (size != x.size) {
                    clear()
                    addAll(List(x.size, { Neuron() }))
                }
            }
        }
        var y = layers[0].neurons.mapIndexed { i, neuron ->
            neuron.activate(x[i])
        }
        for (i in 1 until layers.size) {
            y = layers[i].activate(y)
        }
        return y
    }
}