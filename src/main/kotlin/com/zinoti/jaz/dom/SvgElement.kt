package com.zinoti.jaz.dom

import com.zinoti.jaz.drawing.AffineTransform
import com.zinoti.jaz.drawing.Color
import org.w3c.dom.svg.SVGElement

inline val SVGElement.parent get() = parentNode as SVGElement?


var SVGElement.x
    get(   ) = getAttribute("x")!!.toDouble()
    set(new) { setAttribute("x", "$new") }

var SVGElement.y
    get(   ) = getAttribute("y")!!.toDouble()
    set(new) { setAttribute("y", "$new") }

fun SVGElement.setWidth (value: Double) = setAttribute("width",  "value")
fun SVGElement.setHeight(value: Double) = setAttribute("height", "value")
fun SVGElement.setR     (value: Double) = setAttribute("r",      "value")

fun SVGElement.setX1         ( value : Double      ) = setAttribute  ( "x1",               "$value"        )
fun SVGElement.setX2         ( value : Double      ) = setAttribute  ( "x2",               "$value"        )
fun SVGElement.setY1         ( value : Double      ) = setAttribute  ( "y1",               "$value"        )
fun SVGElement.setY2         ( value : Double      ) = setAttribute  ( "y2",               "$value"        )
fun SVGElement.setX1Percent  ( value : Double      ) = setAttribute  ( "x1",               "$value%"       )
fun SVGElement.setX2Percent  ( value : Double      ) = setAttribute  ( "x2",               "$value%"       )
fun SVGElement.setY1Percent  ( value : Double      ) = setAttribute  ( "y1",               "$value%"       )
fun SVGElement.setY2Percent  ( value : Double      ) = setAttribute  ( "y2",               "$value%"       )
fun SVGElement.setRX         ( value : Double      ) = setAttribute  ( "rx",               "$value"        )
fun SVGElement.setRY         ( value : Double      ) = setAttribute  ( "ry",               "$value"        )
fun SVGElement.setCX         ( value : Double      ) = setAttribute  ( "cx",               "$value"        )
fun SVGElement.setCY         ( value : Double      ) = setAttribute  ( "cy",               "$value"        )
fun SVGElement.setPathData   ( value : String      ) = setAttribute  ( "d",                  value         )
fun SVGElement.setOpacity    ( value : kotlin.Float) = setAttribute  ( "opacity",          "$value"        )
fun SVGElement.setStrokeWidth( value : Double      ) = setAttribute  ( "stroke-width",     "$value"        )
fun SVGElement.setStrokeDash ( value : String      ) = setAttribute  ( "stroke-dasharray",   value         )
fun SVGElement.setClipPath   ( clipId: String      ) = setAttribute  ( "clip-path",        "url(#$clipId)" )

fun SVGElement.setXLinkHref  ( value : String ) = setAttributeNS( "http://www.w3.org/1999/xlink", "xlink:href", value )


var SVGElement.shapeRendering
    get(     ) = getAttribute("shape-rendering")?.let { ShapeRendering.valueOf(it) } ?: ShapeRendering.Auto
    set(value) = setAttribute("shape-rendering", value.value)


fun convert(color: Color?, block: (String) -> Unit) = when (color) {
    null -> none
    else -> "#${color.hexString}"
}.let {
    block(it)
}

fun SVGElement.setFill(color: Color?) = convert(color) {
    setAttribute("fill", it)
}

fun SVGElement.setFillPattern(pattern: SVGElement?) = when (pattern) {
    null -> none
    else -> "url(#${pattern.id})"
}.let {
    setAttribute("fill", it)
}

fun SVGElement.setStroke(color: Color?) = convert(color) {
    setAttribute("stroke", it)
}

fun SVGElement.setTransform(transform: AffineTransform?) = when(transform) {
    null -> removeTransform()
    else -> setTransform(transform.run { "matrix($scaleX,$shearY,$shearX,$scaleY,$translateX,$translateY)" })
}

fun SVGElement.setTransform(transform: String) = setAttribute("transform", transform)

fun SVGElement.removeTransform() = removeAttribute("transform")


enum class ShapeRendering(val value: String) {
    CrispEdges("crispEdges"),
    Auto      ("auto"      )
}

private val none = "none";

//public void setStopColor( Color aColor )
//{
//    setStopColor( "#"+ aColor.getHexString() )
//
//    if( aColor.getOpacity() != 1 )
//    {
//        setStopOpacity( aColor.getOpacity() )
//    }
//}
//
//public void setStopOffset( float aValue )
//{
//    setStopOffsetInternal( Math.min( 1, Math.max( 0, aValue ) ) )
//}

