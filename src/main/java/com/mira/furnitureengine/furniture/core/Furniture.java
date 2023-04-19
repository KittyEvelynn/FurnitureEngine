package com.mira.furnitureengine.furniture.core;

import com.mira.furnitureengine.FurnitureEngine;
import com.mira.furnitureengine.events.FurniturePlaceEvent;
import com.mira.furnitureengine.furniture.FurnitureManager;
import com.mira.furnitureengine.furniture.functions.FunctionManager;
import com.mira.furnitureengine.furniture.functions.FunctionType;
import com.mira.furnitureengine.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mira.furnitureengine.utils.FormatUtils.format;

public class Furniture {
    public static FurnitureEngine plugin = FurnitureEngine.getInstance();

    public enum RotSides {
        FOUR_SIDED, EIGHT_SIDED ;

        public static RotSides valueOf(int rotation) {
            if(rotation == 4) return FOUR_SIDED;
            else if(rotation == 8) return EIGHT_SIDED;
            else return null;
        }
    }
    /* Generated itemstacks */
    private ItemStack generatedItem;
    private ItemStack generatedFrameItem;
    private ItemStack generatedDropItem;

    /* Basic Furniture Data */
    private final String id;

    private final Material material;

    private final String displayName;
    private final List<String> lore;
    private final int modelData;

    private final RotSides rotSides;

    /* Advanced Furniture Data */
    private final List<SubModel> subModels = new ArrayList<>();

    private final HashMap<FunctionType, List<HashMap<String, Object>>> functions = new HashMap<>();
    // I am aware that the thing above looks awful, but if you take a look at the code below, you'll see why I did it this way


