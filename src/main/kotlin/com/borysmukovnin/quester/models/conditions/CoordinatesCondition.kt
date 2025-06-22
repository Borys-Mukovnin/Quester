package com.borysmukovnin.quester.models.conditions

import com.borysmukovnin.quester.models.dataclasses.Condition
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

class CoordinatesCondition : Condition {
    private var _location: MutableList<Location> = mutableListOf(Location(Bukkit.getServer().getWorld("world"),0.0,0.0,0.0))

    var Location: MutableList<Location>
        get() = _location
        set(value) {
            _location = value
        }

    override fun isMet(player: Player): Boolean {
        val locationMatch = _location.any {loc ->
            player.location == loc
        }
        return locationMatch
    }

    override fun deepCopy(): Condition {
        val copy = CoordinatesCondition()
        copy.Location = this._location.map { it.clone() }.toMutableList()
        return copy
    }

}