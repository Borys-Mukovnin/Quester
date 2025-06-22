package com.borysmukovnin.quester.models.conditions

import com.borysmukovnin.quester.models.dataclasses.Condition
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

    override fun isMet(player: Player) : Boolean {
        val time = player.world.time

        return time in _startTime.._endTime
    }
    override fun deepCopy(): Condition {
        val copy = TimeCondition()
        copy.StartTime = this._startTime
        copy.EndTime = this._endTime
        return copy
    }

}