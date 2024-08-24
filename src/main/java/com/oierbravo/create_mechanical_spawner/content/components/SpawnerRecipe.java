package com.oierbravo.create_mechanical_spawner.content.components;

import com.google.gson.JsonObject;
import com.oierbravo.create_mechanical_spawner.CreateMechanicalSpawner;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class SpawnerRecipe implements Recipe<SimpleContainer>, IRecipeTypeInfo {
    private ResourceLocation id;
    private final FluidIngredient fluidIngredient;

    private SpawnerRecipeOutput mob;
    private final int processingTime;
    public SpawnerRecipe(SpawnerRecipeBuilder.SpawnerRecipeParams params) {
        this.id = params.id;
        this.mob = params.mob;
        this.fluidIngredient = params.fluidIngredient;
        this.processingTime = params.processingTime;
    }
    public boolean matches(SpawnerRecipeWrapper pContainer, @NotNull Level pLevel) {
        FluidStack fluidStack = pContainer.getFluidStack();
        boolean test = this.fluidIngredient.test(fluidStack);
         return this.fluidIngredient.test(fluidStack);
    }

    public boolean matches(FluidStack fluidStack) {

        return this.fluidIngredient.test(fluidStack);

    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        return false;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public int getFluidAmount() {
        return fluidIngredient.getRequiredAmount();
    }

    public FluidIngredient getFluidIngredient() { return fluidIngredient; }

    public EntityType<?> getMob() {
        return mob.getMob();
    }


    public static class Type implements RecipeType<SpawnerRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "spawner";
    }
    public static class Serializer implements RecipeSerializer<SpawnerRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(CreateMechanicalSpawner.MODID,"spawner");

        @Override
        public SpawnerRecipe fromJson(ResourceLocation id, JsonObject json) {
            SpawnerRecipeBuilder builder = new SpawnerRecipeBuilder(id);
           FluidIngredient fluidIngredient = FluidIngredient.EMPTY;
            SpawnerRecipeOutput mob = SpawnerRecipeOutput.EMPTY;

                fluidIngredient = FluidIngredient.deserialize(GsonHelper.getAsJsonObject(json, "fluid"));

            if(GsonHelper.isValidNode(json,"mob")){
                mob = SpawnerRecipeOutput.fromJson(GsonHelper.getAsString(json, "mob"));
            }
            int processingTime = 200;
            if (GsonHelper.isValidNode(json, "processingTime")) {
                processingTime = GsonHelper.getAsInt(json, "processingTime");
            }

            builder.withFluid(fluidIngredient)
                    .withMob(mob)
                    .withProcessingTime(processingTime);
            return builder.save();
        }
        protected JsonObject toJson(JsonObject json, SpawnerRecipe pRecipe) {

            json.add("fluid", pRecipe.getFluidIngredient().serialize());

            json.addProperty("mob", pRecipe.getOutput().toJson());
            json.addProperty("processingTime", pRecipe.getProcessingTime());

            int processingDuration = pRecipe.getProcessingTime();
            if (processingDuration > 0)
                json.addProperty("processingTime", processingDuration);

            return json;
        }


        @Override
        public SpawnerRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            SpawnerRecipeBuilder builder = new SpawnerRecipeBuilder(id);
            FluidIngredient fluidIngredient = FluidIngredient.read(buffer);
            SpawnerRecipeOutput mob = SpawnerRecipeOutput.fromNetwork(buffer);
            int processingTime = buffer.readInt();

            builder.withFluid(fluidIngredient)
                    .withMob(mob)
                    .withProcessingTime(processingTime);
            return builder.save();
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SpawnerRecipe recipe) {
            FluidIngredient fluidIngredient = recipe.fluidIngredient;
            SpawnerRecipeOutput mob = recipe.mob;
            fluidIngredient.write(buffer);
            mob.toNetwork(buffer);
            buffer.writeInt(recipe.getProcessingTime());

        }


    }

    private SpawnerRecipeOutput getOutput() {
        if(mob == null)
            return SpawnerRecipeOutput.EMPTY;
        return this.mob;
    }

    public static class SpawnerRecipeWrapper extends SimpleContainer {

        protected FluidStack fluidStack;
        public SpawnerRecipeWrapper(FluidStack fluidStack) {
            super(0);
            this.fluidStack = fluidStack;
        }
        public FluidStack getFluidStack(){
            return fluidStack;
        }
    }
}
