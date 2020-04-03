package net.corda.node.serialization.kryo

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.lang.reflect.Field

class IteratorSerializer(private val serializer : Serializer<Iterator<*>>) : Serializer<Iterator<*>>(false, false) {

    override fun write(kryo: Kryo, output: Output, obj: Iterator<*>) {
        serializer.write(kryo, output, obj)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<Iterator<*>>): Iterator<*> {
        val iterator = serializer.read(kryo, input, type)
        return fixIterator(iterator)
    }

    private fun <T> fixIterator(iterator: Iterator<T>) : Iterator<T> {

        // Find the outer list
        val outerClassField = findField(iterator, "this\$0") ?: return iterator
        outerClassField.isAccessible = true
        val outerClass = outerClassField.get(iterator) ?: return iterator

        // Get the modCount of the outer list
        val modCountField = findField(outerClass, "modCount") ?: return iterator
        modCountField.isAccessible = true
        val modCountValue = modCountField.getInt(outerClass)

        // Set expectedModCount of iterator
        val expectedModCountField = findField(iterator, "expectedModCount") ?: return iterator
        expectedModCountField.isAccessible = true
        expectedModCountField.setInt(iterator, modCountValue)

        return iterator
    }

    /**
     * Find field in clazz or any superclass
     */
    private fun findField(obj: Any, fieldName: String, clazz: Class<*> = obj.javaClass): Field? {
        return clazz.declaredFields.firstOrNull { x -> x.name == fieldName } ?: when {
            clazz.superclass != null -> {
                // Look in superclasses
                findField(obj, fieldName, clazz.superclass)
            }
            else -> null // Not found
        }
    }
}


