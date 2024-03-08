/**A Map<E, Int> where absent keys equal zero.
 * [containsKey] etc still return false, though.
 * Useful for keeping individual counts for each item in another collection.*/
typealias CountingMapLong<K> = MutableMap<K, Long>
    fun <K> CountingMapLong<K>.getCount(key: K): Long = this[key] ?: 0L

    fun <K> CountingMapLong<K>.putCount(key: K, value: Long): Long = put(key, value) ?: 0L

    fun <K> CountingMapLong<K>.putAllCount(from: Map<out K, Long>) = from.forEach {
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

    fun <K> CountingMapLong<K>.putAllCount(from: Map<out K, Int>) = from.forEach {
        println("putAll")
        for((key, count) in from) {
            if(containsKey(key)) {
                val currentCount = getCount(key)
                putCount(key, currentCount + count)
            } else {
                putCount(key, count.toLong())
            }
        }
    }

    fun <K> CountingMapLong<K>.removeCount(key: K): Long = remove(key) ?: 0

    operator fun <K> CountingMapLong<K>.plus(other: CountingMapLong<K>): CountingMapLong<K> {
        println("plus")
        val newMap: CountingMapLong<K> = mutableMapOf()
        newMap.putAllCount(this)
        for((key, count) in other) {
            if(newMap.containsKey(key)) {
                val currentCount = this.getCount(key)
                newMap.putCount(key, currentCount + count)
            } else {
                newMap.putCount(key, count)
            }
        }
        return newMap
    }

    operator fun <K> CountingMapLong<K>.plus(other: CountingMap<K>): CountingMapLong<K> {
        println("plus")
        val newMap: CountingMapLong<K> = mutableMapOf()
        newMap.putAll(this)
        for((key, count) in other) {
            if(newMap.containsKey(key)) {
                val currentCount = this.getCount(key)
                newMap.putCount(key, currentCount + count)
            } else {
                newMap.putCount(key, count.toLong())
            }
        }
        return newMap
    }

    operator fun <K> CountingMapLong<K>.plusAssign(other: CountingMapLong<K>) {
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

    operator fun <K> CountingMapLong<K>.plusAssign(other: CountingMap<K>) {
        println("plusAssign")
        for((key, count) in other) {
            if(containsKey(key)) {
                val currentCount = getCount(key)
                putCount(key, currentCount + count)
            } else {
                putCount(key, count.toLong())
            }
        }
    }

fun <K> CountingMapLong<K>.addNicely(other: Map<K, Int>) {
    for((key, count) in other) {
        if(containsKey(key)) {
            val currentCount = this[key]!!
            put(key, currentCount + count)
        } else {
            put(key, count.toLong())
        }
    }
}

fun <K> CountingMapLong<K>.addNicely(key:K, count:Int) {
    if(containsKey(key)) {
        val currentCount = this[key]!!
        put(key, currentCount + count)
    } else {
        put(key, count.toLong())
    }
}

fun <K> CountingMapLong<K>.addNicely(key:K, count:Long) {
    if(containsKey(key)) {
        val currentCount = this[key]!!
        put(key, currentCount + count)
    } else {
        put(key, count.toLong())
    }
}
