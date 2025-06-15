package com.borysmukovnin.quester.quests

import com.borysmukovnin.quester.models.*
import com.borysmukovnin.quester.models.conditions.*
import com.borysmukovnin.quester.models.objectives.*
import com.borysmukovnin.quester.models.actions.CommandAction
import com.borysmukovnin.quester.models.actions.ExpAction
import com.borysmukovnin.quester.models.actions.ItemAction
import com.borysmukovnin.quester.Quester
import com.borysmukovnin.quester.utils.MainUtils
import com.borysmukovnin.quester.utils.PluginLogger
import com.borysmukovnin.quester.utils.deepCopy
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.yaml.snakeyaml.scanner.Constant
import java.io.File
import java.util.*

object QuestManager {

    lateinit var plugin: Quester

    fun init(plugin: Quester) {
        this.plugin = plugin
        this.reload()
    }

    fun reload() {
        this.loadConditions()
        this.loadActions()
        this.loadObjectives()

        this.loadQuests()
    }

    private val activePlayersQuests: MutableMap<UUID, MutableMap<String, Quest>> = mutableMapOf()
    private val activePlayersCompletedQuests: MutableMap<UUID, MutableList<String>> = mutableMapOf()
    private val quests: MutableMap<String, Quest> = mutableMapOf()
    private val actions: MutableMap<String, Action> = mutableMapOf()
    private val conditions: MutableMap<String, Condition> = mutableMapOf()
    private val objectives: MutableMap<String, Objective> = mutableMapOf()


    fun loadQuests() {
        quests.clear()

        val questsFolder = File(plugin.dataFolder, "quests")
        if (!questsFolder.exists()) return

        questsFolder.walkTopDown()
            .filter { it.isFile && it.extension.equals("yml", ignoreCase = true) }
            .forEach { file ->
                quests[file.nameWithoutExtension] = parseQuest(YamlConfiguration.loadConfiguration(file))
            }
    }

    fun startPlayerQuest(player: Player, questName: String) {
        if (activePlayersQuests[player.uniqueId]?.containsKey(questName) == true) {
            player.sendMessage("Quest is already active")
            return
        }

        val quest: Quest = quests[questName] ?: run {
            player.sendMessage("No such quest exists")
            return
        }

        activePlayersQuests[player.uniqueId]?.set(questName, quest)
    }

    fun stopPlayerQuest(player: Player, questName: String) {
        if (activePlayersQuests[player.uniqueId]?.containsKey(questName) == false) {
            player.sendMessage("Quest is not active")
            return
        }

        activePlayersQuests[player.uniqueId]?.remove(questName)
    }

