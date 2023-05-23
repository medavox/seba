/**A Map<E, Int> where absent keys equal zero.
 * [containsKey] etc still return false, though.
 * Useful for keeping individual counts for each item in another collection.*/
open class CountingMap<K>(
    protected val backingMap: MutableMap<K, Int> = mutableMapOf<K, Int>()
) : MutableMap<K, Int> {
    override val size: Int get() = backingMap.size
    override val entries: MutableSet<MutableMap.MutableEntry<K, Int>> get() = backingMap.entries
    override val keys: MutableSet<K> get() = backingMap.keys
    override val values: MutableCollection<Int> get() = backingMap.values

    override fun containsKey(key: K): Boolean = backingMap.containsKey(key)
    override fun containsValue(value: Int): Boolean = backingMap.containsValue(value)
    override fun get(key: K): Int = backingMap[key] ?: 0
    override fun isEmpty(): Boolean = backingMap.isEmpty()

    override fun clear() = backingMap.clear()

    override fun put(key: K, value: Int): Int = backingMap.put(key, value) ?: 0

    override fun putAll(from: Map<out K, Int>) = from.forEach {
        backingMap.put(it.key, it.value)
    }

    override fun remove(key: K): Int = backingMap.remove(key) ?: 0
    override fun toString(): String = backingMap.toString()

    operator fun plus(other: CountingMap<K>): CountingMap<K> {
        val newMap = CountingMap<K>()
        newMap.putAll(this)
        for((key, count) in other) {
            if(newMap.containsKey(key)) {
                val currentCount = this.get(key)
                newMap.put(key, currentCount + count)
            } else {
                newMap.put(key, count)
            }
        }
        return newMap
    }

    operator fun plusAssign(other: CountingMap<K>) {
        for((key, count) in other) {
            if(containsKey(key)) {
                val currentCount = get(key)
                put(key, currentCount + count)
            } else {
                put(key, count)
            }
        }
    }
}