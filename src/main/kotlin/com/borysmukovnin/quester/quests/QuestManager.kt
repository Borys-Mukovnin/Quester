package com.borysmukovnin.quester.quests

import com.borysmukovnin.quester.models.*
import com.borysmukovnin.quester.models.conditions.*
import com.borysmukovnin.quester.models.objectives.*
import com.borysmukovnin.quester.models.actions.CommandAction
import com.borysmukovnin.quester.models.actions.ExpAction
import com.borysmukovnin.quester.models.actions.ItemAction
import com.borysmukovnin.quester.Quester
import com.borysmukovnin.quester.utils.MainUtils
import com.borysmukovnin.quester.utils.deepCopy
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.*

class QuestManager(private val plugin: Quester) {

    private val activePlayersQuests: MutableMap<UUID, MutableMap<String, Quest>> = mutableMapOf()
    private val activePlayersCompletedQuests: MutableMap<UUID, MutableList<String>> = mutableMapOf()

    private val quests: MutableMap<String, Quest> = mutableMapOf()


    fun loadAllQuests() {
        val questsFolder = File(plugin.dataFolder, "quests")
        questsFolder.listFiles()?.forEach { file ->
            val quest: Quest? = createQuestData(YamlConfiguration.loadConfiguration(file))
            if (quest != null) {
                quests[file.name] = quest
            }
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

    private fun createQuestData(config: YamlConfiguration): Quest? {
        val stagesList: MutableMap<String, Stage> = mutableMapOf()

        val questName = config.getString("name") ?: "unknown name"

        val questDesc = config.getStringList("description")


        val startConditions = parseConditionsList(config.getStringList("start_conditions"))

        val stagesSection = config.getConfigurationSection("stages") ?: run { return null }
        val stageNames = stagesSection.getKeys(false)

        for (stageName in stageNames) {
            val stageConfig = stagesSection.getConfigurationSection(stageName) ?: continue

            val stageSubName = stageConfig.getString("name") ?: "unknown name"
            val stageDescription = stageConfig.getStringList("description")
            val stageObjectives = parseObjectivesList(stageConfig.getStringList("objectives"))
            val stageRewards = parseRewardsList(stageConfig.getStringList("rewards"))

            stagesList[stageName] = Stage(stageSubName, stageDescription, stageObjectives, stageRewards)
        }

        return Quest(questName, questDesc, startConditions, stagesList)
    }


    private fun parseConditionsList(conditionsList: List<String>): List<Condition> {
        val conList: MutableList<Condition> = mutableListOf()

        conditionsList.forEach { c ->
            val conSplit = c.split(" ")
            var condition: Condition = AdvancementCondition()

            when (conSplit[0].uppercase()) {
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
                            Location = mutableListOf(
                                Location(
                                    Bukkit.getServer().getWorld(world),
                                    x.toDouble(),
                                    y.toDouble(),
                                    z.toDouble()
                                )
                            )
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
                    condition = BlockCondition().apply {
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

    private fun parseObjectivesList(objectivesList: List<String>): List<Objective> {
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
                            ProgressGoal = objectiveSplit.getOrNull(2)!!.toIntOrNull()!!
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
                            ProgressGoal = objectiveSplit.getOrNull(2)!!.toInt()
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
                            ProgressGoal = objectiveSplit.getOrNull(2)!!.toInt()
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
                            ProgressGoal = objectiveSplit.getOrNull(2)!!.toInt()
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
                            ProgressGoal = objectiveSplit.getOrNull(2)!!.toInt()
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
                            ProgressGoal = objectiveSplit.getOrNull(2)!!.toInt()
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
                            ProgressGoal = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }

                "TRADE" -> {
                    objective = TradeObjective().apply {
                        if (objectiveSplit.getOrNull(1)?.toIntOrNull() != null) {
                            ProgressGoal = objectiveSplit.getOrNull(1)!!.toInt()
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
                            ProgressGoal = objectiveSplit.getOrNull(3)!!.toInt()
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
                            ProgressGoal = objectiveSplit.getOrNull(2)!!.toInt()
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
                            ProgressGoal = objectiveSplit.getOrNull(2)!!.toInt()
                        }
                    }
                }

                "GOTO" -> {
                    objective = GotoObjective().apply {
                        if (objectiveSplit.getOrNull(1) != null && objectiveSplit.getOrNull(2)
                                ?.toDoubleOrNull() != null && objectiveSplit.getOrNull(3)
                                ?.toDoubleOrNull() != null && objectiveSplit.getOrNull(4) != null
                        ) {
                            Goto = Location(
                                Bukkit.getWorld(objectiveSplit[4]),
                                objectiveSplit[1].toDouble(),
                                objectiveSplit[2].toDouble(),
                                objectiveSplit[3].toDouble()
                            )
                        }
                    }
                }

                "TRAVEL" -> {
                    objective = TravelObjective().apply {
                        if (objectiveSplit.getOrNull(1)?.toIntOrNull() != null) {
                            ProgressGoal = objectiveSplit[1].toInt()
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

    private fun parseRewardsList(rewardsList: List<String>): List<Action> {
        val actions: MutableList<Action> = mutableListOf()

        for (rewardStr in rewardsList) {
            val rewardSplit = rewardStr.split(" ")
            var action: Action = ItemAction()

            when (rewardSplit[0].uppercase()) {
                "COMMAND" -> {
                    val command = rewardSplit.getOrNull(1) ?: ""
                    action = CommandAction().apply {
                        Command = command
                    }
                }

                "EXP" -> {
                    val exp = rewardSplit.getOrNull(1)?.toIntOrNull() ?: 0
                    action = ExpAction().apply {
                        Amount = exp
                    }
                }

                "ITEM" -> {
                    val itemMaterialStr = rewardSplit.getOrNull(1) ?: "DIRT"
                    val itemMaterial = Material.valueOf(itemMaterialStr)
                    val amount = rewardSplit.getOrNull(2)?.toIntOrNull() ?: 1

                    val item = ItemStack(itemMaterial, amount)
                    action = ItemAction().apply {
                        Item = item
                    }
                }
            }
            actions.add(action)
        }

        return actions
    }
}