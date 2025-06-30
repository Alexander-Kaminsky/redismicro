package il.ac.afeka.cloud.WebMVCEmployees
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class TwoDigitSerializer : JsonSerializer<Int>() {
    override fun serialize(value: Int, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(String.format("%02d", value))
    }
}

class FourDigitSerializer : JsonSerializer<Int>() {
    override fun serialize(value: Int, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(String.format("%04d", value))
    }
}