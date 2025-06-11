package com.borysmukovnin.quester.models.conditions

import com.borysmukovnin.quester.models.Condition
import com.borysmukovnin.quester.models.Weather
import org.bukkit.entity.Player

class WeatherCondition : Condition {
    private var _weather: Weather = Weather.ANY

    var Weathers: Weather
        get() = _weather
        set(value) {
            _weather = value
        }

    override fun isMet(player: Player): Boolean {
        if (_weather == Weather.RAIN) {
            return player.world.hasStorm()
        }
        if (_weather == Weather.THUNDER) {
            return player.world.isThundering
        }
        return true
    }
    override fun deepCopy(): Condition {
        val copy = WeatherCondition()
        copy.Weathers = this._weather
        return copy
    }

}