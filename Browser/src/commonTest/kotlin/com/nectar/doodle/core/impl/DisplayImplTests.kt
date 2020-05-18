package com.nectar.doodle.core.impl

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.core.Box
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.LookupResult.Found
import com.nectar.doodle.core.LookupResult.Ignored
import com.nectar.doodle.core.PositionableWrapper
import com.nectar.doodle.core.View
import com.nectar.doodle.core.height
import com.nectar.doodle.core.width
import com.nectar.doodle.dom.Event
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.drawing.AffineTransform.Companion.Identity
import com.nectar.doodle.drawing.CanvasFactory
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.nectar.doodle.layout.Insets.Companion.None
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.utils.PropertyObserver
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.js.JsName
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.expect


/**
 * Created by Nicholas Eddy on 8/10/19.
 */

@Suppress("FunctionName")
class DisplayImplTests {
    @Test @JsName("defaultsValid") fun `defaults valid`() {
        expect(true, "DisplayImpl::children.isEmpty()") { display().children.isEmpty() }

        mapOf(
            DisplayImpl::size               to Empty,
            DisplayImpl::width              to 0.0,
            DisplayImpl::height             to 0.0,
            DisplayImpl::cursor             to null,
            DisplayImpl::insets             to None,
            DisplayImpl::layout             to null,
            DisplayImpl::transform          to Identity
        ).forEach { validateDefault(it.key, it.value) }
    }

    @Test @JsName("registersOnResize") fun `registers onresize`() {
        val rootElement = mockk<HTMLElement>()

        display(rootElement = rootElement)

        verify(exactly = 1) { rootElement.onresize = any() }
    }

    @Test @JsName("hasInitialWindowSize") fun `has initial window size`() {
        val rootElement = mockk<HTMLElement>().apply {
            every { offsetWidth  } returns 100
            every { offsetHeight } returns 150
        }

        expect(Size(100, 150)) { display(rootElement = rootElement).size }
    }

    @Test @JsName("handlesWindowResize") fun `handles window resize`() {
        var slot = slot<(Event) -> Unit>()

        val rootElement = mockk<HTMLElement>().apply {
            every { onresize = captureLambda() } answers {
                slot = lambda()
            }
        }

        val sizeObserver = mockk<PropertyObserver<Display, Size>>()

        val display = display(rootElement = rootElement).apply {
            sizeChanged += sizeObserver
        }

        val newSize = Size(100, 150)

        rootElement.apply {
            every { offsetWidth  } returns newSize.width.toInt ()
            every { offsetHeight } returns newSize.height.toInt()
        }

        slot.captured(mockk())

        verify { sizeObserver(display, Empty, newSize) }

        expect(newSize) { display.size }
    }

    @Test @JsName("notifiesCursorChange") fun `notifies cursor change`() {
        val cursorObserver = mockk<PropertyObserver<Display, Cursor?>>()

        val display = display().apply {
            cursorChanged += cursorObserver
        }

        display.cursor = Cursor.Grab

        verify { cursorObserver(display, null, Cursor.Grab) }

        expect(Cursor.Grab) { display.cursor!! }
    }

    @Test @JsName("childAtNoLayoutWorks") fun `child at (no layout) works`() {
        val display = display()
        val child0  = view().apply { x += 10.0; y += 12.0 }
        val child1  = view().apply { x += 10.0; y += 12.0 }
        val child2  = view().apply { x += 20.0; y += 12.0 }
        val child3  = view().apply { x += 10.0; y += 23.0; width = 0.0 }

        display.children += child0
        display.children += child1
        display.children += child2
        display.children += child3

        expect(child1) { display.child(at = Point(11.0, 13.0)) }
        expect(child2) { display.child(at = Point(20.0, 12.0)) }
        expect(null  ) { display.child(at = child3.position  ) }

        child1.visible = false

        expect(child0) { display.child(at = Point(11.0, 13.0)) }
    }

    @Test @JsName("childAtWorks") fun `child at works`() {
        val at     = Point(11.0, 13.0)
        val result = mockk<View>()
        val layout = mockk<Layout>().apply {
            every { child(any(), at = at) } returns Found(PositionableWrapper(result))
        }

        display().apply {
            this.layout = layout

            expect(result) { child(at) }

            every { layout.child(any(), at = at) } returns Ignored

            expect(null) { child(at) }

            verify(exactly = 2) { layout.child(any(), at) }
        }
    }

    @Test @JsName("isAncestorWorks") fun `is-ancestor works`() {
        val display = display()
        val parent  = object: Box() {}
        val child   = object: View() {}

        expect(false) { display ancestorOf mockk() }
        expect(false) { display ancestorOf child   }

        display.children += parent as View
        parent.children  += child

        expect(true) { display ancestorOf parent }
        expect(true) { display ancestorOf child  }
    }

    @Test @JsName("layoutWorks") fun `layout works`() {
        val layout = mockk<Layout>()

        display().apply {
            relayout() // should no-op

            this.layout = layout

            verify (exactly= 1) { layout.layout(any()) }

            relayout()

            verify (exactly= 2) { layout.layout(any()) }
        }
    }

    private fun view(): View = object: View() {}.apply { bounds = Rectangle(size = Size(10.0, 10.0)) }

    private fun display(htmlFactory  : HtmlFactory   = mockk(),
                        canvasFactory: CanvasFactory = mockk(),
                        rootElement  : HTMLElement   = mockk()) = DisplayImpl(htmlFactory, canvasFactory, rootElement)

    private fun <T> validateDefault(p: KProperty1<DisplayImpl, T>, default: T?) {
        expect(default, "$p defaults to $default") { p.get(display()) }
    }
}