package com.borysmukovnin.quester.models.conditions

import com.borysmukovnin.quester.models.dataclasses.Condition
import org.bukkit.block.Biome
import org.bukkit.entity.Player

class BiomeCondition : Condition {
    private var _biome: MutableList<Biome> = mutableListOf(Biome.PLAINS)

    var Biomes: MutableList<Biome>
        get() = _biome
        set(value) {
            _biome = value
        }

    override fun isMet(player: Player): Boolean {
        val location = player.location
        val biomeMatches = _biome.any { bio ->
            location.world?.getBiome(location.blockX, location.blockY, location.blockZ) == bio
        }
        return biomeMatches
    }

    override fun deepCopy(): Condition {
        val copy = BiomeCondition()
        copy.Biomes = this._biome.toMutableList()
        return copy
    }

}