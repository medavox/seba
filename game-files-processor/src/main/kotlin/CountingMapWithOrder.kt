/**A Map<E, Int> where absent keys equal zero.
 * [containsKey] etc still return false, though.
 * Useful for keeping individual counts for each item in another collection.*/
open class CountingMapWithOrder<K> : CountingMap<K>() {
    private val insertionOrder = mutableListOf<K>()

    override fun clear() {
        insertionOrder.clear()
        backingMap.clear()
    }

    override fun put(key: K, value: Int): Int {
        insertionOrder.add(key)
        return backingMap.put(key, value) ?: 0
    }

    override fun putAll(from: Map<out K, Int>) {
        insertionOrder.addAll(from.keys)
        backingMap.putAll(from)
    }

    override fun remove(key: K): Int {
        insertionOrder.remove(key)
        return backingMap.remove(key) ?: 0
    }
    fun elementsInInsertionOrder():List<K> = insertionOrder
}