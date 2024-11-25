
package com.maya.newbulgariankeyboard.text_inputs.keyboard_gestures_input

import android.content.Context
import android.view.MotionEvent
import com.maya.newbulgariankeyboard.R
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan


abstract class LatestSwippingGesturring {

    class AppGestureDetector(private val context: Context, private val listener: Listener) {
        private val eventList: MutableList<MotionEvent> = mutableListOf()
        private var indexFirst: Int = 0
        private var indexLastMoveRecognized: Int = 0

        var latestGestureDistanceValues: LatestGestureDistanceValues = LatestGestureDistanceValues.NORMAL
        var latestSpeedHelperGesture: LatestSpeedHelperGesture = LatestSpeedHelperGesture.NORMAL

        fun onTouchEvent(event: MotionEvent): Boolean {
            try {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        clearEventList()
                        eventList.add(MotionEvent.obtainNoHistory(event))
                    }
                    MotionEvent.ACTION_MOVE -> {
                        eventList.add(MotionEvent.obtainNoHistory(event))
                        val lastEvent = eventList[indexLastMoveRecognized]
                        val diffX = event.x - lastEvent.x
                        val diffY = event.y - lastEvent.y
                        val distanceThresholdNV = numericValue(latestGestureDistanceValues) / 4.0f
                        return if (abs(diffX) > distanceThresholdNV || abs(diffY) > distanceThresholdNV) {
                            indexLastMoveRecognized = eventList.size - 1
                            val direction = detectDirection(diffX.toDouble(), diffY.toDouble())
                            listener.onSwipe(direction, Type.TOUCH_MOVE)
                        } else {
                            false
                        }
                    }
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_POINTER_UP -> {
                        val firstEvent = eventList[indexFirst]
                        val diffX = event.x - firstEvent.x
                        val diffY = event.y - firstEvent.y
                        val distanceThresholdNV = numericValue(latestGestureDistanceValues)
                        clearEventList()
                        return if ((abs(diffX) > distanceThresholdNV || abs(diffY) > distanceThresholdNV)) {
                            val direction = detectDirection(diffX.toDouble(), diffY.toDouble())
                            listener.onSwipe(direction, Type.TOUCH_UP)
                        } else {
                            false
                        }
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        clearEventList()
                    }
                    else -> return false
                }
                return false
            } catch(e: Exception) {
                return false
            }
        }

        private fun angle(diffX: Double, diffY: Double): Double {
            val tmpAngle = abs(360 * atan(diffY / diffX) / (2 * PI))
            return if (diffX < 0 && diffY >= 0) {
                180.0f - tmpAngle
            } else if (diffX < 0 && diffY < 0) {
                180.0f + tmpAngle
            } else if (diffX >= 0 && diffY < 0) {
                360.0f - tmpAngle
            } else {
                tmpAngle
            }
        }

        private fun detectDirection(diffX: Double, diffY: Double): Direction {
            val diffAngle = angle(diffX, diffY) / 360
            return when {
                diffAngle >= (1/16.0f) && diffAngle < (3/16.0f) ->      Direction.DOWN_RIGHT
                diffAngle >= (3/16.0f) && diffAngle < (5/16.0f) ->      Direction.DOWN
                diffAngle >= (5/16.0f) && diffAngle < (7/16.0f) ->      Direction.DOWN_LEFT
                diffAngle >= (7/16.0f) && diffAngle < (9/16.0f) ->      Direction.LEFT
                diffAngle >= (9/16.0f) && diffAngle < (11/16.0f) ->     Direction.UP_LEFT
                diffAngle >= (11/16.0f) && diffAngle < (13/16.0f) ->    Direction.UP
                diffAngle >= (13/16.0f) && diffAngle < (15/16.0f) ->    Direction.UP_RIGHT
                else ->                                                 Direction.RIGHT
            }
        }

        private fun clearEventList() {
            for (event in eventList) {
                event.recycle()
            }
            eventList.clear()
            indexFirst = 0
            indexLastMoveRecognized = 0
        }

        private fun numericValue(of: LatestGestureDistanceValues): Double {
            return when (of) {
                LatestGestureDistanceValues.VERY_SHORT -> context.resources.getDimension(R.dimen.action_value_very_short)
                LatestGestureDistanceValues.SHORT -> context.resources.getDimension(R.dimen.action_value_short)
                LatestGestureDistanceValues.NORMAL -> context.resources.getDimension(R.dimen.action_value_normal)
                LatestGestureDistanceValues.LONG -> context.resources.getDimension(R.dimen.action_value_long)
                LatestGestureDistanceValues.VERY_LONG -> context.resources.getDimension(R.dimen.action_value_very_long)
            }.toDouble()
        }

        private fun numericValue(of: LatestSpeedHelperGesture): Double {
            return when (of) {
                LatestSpeedHelperGesture.VERY_SLOW -> context.resources.getInteger(R.integer.action_value_very_slow)
                LatestSpeedHelperGesture.SLOW -> context.resources.getInteger(R.integer.action_value_slow)
                LatestSpeedHelperGesture.NORMAL -> context.resources.getInteger(R.integer.action_value_normal)
                LatestSpeedHelperGesture.FAST -> context.resources.getInteger(R.integer.action_value_fast)
                LatestSpeedHelperGesture.VERY_FAST -> context.resources.getInteger(R.integer.action_value_vel_very_fast)
            }.toDouble()
        }
    }

    interface Listener {
        fun onSwipe(direction: Direction, type: Type): Boolean
    }

    enum class Direction {
        UP_LEFT,
        UP,
        UP_RIGHT,
        RIGHT,
        DOWN_RIGHT,
        DOWN,
        DOWN_LEFT,
        LEFT,
    }

    enum class Type {
        TOUCH_UP,
        TOUCH_MOVE;
    }
}
