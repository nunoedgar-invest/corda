package net.corda.node.serialization.kryo

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.lang.reflect.Field

class IteratorSerializer(private val serializer : Serializer<Iterator<*>>) : Serializer<Iterator<*>>(false, false) {

    private val fieldCache: MutableMap<Pair<Class<*>, String>, Field?> = mutableMapOf()

    override fun write(kryo: Kryo, output: Output, obj: Iterator<*>) {
        serializer.write(kryo, output, obj)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<Iterator<*>>): Iterator<*> {
        val iterator = serializer.read(kryo, input, type)
        return fixIterator(iterator)
    }

    private fun <T> fixIterator(iterator: Iterator<T>) : Iterator<T> {

        // Find the outer list
        val iterableReferenceField = cachedField(iterator.javaClass, "this\$0") ?: return iterator
        val iterableInstance = iterableReferenceField.get(iterator) ?: return iterator

        // Get the modCount of the outer list
        val modCountField = cachedField(iterableInstance.javaClass, "modCount") ?: return iterator
        val modCountValue = modCountField.getInt(iterableInstance)

        // Set expectedModCount of iterator
        val expectedModCountField = cachedField(iterator.javaClass, "expectedModCount") ?: return iterator
        expectedModCountField.setInt(iterator, modCountValue)

        return iterator
    }

    /**
     * Keep a cache of Field objects so we can reuse them
     */
    private fun cachedField(clazz: Class<*>, fieldName: String): Field? {
        val key = Pair(clazz, fieldName)
        if (!fieldCache.containsKey(key)) {
            fieldCache[key] = findField(clazz, fieldName)?.apply { isAccessible = true }
        }
        return fieldCache[key]
    }

    /**
     * Find field in clazz or any superclass
     */
    private fun findField(clazz: Class<*>, fieldName: String): Field? {
        return clazz.declaredFields.firstOrNull { x -> x.name == fieldName } ?: when {
            clazz.superclass != null -> {
                // Look in superclasses
                findField(clazz.superclass, fieldName)
            }
            else -> null // Not found
        }
    }
}


