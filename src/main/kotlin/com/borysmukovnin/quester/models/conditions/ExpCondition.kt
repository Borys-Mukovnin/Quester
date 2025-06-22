package com.borysmukovnin.quester.models.conditions

import com.borysmukovnin.quester.models.dataclasses.Condition
import org.bukkit.entity.Player

class ExpCondition : Condition {
    private var _minExp: Int = 0
    private var _maxExp: Int = 99999

    var MinExp: Int
        get() = _minExp
        set(value) {
            _minExp = value
        }
    var MaxExp: Int
        get() = _maxExp
        set(value) {
            _maxExp = value
        }

    override fun isMet(player: Player): Boolean {
        val exp = player.level

        return exp in _minExp.._maxExp
    }
    override fun deepCopy(): Condition {
        val copy = ExpCondition()
        copy.MinExp = this._minExp
        copy.MaxExp = this._maxExp
        return copy
    }

}