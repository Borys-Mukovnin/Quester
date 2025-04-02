package com.borysmukovnin.quester.Models.Conditions

import com.borysmukovnin.quester.Models.Condition
import org.bukkit.entity.Player

class WeatherCondition : Condition {
    private var _weather: Weather? = null

    var Weathers: Weather?
        get() = _weather
        set(value) {
            _weather = value
        }

    override fun isFulfiled(player: Player): Boolean {
        if (_weather == Weather.Rain) {
            return player.world.hasStorm()
        }
        if (_weather == Weather.Thunder) {
            return player.world.isThundering
        }
        return true
    }
}
enum class Weather {
    Rain,
    Thunder
}