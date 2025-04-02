package com.borysmukovnin.quester.Models.Conditions

import com.borysmukovnin.quester.Models.Condition
import org.bukkit.block.Biome
import org.bukkit.entity.Player

class BiomeCondition : Condition {
    private var _biome: MutableList<Biome> = mutableListOf(Biome.PLAINS)

    var Biomes: MutableList<Biome>
        get() = _biome
        set(value) {
            _biome = value
        }

    override fun isFulfiled(player: Player): Boolean {
        val location = player.location
        val biomeMatches = _biome.any { bio ->
            location.world?.getBiome(location.blockX, location.blockY, location.blockZ) == bio
        }
        return biomeMatches
    }
}