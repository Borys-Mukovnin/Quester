package com.borysmukovnin.quester.utils

import org.bukkit.Bukkit
import org.bukkit.enchantments.Enchantment

object MainUtils {
    fun getEnchantmentFromString(enchantmentName: String): Enchantment? {
        // Map the enchantment names to their respective Enchantment objects
        val enchantmentMap = mapOf(
            "sharpness" to Enchantment.DAMAGE_ALL,
            "efficiency" to Enchantment.DIG_SPEED,
            "unbreaking" to Enchantment.DURABILITY,
            "fortune" to Enchantment.LOOT_BONUS_BLOCKS,
            "mending" to Enchantment.MENDING,
            "looting" to Enchantment.LOOT_BONUS_MOBS,
            "fire_aspect" to Enchantment.FIRE_ASPECT,
            "knockback" to Enchantment.KNOCKBACK,
            "protection" to Enchantment.PROTECTION_ENVIRONMENTAL,
            "fire_protection" to Enchantment.PROTECTION_FIRE,
            "feather_falling" to Enchantment.PROTECTION_FALL,
            "blast_protection" to Enchantment.PROTECTION_EXPLOSIONS,
            "projectile_protection" to Enchantment.PROTECTION_PROJECTILE,
            "respiration" to Enchantment.OXYGEN,
            "aqua_affinity" to Enchantment.WATER_WORKER,
            "thorns" to Enchantment.THORNS,
            "depth_strider" to Enchantment.DEPTH_STRIDER,
            "frost_walker" to Enchantment.FROST_WALKER,
            "binding_curse" to Enchantment.BINDING_CURSE,
            "vanishing_curse" to Enchantment.VANISHING_CURSE,
            "luck_of_the_sea" to Enchantment.LUCK,
            "lure" to Enchantment.LURE,
            "power" to Enchantment.ARROW_DAMAGE,
            "punch" to Enchantment.ARROW_KNOCKBACK,
            "flame" to Enchantment.ARROW_FIRE,
            "infinity" to Enchantment.ARROW_INFINITE,
            "loyalty" to Enchantment.LOYALTY,
            "impaling" to Enchantment.IMPALING,
            "channeling" to Enchantment.CHANNELING,
            "quick_charge" to Enchantment.QUICK_CHARGE,
            "multishot" to Enchantment.MULTISHOT,
            "piercing" to Enchantment.PIERCING,
            "smite" to Enchantment.DAMAGE_UNDEAD,
            "bane_of_arthropods" to Enchantment.DAMAGE_ARTHROPODS,
            "sharpness" to Enchantment.DAMAGE_ALL,
            "silk_touch" to Enchantment.SILK_TOUCH,
            "fortune" to Enchantment.LOOT_BONUS_BLOCKS,
            "fortune" to Enchantment.LOOT_BONUS_BLOCKS,
            "silk_touch" to Enchantment.SILK_TOUCH,
            "soul_speed" to Enchantment.SOUL_SPEED,
            "sweeping" to Enchantment.SWEEPING_EDGE
        )

        return enchantmentMap[enchantmentName.lowercase()]
    }
}