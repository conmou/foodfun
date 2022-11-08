package edu.foodfun.deserializer

interface IDeserializer<I, O> {
    fun deserialize(input: I): O
    class DeserializerException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
}