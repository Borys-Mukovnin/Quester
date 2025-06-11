package com.borysmukovnin.quester.models.conditions

import com.borysmukovnin.quester.models.Condition
import org.bukkit.entity.Player

class HungerCondition : Condition {
    private var _minHunger: Int = 0
    private var _maxHunger: Int = 99999

    var MinHunger: Int
        get() = _minHunger
        set(value) {
            _minHunger = value
        }
    var MaxHunger: Int
        get() = _maxHunger
        set(value) {
            _maxHunger = value
        }

    override fun isMet(player: Player): Boolean {
        val hunger = player.foodLevel

        return hunger in _minHunger.._maxHunger
    }
    override fun deepCopy(): Condition {
        val copy = HungerCondition()
        copy.MinHunger = this._minHunger
        copy.MaxHunger = this._maxHunger
        return copy
    }
}