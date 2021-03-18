package io.nacular.doodle.event

import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.system.SystemInputEvent.Modifier
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.doodle.system.SystemPointerEvent.Button
import io.nacular.doodle.system.SystemPointerEvent.Type

/**
 * Represents a pointing device (Mouse, Pen, Touch, etc.).
 */
public inline class Pointer(internal val id: Int)

/**
 * Represents an interaction with a View by a [Pointer].
 *
 * @constructor
 * @param pointer responsible for the interaction
 * @param target of the interaction
 * @param state the Pointer is in
 * @param location of the Pointer within its [target]
 *
 * @property pointer responsible for the interaction
 * @property target of the interaction
 * @property state the Pointer is in
 * @property location of the Pointer within its [target]
 */
public class Interaction internal constructor(public val pointer: Pointer, public val target: View, public val state: Type, public val location: Point)

/**
 * Event triggered when a pointing device (Mouse, Pen, Touch, etc.) interacts with a View.
 *
 * @constructor
 * @param source receiving the notification
 * @param target where the Pointer interaction occurred
 * @param buttons that are pressed (applicable for Mouse)
 * @param targetInteractions active Pointers that started within the [target]
 * @param changedInteractions active Pointers that changed since the last event
 * @param allInteractions that are currently active (even those not directed at the [target])
 * @param modifiers that are pressed
 *
 * @property target where the Pointer interaction occurred
 * @property buttons that are pressed (applicable for Mouse)
 * @property targetInteractions active Pointers that started within the [target]
 * @property changedInteractions active Pointers that changed since the last event
 */
public class PointerEvent internal constructor(
                   source             : View,
        public val target             : View,
        public val buttons            : Set<Button>,
        public val clickCount         : Int,
        public val targetInteractions : List<Interaction>,
        public val changedInteractions: Set<Interaction>,
                   allInteractions    : () -> List<Interaction>,
                   modifiers          : Set<Modifier>): InputEvent(source, modifiers) {

    /** Pointers that are currently active (even those not directed at the [target]) */
    public val allInteractions: List<Interaction> by lazy { allInteractions() }

    /** Type of the first item in [changedInteractions] */
    public val type: Type  get() = changedInteractions.first().state

    /** Location of the first item in [changedInteractions] */
    public val location: Point get() = changedInteractions.first().location

    public companion object {
        @Internal
        public operator fun invoke(target: View, event: SystemPointerEvent): PointerEvent {
            val pointers = listOf(Interaction(Pointer(event.id), target, event.type, target.fromAbsolute(event.location)))

            return PointerEvent(target,
                                target,
                                event.buttons,
                                event.clickCount,
                                targetInteractions  = pointers,
                                changedInteractions = pointers.toSet(),
                                allInteractions     = { pointers },
                                modifiers           = event.modifiers)
        }
    }
}

internal fun PointerEvent.with(source: View) = PointerEvent(source, target, buttons, clickCount, targetInteractions, changedInteractions, { allInteractions }, modifiers)