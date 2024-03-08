/**A Map<E, Int> where absent keys equal zero.
 * [containsKey] etc still return false, though.
 * Useful for keeping individual counts for each item in another collection.*/
typealias CountingMap<K> = MutableMap<K, Int>

    fun <K>  CountingMap<K>.getCount(key: K): Int = this[key] ?: 0

    fun <K> CountingMap<K>.putCount(key: K, value: Int): Int = put(key, value) ?: 0

    fun <K> CountingMap<K>.putAllCount(from: Map<out K, Int>) = from.forEach {
        println("putAll")
        for((key, count) in from) {
            if(containsKey(key)) {
                val currentCount = getCount(key)
                putCount(key, currentCount + count)
            } else {
                putCount(key, count)
            }
        }
    }

    fun <K> CountingMap<K>.removeCount(key: K): Int = remove(key) ?: 0

    operator fun <K> CountingMap<K>.plus(other: CountingMap<K>): CountingMap<K> {
        println("plus")
        val newMap = mutableMapOf<K, Int>()
        newMap.putAllCount(this)
        for((key, count) in other) {
            if(newMap.containsKey(key)) {
                val currentCount = getCount(key)
                newMap.putCount(key, currentCount + count)
            } else {
                newMap.putCount(key, count)
            }
        }
        return newMap
    }

    operator fun <K> CountingMap<K>.plusAssign(other: CountingMap<K>) {
        println("plusAssign")
        for((key, count) in other) {
            if(containsKey(key)) {
                val currentCount = getCount(key)
                putCount(key, currentCount + count)
            } else {
                putCount(key, count)
            }
        }
    }

    fun <K> CountingMap<K>.addCount(key: K, count: Int) = putCount(key, getCount(key) + count)
