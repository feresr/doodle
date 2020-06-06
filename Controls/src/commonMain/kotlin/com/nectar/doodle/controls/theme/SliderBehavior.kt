package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.Slider
import com.nectar.doodle.core.Behavior
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.KeyText.Companion.ArrowDown
import com.nectar.doodle.event.KeyText.Companion.ArrowLeft
import com.nectar.doodle.event.KeyText.Companion.ArrowRight
import com.nectar.doodle.event.KeyText.Companion.ArrowUp
import com.nectar.doodle.event.PointerEvent
import com.nectar.doodle.event.PointerListener
import com.nectar.doodle.event.PointerMotionListener
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.utils.Orientation.Horizontal
import com.nectar.doodle.utils.Orientation.Vertical
import com.nectar.doodle.utils.size
import kotlin.math.round

/**
 * Created by Nicholas Eddy on 2/13/18.
 */

abstract class SliderBehavior(private val focusManager: FocusManager?): Behavior<Slider>, PointerListener, PointerMotionListener, KeyListener {

    private   var lastStart           = -1.0
    protected var lastPointerPosition = -1.0
        private set

    private val changed: (Slider, Double, Double) -> Unit = { it,_,_ -> it.rerender() }

    override fun install(view: Slider) {
        view.changed              += changed
        view.keyChanged           += this
        view.pointerChanged       += this
        view.pointerMotionChanged += this
    }

    override fun uninstall(view: Slider) {
        view.changed              -= changed
        view.keyChanged           -= this
        view.pointerChanged       -= this
        view.pointerMotionChanged -= this
    }

    override fun pressed(event: PointerEvent) {
        val slider      = event.source as Slider
        val scaleFactor = scaleFactor(slider).let { if ( it != 0f) 1 / it else 0f }

        val offset = when (slider.orientation) {
            Horizontal -> event.location.x
            Vertical   -> event.location.y
        }

        val barSize     = barSize(slider)
        val barPosition = barPosition(slider)

        if (offset < barPosition || offset > barPosition + barSize) {
            slider.value += scaleFactor * (offset - (barPosition + barSize / 2))
        }

        lastPointerPosition = offset
        lastStart           = slider.value

        focusManager?.requestFocus(slider)

        event.consume()
    }

    override fun released(event: PointerEvent) {
        lastStart         = -1.0
        lastPointerPosition = -1.0
    }

    override fun keyPressed(event: KeyEvent) {
        val slider    = event.source as Slider
        val increment = slider.range.size / 100

        when (event.key) {
            ArrowLeft,  ArrowDown -> slider.value -= increment
            ArrowRight, ArrowUp   -> slider.value += increment
        }
    }

    override fun dragged(event: PointerEvent) {
        val slider = event.source as Slider

        val delta = when (slider.orientation) {
            Horizontal -> event.location.x - lastPointerPosition
            Vertical   -> event.location.y - lastPointerPosition
        }

        val deltaValue = delta / scaleFactor(slider)

        slider.value = lastStart + deltaValue

        event.consume()
    }

    private fun scaleFactor(slider: Slider): Float {
        val size = (if (slider.orientation === Horizontal) slider.width else slider.height) - barSize(slider)

        return if (!slider.range.isEmpty()) (size / slider.range.size).toFloat() else 0f
    }

    protected fun barPosition(slider: Slider) = round((slider.value - slider.range.start) * scaleFactor(slider))

    protected fun barSize(slider: Slider) = if (slider.orientation === Horizontal) slider.height else slider.width
}