    fun saveQuestProgressAsync(player: Player, quest: Quest) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            try {
                val playerFile = File(plugin.dataFolder, "progress/${player.uniqueId}.yml")
                if (!playerFile.exists()) {
                    playerFile.parentFile.mkdirs()
                    playerFile.createNewFile()
                }

                val config = YamlConfiguration.loadConfiguration(playerFile)

                val activeQuestsSection = config.getConfigurationSection("active_quests") ?: config.createSection("active_quests")
                val questSection = activeQuestsSection.createSection(quest.Name)
                val stagesSection = questSection.createSection("stages")

                for ((stageName, stage) in quest.Stages) {
                    val stageSection = stagesSection.createSection(stageName)
                    val objectiveProgress = stage.Objectives.map { it.ProgressCurrent }
                    stageSection.set("objectives", objectiveProgress)
                }

                config.save(playerFile)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    fun deleteQuestProgressAsync(player: Player, quest: Quest) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            try {
                val playerFile = File(plugin.dataFolder, "progress/${player.uniqueId}.yml")
                if (!playerFile.exists()) return@Runnable  // Nothing to delete

                val config = YamlConfiguration.loadConfiguration(playerFile)

                // Get "active_quests" section and remove the specific quest if it exists
                val activeQuestsSection = config.getConfigurationSection("active_quests")
                if (activeQuestsSection != null && activeQuestsSection.contains(quest.Name)) {
                    activeQuestsSection.set(quest.Name, null)

                    // Clean up the active_quests section if it's empty
                    if (activeQuestsSection.getKeys(false).isEmpty()) {
                        config.set("active_quests", null)
                    }

                    config.save(playerFile)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    fun loadActivePlayerQuests(player: Player) {
        val dir = File(plugin.dataFolder, "progress")

        val configFile = File(dir, "${player.uniqueId}.yml")
        if (!configFile.exists()) return

        val config = YamlConfiguration.loadConfiguration(configFile)
        val activeQuestsSection = config.getConfigurationSection("active_quests") ?: return

        val playerQuests: MutableMap<String, Quest> = mutableMapOf()
        for (questKey in activeQuestsSection.getKeys(false)) {
            val questSection = activeQuestsSection.getConfigurationSection(questKey) ?: return
            val stagesSection = questSection.getConfigurationSection("stages") ?: return

            for (stage in stagesSection.getKeys(false)) {
                val stageSection = stagesSection.getConfigurationSection(stage) ?: return
                val objectives = stageSection.getStringList("objectives")

                val quest: Quest = quests[questKey]?.deepCopy() ?: return
                val stage: Stage = quest.Stages[stage]?.deepCopy() ?: return

                if (objectives.size != stage.Objectives.size) return

                for (obj in stage.Objectives) {
                    val cur = objectives[1].toIntOrNull() ?: return

                    obj.ProgressCurrent = cur
                }
                playerQuests[questKey] = quest
            }
            activePlayersQuests[player.uniqueId] = playerQuests
        }

        val completedQuests: MutableList<String> = config.getStringList("completed_quests")
        activePlayersCompletedQuests[player.uniqueId] = completedQuests

    }

    private fun parseQuest(config: FileConfiguration): Quest {
        val name = config.getString("name") ?: error("Quest must have a name")
        val description = config.getStringList("description")

        val startConditions = config.getStringList("start_conditions").mapNotNull { conditions[it] }

        val stagesSection = config.getConfigurationSection("stages") ?: error("Quest must have stages")
        val stages = mutableMapOf<String, Stage>()

        for (key in stagesSection.getKeys(false)) {
            val stageSection = stagesSection.getConfigurationSection(key) ?: continue

            val stageName = stageSection.getString("name") ?: key
            val stageDescription = stageSection.getStringList("description")

            val objectiveIds = stageSection.getStringList("objectives")
            val objectiveList = objectiveIds.mapNotNull { objectives[it]?.deepCopy() }

            val actionIds = stageSection.getStringList("actions")
            val actionList = actionIds.mapNotNull { actions[it] }

            val stage = Stage(
                Name = stageName,
                Description = stageDescription,
                Objectives = objectiveList,
                actions = actionList
            )

            stages[key] = stage
        }

        return Quest(
            Name = name,
            Description = description,
            StartConditions = startConditions,
            Stages = stages
        )
    }

    private fun loadActions() {
        actions.clear()

        val actionsFolder = File(plugin.dataFolder, "actions")
        if (!actionsFolder.exists()) return

        actions.clear()

        actionsFolder.walkTopDown()
            .filter { it.isFile && it.extension == "yml" }
            .forEach { file ->
                val config = YamlConfiguration.loadConfiguration(file)
                for (key in config.getKeys(false)) {
                    val section = config.getConfigurationSection(key) ?: return@forEach
                    val type = section.getString("type")?.uppercase() ?: return@forEach

                    val action = when (type) {
                        "COMMAND" -> {
                            val command = section.getString("command") ?: ""
                            CommandAction().apply {
                                Command = command
                            }
                        }

                        "ITEM" -> {
                            val itemName = section.getString("item") ?: "DIRT"
                            val amount = section.getInt("amount", 1)
                            val itemStack = ItemStack(Material.valueOf(itemName), amount)
                            ItemAction().apply {
                                Item = itemStack
                            }
                        }

                        "EXP" -> {
                            val amount = section.getInt("amount", 0)
                            val modeStr = section.getString("mode") ?: "GAIN"
                            val mode = try {
                                Mode.valueOf(modeStr.uppercase())
                            } catch (e: IllegalArgumentException) {
                                Mode.GAIN
                            }

                            ExpAction().apply {
                                Mode = mode
                                Amount = amount
                            }
                        }

                        else -> return@forEach
                    }

                    actions[key] = action
                    PluginLogger.logInfo("Loaded action: $key")
                }
            }
    }

    private fun loadConditions() {
        conditions.clear()

        val conditionsFolder = File(plugin.dataFolder, "conditions")
        if (!conditionsFolder.exists()) return

        conditions.clear()

        conditionsFolder.walkTopDown()
            .filter { it.isFile && it.extension == "yml" }
            .forEach { file ->
                val config = YamlConfiguration.loadConfiguration(file)
                for (key in config.getKeys(false)) {
                    val section = config.getConfigurationSection(key) ?: continue
                    val type = section.getString("type")?.uppercase() ?: continue

                    val condition = when (type) {
                        "ADVANCEMENT" -> AdvancementCondition().apply {
                            Advancements = section.getStringList("advancements").map { "minecraft:$it" }.toMutableList()
                        }

                        "BIOME" -> BiomeCondition().apply {
                            Biomes = section.getStringList("biomes").map { Biome.valueOf(it.uppercase()) }.toMutableList()
                        }

                        "BLOCK" -> BlockCondition().apply {
                            Block = section.getStringList("blocks").map { MainUtils.parseMaterial(it) }.toMutableList()
                        }

                        "COORDINATES" -> CoordinatesCondition().apply {
                            val x = section.getDouble("x",0.0)
                            val y = section.getDouble("y",0.0)
                            val z = section.getDouble("z",0.0)
                            val world = Bukkit.getWorld(section.getString("world") ?: "world") ?: return@forEach
                            Location = mutableListOf(Location(world, x, y, z))
                        }

                        "EXP" -> ExpCondition().apply {
                            MinExp = section.getInt("min",0)
                            MaxExp = section.getInt("max",99999)
                        }

                        "HEALTH" -> HealthCondition().apply {
                            MinHealth = section.getDouble("min",0.0)
                            MaxHealth = section.getDouble("max",99999.0)
                        }

                        "HUNGER" -> HungerCondition().apply {
                            MinHunger = section.getInt("min",0)
                            MaxHunger = section.getInt("max",99999)
                        }

                        "ITEM" -> ItemCondition().apply {
                            val locStr = section.getString("location") ?: "ANY"
                            Location =
                                try {
                                    ItemLocation.valueOf(locStr.uppercase())
                                } catch (_: IllegalArgumentException) {
                                    ItemLocation.ANY
                                }

                            val items = section.getMapList("items")
                            val itemStacks = mutableListOf<ItemStack>()
                            var totalAmount = 0

                            items.forEach { itemMap ->
                                val matStr = itemMap["item"] as? String ?: return@forEach
                                val mat = MainUtils.parseMaterial(matStr)
                                val amt = (itemMap["amount"] as? Int) ?: 1

                                itemStacks.add(ItemStack(mat, amt))
                                totalAmount += amt
                            }

                            ItemType = itemStacks
                        }

                        "PERMISSION" -> PermissionCondition().apply {
                            Permission = section.getStringList("permissions").toMutableList()
                        }

                        "TIME" -> TimeCondition().apply {
                            StartTime = section.getLong("start")
                            EndTime = section.getLong("end")
                        }

                        "WEATHER" -> WeatherCondition().apply {
                            Weathers = when (section.getString("weather")?.uppercase()) {
                                "RAIN" -> Weather.RAIN
                                "THUNDER" -> Weather.THUNDER
                                else -> Weather.ANY
                            }
                        }

                        "WORLD" -> WorldCondition().apply {
                            val worldNames = section.getStringList("worlds")
                            val worldList = worldNames.mapNotNull { Bukkit.getWorld(it) }
                            Worlds = worldList.toMutableList()
                        }

                        else -> null
                    }

                    if (condition != null) {
                        conditions[key] = condition
                        PluginLogger.logInfo("Loaded condition: $key")
                    }
                }
            }
    }

    private fun loadObjectives() {
        objectives.clear()

        val objectivesDir = File(plugin.dataFolder, "objectives")
        if (!objectivesDir.exists() || !objectivesDir.isDirectory) return

        objectivesDir.walkTopDown().filter { it.isFile && it.extension == "yml" }.forEach { file ->
            val config = YamlConfiguration.loadConfiguration(file)
            for (key in config.getKeys(false)) {
                val section = config.getConfigurationSection(key) ?: continue
                val type = section.getString("type")?.uppercase() ?: continue

                val objective: Objective = when (type) {
                    "COMMAND" -> CommandObjective().apply {
                        Command = section.getString("command") ?: ""
                    }

                    "CRAFT" -> CraftObjective().apply {
                        val items = section.getStringList("items")
                        Item = if (items.isNotEmpty()) items.map { ItemStack(MainUtils.parseMaterial(it)) } else null
                        ProgressGoal = section.getInt("amount", 0)
                    }

                    "ENCHANT" -> EnchantObjective().apply {
                        val itemList = section.getStringList("items")
                        val enchantList = section.getStringList("enchants")

                        Item = if (itemList.isNotEmpty()) itemList.map { ItemStack(MainUtils.parseMaterial(it)) } else null
                        Enchant = if (enchantList.isNotEmpty()) enchantList.mapNotNull {
                            val split = it.split(" ")
                            MainUtils.getEnchantmentFromString(split[0])
                        } else null

                        ProgressGoal = section.getInt("amount", 0)
                    }

                    "EXP" -> ExpObjective().apply {
                        ProgressGoal = section.getInt("amount", 0)
                    }

                    "GOTO" -> GotoObjective().apply {
                        val world = Bukkit.getWorld(section.getString("world") ?: "world")
                        val x = section.getDouble("x")
                        val y = section.getDouble("y")
                        val z = section.getDouble("z")
                        Goto = Location(world, x, y, z)
                    }

                    "INTERACT" -> InteractObjective().apply {
                        val targets = section.getStringList("targets")
                        Block = if (targets.isNotEmpty()) targets.map { MainUtils.parseMaterial(it) } else null
                        ProgressGoal = section.getInt("amount", 0)
                    }

                    "KILL" -> KillObjective().apply {
                        val targets = section.getStringList("targets")
                        Target = if (targets.isNotEmpty()) targets.mapNotNull {
                            runCatching { EntityType.valueOf(it.uppercase()) }.getOrNull()
                        } else null

                        val items = section.getStringList("allowed_items")
                        Item = if (items.isNotEmpty()) items.map { ItemStack(MainUtils.parseMaterial(it)) } else null

                        ProgressGoal = section.getInt("amount", 0)
                    }

                    "LOOT" -> LootObjective().apply {
                        val items = section.getStringList("items")
                        Item = if (items.isNotEmpty()) items.map { ItemStack(MainUtils.parseMaterial(it)) } else null
                        ProgressGoal = section.getInt("amount", 0)
                    }

                    "MINE" -> MineObjective().apply {
                        val blocks = section.getStringList("blocks")
                        Block = if (blocks.isNotEmpty()) blocks.map { MainUtils.parseMaterial(it) } else null
                        ProgressGoal = section.getInt("amount", 0)
                    }

                    "PICK" -> PickObjective().apply {
                        val items = section.getStringList("items")
                        Item = if (items.isNotEmpty()) items.map { ItemStack(MainUtils.parseMaterial(it)) } else null
                        ProgressGoal = section.getInt("amount", 0)
                    }

                    "PLACE" -> PlaceObjective().apply {
                        val blocks = section.getStringList("blocks")
                        Block = if (blocks.isNotEmpty()) blocks.map { MainUtils.parseMaterial(it) } else null
                        ProgressGoal = section.getInt("amount", 0)
                    }

                    "TRADE" -> TradeObjective().apply {
                        ProgressGoal = section.getInt("amount", 0)
                    }

                    "TRAVEL" -> TravelObjective().apply {
                        ProgressGoal = section.getInt("amount", 0)
                    }

                    "USE" -> UseObjective().apply {
                        val items = section.getStringList("items")
                        Block = if (items.isNotEmpty()) items.map { MainUtils.parseMaterial(it) } else null
                        ProgressGoal = section.getInt("amount", 0)
                    }

                    else -> continue
                }

                if (section.contains("conditions")) {
                    val conditionList = section.getStringList("conditions")
                    objective.Conditions = conditionList.mapNotNull { conditions[it] }
                }

                objectives[key] = objective
            }
        }
    }

    fun getAction(name: String): Action {
        return actions[name]
            ?: error("Condition '$name' not found")
    }

    fun getCondition(name: String): Condition {
        return conditions[name]
            ?: error("Condition '$name' not found")
    }

    fun getObjective(name: String): Objective {
        return objectives[name]?.deepCopy()
            ?: error("Condition '$name' not found")
    }

}