package com.oierbravo.create_mechanical_spawner.compat.jei;

import com.oierbravo.create_mechanical_spawner.compat.jei.animations.AnimatedSpawner;
import com.oierbravo.create_mechanical_spawner.content.components.SpawnerConfig;
import com.oierbravo.create_mechanical_spawner.content.components.SpawnerRecipe;
import com.oierbravo.create_mechanical_spawner.foundation.utility.ModLang;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class SpawnerCategory extends CreateRecipeCategory<SpawnerRecipe> {
    private final AnimatedSpawner spawner = new AnimatedSpawner();
    private final RandomMobCycleTimer randomMobCycleTimer;
    private List<LivingEntity> displayedMobs = List.of();
    private List<Optional<EntityType<?>>> allMobs = List.of();

    public SpawnerCategory(Info<SpawnerRecipe> info) {
        super(info);
        this.randomMobCycleTimer = new RandomMobCycleTimer(0);

    }


    public void setRecipe(IRecipeLayoutBuilder builder, SpawnerRecipe recipe, IFocusGroup focuses) {
        FluidIngredient fluidIngredient = recipe.getFluidIngredient();

        List<ItemStack> invisibleIngredientsBuckets = fluidIngredient.getMatchingFluidStacks().stream().map(fluidStack -> new ItemStack(fluidStack.getFluid().getBucket())).toList();
        List<Fluid> listFluids = fluidIngredient.getMatchingFluidStacks().stream().map(fluidStack -> fluidStack.getFluid()).toList();
        List<ItemStack> buckets = fluidIngredient.getMatchingFluidStacks().stream().map(fluidStack -> fluidStack.getFluid().getFluidType().getBucket(fluidStack)).toList();
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStacks(invisibleIngredientsBuckets);
        EntityType<?> mob = recipe.getMob();

        boolean useCustomLoot = !recipe.getCustomLoot().isEmpty() && SpawnerConfig.CUSTOM_LOOT_PER_SPAWN_RECIPE_ENABLED.get();

        if(mob != null && !useCustomLoot) {
            Level level = Minecraft.getInstance().level;
            ItemStack egg = mob.create(level).getPickResult();
            builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(egg);
        }
        if(useCustomLoot){
            List<ProcessingOutput> customLoot = recipe.getCustomLoot();
            boolean single = customLoot.size() == 1;
            int i = 0;
            for (ProcessingOutput output : customLoot) {
                int xOffset = i % 2 == 0 ? 0 : 19;
                int yOffset = (i / 2) * -19;

                builder
                        .addSlot(RecipeIngredientRole.OUTPUT, single ? 139 : 133 + xOffset, 27 + yOffset)
                        .setBackground(getRenderedSlot(output), -1, -1)
                        .addItemStack(output.getStack())
                        .addTooltipCallback(addStochasticTooltip(output));

                i++;
            }
        }

        builder
            .addSlot(RecipeIngredientRole.INPUT, 15, 9)
            .setBackground(getRenderedSlot(), -1, -1)
            .addIngredients(ForgeTypes.FLUID_STACK, withImprovedVisibility(fluidIngredient.getMatchingFluidStacks()))
            .addTooltipCallback(addFluidTooltip(fluidIngredient.getRequiredAmount()));

    }

    public LivingEntity getDisplayedMob() {
        return randomMobCycleTimer.getCycledLivingEntity(displayedMobs);
    }
    public void draw(SpawnerRecipe recipe, @NotNull IRecipeSlotsView iRecipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        randomMobCycleTimer.onDraw();
        Font font = Minecraft.getInstance().font;

        AllGuiTextures.JEI_DOWN_ARROW.render(guiGraphics, 43, 4);
        spawner.draw(guiGraphics, 48, 27);
        Level level = Minecraft.getInstance().level;
        EntityType<?> mob = recipe.getMob();

        boolean useCustomLoot = !recipe.getCustomLoot().isEmpty() && SpawnerConfig.CUSTOM_LOOT_PER_SPAWN_RECIPE_ENABLED.get();


        if(mob != null) {
            assert level != null;
            LivingEntity mobEntity = (LivingEntity) mob.create(level);
            assert mobEntity != null;
            String id = mobEntity.getEncodeId();

            assert id != null;
            RenderHelper.renderEntity(guiGraphics, 100, 35, 20.0F * getMobScaleModifier(id),
                    38 - mouseX,
                    80 - mouseY,
                    randomMobCycleTimer.getCycledLivingEntity(List.of(mobEntity)));

            Component displayName = mobEntity.getDisplayName();
            guiGraphics.drawString(font, displayName, 20, 57, 8, false);

            if(useCustomLoot){
                String customLoottext = ModLang.translate("generic.with_custom_loot").string();
                guiGraphics.drawString(font, customLoottext, 20, 65, 8, false);
            }

            return;
        }


        if(!useCustomLoot) {
            String text = ModLang.translate("generic.biome_dependant").string();// "Biome dependent";
            guiGraphics.drawString(font, text, 80, 57, 8, false);
        }
    }
    private void drawMob(){

    }

    private float getMobScaleModifier(String mobId){
        return switch (mobId) {
            case "minecraft:ghast":
                yield .25f;
            case "minecraft:enderman":
                yield .9f;
            default:
                yield 1f;
        };
    }

}
