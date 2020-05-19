package com.nectar.doodle.datatransport.dragdrop.impl

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.HTMLInputElement
import com.nectar.doodle.core.View
import com.nectar.doodle.datatransport.DataBundle
import com.nectar.doodle.datatransport.MimeType
import com.nectar.doodle.datatransport.PlainText
import com.nectar.doodle.datatransport.UriList
import com.nectar.doodle.datatransport.dragdrop.DragManager
import com.nectar.doodle.datatransport.dragdrop.DragOperation
import com.nectar.doodle.datatransport.dragdrop.DragOperation.Action
import com.nectar.doodle.datatransport.dragdrop.DragOperation.Action.Copy
import com.nectar.doodle.datatransport.dragdrop.DragOperation.Action.Link
import com.nectar.doodle.datatransport.dragdrop.DragOperation.Action.Move
import com.nectar.doodle.datatransport.dragdrop.DropEvent
import com.nectar.doodle.datatransport.dragdrop.DropReceiver
import com.nectar.doodle.deviceinput.ViewFinder
import com.nectar.doodle.dom.DataTransfer
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.setTop
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.PatternBrush
import com.nectar.doodle.drawing.Renderable
import com.nectar.doodle.drawing.impl.RealGraphicsSurface
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.system.MouseInputService
import com.nectar.doodle.system.MouseInputService.Preprocessor
import com.nectar.doodle.system.SystemMouseEvent
import com.nectar.doodle.system.SystemMouseEvent.Type.Down
import com.nectar.doodle.system.SystemMouseEvent.Type.Up
import kotlin.math.abs
import com.nectar.doodle.dom.MouseEvent as DomMouseEvent


