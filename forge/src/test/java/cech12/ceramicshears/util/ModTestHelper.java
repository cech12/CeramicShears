package cech12.ceramicshears.util;
/*
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ModTestHelper {

    public static ServerPlayer makeMockCreativePlayer(GameTestHelper helper) {
        return new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(UUID.randomUUID(), "test-mock-creative-player")) {
            public boolean isSpectator() {
                return false;
            }

            public boolean isCreative() {
                return true;
            }

            @Override
            @NotNull
            public Abilities getAbilities() {
                Abilities abilities = super.getAbilities();
                abilities.instabuild = true;
                return abilities;
            }
        };
    }

    public static ServerPlayer makeMockSurvivalPlayer(GameTestHelper helper) {
        return new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(UUID.randomUUID(), "test-mock-survival-player")) {
            public boolean isSpectator() {
                return false;
            }

            public boolean isCreative() {
                return false;
            }
        };
    }

    public static PlayerInteractionResult useItemStackOnBlock(GameTestHelper helper, ItemStack itemStack, BlockPos pos, boolean isCreative) {
        BlockPos blockpos = helper.absolutePos(pos);
        Player player = isCreative ? makeMockCreativePlayer(helper) : makeMockSurvivalPlayer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack.copy());
        //centered one block above looking down
        player.absMoveTo(blockpos.getX() + 0.5D, blockpos.getY() + 1.0D, blockpos.getZ() + 0.5D, 0F, 90F);
        InteractionResultHolder<ItemStack> result = itemStack.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        if (!isCreative && result.getResult().consumesAction()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, result.getObject());
        }
        return new PlayerInteractionResult(player, result);
    }

    public static PlayerInteractionResult useItemOnEntity(GameTestHelper helper, ItemStack mainHandStack, Entity entity, boolean isCreative) {
        Player player = isCreative ? makeMockCreativePlayer(helper) : makeMockSurvivalPlayer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, mainHandStack);
        InteractionResult interactionResult = player.interactOn(entity, InteractionHand.MAIN_HAND);
        return new PlayerInteractionResult(player, interactionResult, player.getItemInHand(InteractionHand.MAIN_HAND));
    }

    public static InteractionResultHolder<ItemStack> destroyBlockWithItem(GameTestHelper helper, BlockPos pos, ItemStack stack) {
        BlockPos actualPos = helper.absolutePos(pos);
        ServerPlayer player = FakePlayerFactory.getMinecraft(helper.getLevel());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.absMoveTo(actualPos.getX() + 0.5D, actualPos.getY(), actualPos.getZ() + 0.5D, 0F, 90F);
        InteractionResult actionResultType = InteractionResult.FAIL;
        if (player.gameMode.destroyBlock(actualPos)) {
            actionResultType = InteractionResult.SUCCESS;
        }
        return new InteractionResultHolder<>(actionResultType, player.getItemInHand(InteractionHand.MAIN_HAND));
    }

}
 */