    public Furniture(String id) throws IllegalArgumentException {
        this.id = id;

        // Manually get all other values from config
        String tempDisplayName = plugin.getConfig().getString("Furniture." + id + ".display");
        if(tempDisplayName != null) {
            displayName = format(tempDisplayName);
        } else {
            displayName = null;
        }

        List<String> tempLore = plugin.getConfig().getStringList("Furniture." + id + ".lore");
        if(tempLore.isEmpty()) {
            lore = format(tempLore);
        } else {
            lore = null;
        }

        material = Material.valueOf(plugin.getConfig().getString("Furniture." + id + ".item"));

        modelData = plugin.getConfig().getInt("Furniture." + id + ".model_data");
        // If modelData is 0, throw an error
        if(modelData == 0) {
            plugin.getLogger().warning("Model data for furniture " + id + " is 0. This is not allowed.");

            throw new IllegalArgumentException("Model data for furniture " + id + " is 0. This is not allowed.");
        }

        rotSides = RotSides.valueOf(plugin.getConfig().getInt("Furniture." + id + ".rotation"));

        // Get all submodels (object list)
        try {
            for (Object obj : plugin.getConfig().getList("Furniture." + id + ".submodels", new ArrayList<>())) {
                if(rotSides == RotSides.EIGHT_SIDED) {
                    throw new IllegalArgumentException("Furniture " + id + " has 8 sided rotation, but has submodels. This is not allowed.");
                }

                // Example format: {offset={x=1, y=0, z=0}, model_data=2}
                if (obj instanceof Map<?, ?> map) {
                    Vector offset = map.get("offset") instanceof Map<?, ?> offsetMap ? new Vector(
                            offsetMap.get("x") instanceof Number x ? x.intValue() : 0,
                            offsetMap.get("y") instanceof Number y ? y.intValue() : 0,
                            offsetMap.get("z") instanceof Number z ? z.intValue() : 0
                    ) : null;

                    int modelData = map.get("model_data") instanceof Number number ? number.intValue() : 0;

                    if(offset == null || offset.lengthSquared() == 0) {
                        throw new IllegalArgumentException("Offset for a submodel of furniture " + id + " is null or 0. This is not allowed.");
                    }
                    if(modelData == 0) {
                        throw new IllegalArgumentException("Model data for a submodel of furniture " + id + " is 0. This is not allowed.");
                    }

                    subModels.add(new SubModel(offset, modelData));
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load submodels for furniture " + id + ". Error: " + e.getMessage());

            throw new IllegalArgumentException("Failed to load submodels for furniture " + id + ". Error: " + e.getMessage());
        }

        // And now get all functions
        try {
            for (FunctionType type : FunctionType.values()) {
                for(Object obj : plugin.getConfig().getList("Furniture." + id + ".functions." + type.name().toUpperCase(), new ArrayList<>())) {
                    if(obj instanceof Map<?, ?> map) {
                        // it contains a type ("type") and multiple arguments, which are stored in a map ("args")
                        String functionType = map.get("type") instanceof String string ? string : null;

                        if(functionType == null) {
                            throw new IllegalArgumentException("Function type for furniture " + id + " is null. This is not allowed.");
                        }

                        HashMap<String, Object> args = new HashMap<>();

                        args.put("type", functionType); // Why not put it in the for below? well for error handling, of course

                        for(Map.Entry<?, ?> entry : map.entrySet()) {
                            if(entry.getKey().equals("type")) continue;

                            args.put(entry.getKey().toString(), entry.getValue());
                        }

                        if(!functions.containsKey(type)) {
                            ArrayList<HashMap<String, Object>> list = new ArrayList<>();

                            list.add(args);

                            functions.put(type, list);
                        } else {
                            functions.get(type).add(args);
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load functions for furniture " + id + ". Error: " + e.getMessage());

            throw new IllegalArgumentException("Failed to load functions for furniture " + id + ". Error: " + e.getMessage());
        }

        init();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public int getModelData() {
        return modelData;
    }

    public RotSides getRotation() {
        return rotSides;
    }

    public List<SubModel> getSubModels() {
        return subModels;
    }

    @Override
    public String toString() {
        return "Furniture{" +
                "id='" + id + '\'' +
                ", modelData=" + modelData +
                '}';
    }

    public ItemStack getGeneratedItem() {
        return generatedItem;
    }

    public ItemStack getBlockItem() {
        return generatedFrameItem;
    }

    private void init() throws IllegalArgumentException {
        // Generate itemstack
        generatedItem = new ItemStack(material);
        generatedItem.setAmount(1);

        ItemMeta meta = generatedItem.getItemMeta();

        if(meta == null) {
            throw new IllegalArgumentException("Failed to generate item for furniture " + id + ". ItemMeta is null. (Material: " + material + ")");
        }

        if(displayName != null) {
            meta.setDisplayName(displayName);
        }
        if(lore != null) {
            meta.setLore(lore);
        }
        meta.setCustomModelData(modelData);

        generatedItem.setItemMeta(meta);



        // Load overrides (drop-item, block-item)
        if(plugin.getConfig().contains("Furniture." + id + ".overrides")) {
            // Drop item
            if(plugin.getConfig().contains("Furniture." + id + ".overrides.drop-item")) {
                // Get fields (item, amount, model_data, display, lore)
                Material dropItem = Material.getMaterial(plugin.getConfig().getString("Furniture." + id + ".overrides.drop-item.item", material.name()));
                int dropAmount = plugin.getConfig().getInt("Furniture." + id + ".overrides.drop-item.amount", 1);
                int dropModelData = plugin.getConfig().getInt("Furniture." + id + ".overrides.drop-item.model_data", modelData);
                String dropDisplayName = plugin.getConfig().getString("Furniture." + id + ".overrides.drop-item.display", displayName);
                List<String> dropLore = plugin.getConfig().getStringList("Furniture." + id + ".overrides.drop-item.lore");

                if(dropItem == null) {
                    throw new IllegalArgumentException("Failed to generate drop item for furniture " + id + ". Material is null.");
                }

                // Generate item
                ItemStack drop = new ItemStack(dropItem);
                drop.setAmount(dropAmount);

                ItemMeta dropMeta = drop.getItemMeta();

                if(dropMeta == null) {
                    throw new IllegalArgumentException("Failed to generate drop item for furniture " + id + ". ItemMeta is null. (Material: " + dropItem + ")");
                }

                if(dropDisplayName != null) {
                    dropMeta.setDisplayName(dropDisplayName);
                }

                if(!dropLore.isEmpty()) {
                    dropMeta.setLore(dropLore);
                }

                if(dropModelData != 0) {
                    dropMeta.setCustomModelData(dropModelData);
                }

                drop.setItemMeta(dropMeta);

                // Now set it
                generatedDropItem = drop;
            }
            if(plugin.getConfig().contains("Furniture." + id + ".overrides.block-item")) {
                // Get fields (item, model_data, display, lore)
                Material blockItem = Material.getMaterial(plugin.getConfig().getString("Furniture." + id + ".overrides.block-item.item", material.name()));
                int blockModelData = plugin.getConfig().getInt("Furniture." + id + ".overrides.block-item.model_data", modelData);
                String blockDisplayName = plugin.getConfig().getString("Furniture." + id + ".overrides.block-item.display", displayName);
                List<String> blockLore = plugin.getConfig().getStringList("Furniture." + id + ".overrides.block-item.lore");

                if(blockItem == null) {
                    throw new IllegalArgumentException("Failed to generate block item for furniture " + id + ". Material is null.");
                }

                // Generate item
                ItemStack block = new ItemStack(blockItem);
                block.setAmount(1);

                ItemMeta blockMeta = block.getItemMeta();

                if(blockMeta == null) {
                    throw new IllegalArgumentException("Failed to generate block item for furniture " + id + ". ItemMeta is null. (Material: " + blockItem + ")");
                }

                if(blockDisplayName != null) {
                    blockMeta.setDisplayName(blockDisplayName);
                }

                if(!blockLore.isEmpty()) {
                    blockMeta.setLore(blockLore);
                }

                if(blockModelData != 0) {
                    blockMeta.setCustomModelData(blockModelData);
                }

                block.setItemMeta(blockMeta);

                // Now set it
                generatedFrameItem = block;
            }
        }

        // if there is no drop item, use the generated item (and same for block item)
        if(generatedDropItem == null) {
            generatedDropItem = generatedItem;
        }
        if(generatedFrameItem == null) {
            generatedFrameItem = generatedItem;
        }
    }

    public ItemStack generateSubModelItem(SubModel subModel) {
        ItemStack item = generatedFrameItem.clone();

        ItemMeta meta = item.getItemMeta();

        assert meta != null;

        meta.setCustomModelData(subModel.getCustomModelData());

        item.setItemMeta(meta);

        return item;
    }

    public boolean place(@NotNull Player player, EquipmentSlot hand, @NotNull Location location) {
        // Go thru all submodels and check if there is space for them
        Rotation rotation = Utils.getRotation(player, rotSides);

        for(SubModel subModel : subModels) {
            Location subModelLocation = Utils.getRelativeLocation(location, subModel.getOffset(), rotation);

            if(Utils.isSolid(subModelLocation.getBlock()) || Utils.entityObstructing(subModelLocation)) {
                return false;
            }
        }

        if(Utils.entityObstructing(location)) {
            return false;
        }

        FurniturePlaceEvent event = new FurniturePlaceEvent(this, player, location);
        plugin.getServer().getPluginManager().callEvent(event);

        if(event.isCancelled()) {
            return false;
        }

        // Set a barrier block at the location
        location.getBlock().setType(Material.AIR);
        // Spawn an item frame at the location
        ItemFrame itemFrame = location.getWorld().spawn(location, ItemFrame.class, (frame) -> {
            // Set the item frame's item to the generated item
            frame.setItem(generatedFrameItem);

            frame.setSilent(true);
            frame.setVisible(false);
            frame.setFixed(true);
            frame.setInvulnerable(true);

            frame.setRotation(rotation);

            frame.setFacingDirection(BlockFace.UP);

            frame.getPersistentDataContainer().set(new NamespacedKey(FurnitureEngine.getPlugin(FurnitureEngine.class), "format"), PersistentDataType.INTEGER, Utils.getFurnitureFormatVersion());
        });

        location.getBlock().setType(Material.BARRIER);

        // Now go thru all submodels and place them
        for(SubModel subModel : subModels) {
            Location subModelLocation = Utils.getRelativeLocation(location, subModel.getOffset(), rotation);

            subModelLocation.getBlock().setType(Material.AIR);
            ItemFrame subModelItemFrame = subModelLocation.getWorld().spawn(subModelLocation, ItemFrame.class, (frame) -> {
                frame.setItem(generateSubModelItem(subModel));

                frame.setSilent(true);
                frame.setVisible(false);
                frame.setFixed(true);
                frame.setInvulnerable(true);

                frame.setRotation(rotation);

                frame.setFacingDirection(BlockFace.UP);

                frame.getPersistentDataContainer().set(new NamespacedKey(FurnitureEngine.getPlugin(FurnitureEngine.class), "format"), PersistentDataType.INTEGER, Utils.getFurnitureFormatVersion());
            });

            subModelLocation.getBlock().setType(Material.BARRIER);
        }

        // play placing animation & remove item from hand (if not in creative)
        if(hand == EquipmentSlot.HAND) {
            player.swingMainHand();

            if(!player.getGameMode().equals(GameMode.CREATIVE)) {
                player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
            }
        } else {
            player.swingOffHand();

            if(!player.getGameMode().equals(GameMode.CREATIVE)) {
                player.getInventory().getItemInOffHand().setAmount(player.getInventory().getItemInOffHand().getAmount() - 1);
            }
        }

        this.callFunction(
                FunctionType.PLACE,
                location,
                player
        );

        return true;
    }

    public void callFunction(FunctionType type, Location clickedLocation, Player interactingPlayer) {
        if(!functions.containsKey(type)) return;

        List<HashMap<String, Object>> funList = functions.get(type);

        for(HashMap<String, Object> args : funList) {
            FunctionManager.getInstance().call(
                    args.get("type").toString(),
                    args,
                    interactingPlayer,
                    this,
                    clickedLocation
            );
        }
    }
}