@Suppress("NestedLambdaShadowedImplicitParameter")
internal class DragManagerImpl(
                      private val viewFinder       : ViewFinder,
                      private val scheduler        : Scheduler,
                      private val mouseInputService: MouseInputService,
                      private val graphicsDevice   : GraphicsDevice<RealGraphicsSurface>,
                                  htmlFactory      : HtmlFactory): DragManager {
    private val isIE                 = htmlFactory.create<HTMLElement>().asDynamic()["dragDrop"] != undefined
    private var mouseDown            = null as MouseEvent?
    private var rootElement          = htmlFactory.root
    private var dropAllowed          = false
    private var currentAction        = null as Action?
    private var allowedActions       = "all"
    private var currentDropHandler   = null as Pair<View, DropReceiver>?
    private var currentMouseLocation = Origin
    private var dataBundle           = null as DataBundle?

    private lateinit var visualCanvas: RealGraphicsSurface

    private fun createBundle(dataTransfer: DataTransfer?) = dataTransfer?.let {
        object: DataBundle {
            override fun <T> invoke(type: MimeType<T>) = when (type) {
                in this -> it.getData(type.toString()) as? T
                else    -> null
            }

            override fun <T> contains(type: MimeType<T>) = type.toString() in it.types
        }
    }

    init {
        mouseInputService += object: Preprocessor {
            override fun preprocess(event: SystemMouseEvent) {
                when (event.type) {
                    Up   -> mouseUp  (     )
                    Down -> mouseDown(event)
                    else -> mouseDrag(event)
                }
            }
        }

        rootElement.ondragover = { event ->
            if (event.target !is HTMLInputElement) {
                (dataBundle ?: createBundle(event.dataTransfer))?.let {
                    if (!isIE) {
                        visualCanvas.release()
                    }

                    if (currentDropHandler == null) {
                        dropAllowed = false
                    }

                    dropAllowed = dragUpdate(it, action(event.dataTransfer), mouseLocation(event))

                    if (dropAllowed) {
                        event.preventDefault ()
                        event.stopPropagation()
                    } else {
                        event.dataTransfer?.dropEffect = "none"
                    }
                }
            }
        }

        rootElement.ondrop = { event ->
            if (event.target !is HTMLInputElement) {
                // This is the case of a drop that originated from outside the browser
                if (dataBundle == null) {
                    createBundle(event.dataTransfer)?.let {
                        currentDropHandler?.let { (view, handler) ->
                            handler.drop(DropEvent(view, view.fromAbsolute(mouseLocation(event)), it, action(event.dataTransfer?.dropEffect)))
                        }
                    }
                }

                event.preventDefault ()
                event.stopPropagation()
            }
        }
    }

    override fun shutdown() {
        rootElement.ondragover = null
        rootElement.ondrop     = null
    }

    private fun mouseEvent(event: SystemMouseEvent, view: View) = MouseEvent(
            view,
            view,
            event.type,
            view.fromAbsolute(event.location),
            event.buttons,
            event.clickCount,
            event.modifiers)

    private fun mouseDrag(event: SystemMouseEvent) {
        mouseDown?.let {
            viewFinder.find(event.location)?.let { view ->

                if ((event.location - it.location).run { abs(x) >= DRAG_THRESHOLD || abs(y) >= DRAG_THRESHOLD }) {
                    view.dragRecognizer?.dragRecognized(mouseEvent(event, view))?.let { dragOperation ->
                        dataBundle = dragOperation.bundle

                        createVisual(dragOperation.visual)

                        visualCanvas.position = dragOperation.visualOffset // FIXME: Need to figure out how to position visual

                        scheduler.now { visualCanvas.release() } // FIXME: This doesn't happen fast enough

                        visualCanvas.rootElement.apply {
                            ondragstart = {
                                it.dataTransfer?.apply {
                                    effectAllowed = allowedActions(dragOperation.allowedActions)

                                    setOf(PlainText, UriList).forEach { mimeType ->
                                        dragOperation.bundle(mimeType)?.let { text ->
                                            it.dataTransfer?.setData("$it", text)
                                        }
                                    }
                                }
                            }
                        }

                        registerListeners(dragOperation, rootElement)

                        // FIXME: Need to find way to avoid dragging selected inputs even when they aren't being dragged
//                        if(document.asDynamic()["selection"] != undefined) {
//                            document.asDynamic().selection.empty()
//                        }

                        visualCanvas.rootElement.asDynamic().dragDrop()

                        mouseDown = null
                    }
                }
            }
        }
    }

    private fun mouseDown(event: SystemMouseEvent) {
        viewFinder.find(event.location).let {
            var view = it

            while (view != null) {
                if (tryDrag(view, event)) {
                    break
                }

                view = view.parent
            }
        }
    }

    private fun tryDrag(view: View, event: SystemMouseEvent): Boolean {
        if (!view.enabled || !view.visible) {
            return false
        }

        val mouseEvent = mouseEvent(event, view)

        if (isIE) {
            mouseDown = mouseEvent
            return true // IE doesn't start a drag after this.  We just have to hand-off at this point.
        }

        view.dragRecognizer?.dragRecognized(mouseEvent)?.let { dragOperation ->

            dataBundle            = dragOperation.bundle
            rootElement.draggable = true

            rootElement.ondragstart = {
                createVisual(dragOperation.visual)

                it.dataTransfer?.effectAllowed = allowedActions(dragOperation.allowedActions)

                it.dataTransfer?.setDragImage(visualCanvas.rootElement, dragOperation.visualOffset.x.toInt(), dragOperation.visualOffset.y.toInt())

                setOf(PlainText, UriList).forEach { mimeType ->
                    dragOperation.bundle(mimeType)?.let { text ->
                        it.dataTransfer?.setData("$mimeType", text)
                    }
                }

                dragOperation.started()
            }

            registerListeners(dragOperation, rootElement)

            return true
        }

        return false
    }

    private fun mouseUp() {
        mouseDown               = null
        dataBundle              = null
        rootElement.draggable   = false
        rootElement.ondragstart = null
    }

    private fun allowedActions(actions: Set<Action>): String {
        val builder = StringBuilder()

        if (Copy in actions) builder.append("copy")
        if (Link in actions) builder.append("Link")
        if (Move in actions) builder.append("Move")

        return when (val string = builder.toString()) {
            "copyLinkMove" -> "all"
            ""             -> "none"
            else           -> string.decapitalize()
        }.also {
            allowedActions = it
        }
    }

    private fun mouseLocation(event: DomMouseEvent) = Point(
            x = event.clientX - rootElement.offsetLeft + rootElement.scrollLeft,
            y = event.clientY - rootElement.offsetTop  + rootElement.scrollLeft)

    private fun action(name: String?) = when {
        name == null            -> null
        name.startsWith("copy") -> Copy
        name.startsWith("move") -> Move
        name.startsWith("link") -> Link
        name.startsWith("all" ) -> Copy
        else                    -> null
    }

    private fun action(dataTransfer: DataTransfer?) = action(dataTransfer?.run {
        when {
            effectAllowed != allowedActions && effectAllowed != "uninitialized" -> effectAllowed
            dropEffect    == "none"                                             -> "move"
            else                                                                -> dropEffect
        }
    })

    private fun registerListeners(dragOperation: DragOperation, element: HTMLElement) {
        element.ondragenter = {
            if (it.target !is HTMLInputElement) {
                it.preventDefault ()
                it.stopPropagation()
            }
        }
        element.ondragend = {
            element.draggable   = false
            element.ondragenter = null
            element.ondragstart = null

            val action = action(it.dataTransfer?.dropEffect)

            if (action == null) {
                dragOperation.canceled ()
            } else {
                currentDropHandler?.let { (view, handler) ->
                    if (handler.drop(DropEvent(view, view.fromAbsolute(mouseLocation(it)), dragOperation.bundle, action)) && it.target !is HTMLInputElement) {
                        dragOperation.completed(action)
                    } else {
                        dragOperation.canceled()
                    }
                } ?: dragOperation.completed(action)
            }

            currentDropHandler = null

            null
        }
    }

    private fun dragUpdate(bundle: DataBundle, desired: Action?, location: Point): Boolean {
        var dropAllowed = this.dropAllowed
        val dropHandler = getDropEventHandler(viewFinder.find(location))

        if (dropHandler != currentDropHandler) {
            currentDropHandler?.let { (view, handler) ->
                handler.dropExit(DropEvent(view, view.fromAbsolute(location), bundle, desired))
            }

            if (dropHandler != null) {
                dropAllowed = dropHandler.second.dropEnter(DropEvent(dropHandler.first, dropHandler.first.fromAbsolute(location), bundle, desired))

                currentDropHandler = when (desired) {
                    null -> null
                    else -> dropHandler
                }
            } else {
                currentDropHandler = null
            }
        } else {
            currentDropHandler?.let { (view, handler) ->
                val dropEvent = DropEvent(view, view.fromAbsolute(location), bundle, desired)

                when {
                    desired == null -> {
                        handler.dropExit(dropEvent)

                        currentDropHandler = null
                    }
                    currentMouseLocation != location -> {
                        currentMouseLocation = location

                        dropAllowed = handler.dropOver(dropEvent)
                    }
                    currentAction != desired -> {
                        currentAction = desired

                        dropAllowed = handler.dropActionChanged(DropEvent(view, view.fromAbsolute(location), bundle, desired))
                    }
                }
            }
        }

        return dropAllowed
    }

    private fun createVisual(visual: Renderable?) {
        // TODO: Make this a general purpose View -> Image generator
        visualCanvas = graphicsDevice.create()

        if (visual != null) {
            visualCanvas.rootElement.style.setTop(-visual.size.height)

            visualCanvas.zOrder = -Int.MAX_VALUE
            visualCanvas.size   = visual.size

            visualCanvas.canvas.rect(Rectangle(size = visual.size), PatternBrush(visual.size) {
                visual.render(this)
            })
        }
    }

    private fun getDropEventHandler(view: View?): Pair<View, DropReceiver>? {
        var current = view
        var handler = null as DropReceiver?

        while (current != null && current.enabled && current.visible) {
            handler = current.dropReceiver

            if (handler == null || !handler.active) {
                current = current.parent
            } else {
                break
            }
        }

        return if (handler != null && current != null) current to handler else null
    }

    companion object {
        private const val DRAG_THRESHOLD = 5.0
    }
}