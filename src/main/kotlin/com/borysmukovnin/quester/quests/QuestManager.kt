package com.borysmukovnin.quester.quests

import com.borysmukovnin.quester.Models.*
import com.borysmukovnin.quester.Models.Conditions.*
import com.borysmukovnin.quester.Models.Objectives.*
import com.borysmukovnin.quester.Quester
import com.borysmukovnin.quester.utils.MainUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.*

class QuestManager(private val plugin: Quester) {

    private val activePlayersQuests: MutableMap<UUID,List<Quest>> = mutableMapOf()
    private val questList: MutableMap<String,Quest> = mutableMapOf()
    private val questsFolder: File = File(plugin.dataFolder, "quests")

    fun loadActivePlayersQuests(playerUUID: UUID) {
        val progressFolder = File(plugin.dataFolder, "progress")

        val fileToSearch = playerUUID.toString()
        val file = progressFolder.listFiles()?.find { it.name.equals("${fileToSearch}.yml", ignoreCase = true) } ?: return
        val config = YamlConfiguration.loadConfiguration(file)

        val playerName = config.getString("player_name")

        val questsSection = config.getConfigurationSection("active_quests")
    }

    fun loadAllQuests() {
        questsFolder.listFiles()?.forEach { file ->
            val quest: Quest? = CreateQuestData(YamlConfiguration.loadConfiguration(file))
            if (quest != null) {
                questList[file.name] = quest
            }
        }
    }

    private fun CreateQuestData(config: YamlConfiguration): Quest? {
        val stagesList: MutableMap<String,Stage> = mutableMapOf()

        val questName = config.getString("name") ?: "unknown name"

        val questDesc = config.getStringList("description")

        val startConditions = config.getStringList("start_conditions")

        val stagesSection = config.getConfigurationSection("stages") ?: run { return null }
        val stageNames = stagesSection.getKeys(false)

        for (stageName in stageNames) {
            val stageConfig = stagesSection.getConfigurationSection(stageName) ?: continue

            val stageSubName = stageConfig.getString("name") ?: "unknown name"
            val stageDescription = stageConfig.getStringList("description")
            val stageObjectives = ParseObjectiveList(stageConfig.getStringList("objectives"))
            val stageRewards = stageConfig.getStringList("rewards")

            stagesList[stageName] = Stage(stageSubName,stageDescription,stageObjectives,stageRewards)
        }

        return Quest(questName,questDesc,startConditions,stagesList)
    }

//    private fun ParseConditionsList(conditionsList: MutableList<String>)

