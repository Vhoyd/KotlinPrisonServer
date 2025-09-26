package dev.vhoyd.prisonServerKT

import mininglib.core.Config
import mininglib.core.MiningManager
import org.bukkit.plugin.java.JavaPlugin

class PrisonServerKT : JavaPlugin() {
    companion object {
        lateinit var PLUGIN : PrisonServerKT
        private set
    }

    override fun onEnable() {
        PLUGIN = this
        logger.info("Hello World!")
        val config = Config(PrisonBlockDataLoader(),
            miningRateScale = 2.0
        )
        val manager = MiningManager(this, config)
    }

    override fun onDisable() {
        // Do absolutely nothing lmfao
    }
}
