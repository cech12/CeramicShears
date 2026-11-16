package de.cech12.ceramicshears.gametest.tests;

import de.cech12.ceramicshears.gametest.KnownIssues;
import de.cech12.ceramicshears.gametest.Platforms;
import de.cech12.ceramicshears.gametest.TestUtils;
import de.cech12.ceramicshears.gametest.CeramicShearsGameTests;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Right-clicking entities with ceramic shears does everything vanilla shears do.
 */
public final class EntityTests {

    private EntityTests() {}

    public static void register(CeramicShearsGameTests.TestCollector tests) {
        tests.add("entity/shears_sheep", EntityTests::shearsSheep);
        tests.add("entity/shears_in_creative_without_damage", EntityTests::shearsInCreativeWithoutDamage);
        tests.add("entity/shears_mooshroom", EntityTests::shearsMooshroom);
        tests.add("entity/shears_brown_mooshroom", EntityTests::shearsBrownMooshroom);
        tests.add("entity/shears_snow_golem", EntityTests::shearsSnowGolem);
        tests.add("entity/shears_bogged", EntityTests::shearsBogged);
        tests.add("entity/shears_copper_golem", EntityTests::shearsCopperGolem);
        tests.add("entity/does_not_shear_when_not_ready", EntityTests::doesNotShearWhenNotReady);
        tests.add("entity/removes_saddle", EntityTests::removesSaddle);
        tests.add("entity/removes_wolf_armor", EntityTests::removesWolfArmor);
        tests.add("entity/removes_equipment_like_vanilla_shears", EntityTests::removesEquipmentLikeVanillaShears);
        tests.add("entity/cuts_leash_of_entity", EntityTests::cutsLeashOfEntity);
        tests.add("entity/cuts_leash_knot", EntityTests::cutsLeashKnot);
    }

    private static ServerPlayer survivalPlayer(GameTestHelper helper) {
        return TestUtils.player(helper, GameType.SURVIVAL, TestUtils.ceramicShears());
    }

    private static void assertConsumed(GameTestHelper helper, InteractionResult result, String target) {
        helper.assertTrue(result.consumesAction(), "interaction with " + target + " was not successful: " + result);
    }

    /** Shearing a sheep drops wool, marks it as sheared and damages the shears by one. */
    public static void shearsSheep(GameTestHelper helper) {
        Sheep sheep = helper.spawn(EntityType.SHEEP, TestUtils.CENTER);
        ServerPlayer player = survivalPlayer(helper);
        assertConsumed(helper, TestUtils.interact(player, sheep), "sheep");
        helper.assertTrue(sheep.isSheared(), "sheep was not sheared");
        TestUtils.assertItemEntityPresent(helper, ItemTags.WOOL);
        TestUtils.assertDamage(helper, player.getMainHandItem(), 1, "after shearing a sheep");
        helper.succeed();
    }

    /** Shearing a sheep, a mooshroom and a snow golem in creative mode works and does not damage the shears. */
    public static void shearsInCreativeWithoutDamage(GameTestHelper helper) {
        Sheep sheep = helper.spawn(EntityType.SHEEP, TestUtils.CENTER);
        MushroomCow mooshroom = helper.spawn(EntityType.MOOSHROOM, TestUtils.CENTER.west());
        SnowGolem golem = helper.spawn(EntityType.SNOW_GOLEM, TestUtils.CENTER.east());
        ServerPlayer player = TestUtils.player(helper, GameType.CREATIVE, TestUtils.ceramicShears());
        assertConsumed(helper, TestUtils.interact(player, sheep), "sheep");
        assertConsumed(helper, TestUtils.interact(player, mooshroom), "mooshroom");
        assertConsumed(helper, TestUtils.interact(player, golem), "snow golem");
        helper.assertTrue(sheep.isSheared(), "sheep was not sheared");
        helper.assertTrue(!golem.hasPumpkin(), "snow golem still has its pumpkin");
        helper.succeedWhen(() -> {
            helper.assertEntityPresent(EntityType.COW);
            TestUtils.assertDamage(helper, player.getMainHandItem(), 0, "after shearing in creative mode");
        });
    }

