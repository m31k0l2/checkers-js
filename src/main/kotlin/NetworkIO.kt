class NetworkIO {
    fun load(): Network {
        val lines = net.split("\n")
        val nw = Network()
        var layer: Layer? = null
        var neuron: Neuron? = null
        lines.forEach { line ->
            if (line == "layer") {
                layer?.let {
                    it.neurons.add(neuron!!)
                    nw.layers.add(it)
                    neuron = null
                }
                layer = Layer()
            } else if (line == "neuron") {
                neuron?.let { layer!!.neurons.add(it) }
                neuron = Neuron()
            } else if (line == "end") {
                layer!!.neurons.add(neuron!!)
                nw.layers.add(layer!!)
            } else {
                neuron!!.weights.add(line.toDouble())
            }
        }
        return nw
    }
}