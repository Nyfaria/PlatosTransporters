package com.nyfaria.nyfsairships;

import com.nyfaria.nyfsairships.blocks.BlockControlWheel;
import com.nyfaria.nyfsairships.blocks.NotFullBlock;
import com.nyfaria.nyfsairships.entity.BlockShipEntity;
import com.nyfaria.nyfsairships.items.BoardingStairsItem;
import com.nyfaria.nyfsairships.items.ClearingScytheItem;
import com.nyfaria.nyfsairships.items.ControlKeyItem;
import com.nyfaria.nyfsairships.items.LiftJackItem;
import com.nyfaria.nyfsairships.items.WrenchItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;

public class NyfsAirships implements ModInitializer {
	public static final CreativeModeTab TAB = FabricItemGroupBuilder.build(
			new ResourceLocation("nyfsairships", "tab"),
			() -> new ItemStack(NyfsAirships.BLOCK_CONTROL_WHEEL));

	
	public static final EntityType<BlockShipEntity> BLOCK_SHIP_ENTITY_ENTITY_TYPE =
			Registry.register(Registry.ENTITY_TYPE,new ResourceLocation("nyfsairships","block_ship")
					, FabricEntityTypeBuilder.create(MobCategory.AMBIENT,BlockShipEntity::new).dimensions(EntityDimensions.fixed(3,1)).trackRangeBlocks(100).build());

	public static final TagKey<Block> BOAT_MATERIAL = TagKey.create(Registry.BLOCK_REGISTRY,new ResourceLocation("nyfsairships","boat_material"));
	public static final TagKey<Block> BOAT_MATERIAL_BLACKLIST = TagKey.create(Registry.BLOCK_REGISTRY,new ResourceLocation("nyfsairships","boat_material_blacklist"));
	public static final TagKey<Block> SCYTHEABLE = TagKey.create(Registry.BLOCK_REGISTRY,new ResourceLocation("nyfsairships","scytheable"));

	public static ResourceLocation forwardPacket = new ResourceLocation("nyfsairships","forwardpacket");


	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		FabricDefaultAttributeRegistry.register(BLOCK_SHIP_ENTITY_ENTITY_TYPE, Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 100.0D).add(Attributes.MOVEMENT_SPEED, 0.25D));
		registerBlocks();
		registerItems();
		ConfigUtils.checkConfigs();


		ServerPlayNetworking.registerGlobalReceiver(forwardPacket,(server,handler,packetContext, attachedData,sender) ->
		{
			Player user = packetContext.player;

			if(user.isPassenger() && user.getVehicle() instanceof BlockShipEntity)
			{

				int move = attachedData.readInt();
				BlockShipEntity vehicle = (BlockShipEntity) user.getVehicle();
				if(move==0)
				{

					user.getVehicle().setDeltaMovement(new Vec3(vehicle.getLookAngle().x,vehicle.getLookAngle().y,vehicle.getLookAngle().z).scale(0.8));
				}
				if(move==2)
				{
					vehicle.setYRot(vehicle.getYRot()+5);
				}
				if(move==1)
				{
					vehicle.setYRot(vehicle.getYRot()-5);

				}
				if(move==3 && ((BlockShipEntity) user.getVehicle()).getItemBySlot(EquipmentSlot.CHEST).getTag().getInt("type")==1)
				{
					vehicle.setDeltaMovement(new Vec3(0,0.2,0));
				}
				if(move==4 && ((BlockShipEntity) user.getVehicle()).getItemBySlot(EquipmentSlot.CHEST).getTag().getInt("type")==1)
				{
					vehicle.setDeltaMovement(new Vec3(0,-0.2,0));

				}
				if(move==5)
				{

					if(vehicle.getItemBySlot(EquipmentSlot.HEAD).getItem()==Items.STICK)
					{
						vehicle.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);
					}
					else
					{
						vehicle.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.STICK));
					}

				}
			}
		});
	}
	public static final BlockControlWheel BLOCK_CONTROL_WHEEL = new BlockControlWheel(BlockBehaviour.Properties.of(Material.WOOD));
	public static final NotFullBlock BALLOON_BLOCK = new NotFullBlock(BlockBehaviour.Properties.of(Material.WOOL));
