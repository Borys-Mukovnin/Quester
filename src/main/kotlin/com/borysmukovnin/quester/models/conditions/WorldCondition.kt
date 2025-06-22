package com.borysmukovnin.quester.models.conditions

import com.borysmukovnin.quester.models.dataclasses.Condition
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player

class WorldCondition : Condition {
    private var _world: MutableList<World> = mutableListOf(Bukkit.getServer().getWorld("world")!!)

    var Worlds: MutableList<World>
        get() = _world
        set(value) {
            _world = value
        }

    override fun isMet(player: Player): Boolean {
        val worldMatch = _world.any { w ->
            player.location.world == w
        }
        return worldMatch
    }
    override fun deepCopy(): Condition {
        val copy = WorldCondition()
        copy.Worlds = this._world.toMutableList()
        return copy
    }

}