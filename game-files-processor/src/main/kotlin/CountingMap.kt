/**A Map<E, Int> where absent keys equal zero.
 * [containsKey] etc still return false, though.
 * Useful for keeping individual counts for each item in another collection.*/
open class CountingMap<K> : MutableMap<K, Int> {
    protected val backingMap = mutableMapOf<K, Int>()
    override val size: Int = backingMap.size
    override val entries: MutableSet<MutableMap.MutableEntry<K, Int>> = backingMap.entries
    override val keys: MutableSet<K> = backingMap.keys
    override val values: MutableCollection<Int> = backingMap.values

    override fun containsKey(key: K): Boolean = backingMap.containsKey(key)
    override fun containsValue(value: Int): Boolean = backingMap.containsValue(value)
    override fun get(key: K): Int = backingMap[key] ?: 0
    override fun isEmpty(): Boolean = backingMap.isEmpty()

    override fun clear() = backingMap.clear()

    override fun put(key: K, value: Int): Int = backingMap.put(key, value) ?: 0

    override fun putAll(from: Map<out K, Int>) = backingMap.putAll(from)

    override fun remove(key: K): Int = backingMap.remove(key) ?: 0
    override fun toString(): String = backingMap.toString()

}