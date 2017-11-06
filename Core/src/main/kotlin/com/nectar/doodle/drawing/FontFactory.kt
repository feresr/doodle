package com.nectar.doodle.drawing

import com.nectar.doodle.drawing.Font.Style


class FontInfo(
        var size    : Int = -1,
        var style   : Set<Style> = setOf(),
        var families: List<String> = listOf())

interface FontFactory {
    operator fun invoke(builder: FontInfo.() -> Unit): Font

    operator fun invoke(font: Font, builder: FontInfo.() -> Unit): Font

    fun family(vararg families: String): String
}

class FontFactoryImpl: FontFactory {
    override operator fun invoke(builder: FontInfo.() -> Unit) = create(FontInfo(), builder)

    override operator fun invoke(font: Font, builder: FontInfo.() -> Unit) = create(FontInfo(font.size, font.style, font.family.split(",").map { it.drop(1).dropLast(1) }), builder)

    private fun create(fontInfo: FontInfo, builder: FontInfo.() -> Unit) = fontInfo.apply(builder).run {
        val family = families.map { "\"$it\"" }.joinToString(",")

        fonts.getOrPut(getHash(family, size, style)) {
            FontImpl(size, style, family)
        }
    }

    override fun family(vararg families: String) = families.map { "\"$it\"" } .joinToString(",")

    private val fonts = HashMap<Int, Font>()

    private fun getHash(family: String, size: Int, style: Set<Style>) = arrayOf(family, size, style).contentHashCode()
}

private class FontImpl(
        override val size  : Int,
        override val style : Set<Style>,
        override val family: String): Font {
}