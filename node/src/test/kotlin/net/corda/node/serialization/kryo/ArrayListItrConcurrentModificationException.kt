package net.corda.node.serialization.kryo

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.whenever
import net.corda.core.serialization.EncodingWhitelist
import net.corda.core.serialization.internal.CheckpointSerializationContext
import net.corda.core.serialization.internal.checkpointDeserialize
import net.corda.core.serialization.internal.checkpointSerialize
import net.corda.serialization.internal.AllWhitelist
import net.corda.serialization.internal.CheckpointSerializationContextImpl
import net.corda.serialization.internal.CordaSerializationEncoding
import net.corda.testing.core.internal.CheckpointSerializationEnvironmentRule
import net.corda.testing.internal.rigorousMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class ArrayListItrConcurrentModificationException(private val compression: CordaSerializationEncoding?) {
    companion object {
        @Parameters(name = "{0}")
        @JvmStatic
        fun compression() = arrayOf<CordaSerializationEncoding?>(null) + CordaSerializationEncoding.values()
    }

    @get:Rule
    val serializationRule = CheckpointSerializationEnvironmentRule()
    private lateinit var context: CheckpointSerializationContext

    @Before
    fun setup() {
        context = CheckpointSerializationContextImpl(
                deserializationClassLoader = javaClass.classLoader,
                whitelist = AllWhitelist,
                properties = emptyMap(),
                objectReferencesEnabled = true,
                encoding = compression,
                encodingWhitelist = rigorousMock<EncodingWhitelist>().also {
                    if (compression != null) doReturn(true).whenever(it).acceptEncoding(compression)
                })
    }

    @Test
    fun `ArrayList iterator can checkpoint without error`() {

        data class TestCheckpoint(val list: List<Int>, val iterator: Iterator<Int>)

        val list = ArrayList<Int>(10)
        (1..1000).forEach { i -> list.add(i) }

        val iterator = list.iterator()
        iterator.next()

        val checkpoint = TestCheckpoint(list, iterator)

        val serializedBytes = checkpoint.checkpointSerialize(context)
        val deserializedCheckpoint = serializedBytes.checkpointDeserialize(context)

        assertThat(deserializedCheckpoint.list).isEqualTo(list)
        assertThat(deserializedCheckpoint.iterator.next()).isEqualTo(2)
        assertThat(deserializedCheckpoint.iterator.hasNext()).isTrue()
    }
}
