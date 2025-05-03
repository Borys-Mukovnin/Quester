package com.borysmukovnin.quester.Models.Conditions

import com.borysmukovnin.quester.Models.Condition
import org.bukkit.entity.Player

class TimeCondition : Condition {
    private var _startTime: Long = 0
    private var _endTime: Long = 24000

    var StartTime: Long
        get() = _startTime
        set(value) {
            _startTime = value
        }
    var EndTime: Long
        get() = _endTime
        set(value) {
            _endTime = value
        }

    override fun isFulfilled(player: Player) : Boolean {
        val time = player.world.time

        return time in _startTime.._endTime
    }
}