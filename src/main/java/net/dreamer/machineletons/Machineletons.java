package net.dreamer.machineletons;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class Machineletons implements ModInitializer {

    public static final GameRules.Key<GameRules.IntRule> SKELETON_ARROW_COUNT = GameRuleRegistry.register("skeletonArrowCount", GameRules.Category.MOBS, GameRuleFactory.createIntRule(10));

    @Override
    public void onInitialize() {

    }
}
