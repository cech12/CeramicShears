package de.cech12.ceramicshears.gametest;

import com.mojang.authlib.GameProfile;
import de.cech12.ceramicshears.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Small helpers shared by all game tests. Only uses vanilla API, so it works on every loader.
 */
public final class TestUtils {

    /** Position in the middle of the "ceramicshears:platform" structure, one block above the floor. */
    public static final BlockPos CENTER = new BlockPos(2, 1, 2);

    private TestUtils() {}

    /**
     * @return the registered ceramic shears item
     */
    public static Item ceramicShearsItem() {
        return BuiltInRegistries.ITEM.getValue(Constants.id("ceramic_shears"));
    }

    /**
     * @return the registered clay shears part item (crafted from clay balls, smelted to a ceramic shears part)
     */
    public static Item clayShearsPartItem() {
        return BuiltInRegistries.ITEM.getValue(Constants.id("clay_shears_part"));
    }

    /**
     * @return the registered ceramic shears part item (two of them craft ceramic shears)
     */
    public static Item ceramicShearsPartItem() {
        return BuiltInRegistries.ITEM.getValue(Constants.id("ceramic_shears_part"));
    }

    /**
     * @return a new ceramic shears stack
     */
    public static ItemStack ceramicShears() {
        return new ItemStack(ceramicShearsItem());
    }

    /**
     * Creates a mock player (not added to the level) that holds the given stack in its main hand.
     */
    public static ServerPlayer player(GameTestHelper helper, GameType gameType, ItemStack mainHand) {
        ServerPlayer player = mockServerPlayer(helper, gameType);
        Vec3 pos = helper.absoluteVec(Vec3.atBottomCenterOf(CENTER.north(2)));
        player.snapTo(pos.x, pos.y, pos.z, 0, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, mainHand);
        return player;
    }

    /**
     * Mock server player that is not added to the level (no connection, does not pick up items). Same as
     * GameTestHelper#makeMockServerPlayer(GameType) of Minecraft 26.2, which does not exist in 26.1 yet
     * (GameTestHelper#makeMockServerPlayerInLevel adds a connected player to the level and the player list).
     */
    private static ServerPlayer mockServerPlayer(GameTestHelper helper, GameType gameType) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "test-mock-player"),
                ClientInformation.createDefault()) {
            @Override
            public GameType gameMode() {
                return gameType;
            }

            @Override
            public boolean isClientAuthoritative() {
                return false;
            }
        };
        gameType.updatePlayerAbilities(player.getAbilities());
        return player;
    }

    /**
     * Right-clicks an entity like a real player would do.
     */
    public static InteractionResult interact(ServerPlayer player, Entity entity) {
        try {
            return player.interactOn(entity, InteractionHand.MAIN_HAND, entity.position());
        } finally {
            if (player.isPassenger()) {
                // e.g. a saddled pig was mounted instead of sheared: the mock player has no connection, so mounting throws
                // and a riding mock player would crash the whole test server on the next tick
                try {
                    player.stopRiding();
                } catch (RuntimeException ignored) {
                    // the vehicle is already removed when the missing connection is accessed
                }
            }
        }
    }

    /**
     * Breaks a block like a real survival player would do (drops, tool damage, block callbacks).
     */
    public static boolean destroyBlock(GameTestHelper helper, ServerPlayer player, BlockPos relativePos) {
        return player.gameMode.destroyBlock(helper.absolutePos(relativePos));
    }

    /**
     * Asserts the damage value of a stack.
     */
    public static void assertDamage(GameTestHelper helper, ItemStack stack, int expected, String context) {
        helper.assertValueEqual(stack.getDamageValue(), expected, "damage of ceramic shears " + context);
    }

    /**
     * Asserts that an item entity of the given item tag lies inside the test area.
     */
    public static void assertItemEntityPresent(GameTestHelper helper, TagKey<Item> tag) {
        boolean present = helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds()).stream()
                .anyMatch(entity -> entity.getItem().is(tag));
        helper.assertTrue(present, "no item of #" + tag.location() + " was dropped");
    }


    /**
     * Asserts that a sheared bogged dropped its two mushrooms (red or brown, the loot table rolls twice) inside the
     * test area.
     */
    public static void assertBoggedMushroomsDropped(GameTestHelper helper) {
        int mushrooms = helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds()).stream()
                .filter(entity -> entity.getItem().is(Items.RED_MUSHROOM) || entity.getItem().is(Items.BROWN_MUSHROOM))
                .mapToInt(entity -> entity.getItem().getCount()).sum();
        helper.assertValueEqual(mushrooms, 2, "mushrooms dropped by the sheared bogged");
    }

    /** First item of a vanilla item tag that a test needs, with a clear message if the tag is empty. */
    public static Item firstOfTag(GameTestHelper helper, TagKey<Item> tag) {
        var items = BuiltInRegistries.ITEM.getTagOrEmpty(tag).iterator();
        helper.assertTrue(items.hasNext(), "test setup broken: item tag " + tag.location() + " is empty");
        return items.next().value();
    }

}