    private fun ParseConditionsList(conditionsList: List<String>) : List<Condition> {
        val conList: MutableList<Condition> = mutableListOf()

        conditionsList.forEach { c ->
            val conSplit = c.split(" ")
            var condition: Condition = AdvancementCondition()

            when(conSplit[0].uppercase()) {
                "TIME" -> {
                    condition = TimeCondition().apply {

                        val startTime = conSplit.getOrNull(1)
                        val endTime = conSplit.getOrNull(2)

                        if (startTime != null) {
                            if (startTime.toLongOrNull() != null) {
                                StartTime = startTime.toLong()
                            }
                        }

                        if (endTime != null) {
                            if (endTime.toLongOrNull() != null) {
                                EndTime = endTime.toLong()
                            }
                        }
                    }
                }
                "EXP" -> {
                    condition = ExpCondition().apply {

                        val minExp = conSplit.getOrNull(1)
                        val maxExp = conSplit.getOrNull(2)

                        if (minExp != null) {
                            if (minExp.toIntOrNull() != null) {
                                MinExp = minExp.toInt()
                            }
                        }
                        if (maxExp != null) {
                            if (maxExp.toIntOrNull() != null) {
                                MaxExp = maxExp.toInt()
                            }
                        }
                    }
                }
                "HEALTH" -> {
                    condition = HealthCondition().apply {
                        val minHealth = conSplit.getOrNull(1)
                        val maxHealth = conSplit.getOrNull(2)

                        if (minHealth != null) {
                            if (minHealth.toDoubleOrNull() != null) {
                                MinHealth = minHealth.toDouble()
                            }
                        }
                        if (maxHealth != null) {
                            if (maxHealth.toDoubleOrNull() != null) {
                                MaxHealth = maxHealth.toDouble()
                            }
                        }
                    }
                }
                "HUNGER" -> {
                    condition = HungerCondition().apply {
                        val minHunger = conSplit.getOrNull(1)
                        val maxHunger = conSplit.getOrNull(2)

                        if (minHunger != null) {
                            if (minHunger.toIntOrNull() != null) {
                                MinHunger = minHunger.toInt()
                            }
                        }
                        if (maxHunger != null) {
                            if (maxHunger.toIntOrNull() != null) {
                                MaxHunger = maxHunger.toInt()
                            }
                        }
                    }
                }
                "HAS_ITEM" -> {
                    condition = ItemCondition().apply {
                        val locationsList = conSplit.getOrNull(1)
                        val locations: MutableList<ItemLocation> = mutableListOf()
                        if (locationsList != null) {
                            locationsList.split(",").forEach { l ->
                                when (l) {
                                    "OFFHAND" -> {
                                        locations.add(ItemLocation.OFF_HAND)
                                    }
                                    "MAINHAND" -> {
                                        locations.add(ItemLocation.MAIN_HAND)
                                    }
                                    "INVENTORY" -> {
                                        locations.add(ItemLocation.INVENTORY)
                                    }
                                    else -> {
                                        locations.add(ItemLocation.ANY)
                                    }
                                }
                            }
                            Location = locations
                        }

                        val itemList = conSplit.getOrNull(2)
                        val items: MutableList<ItemStack> = mutableListOf()
                        if (itemList != null) {
                            itemList.split(",").forEach { i ->
                                items.add(ItemStack(MainUtils.parseMaterial(i)))
                            }
                            ItemType = items
                        }

                        val amount = conSplit.getOrNull(3)
                        if (amount != null) {
                            if (amount.toIntOrNull() != null) {
                                Amount = amount.toInt()
                            }
                        }
                    }
                }
                "PERMISSION" -> {
                    condition = PermissionCondition().apply {
                        val permissionList = conSplit.getOrNull(1)
                        if (permissionList != null) {
                            Permission = permissionList.split(",").toMutableList()
                        }
                    }
                }
                "COORDINATES" -> {
                    condition = CoordinatesCondition().apply {
                        val x = conSplit.getOrNull(1)
                        val y = conSplit.getOrNull(2)
                        val z = conSplit.getOrNull(3)
                        val world = conSplit.getOrNull(4)
                        if (x != null && y != null && z != null && world != null) {
                            Location = mutableListOf(Location(Bukkit.getServer().getWorld(world),x.toDouble(),y.toDouble(),z.toDouble()))
                        }
                    }
                }
                "BIOME" -> {
                    condition = BiomeCondition().apply {
                        val biomeList = conSplit.getOrNull(1)
                        val biomes: MutableList<Biome> = mutableListOf()

                        if (biomeList != null) {
                            biomeList.split(",").forEach { b ->
                                biomes.add(Biome.valueOf(b))
                            }
                            Biomes = biomes
                        }
                    }
                }
                "ADVANCEMENT" -> {
                    condition = AdvancementCondition().apply {
                        val advList = conSplit.getOrNull(1)
                        val advancements: MutableList<String> = mutableListOf()

                        if (advList != null) {
                            advList.split(",").forEach { a ->
                                advancements.add("minecraft:$a")
                            }
                        }
                        Advancements = advancements
                    }
                }
                "WEATHER" -> {
                    condition = WeatherCondition().apply {
                        val weather = conSplit.getOrNull(1) ?: "ANY"

                        when (weather.uppercase()) {
                            "RAIN" -> Weathers = Weather.RAIN
                            "THUNDER" -> Weathers = Weather.THUNDER
                            else -> Weathers = Weather.ANY
                        }
                    }
                }
                "BLOCK" -> {
                    condition =BlockCondition().apply {
                        val blockList = conSplit.getOrNull(1)
                        val blocks: MutableList<Material> = mutableListOf()

                        if (blockList != null) {
                            blockList.split(",").forEach { b ->
                                blocks.add(MainUtils.parseMaterial(b))
                            }
                            Block = blocks
                        }
                    }
                }
                "WORLD" -> {
                    condition = WorldCondition().apply {
                        val worldList = conSplit.getOrNull(1)
                        val worlds: MutableList<World> = mutableListOf()

                        if (worldList != null) {
                            worldList.split(",").forEach { w ->
                                worlds.add(Bukkit.getServer().getWorld(w)!!)
                            }
                        }
                    }
                }
            }
            conList.add(condition)
        }

        return conList
    }

