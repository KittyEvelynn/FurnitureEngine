package com.mira.furnitureengine.Events;

import com.mira.furnitureengine.furnituremanager.Furniture;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FurnitureBreakEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private boolean isDroppingItems;

    private Furniture furniture;

    private Player interactingPlayer;

    private Location furnitureLocation;

    public FurnitureBreakEvent(Furniture furniture, Player interactingPlayer, Location furnitureLocation) {
        this.furniture = furniture;
        this.interactingPlayer = interactingPlayer;
        this.furnitureLocation = furnitureLocation;

        isDroppingItems = true;
    }

    public Player getPlayer() {
        return interactingPlayer;
    }

    public Location getFurnitureLocation() {
        return furnitureLocation;
    }

    public Furniture getFurniture() {
        return furniture;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isDroppingItems() {
        return isDroppingItems;
    }

    public void setDroppingItems(boolean isDroppingItems) {
        isDroppingItems = isDroppingItems;
    }

}
