package mininglib.util

import mininglib.core.Config
import mininglib.block.BlockBreakAction
import mininglib.block.BlockInstance
import mininglib.loot.ConditionalDrop
import mininglib.block.BlockDefinition
import mininglib.mining.MiningPlayer
import mininglib.loot.WeightedEntryUtil
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * Utility class for null-safe "null" values of various types.
 */
class EmptyValue {
    private constructor()
    companion object {
        val ITEMSTACK = ItemStack(Material.AIR, 1)
        val BLOCKBREAKACTION : BlockBreakAction = BlockBreakAction { _ : BlockInstance, _ : MiningPlayer -> }
        val CONDITIONALDROP = ConditionalDrop(
            WeightedEntryUtil.single(0), WeightedEntryUtil.single(ItemStack(
            Material.AIR))) { true }
        val BLOCKDEFINITION = BlockDefinition(Material.AIR, CONDITIONALDROP, -1, -1, BLOCKBREAKACTION )
        val CONFIG = Config({ listOf(BLOCKDEFINITION) }, miningSpeedScale = 0.0)

        val BLOCKINSTANCE = BlockInstance(
            BLOCKDEFINITION,
            location = Location(null, -1.0, -1.0, -1.0),
            config = CONFIG
        )
    }
}