    private fun ParseObjectiveList(objectivesList: List<String>) : List<Objective> {
        val objList: MutableList<Objective> = emptyList<Objective>().toMutableList()

        objectivesList.forEach { o ->
            val objectiveSplit = o.split(" ")
            var objective: Objective? = null

            when (objectiveSplit[0].uppercase()) {
                "KILL" -> {
                    objective = KillObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val targetList: MutableList<EntityType> = mutableListOf()
                            val targetString: List<String> = objectiveSplit[1].split(",")
                            targetString.forEach { e ->
                                targetList.add(EntityType.valueOf(e.uppercase()))
                            }
                            Target = targetList
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            TargetAmount = objectiveSplit.getOrNull(2)!!.toIntOrNull()!!
                        }
                        if (objectiveSplit.getOrNull(3) != null && objectiveSplit.getOrNull(3) != "ANY") {
                            val itemList: MutableList<ItemStack> = mutableListOf()
                            val itemString: List<String> = objectiveSplit[3].split(",")
                            itemString.forEach { i ->
                                itemList.add(ItemStack(MainUtils.parseMaterial(i)))
                            }
                            Item = itemList
                        }
                    }
                }
                "EXP" -> {
                    objective = ExpObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null) {
                            if (enumValues<ExpMode>().any { it.name == objectiveSplit[1].uppercase() }) {
                                Mode = ExpMode.valueOf(objectiveSplit[1].uppercase())
                            }
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }
                "CRAFT" -> {
                    objective = CraftObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val itemList: MutableList<ItemStack> = mutableListOf()
                            val itemString: List<String> = objectiveSplit[1].split(",")
                            itemString.forEach { i ->
                                itemList.add(ItemStack(MainUtils.parseMaterial(i)))
                            }
                            Item = itemList
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }
                "PLACE" -> {
                    objective = PlaceObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val blockList: MutableList<Material> = mutableListOf()
                            val blockString: List<String> = objectiveSplit[1].split(",")
                            blockString.forEach { b ->
                                blockList.add(MainUtils.parseMaterial(b))
                            }
                            Block = blockList
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }
                "INTERACT" -> {
                    objective = InteractObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val blockList: MutableList<Material> = mutableListOf()
                            val blockString: List<String> = objectiveSplit[1].split(",")
                            blockString.forEach { b ->
                                blockList.add(MainUtils.parseMaterial(b))
                            }
                            Block = blockList
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }
                "MINE" -> {
                    objective = MineObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val blockList: MutableList<Material> = mutableListOf()
                            val blockString: List<String> = objectiveSplit[1].split(",")
                            blockString.forEach { b ->
                                blockList.add(MainUtils.parseMaterial(b))
                            }
                            Block = blockList
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }
                "USE" -> {
                    objective = UseObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val blockList: MutableList<Material> = mutableListOf()
                            val blockString: List<String> = objectiveSplit[1].split(",")
                            blockString.forEach { b ->
                                blockList.add(MainUtils.parseMaterial(b))
                            }
                            Block = blockList
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }
                "TRADE" -> {
                    objective = TradeObjective().apply {
                        if (objectiveSplit.getOrNull(1)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(1)!!.toInt()
                        }
                    }
                }
                "ENCHANT" -> {
                    objective = EnchantObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val enchantList: MutableList<Enchantment> = mutableListOf()
                            val enchantString: List<String> = objectiveSplit[1].split(",")
                            enchantString.forEach { e ->
                                enchantList.add(MainUtils.getEnchantmentFromString(e)!!)
                            }
                            Enchant = enchantList
                        }
                        if (objectiveSplit.getOrNull(2) != null && objectiveSplit.getOrNull(2) != "ANY") {
                            val itemList: MutableList<ItemStack> = mutableListOf()
                            val itemString: List<String> = objectiveSplit[2].split(",")
                            itemString.forEach { i ->
                                itemList.add(ItemStack(MainUtils.parseMaterial(i)))
                            }
                            Item = itemList
                        }
                        if (objectiveSplit.getOrNull(3)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(3)!!.toInt()
                        }
                    }
                }
                "PICK" -> {
                    objective = PickObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val itemList: MutableList<ItemStack> = mutableListOf()
                            val itemString: List<String> = objectiveSplit[1].split(",")
                            itemString.forEach { i ->
                                itemList.add(ItemStack(MainUtils.parseMaterial(i)))
                            }
                            Item = itemList
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }
                "LOOT" -> {
                    objective = LootObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(1) != "ANY") {
                            val itemList: MutableList<ItemStack> = mutableListOf()
                            val itemString: List<String> = objectiveSplit[1].split(",")
                            itemString.forEach { i ->
                                itemList.add(ItemStack(MainUtils.parseMaterial(i)))
                            }
                            Item = itemList
                        }
                        if (objectiveSplit.getOrNull(2)?.toIntOrNull() != null) {
                            Amount = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }
                "GOTO" -> {
                    objective = GotoObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(2)?.toDoubleOrNull() != null && objectiveSplit.getOrNull(3)?.toDoubleOrNull() != null && objectiveSplit.getOrNull(4) != null) {
                            Goto = Location(Bukkit.getWorld(objectiveSplit[4]),objectiveSplit[1].toDouble(),objectiveSplit[2].toDouble(),objectiveSplit[3].toDouble())
                        }
                    }
                }
                "TRAVEL" -> {
                    objective = TravelObjective().apply {
                        if (objectiveSplit.getOrNull(1)?.toIntOrNull() != null) {
                            Amount = objectiveSplit[1].toInt()
                        }
                    }
                }
                "COMMAND" -> {
                    objective = CommandObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null) {
                            Command = objectiveSplit.drop(1).joinToString(" ")
                        }
                    }
                }
                else -> {
                    objective = KillObjective()
                }
            }

            objList.add(objective)
        }

        return objList
    }
}