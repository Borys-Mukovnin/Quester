package com.borysmukovnin.quester.Models.Conditions

import com.borysmukovnin.quester.Models.Condition
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

    override fun isFulfilled(player: Player): Boolean {
        val hunger = player.foodLevel

        return hunger in _minHunger.._maxHunger
    }
}