    /** Shearing a mooshroom turns it into a cow and drops mushrooms. */
    public static void shearsMooshroom(GameTestHelper helper) {
        MushroomCow mooshroom = helper.spawn(EntityType.MOOSHROOM, TestUtils.CENTER);
        ServerPlayer player = survivalPlayer(helper);
        assertConsumed(helper, TestUtils.interact(player, mooshroom), "mooshroom");
        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertEntityPresent(EntityType.COW);
                    TestUtils.assertDamage(helper, player.getMainHandItem(), 1, "after shearing a mooshroom");
                })
                .thenExecute(() -> KnownIssues.run(helper, "Forge 62.0.x - 64.x (26.1.x): sheared mooshrooms drop the shears instead of the mushrooms (MushroomCow#shearInternal adds the tool instead of the drop, also with vanilla shears), fixed in Forge 65.0.1 for 26.2", Set.of(Platforms.FORGE), () ->
                        helper.assertItemEntityPresent(Items.RED_MUSHROOM)));
    }

    /** Shearing a brown mooshroom (own loot table) turns it into a cow and drops brown mushrooms. */
    public static void shearsBrownMooshroom(GameTestHelper helper) {
        MushroomCow mooshroom = helper.spawn(EntityType.MOOSHROOM, TestUtils.CENTER);
        TestUtils.setMooshroomVariant(mooshroom, MushroomCow.Variant.BROWN);
        helper.assertValueEqual(mooshroom.getVariant(), MushroomCow.Variant.BROWN, "test setup broken: mooshroom variant");
        ServerPlayer player = survivalPlayer(helper);
        assertConsumed(helper, TestUtils.interact(player, mooshroom), "brown mooshroom");
        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertEntityPresent(EntityType.COW);
                    TestUtils.assertDamage(helper, player.getMainHandItem(), 1, "after shearing a brown mooshroom");
                })
                .thenExecute(() -> KnownIssues.run(helper, "Forge 62.0.x - 64.x (26.1.x): sheared mooshrooms drop the shears instead of the mushrooms (MushroomCow#shearInternal adds the tool instead of the drop, also with vanilla shears), fixed in Forge 65.0.1 for 26.2", Set.of(Platforms.FORGE), () -> {
                    helper.assertItemEntityPresent(Items.BROWN_MUSHROOM);
                    helper.assertItemEntityNotPresent(Items.RED_MUSHROOM);
                }));
    }

    /** Shearing a snow golem removes its pumpkin. */
    public static void shearsSnowGolem(GameTestHelper helper) {
        SnowGolem golem = helper.spawn(EntityType.SNOW_GOLEM, TestUtils.CENTER);
        ServerPlayer player = survivalPlayer(helper);
        assertConsumed(helper, TestUtils.interact(player, golem), "snow golem");
        helper.assertTrue(!golem.hasPumpkin(), "snow golem still has its pumpkin");
        helper.assertItemEntityPresent(Items.CARVED_PUMPKIN);
        TestUtils.assertDamage(helper, player.getMainHandItem(), 1, "after shearing a snow golem");
        helper.succeed();
    }

    /** Shearing a bogged drops two mushrooms and marks it as sheared (Forge known issue fixed in 1.21-3.1.0.0). */
    public static void shearsBogged(GameTestHelper helper) {
        Bogged bogged = helper.spawn(EntityType.BOGGED, TestUtils.CENTER);
        ServerPlayer player = survivalPlayer(helper);
        assertConsumed(helper, TestUtils.interact(player, bogged), "bogged");
        helper.assertTrue(bogged.isSheared(), "bogged was not sheared");
        TestUtils.assertBoggedMushroomsDropped(helper);
        TestUtils.assertDamage(helper, player.getMainHandItem(), 1, "after shearing a bogged");
        helper.succeed();
    }

    /** 1.21.9-3.6.0.1: the flower of a copper golem can be sheared off. */
    public static void shearsCopperGolem(GameTestHelper helper) {
        CopperGolem golem = helper.spawn(EntityType.COPPER_GOLEM, TestUtils.CENTER);
        Item flower = TestUtils.firstOfTag(helper, ItemTags.SHEARABLE_FROM_COPPER_GOLEM);
        golem.setItemSlot(CopperGolem.EQUIPMENT_SLOT_ANTENNA, new ItemStack(flower));
        ServerPlayer player = survivalPlayer(helper);
        assertConsumed(helper, TestUtils.interact(player, golem), "copper golem");
        helper.assertTrue(golem.getItemBySlot(CopperGolem.EQUIPMENT_SLOT_ANTENNA).isEmpty(), "copper golem still has its flower");
        helper.assertItemEntityPresent(flower);
        TestUtils.assertDamage(helper, player.getMainHandItem(), 1, "after shearing a copper golem");
        helper.succeed();
    }

    /** Mob that is not ready for shearing, with the setup that makes it so. */
    private record NotReady(String name, EntityType<? extends Mob> type, Consumer<Mob> setup) {}

    /** Result of using shears on a mob: interaction consumed, durability loss and dropped items. */
    private record UseResult(boolean consumed, int damage, List<String> drops) {}

    /**
     * Using shears on mobs that cannot be sheared (already sheared, baby, nothing to remove) does nothing, exactly like
     * vanilla shears: no drops, no durability loss. Catches Fabric mixins that replace an is(Items.SHEARS) check but
     * skip the readiness check of vanilla.
     */
    public static void doesNotShearWhenNotReady(GameTestHelper helper) {
        List<NotReady> cases = List.of(
                new NotReady("sheared sheep", EntityType.SHEEP, mob -> ((Sheep) mob).setSheared(true)),
                new NotReady("baby sheep", EntityType.SHEEP, mob -> ((AgeableMob) mob).setBaby(true)),
                new NotReady("baby mooshroom", EntityType.MOOSHROOM, mob -> ((AgeableMob) mob).setBaby(true)),
                new NotReady("snow golem without pumpkin", EntityType.SNOW_GOLEM, mob -> ((SnowGolem) mob).setPumpkin(false)),
                new NotReady("sheared bogged", EntityType.BOGGED, mob -> ((Bogged) mob).setSheared(true)),
                new NotReady("copper golem without flower", EntityType.COPPER_GOLEM, mob -> mob.setItemSlot(CopperGolem.EQUIPMENT_SLOT_ANTENNA, ItemStack.EMPTY)),
                new NotReady("pig without saddle", EntityType.PIG, mob -> mob.setItemSlot(EquipmentSlot.SADDLE, ItemStack.EMPTY)));
        List<String> problems = new ArrayList<>();
        for (NotReady notReady : cases) {
            UseResult vanilla = useShears(helper, notReady, new ItemStack(Items.SHEARS));
            UseResult ceramic = useShears(helper, notReady, TestUtils.ceramicShears());
            if (vanilla.damage() != 0 || !vanilla.drops().isEmpty()) {
                problems.add(notReady.name() + ": test setup broken, vanilla shears shear it (" + vanilla + ")");
            } else if (!ceramic.equals(vanilla)) {
                problems.add(notReady.name() + ": " + ceramic + " instead of " + vanilla);
            }
        }
        helper.assertTrue(problems.isEmpty(), "using ceramic shears on mobs that are not ready for shearing differs from vanilla shears: " + problems);
        helper.succeed();
    }

    /** Spawns the mob, uses the shears on it, and removes the mob and all dropped items afterward. */
    private static UseResult useShears(GameTestHelper helper, NotReady notReady, ItemStack shears) {
        Mob mob = helper.spawnWithNoFreeWill(notReady.type(), TestUtils.CENTER);
        notReady.setup().accept(mob);
        ServerPlayer player = TestUtils.player(helper, GameType.SURVIVAL, shears);
        boolean consumed = TestUtils.interact(player, mob).consumesAction();
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds());
        List<String> dropNames = drops.stream().map(drop -> drop.getItem().toString()).sorted().toList();
        drops.forEach(ItemEntity::discard);
        mob.discard();
        return new UseResult(consumed, player.getMainHandItem().getDamageValue(), dropNames);
    }

    // Positions of the ceramic and the vanilla shears run of the equipment tests. They are 4 blocks apart and the drop is
    // searched within 1.5 blocks, so a run never finds the drop of the other one.
    private static final BlockPos CERAMIC_POS = TestUtils.CENTER.west(2);
    private static final BlockPos VANILLA_POS = TestUtils.CENTER.east(2);
    private static final double DROP_RADIUS = 1.5;

    /** Shears cut off equipment like a saddle, with the same durability loss as vanilla shears. */
    public static void removesSaddle(GameTestHelper helper) {
        ItemStack ceramic = removeSaddle(helper, CERAMIC_POS, TestUtils.ceramicShears());
        ItemStack vanilla = removeSaddle(helper, VANILLA_POS, new ItemStack(Items.SHEARS));
        TestUtils.assertDamage(helper, ceramic, vanilla.getDamageValue(), "after removing a saddle (like vanilla shears)");
        helper.succeed();
    }

    private static ItemStack removeSaddle(GameTestHelper helper, BlockPos pos, ItemStack shears) {
        Pig pig = helper.spawn(EntityType.PIG, pos);
        pig.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
        ServerPlayer player = TestUtils.player(helper, GameType.SURVIVAL, shears);
        assertConsumed(helper, TestUtils.interact(player, pig), "saddled pig with " + shears.getItem());
        helper.assertTrue(pig.getItemBySlot(EquipmentSlot.SADDLE).isEmpty(), "saddle was not removed by " + shears.getItem());
        helper.assertItemEntityPresent(Items.SADDLE, pos, DROP_RADIUS);
        return player.getMainHandItem();
    }

    /**
     * Fixed known issue 1.21-3.1.0.0: wolf armor can be removed by the owner, with the same durability loss as vanilla
     * shears.
     */
    public static void removesWolfArmor(GameTestHelper helper) {
        ItemStack ceramic = removeWolfArmor(helper, CERAMIC_POS, TestUtils.ceramicShears());
        ItemStack vanilla = removeWolfArmor(helper, VANILLA_POS, new ItemStack(Items.SHEARS));
        TestUtils.assertDamage(helper, ceramic, vanilla.getDamageValue(), "after removing wolf armor (like vanilla shears)");
        helper.succeed();
    }

    private static ItemStack removeWolfArmor(GameTestHelper helper, BlockPos pos, ItemStack shears) {
        Wolf wolf = helper.spawn(EntityType.WOLF, pos);
        ServerPlayer player = TestUtils.player(helper, GameType.SURVIVAL, shears);
        wolf.tame(player);
        wolf.setItemSlot(EquipmentSlot.BODY, new ItemStack(Items.WOLF_ARMOR));
        assertConsumed(helper, TestUtils.interact(player, wolf), "wolf with armor and " + shears.getItem());
        helper.assertTrue(wolf.getItemBySlot(EquipmentSlot.BODY).isEmpty(), "wolf armor was not removed by " + shears.getItem());
        helper.assertItemEntityPresent(Items.WOLF_ARMOR, pos, DROP_RADIUS);
        // advancement "Shear Brilliance" (minecraft:husbandry/remove_wolf_armor)
        AdvancementHolder advancement = helper.getLevel().getServer().getAdvancements().get(Identifier.withDefaultNamespace("husbandry/remove_wolf_armor"));
        helper.assertTrue(advancement != null, "test setup broken: advancement husbandry/remove_wolf_armor is missing");
        helper.assertTrue(player.getAdvancements().getOrStartProgress(advancement).isDone(),
                "advancement \"Shear Brilliance\" was not granted after removing wolf armor with " + shears.getItem());
        return player.getMainHandItem();
    }

    /** Mob, equipment slot and equipment that shears can remove. */
    private record Equipment(EntityType<? extends Mob> type, EquipmentSlot slot, Item item) {}

    /**
     * Shears remove saddles, horse and nautilus armor, llama carpets and happy ghast harnesses of every mob that can
     * wear them (vanilla 1.21.6+), exactly like vanilla shears: same result, same durability loss. Wolf armor needs the
     * owner, see above.
     */
    public static void removesEquipmentLikeVanillaShears(GameTestHelper helper) {
        Item carpet = TestUtils.firstOfTag(helper, ItemTags.WOOL_CARPETS);
        Item harness = TestUtils.firstOfTag(helper, ItemTags.HARNESSES);
        List<Equipment> cases = List.of(
                new Equipment(EntityType.PIG, EquipmentSlot.SADDLE, Items.SADDLE),
                new Equipment(EntityType.STRIDER, EquipmentSlot.SADDLE, Items.SADDLE),
                new Equipment(EntityType.HORSE, EquipmentSlot.SADDLE, Items.SADDLE),
                new Equipment(EntityType.HORSE, EquipmentSlot.BODY, Items.IRON_HORSE_ARMOR),
                new Equipment(EntityType.DONKEY, EquipmentSlot.SADDLE, Items.SADDLE),
                new Equipment(EntityType.MULE, EquipmentSlot.SADDLE, Items.SADDLE),
                new Equipment(EntityType.SKELETON_HORSE, EquipmentSlot.SADDLE, Items.SADDLE),
                new Equipment(EntityType.ZOMBIE_HORSE, EquipmentSlot.SADDLE, Items.SADDLE),
                new Equipment(EntityType.ZOMBIE_HORSE, EquipmentSlot.BODY, Items.IRON_HORSE_ARMOR),
                new Equipment(EntityType.CAMEL, EquipmentSlot.SADDLE, Items.SADDLE),
                new Equipment(EntityType.CAMEL_HUSK, EquipmentSlot.SADDLE, Items.SADDLE),
                new Equipment(EntityType.LLAMA, EquipmentSlot.BODY, carpet),
                new Equipment(EntityType.TRADER_LLAMA, EquipmentSlot.BODY, carpet),
                new Equipment(EntityType.HAPPY_GHAST, EquipmentSlot.BODY, harness),
                new Equipment(EntityType.NAUTILUS, EquipmentSlot.SADDLE, Items.SADDLE),
                new Equipment(EntityType.NAUTILUS, EquipmentSlot.BODY, Items.IRON_NAUTILUS_ARMOR),
                new Equipment(EntityType.ZOMBIE_NAUTILUS, EquipmentSlot.SADDLE, Items.SADDLE),
                new Equipment(EntityType.ZOMBIE_NAUTILUS, EquipmentSlot.BODY, Items.IRON_NAUTILUS_ARMOR));
        List<String> problems = new ArrayList<>();
        for (Equipment equipment : cases) {
            String name = BuiltInRegistries.ENTITY_TYPE.getKey(equipment.type()) + " " + BuiltInRegistries.ITEM.getKey(equipment.item());
            ShearResult vanilla = shearEquipment(helper, equipment, new ItemStack(Items.SHEARS));
            ShearResult ceramic = shearEquipment(helper, equipment, TestUtils.ceramicShears());
            if (!vanilla.removed() || !vanilla.dropped()) {
                problems.add(name + ": test setup broken, vanilla shears do not remove and drop it (" + vanilla + ")");
            } else if (!ceramic.removed()) {
                problems.add(name + ": not removed by ceramic shears");
            } else if (!ceramic.dropped()) {
                problems.add(name + ": removed by ceramic shears, but not dropped");
            } else if (ceramic.damage() != vanilla.damage()) {
                problems.add(name + ": durability loss " + ceramic.damage() + " instead of " + vanilla.damage());
            }
        }
        helper.assertTrue(problems.isEmpty(), "equipment removal differs from vanilla shears: " + problems);
        helper.succeed();
    }

    /**
     * Result of using shears on equipment.
     *
     * @param removed the equipment slot is empty
     * @param dropped the equipment was dropped as item entity
     * @param damage durability loss of the shears
     */
    private record ShearResult(boolean removed, boolean dropped, int damage) {}

    /** Puts the equipment on a new mob, uses the shears on it, and removes the mob and all dropped items afterward. */
    private static ShearResult shearEquipment(GameTestHelper helper, Equipment equipment, ItemStack shears) {
        Mob mob = helper.spawn(equipment.type(), TestUtils.CENTER);
        mob.setItemSlot(equipment.slot(), new ItemStack(equipment.item()));
        ServerPlayer player = TestUtils.player(helper, GameType.SURVIVAL, shears);
        TestUtils.interact(player, mob);
        boolean removed = mob.getItemBySlot(equipment.slot()).isEmpty();
        AABB area = mob.getBoundingBox().inflate(DROP_RADIUS);
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area);
        boolean dropped = drops.stream().anyMatch(drop -> drop.getItem().is(equipment.item()));
        drops.forEach(ItemEntity::discard);
        mob.discard();
        return new ShearResult(removed, dropped, player.getMainHandItem().getDamageValue());
    }

    private static LeashFenceKnotEntity leashToFence(GameTestHelper helper, Pig pig, BlockPos fencePos) {
        helper.setBlock(fencePos, Blocks.OAK_FENCE);
        LeashFenceKnotEntity knot = LeashFenceKnotEntity.getOrCreateKnot(helper.getLevel(), helper.absolutePos(fencePos));
        pig.setLeashedTo(knot, true);
        helper.assertTrue(pig.isLeashed(), "leash test setup failed: pig is not leashed");
        return knot;
    }

    /** 1.21.9-3.6.0.1: right-clicking a leashed entity with shears cuts the leash. */
    public static void cutsLeashOfEntity(GameTestHelper helper) {
        Pig pig = helper.spawn(EntityType.PIG, TestUtils.CENTER);
        leashToFence(helper, pig, TestUtils.CENTER.east(2));
        ServerPlayer player = survivalPlayer(helper);
        assertConsumed(helper, TestUtils.interact(player, pig), "leashed pig");
        helper.assertTrue(!pig.isLeashed(), "leash was not cut");
        TestUtils.assertDamage(helper, player.getMainHandItem(), 1, "after cutting a leash");
        helper.succeed();
    }

    /** 1.21.9-3.6.0.1: right-clicking a leash knot on a fence with shears removes the knot and its leashes. */
    public static void cutsLeashKnot(GameTestHelper helper) {
        Pig pig = helper.spawn(EntityType.PIG, TestUtils.CENTER);
        LeashFenceKnotEntity knot = leashToFence(helper, pig, TestUtils.CENTER.east(2));
        ServerPlayer player = survivalPlayer(helper);
        assertConsumed(helper, TestUtils.interact(player, knot), "leash knot");
        helper.assertTrue(!pig.isLeashed(), "leash was not cut");
        helper.assertTrue(knot.isRemoved(), "leash knot was not removed");
        TestUtils.assertDamage(helper, player.getMainHandItem(), 1, "after cutting a leash knot");
        helper.succeed();
    }

}
