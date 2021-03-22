package com.reas.redditdownloaderkotlin.util.downloader.reddit

import android.util.Log
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.internal.*
import kotlinx.serialization.json.JsonElement

object RedditJsonSerializer: KSerializer<RedditJson> {
    @InternalSerializationApi
    @ExperimentalSerializationApi
    override fun deserialize(decoder: Decoder): RedditJson {
        return decoder.decodeStructure(descriptor) {
            var url: String? = null
            var subreddit: String? = null
            var title: String? = null
            var author: String? = null
            var createdAt: Long? = null

            var mediaDomain: String? = null
            var mediaUrl: String? = null
            var isGif: Boolean? = null
            var isNsfw: Boolean? = null
            var secureMedia: HashMap<Any, Any>? = null

            loop@ while (true) {
                print(decodeElementIndex(descriptor))
                when (val index = decodeElementIndex(descriptor)) {
                    DECODE_DONE -> break@loop

                    0 -> url = decodeStringElement(descriptor, 0)
                    1 -> subreddit = decodeStringElement(descriptor, 1)
                    2 -> title = decodeStringElement(descriptor, 2)
                    3 -> author = decodeStringElement(descriptor, 3)
                    4 -> createdAt = decodeLongElement(descriptor, 4)
                    5 -> mediaDomain = decodeStringElement(descriptor, 5)
                    6 -> mediaUrl = decodeStringElement(descriptor, 6)
                    7 -> isGif = decodeBooleanElement(descriptor, 7)
                    8 -> isNsfw = decodeBooleanElement(descriptor, 8)

                    else -> Log.d("RedditJsonSerializer","unexpected index $index")

                }
            }

            RedditJson(
                url = requireNotNull(url),
                subreddit = requireNotNull(subreddit),
                title = requireNotNull(title),
                author = requireNotNull(author),
                createdAt = requireNotNull(createdAt),

                mediaDomain = requireNotNull(mediaDomain),
                mediaUrl = requireNotNull(mediaUrl),
                isGif = requireNotNull(isGif),
                isNsfw = requireNotNull(isNsfw)
            )
        }
    }

    override fun serialize(encoder: Encoder, value: RedditJson) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.url)
            encodeStringElement(descriptor, 1, value.subreddit)
            encodeStringElement(descriptor, 2, value.title)
            encodeStringElement(descriptor, 3, value.author)
            encodeLongElement(descriptor, 4, value.createdAt)
            encodeStringElement(descriptor, 5, value.mediaDomain)
            encodeStringElement(descriptor, 6, value.mediaUrl)
            encodeBooleanElement(descriptor, 7, value.isGif)
            encodeBooleanElement(descriptor, 8, value.isNsfw)
        }
    }

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("kotlinx.serialization.RedditJsonAsString") {
            element<String>("url")
            element<String>("subreddit")
            element<String>("title")
            element<String>("author")
            element<Long>("createdAt")
            element<String>("mediaDomain")
            element<String>("mediaUrl")
            element<Boolean>("isGif", isOptional = true)
            element<Boolean>("isNsfw")
        }
}