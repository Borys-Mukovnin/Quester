package com.borysmukovnin.quester.Models.Conditions

import com.borysmukovnin.quester.Models.Condition
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

    override fun isFulfiled(player: Player): Boolean {
        val locationMatch = _location.any {loc ->
            player.location == loc
        }
        return locationMatch
    }
}