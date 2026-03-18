package com.example.babyparenting.network.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

class FlexListAdapter : JsonDeserializer<List<String>> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): List<String> =
        when {
            json.isJsonArray -> json.asJsonArray.map { it.asString.trim() }.filter { it.isNotBlank() }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                val raw = json.asString.trim()
                if (raw.isEmpty()) emptyList()
                else raw.split(Regex("[,;\n]+")).map { it.trim() }.filter { it.isNotBlank() }
            }
            else -> emptyList()
        }
}

data class AdviceRequest(
    @SerializedName("model") val model: String,
    @SerializedName("text")  val text: String
)

data class AdviceResponse(
    @SerializedName("domain")     val domain:     String = "",
    @SerializedName("title")      val title:      String = "",
    @SerializedName("goal")       val goal:       String = "",
    @SerializedName("why")        val why:        String = "",
    @SerializedName("how")        val how:        String = "",
    @SerializedName("tip")        val tip:        String = "",
    @SerializedName("difficulty") val difficulty: String = "",
    @SerializedName("example")    val example:    String = "",
    @SerializedName("answer")     val answer:     String = "",
    @SerializedName("scenario")   val scenario:   String = "",
    @SerializedName("language")   val language:   String = "",
    @SerializedName("materials")  val materials:  String = "",
    @SerializedName("duration")   val duration:   String = "",

    @SerializedName("dos")   @JsonAdapter(FlexListAdapter::class) val dos:   List<String> = emptyList(),
    @SerializedName("donts") @JsonAdapter(FlexListAdapter::class) val donts: List<String> = emptyList(),
    @SerializedName("steps") @JsonAdapter(FlexListAdapter::class) val steps: List<String> = emptyList()
)