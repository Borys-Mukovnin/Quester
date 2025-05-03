package com.borysmukovnin.quester.Models.Conditions

import com.borysmukovnin.quester.Models.Condition
import com.borysmukovnin.quester.Models.Weather
import org.bukkit.entity.Player

class WeatherCondition : Condition {
    private var _weather: Weather = Weather.ANY

    var Weathers: Weather
        get() = _weather
        set(value) {
            _weather = value
        }

    override fun isFulfilled(player: Player): Boolean {
        if (_weather == Weather.RAIN) {
            return player.world.hasStorm()
        }
        if (_weather == Weather.THUNDER) {
            return player.world.isThundering
        }
        return true
    }
}