//public void setPoints( Point[] aPoints )
//{
//    StringBuilder aPointsString = new StringBuilder()
//
//    int i = 0
//
//    for( ; i < aPoints.length - 1; i++ )
//    {
//        aPointsString.append( aPoints[i].getX() +","+ aPoints[i].getY() +" " )
//    }
//
//    aPointsString.append( aPoints[i].getX() +","+ aPoints[i].getY() )
//
//    setPoints( aPointsString.toString() )
//}
//
//public native void setFontSize( int aSize )
/*-{
    this.setAttributeNS( null, "font-size", aSize );
}-*/

//public final class SVGElement extends Element
//{
//    protected SVGElement() {}
//
//    public static SVGElement create( String aTag )
//    {
//        SVGElement aMaster = sPrototypes.get( aTag );
//
//        // No cached version exists
//
//        if( aMaster == null )
//        {
//            aMaster = createInternal( aTag );
//
//            sPrototypes.put( aTag, aMaster );
//        }
//
//        return (SVGElement)aMaster.cloneNode( false );
//    }
//
//    public static native SVGElement createInternal( String aTag )
// /*-{
//        return $doc.createElementNS( "http://www.w3.org/2000/svg", aTag );
//    }-*/;
//
//    public static String getNextId()
//    {
//        return "" + sID++;
//    }
//
//
//
//    public native void setFontFamily( String aFamily )
// /*-{
//        this.setAttributeNS( null, "font-family", aFamily );
//    }-*/;
//
//    public native void setFontWeight( FontWeight aFontWeight )
// /*-{
//        this.setAttributeNS( null, "font-weight", aFontWeight );
//    }-*/;
//
//    public native void setFontStyle( FontStyle aFontStyle )
// /*-{
//        this.setAttributeNS( null, "font-style", aFontStyle );
//    }-*/;
//
//    public native void setWritingMode( WritingMode aWritingMode )
// /*-{
//        this.setAttributeNS( null, "writing-mode", aWritingMode );
//    }-*/;
//
//    public native void setDirection( Direction aDirection )
// /*-{
//        this.setAttributeNS( null, "direction", aDirection );
//    }-*/;
//
//    public native void setStrokeLinecap( StrokeLinecap aValue )
// /*-{
//        this.setAttributeNS( null, "stroke-linecap", aValue );
//    }-*/;
//
//    public Rectangle getBoundingBox()
//    {
//        BoundingBox aBBox = getBBox();
//
//        return Rectangle.create( aBBox.getX     (),
//                                 aBBox.getY     (),
//                                 aBBox.getWidth (),
//                                 aBBox.getHeight() );
//    }
//
//    public native void setGlyphVerticalOrientation( int aRotation )
// /*-{
//        this.setAttributeNS( null, "glyph-orientation-vertical", aRotation );
//    }-*/;
//
//    public void setRotation( double aRotation )
//    {
//        setTransform( "rotate("+ aRotation +")" );
//    }
//
//    public void setRotation( double aRotation, double aX, double aY )
//    {
//        setTransform( "rotate("+ aRotation +", "+ aX +", "+ aY +")" );
//    }
//
//
//    public void setPreserveAspectRatio( boolean aValue )
//    {
//        setPreserveAspectRatio( aValue ? "defer" : sNone  );
//    }
//
//    public native void setPatternUnits( Units aUnits )
// /*-{
//        this.setAttributeNS( null, "patternUnits", aUnits );
//    }-*/;
//
//    public native void setPatternContentUnits( Units aUnits )
// /*-{
//        this.setAttributeNS( null, "patternContentUnits", aUnits );
//    }-*/;
//
//    private native String getId()
// /*-{
//        return this.getAttributeNS( null, "id" );
//    }-*/;
//
//    private native BoundingBox getBBox()
// /*-{
//        return this.getBBox();
//    }-*/;
//
//    private native void setStroke( String aValue )
// /*-{
//        this.setAttributeNS( null, "stroke", aValue );
//    }-*/;
//
//    private native void setFill( String aValue )
// /*-{
//        this.setAttributeNS( null, "fill", aValue );
//    }-*/;
//
//    private native void setPoints( String aPointString )
// /*-{
//        this.setAttributeNS( null, "points", aPointString );
//    }-*/;
//
//    private native void setTransform( String aTransform )
// /*-{
//        this.setAttributeNS( null, "transform", aTransform );
//    }-*/;
//
//    private native void removeTransform()
// /*-{
//        this.removeAttributeNS( null, "transform" );
//    }-*/;
//
//    private native void setPreserveAspectRatio( String aValue )
// /*-{
//        this.setAttributeNS( null, "preserveAspectRatio", aValue );
//    }-*/;
//
//    private native void setStopColor( String aValue ) /*-{ this.setAttributeNS( null, "stop-color", aValue ); }-*/;
//
//    private native void setStopOpacity( float aValue ) /*-{ this.setAttributeNS( null, "stop-opacity", aValue ); }-*/;
//
//    private native void setStopOffsetInternal( float aValue ) /*-{ this.setAttributeNS( null, "offset", aValue ); }-*/;
//
//    public static final class WritingMode extends JavaScriptObject
//    {
//        protected WritingMode() {}
//
//        public static WritingMode create( String aValue )
//        {
//            if( GWT.isScript() ) { return createScript( aValue ); }
//            else                 { return createHosted( aValue ); }
//        }
//
//        public static native WritingMode createScript( String aValue ) /*-{ return aValue; }-*/;
//        public static native WritingMode createHosted( String aValue ) /*-{ this[0] = [aValue]; return this[0]; }-*/;
//
//        public static final WritingMode lr = create( "lr" );
//        public static final WritingMode rl = create( "rl" );
//        public static final WritingMode tb = create( "tb" );
//    }
//
//    public static final class Direction extends JavaScriptObject
//    {
//        protected Direction() {}
//
//        public static Direction create( String aValue )
//        {
//            if( GWT.isScript() ) { return createScript( aValue ); }
//            else                 { return createHosted( aValue ); }
//        }
//
//        public static native Direction createScript( String aValue ) /*-{ return aValue; }-*/;
//        public static native Direction createHosted( String aValue ) /*-{ this[0] = [aValue]; return this[0]; }-*/;
//
//        public static final Direction ltr = create( "ltr" );
//        public static final Direction rtl = create( "rtl" );
//    }
//
//    public static final class ShapeRendering extends JavaScriptObject
//    {
//        protected ShapeRendering() {}
//
//        public static ShapeRendering create( String aValue )
//        {
//            if( GWT.isScript() ) { return createScript( aValue ); }
//            else                 { return createHosted( aValue ); }
//        }
//
//        static ShapeRendering fromString( String aValue )
//        {
//            if( aValue.equals( "crispEdges" ) ) { return crispEdges; }
//            else                                { return auto;       }
//        }
//
//        public static native ShapeRendering createScript( String aValue ) /*-{ return aValue; }-*/;
//        public static native ShapeRendering createHosted( String aValue ) /*-{ this[0] = [aValue]; return this[0]; }-*/;
//
//        public static final ShapeRendering crispEdges = create( "crispEdges" );
//        public static final ShapeRendering auto       = create( "auto"       );
//    }
//
//    public static final class StrokeLinecap extends JavaScriptObject
//    {
//        protected StrokeLinecap() {}
//
//        public static StrokeLinecap create( String aValue )
//        {
//            if( GWT.isScript() ) { return createScript( aValue ); }
//            else                 { return createHosted( aValue ); }
//        }
//
//        public static native StrokeLinecap createScript( String aValue ) /*-{ return aValue; }-*/;
//        public static native StrokeLinecap createHosted( String aValue ) /*-{ this[0] = [aValue]; return this[0]; }-*/;
//
//        public static final StrokeLinecap square = create( "square" );
//    }
//
//    public static final class Units extends JavaScriptObject
//    {
//        protected Units() {}
//
//        public static Units create( String aValue )
//        {
//            if( GWT.isScript() ) { return createScript( aValue ); }
//            else                 { return createHosted( aValue ); }
//        }
//
//        public static native Units createScript( String aValue ) /*-{ return aValue; }-*/;
//        public static native Units createHosted( String aValue ) /*-{ this[0] = [aValue]; return this[0]; }-*/;
//
//        public static final Units userSpaceOnUse    = create( "userSpaceOnUse"    );
//        public static final Units objectBoundingBox = create( "objectBoundingBox" );
//    }
//
//    private static final class BoundingBox extends JavaScriptObject
//    {
//        protected BoundingBox() {}
//
//        public native double getX     () /*-{ return this.x;      }-*/;
//        public native double getY     () /*-{ return this.y;      }-*/;
//        public native double getWidth () /*-{ return this.width;  }-*/;
//        public native double getHeight() /*-{ return this.height; }-*/;
//    }
//
//
//    private static       double                  sID;
//    private static final String                  sNone       = "none";
//    private static final Map<String, SVGElement> sPrototypes = new HashMap<String, SVGElement>();
//}
