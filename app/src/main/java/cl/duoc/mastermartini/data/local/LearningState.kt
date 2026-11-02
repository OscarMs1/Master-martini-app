package cl.duoc.mastermartini.data.local

data class LearningState(val favorites: Set<String> = emptySet(), val modules: Set<String> = emptySet()) {
    fun completed(id: String, index: Int) = "$id:$index" in modules
    fun progress(id: String, count: Int): Float = if (count <= 0) 0f else
        (0 until count).count { completed(id, it) }.toFloat() / count
}
