package com.zinoti.jaz.drawing.impl

import com.zinoti.jaz.dom.ShapeRendering
import com.zinoti.jaz.dom.SvgFactory
import com.zinoti.jaz.dom.add
import com.zinoti.jaz.dom.childAt
import com.zinoti.jaz.dom.numChildren
import com.zinoti.jaz.dom.parent
import com.zinoti.jaz.dom.remove
import com.zinoti.jaz.dom.removeAll
import com.zinoti.jaz.dom.removeTransform
import com.zinoti.jaz.dom.setCX
import com.zinoti.jaz.dom.setCY
import com.zinoti.jaz.dom.setFill
import com.zinoti.jaz.dom.setHeight
import com.zinoti.jaz.dom.setOpacity
import com.zinoti.jaz.dom.setPathData
import com.zinoti.jaz.dom.setR
import com.zinoti.jaz.dom.setRX
import com.zinoti.jaz.dom.setRY
import com.zinoti.jaz.dom.setStroke
import com.zinoti.jaz.dom.setStrokeDash
import com.zinoti.jaz.dom.setStrokeWidth
import com.zinoti.jaz.dom.setTransform
import com.zinoti.jaz.dom.setWidth
import com.zinoti.jaz.dom.shapeRendering
import com.zinoti.jaz.dom.x
import com.zinoti.jaz.dom.y
import com.zinoti.jaz.drawing.Brush
import com.zinoti.jaz.drawing.Font
import com.zinoti.jaz.drawing.Pen
import com.zinoti.jaz.drawing.Renderer.Optimization
import com.zinoti.jaz.drawing.SolidBrush
import com.zinoti.jaz.geometry.Circle
import com.zinoti.jaz.geometry.Ellipse
import com.zinoti.jaz.geometry.Point
import com.zinoti.jaz.geometry.Polygon
import com.zinoti.jaz.geometry.Rectangle
import com.zinoti.jaz.image.Image
import com.zinoti.jaz.utils.isEven
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.svg.SVGElement
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class VectorRendererSvg constructor(private val context: CanvasContext, private val svgFactory: SvgFactory): VectorRenderer {

    private val svgElement: SVGElement
        get() {
            // Clear the remaining elements from existing SVG element
            // or they will be missed on the final flush

            var renderPosition = context.renderPosition

            while (renderPosition != null && isCompatibleSvgElement(renderPosition.previousSibling)) {
                renderPosition = renderPosition.previousSibling
            }

            if (renderPosition is SVGElement && renderPosition.nodeName == sSvg) {
                if (renderPosition.shapeRendering === shapeRendering) {
                    return renderPosition
                }
            } else {
                val lastChild = region.lastChild

                if (lastChild is SVGElement && lastChild.nodeName == sSvg && lastChild.shapeRendering === shapeRendering) {
                    return lastChild
                }
            }

            flush()

            val svg = svgFactory.create(sSvg)

            svg.shapeRendering = shapeRendering

            return svg
        }


    private val region get() = context.renderRegion

    private var shapeRendering = getShapeRendering(context.optimization)

    private var renderPosition
        get(   ) = context.renderPosition
        set(new) { context.renderPosition = new }


    override fun text(text: String, font: Font, at: Point, brush: Brush) {
        TODO("Implement")

//        if (brush is SolidBrush && brush.color.opacity == 0f) {
//            return
//        }
//
//        val element = makeText(font, text, at)
//
//        fillElement(element, brush)
//
//        completeOperation(element)
    }

    override fun clippedText(text: String, font: Font, point: Point, clipRect: Rectangle, brush: Brush) {
        TODO("Implement")
    }

    override fun wrappedText(text: String, font: Font, point: Point, minBounds: Double, maxBounds: Double, brush: Brush) {
        TODO("Implement")
    }

    override fun line(point1: Point, point2: Point, pen: Pen) = drawPath(pen, point1, point2)

    override fun path(points: List<Point>, pen: Pen) = drawPath(pen, *points.toTypedArray())

    override fun rect(rectangle: Rectangle,           brush: Brush ) = drawRect(rectangle, null, brush)
    override fun rect(rectangle: Rectangle, pen: Pen, brush: Brush?) = drawRect(rectangle, pen,  brush)

    override fun poly(polygon: Polygon,           brush: Brush ) = drawPoly(polygon, null, brush)
    override fun poly(polygon: Polygon, pen: Pen, brush: Brush?) = drawPoly(polygon, pen,  brush)

    override fun rect(rectangle: Rectangle, radius: Double,           brush: Brush ) = drawRect(rectangle, radius, null, brush)
    override fun rect(rectangle: Rectangle, radius: Double, pen: Pen, brush: Brush?) = drawRect(rectangle, radius, pen,  brush)

    override fun arc(center: Point, radius: Double, sweep: Double, rotation: Double,           brush: Brush ) = drawArc(center, radius, sweep, rotation, null, brush)
    override fun arc(center: Point, radius: Double, sweep: Double, rotation: Double, pen: Pen, brush: Brush?) = drawArc(center, radius, sweep, rotation, pen,  brush)

    override fun circle(circle: Circle,           brush: Brush ) = drawCircle(circle, null, brush)
    override fun circle(circle: Circle, pen: Pen, brush: Brush?) = drawCircle(circle, pen,  brush)

    override fun ellipse(ellipse: Ellipse,           brush: Brush ) = drawEllipse(ellipse, null, brush)
    override fun ellipse(ellipse: Ellipse, pen: Pen, brush: Brush?) = drawEllipse(ellipse, pen,  brush)

    override fun image(image: Image, source: Rectangle, destination: Rectangle, opacity: Float) {
        TODO("Implement")

//        var aImageElement: SVGElement?
//        var element      : SVGElement?
//
//        if (source != Rectangle.create(image.size.width, image.size.height)) {
//            aImageElement = SVGElement.create("image")
//
//            val xRatio = destination.width  / source.width
//            val yRatio = destination.height / source.height
//
//            aImageElement!!.setX(destination.x - xRatio * source.x)
//            aImageElement!!.setY(destination.y - yRatio * source.y)
//            aImageElement!!.setWidth(xRatio * image.size.width)
//            aImageElement!!.setHeight(yRatio * image.size.height)
//
//            val aRegion = createClipRect(destination)
//
//            aImageElement!!.setClipPath(aRegion.clipId)
//
//            element = aRegion.element
//
//            element!!.add(aImageElement)
//        } else {
//            element = createElement("image")
//            aImageElement = element
//
//            aImageElement!!.setX(destination.x)
//            aImageElement!!.setY(destination.y)
//            aImageElement!!.setWidth(destination.width)
//            aImageElement!!.setHeight(destination.height)
//        }
//
//        aImageElement!!.setXLinkHref(image.source)
//        aImageElement!!.setPreserveAspectRatio(false)
//        aImageElement!!.setOpacity(opacity)
//
//        completeOperation(element)
    }

    override fun clear() {}

    override fun flush() {
        var element = renderPosition

        while (element != null) {
            val next = element.nextSibling

            element.parent?.remove(element)

            element = next
        }

        renderPosition = null
    }

    private fun drawPath(pen: Pen, vararg points: Point) {
        if (pen.visible && points.isNotEmpty()) {
            val element = makePath(*points)

            outlineElement(element, pen)

            completeOperation(element)
        }
    }

    private fun present(pen: Pen?, brush: Brush?, block: () -> SVGElement?) {
        if (visible(pen, brush)) {
            block()?.let {
                if (brush != null) {
                    fillElement(it, brush, pen == null || !pen.visible)
                }
                if (pen != null) {
                    outlineElement(it, pen, brush == null || !brush.visible)
                }

                completeOperation(it)
            }
        }
    }

    private fun drawRect(rectangle: Rectangle, pen: Pen?, brush: Brush?) = present(pen, brush) {
        when {
            !rectangle.empty -> makeClosedPath(Point.create(rectangle.x, rectangle.y),
                    Point.create(rectangle.x + rectangle.width, rectangle.y),
                    Point.create(rectangle.x + rectangle.width, rectangle.y + rectangle.height),
                    Point.create(rectangle.x, rectangle.y + rectangle.height))
            else -> null
        }
    }

    private fun drawRect(rectangle: Rectangle, radius: Double, pen: Pen?, brush: Brush?) = present(pen, brush) {
        when {
            !rectangle.empty -> makeRoundedRect(rectangle, radius)
            else             -> null
        }
    }

    private fun visible(pen: Pen?, brush: Brush?) = (pen?.visible ?: false) || (brush?.visible ?: false)

    private fun drawPoly(polygon: Polygon, pen: Pen?, brush: Brush?) = present(pen, brush) {
        when {
            polygon.points.size > 1 -> makeClosedPath(*polygon.points.toTypedArray())
            else                    -> null
        }
    }

    private fun drawArc(center: Point, radius: Double, sweep: Double, rotation: Double, pen: Pen?, brush: Brush?) = present(pen, brush) {
        when {
            radius > 0 && sweep != 0.0 -> makeArc(center, radius, sweep, rotation)
            else                       -> null
        }
    }

    private fun drawCircle(circle: Circle, pen: Pen?, brush: Brush?) = present(pen, brush) {
        when {
            !circle.empty -> makeCircle(circle)
            else          -> null
        }
    }

    private fun drawEllipse(ellipse: Ellipse, pen: Pen?, brush: Brush?) = present(pen, brush) {
        when {
            !ellipse.empty -> makeEllipse(ellipse)
            else           -> null
        }
    }

    private fun applyTransform(element: SVGElement) {
        if (!context.transform.isIdentity) {
            element.setTransform(context.transform)
        }
    }

    private fun makeRect(rectangle: Rectangle): SVGElement {
        val element = createElement("rect")

        element.x = rectangle.x
        element.y = rectangle.y
        element.setWidth(rectangle.width)
        element.setHeight(rectangle.height)

        element.setFill(null)
        element.setStroke(null)

        return element
    }

    private fun makeRoundedRect(aRectangle: Rectangle, radius: Double): SVGElement {
        val element = makeRect(aRectangle)

        element.setRX(radius)
        element.setRY(radius)

        element.setFill(null)
        element.setStroke(null)

        return element
    }

    private fun makeCircle(circle: Circle): SVGElement {
        val element = createElement("circle")

        element.setCX(circle.center.x)
        element.setCY(circle.center.y)
        element.setR(circle.radius)

        element.setFill(null)
        element.setStroke(null)

        return element
    }

    private fun makeEllipse(ellipse: Ellipse): SVGElement {
        val element = createElement("ellipse")

        element.setCX(ellipse.center.x)
        element.setCY(ellipse.center.y)
        element.setRX(ellipse.xRadius)
        element.setRY(ellipse.yRadius)

        element.setFill(null)
        element.setStroke(null)

        return element
    }

    private fun makeArc(center: Point, radius: Double, sweep: Double, rotation: Double): SVGElement {
        val element = createElement("path")

        val startX = center.x + radius * cos(rotation)
        val startY = center.y - radius * sin(rotation)
        val endX   = center.x + radius * cos(sweep + rotation)
        val endY   = center.y - radius * sin(sweep + rotation)

        element.setPathData("M$startX,$startY A$radius,$radius $rotation 0,0 $endX,$endY")

        element.setFill  (null)
        element.setStroke(null)

        return element
    }

    private fun makePath(vararg points: Point): SVGElement {
        val aPath = SVGPath()

        aPath.addPath(*points)
        aPath.end()

        return makePath(aPath)
    }

    private fun makeClosedPath(vararg points: Point): SVGElement {
        val path = SVGPath()

        path.addPath(*points)
        path.close()

        return makePath(path)
    }

//    private fun makeText(aFont: Font, aString: String, aPoint: Point): SVGElement {
//        //        TextInfo aText = VectorTextGenerator.createText( aString, aFont, createElement( "text" ), aPoint );
//        //
//        //        return (SVGElement)aText.getData();
//
//        val aText = createElement("text")
//
//        aText.add(HTMLElement.createText(aString))
//
//        aText.setX(aPoint.x)
//        aText.setY(aPoint.y + aFont.size)
//        aText.setFontSize(aFont.size)
//        aText.setFontFamily(aFont.family)
//
//        return aText
//    }

    private fun makePath(path: Path): SVGElement {
        val element = createElement("path")

        element.setPathData(path.data)

        return element
    }

    private fun outlineElement(element: SVGElement, pen: Pen, aClearFill: Boolean = true) {
        if (!pen.visible) {
            return
        }

        val color   = pen.color
        val opacity = color.opacity

        if (aClearFill) {
            element.setFill(null)
        }

        element.setStroke(color)
        element.setOpacity(opacity)
        element.setStrokeWidth(pen.thickness)

        if (pen.dashStyle != null && pen.dashStyle.dashes.size > 1) {
            element.setStrokeDash(dashArray(pen))
        }
    }

    private fun fillElement(element: SVGElement, brush: Brush, clearOutline: Boolean = true) {
        fillHandlers.first { it.fill(this, element, brush) }

        if (clearOutline) {
            element.setStroke(null)
        }
    }

    private fun dashArray(pen: Pen) = pen.dashStyle?.dashes?.mapIndexed { index, dash ->
        max(0, dash + if (index.isEven) -1 else 1)
    }?.joinToString(",") ?: ""

    private fun createElement(tag: String): SVGElement {
        var element: Node? = context.renderPosition

        if (element != null) {
            val previousSibling = element.previousSibling as HTMLElement?

            when {
                isCompatibleSvgElement(previousSibling) -> {
                    context.renderPosition = previousSibling

                    element = renderPosition
                }
                isCompatibleSvgElement(element)         -> {
                    renderPosition = element.childAt(0)

                    element = renderPosition
                }
                else                                    -> flush()
            }
        } else if (region.numChildren > 0) {
            val last = region.lastChild

            if (last != null && last is SVGElement && last.nodeName == sSvg && last.shapeRendering === shapeRendering) {
                context.renderPosition = last

                element = renderPosition
            }
        }

        if (element == null || element.nodeName != tag) {
            return svgFactory.create(tag)
        } else if (element is SVGElement) {
            element.removeAll()
            element.removeTransform()
        }

        // FIXME: handle better
        throw Exception("Error")
    }

//    private fun createClipRect(aRectangle: Rectangle): ClipRegion {
//        val aClip = SVGElement.create("clipPath")
//        val aPathElement = SVGElement.create("path")
//        val aGroup = createElement("g")
//
//        val aPath = SVGPath()
//
//        aPath.addPath(aRectangle.position,
//                Point.create(aRectangle.x + aRectangle.width, aRectangle.y),
//                Point.create(aRectangle.x + aRectangle.width, aRectangle.y + aRectangle.height),
//                Point.create(aRectangle.x, aRectangle.y + aRectangle.height))
//
//        aPath.close()
//
//        aPathElement.setPathData(aPath.getData())
//
//        val aId = "clip" + sClipId++
//
//        aClip.add(aPathElement)
//        aClip.setId(aId)
//
//        aGroup.add(aClip)
//
//        return ClipRegion(aGroup, aId)
//    }

    private fun isCompatibleSvgElement(node: Node?) = node is SVGElement && sSvg == node.nodeName && node.shapeRendering == shapeRendering

    private fun getShapeRendering(optimization: Optimization) = when (context.optimization) {
        Optimization.Speed -> ShapeRendering.CrispEdges
        else               -> ShapeRendering.Auto
    }

    private fun completeOperation(element: SVGElement) {
        applyTransform(element)

        val svg = svgElement

        if (renderPosition == null) {
            if (svg.parent == null) {
                region.add(svg)
            }
        } else if (renderPosition !== svg) {
            renderPosition!!.parent?.replaceChild(svg, renderPosition!!)
        }

        if (renderPosition == null) {
            svg.add(element)
        } else {
            val newRenderPosition = renderPosition?.nextSibling as HTMLElement?

            if (renderPosition !== element && renderPosition!!.parent === svg) {
                svg.replaceChild(element, renderPosition!!)
            }

            renderPosition = newRenderPosition
        }

        context.renderPosition = svg.nextSibling as HTMLElement?
    }

    private class SVGPath: Path("M", "L", "Z")

    private interface FillHandler {
        fun fill(aRenderer: VectorRendererSvg, element: SVGElement, brush: Brush): Boolean
    }

//    private class ClipRegion(val element: SVGElement, val clipId: String)

    private object SolidFillHandler: FillHandler {
        override fun fill(aRenderer: VectorRendererSvg, element: SVGElement, brush: Brush): Boolean {
            if (brush is SolidBrush) {
                val aColor = brush.color
                val aOpacity = aColor.opacity

                element.setFill(aColor)
                element.setOpacity(aOpacity)

                return true
            }

            return false
        }
    }

//    private class TextureFillHandler : FillHandler {
//        override fun fill(aRenderer: VectorRendererSvg, element: SVGElement, brush: Brush): Boolean {
//            if (brush is TextureBrush) {
//                val aImage = brush.image
//                val aDestinationRect = brush.destinationRect
//
//                val aPattern = aRenderer.createElement("pattern")
//
//                aPattern.setId(SVGElement.getNextId())
//                aPattern.setWidth(aDestinationRect.width)
//                aPattern.setHeight(aDestinationRect.height)
//                aPattern.setPatternUnits(Units.userSpaceOnUse)
//
//                val aImageElement = SVGElement.create("image")
//
//                aImageElement.setWidth(aDestinationRect.width)
//                aImageElement.setHeight(aDestinationRect.height)
//                aImageElement.setXLinkHref(aImage.source)
//                aImageElement.setPreserveAspectRatio(false)
//
//                aPattern.removeAll()
//                aPattern.add(aImageElement)
//
//                aRenderer.completeOperation(aPattern)
//
//                element.setFill(null)
//                element.setOpacity(brush.opacity)
//                element.setFillPattern(aPattern)
//
//                val aTask = Service.locator().getTaskFactory().createTask(object : Command() {
//                    fun execute() {
//                        val aBoundingBox = element.getBoundingBox()
//
//                        aPattern.setX(aBoundingBox.x + aDestinationRect.x)
//                        aPattern.setY(aBoundingBox.y + aDestinationRect.y)
//                    }
//                })
//
//                aTask.schedule(0)
//
//                return true
//            }
//
//            return false
//        }
//    }
//
//    private class LinearFillHandler : FillHandler {
//        override fun fill(aRenderer: VectorRendererSvg, element: SVGElement, brush: Brush): Boolean {
//            if (brush !is LinearGradientBrush) {
//                return false
//            }
//
//            val aPIOver4 = Math.PI / 4
//
//            val aColor1 = (brush as LinearGradientBrush).getColor1()
//            val aColor2 = (brush as LinearGradientBrush).getColor2()
//            val aGradient = aRenderer.createElement("linearGradient")
//            val aStop1 = SVGElement.create("stop")
//            val aStop2 = SVGElement.create("stop")
//            var rotation = (brush as LinearGradientBrush).getRotation()
//
//            aGradient.setId(SVGElement.getNextId())
//
//            aStop1.setStopColor(aColor1)
//            aStop2.setStopColor(aColor2)
//            aStop1.setStopOffset(0)
//            aStop2.setStopOffset(1)
//
//            aGradient.removeAll()
//            aGradient.add(aStop1)
//            aGradient.add(aStop2)
//
//            rotation %= 2 * Math.PI
//
//            if (rotation < 0) {
//                rotation = Math.PI - rotation
//            }
//
//            if (rotation > aPIOver4 && rotation < 3 * aPIOver4 || rotation > 5 * aPIOver4 && rotation < 7 * aPIOver4) {
//                configureGradient2(aGradient, Math.cos(rotation), rotation, 5 * aPIOver4)
//            } else {
//                configureGradient1(aGradient, Math.sin(rotation), rotation, 3 * aPIOver4)
//            }
//
//            aRenderer.completeOperation(aGradient)
//
//            element.setFillPattern(aGradient)
//
//            return true
//        }
//
//        private fun configureGradient1(aGradient: SVGElement, aDelta: Double, rotation: Double, rotationThreshold: Double) {
//            aGradient.setY1Percent(50 * (1 - aDelta))
//            aGradient.setY2Percent(50 * (1 + aDelta))
//
//            aGradient.setX1Percent(if (rotation > rotationThreshold) 100 else 0)
//            aGradient.setX2Percent(if (rotation > rotationThreshold) 0 else 100)
//        }
//
//        private fun configureGradient2(aGradient: SVGElement, aDelta: Double, rotation: Double, rotationThreshold: Double) {
//            aGradient.setX1Percent(50 * (1 - aDelta))
//            aGradient.setX2Percent(50 * (1 + aDelta))
//
//            aGradient.setY1Percent(if (rotation > rotationThreshold) 100 else 0)
//            aGradient.setY2Percent(if (rotation > rotationThreshold) 0 else 100)
//        }
//    }

    companion object {

        private val fillHandlers = listOf<FillHandler>(
                SolidFillHandler //,
//                TextureFillHandler,
//                LinearFillHandler
        )

        private val sSvg = "svg"
//        private var sClipId = 0.0
    }
}
