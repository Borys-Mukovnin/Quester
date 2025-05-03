package com.borysmukovnin.quester.Models.Conditions

import com.borysmukovnin.quester.Models.Condition
import org.bukkit.entity.Player

class HealthCondition : Condition {
    private var _minHealth: Double = 0.0
    private var _maxHealth: Double = 99999.0

    var MinHealth: Double
        get() = _minHealth
        set(value) {
            _minHealth = value
        }
    var MaxHealth: Double
        get() = _maxHealth
        set(value) {
            _maxHealth = value
        }

    override fun isFulfilled(player: Player): Boolean {
        val health = player.health

        return health in _minHealth.._maxHealth
    }
}