//	public static final NotFullBlock FLOAT_BLOCK = new NotFullBlock(BlockBehaviour.Properties.of(Material.WOOL));
//	public static final NotFullBlock WHEEL_BLOCK = new NotFullBlock(BlockBehaviour.Properties.of(Material.WOOL));
	private void registerBlocks()
	{
		Registry.register(Registry.BLOCK,new ResourceLocation("nyfsairships","ship_controller"),BLOCK_CONTROL_WHEEL);
		Registry.register(Registry.BLOCK,new ResourceLocation("nyfsairships","balloon_block"),BALLOON_BLOCK);
//		Registry.register(Registry.BLOCK,new ResourceLocation("nyfsairships","float_block"),FLOAT_BLOCK);
//		Registry.register(Registry.BLOCK,new ResourceLocation("nyfsairships","wheel_block"),WHEEL_BLOCK);
	}

	public static final ControlKeyItem CONTROL_KEY_ITEM = new ControlKeyItem(new Item.Properties().tab(NyfsAirships.TAB));
	public static final LiftJackItem LIFT_JACK_ITEM = new LiftJackItem(new Item.Properties().tab(NyfsAirships.TAB));
	public static final WrenchItem WRENCH_ITEM = new WrenchItem(new Item.Properties().tab(NyfsAirships.TAB));
	public static final ClearingScytheItem CLEARING_SCYTHE_ITEM = new ClearingScytheItem(new Item.Properties().tab(NyfsAirships.TAB).durability(100));
	public static final BoardingStairsItem BOARDING_STAIRS_ITEM = new BoardingStairsItem(new Item.Properties().tab(NyfsAirships.TAB));
	private void registerItems()
	{
		Registry.register(Registry.ITEM, new ResourceLocation("nyfsairships", "ship_controller"), new BlockItem(BLOCK_CONTROL_WHEEL, new Item.Properties().tab(NyfsAirships.TAB)));
//		Registry.register(Registry.ITEM, new ResourceLocation("nyfsairships", "float_block"), new BlockItem(FLOAT_BLOCK, new Item.Properties().tab(NyfsAirships.TAB)));
		Registry.register(Registry.ITEM, new ResourceLocation("nyfsairships", "balloon_block"), new BlockItem(BALLOON_BLOCK, new Item.Properties().tab(NyfsAirships.TAB)));
//		Registry.register(Registry.ITEM, new ResourceLocation("nyfsairships", "wheel_block"), new BlockItem(WHEEL_BLOCK, new Item.Properties().tab(NyfsAirships.TAB)));
		Registry.register(Registry.ITEM,new ResourceLocation("nyfsairships","control_key"),CONTROL_KEY_ITEM);
		Registry.register(Registry.ITEM,new ResourceLocation("nyfsairships","lift_jack"),LIFT_JACK_ITEM);
		Registry.register(Registry.ITEM,new ResourceLocation("nyfsairships","wrench"),WRENCH_ITEM);
		Registry.register(Registry.ITEM,new ResourceLocation("nyfsairships","clearing_scythe"),CLEARING_SCYTHE_ITEM);
		Registry.register(Registry.ITEM,new ResourceLocation("nyfsairships","boarding_stairs"),BOARDING_STAIRS_ITEM);
	}

	public static void givePlayerStartBook(Player playerEntity)
	{
		if(!playerEntity.getTags().contains("nyfsairships_new") && playerEntity.level.isClientSide)
		{

			playerEntity.addItem(createBook("Nyfaria","Nyf's Airships"
					,I18n.get("book.nyfsairships.page1")
					,I18n.get("book.nyfsairships.page2")
					,I18n.get("book.nyfsairships.page3")
					,I18n.get("book.nyfsairships.page4")
					,I18n.get("book.nyfsairships.page5")
					,I18n.get("book.nyfsairships.page6")
					,I18n.get("book.nyfsairships.page7")
					));
			playerEntity.addTag("nyfsairships_new");
		}
	}
	private static ItemStack createBook(String author, String title,Object ...pages)
	{
		ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
		CompoundTag tags = new CompoundTag();
		tags.putString("author",author);
		tags.putString("title",title);
		ListTag contents = new ListTag();
		for (Object page : pages) {
			contents.add(StringTag.valueOf("{\"text\":\""+page+"\"}"));
		}
		tags.put("pages",contents);
		book.setTag(tags);
		return book;
	}
}
