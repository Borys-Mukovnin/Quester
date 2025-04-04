package com.borysmukovnin.quester.Models.Conditions

import com.borysmukovnin.quester.Models.Condition
import org.bukkit.entity.Player

class HealthCondition : Condition {
    private var _minHealth: Double = 0.0
    private var _maxHealth: Double = 99999.0

    var MinExp: Double
        get() = _minHealth
        set(value) {
            _minHealth = value
        }
    var MaxExp: Double
        get() = _maxHealth
        set(value) {
            _maxHealth = value
        }

    override fun isFulfiled(player: Player): Boolean {
        val health = player.health

        return health in _minHealth.._maxHealth
    }
}