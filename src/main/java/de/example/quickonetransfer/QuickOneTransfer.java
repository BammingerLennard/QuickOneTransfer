package de.example.quickonetransfer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ItemStack;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.recipe.RecipeType;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class QuickOneTransfer implements ClientModInitializer {
 @Override public void onInitializeClient() {
  ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
   if (!(screen instanceof HandledScreen)) return;
   ScreenMouseEvents.allowMouseClick(screen).register((s, mouseX, mouseY, button) -> {
    if (button != GLFW.GLFW_MOUSE_BUTTON_RIGHT || !InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) && !InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT)) return true;
    HandledScreen<?> handled = (HandledScreen<?>) s;
    ScreenHandler handler = handled.getScreenHandler();
    if (!(handler instanceof CraftingScreenHandler) && !(handler instanceof AbstractFurnaceScreenHandler)) return true;
    // Slot under cursor is resolved using the screen's accessor mixin.
    Slot source = ((HandledScreenAccessor) handled).quickone$getFocusedSlot();
    if (source == null || !source.hasStack() || source.inventory != client.player.getInventory()) return true;
    if (!handler.getCursorStack().isEmpty()) return true;
    int target = -1;
    if (handler instanceof CraftingScreenHandler) {
     for (int i = 1; i <= 9; i++) if (!handler.getSlot(i).hasStack() && handler.getSlot(i).canInsert(source.getStack())) { target=i; break; }
    } else {
     ItemStack stack = source.getStack();
     if (client.world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, new net.minecraft.inventory.SimpleInventory(stack), client.world).isPresent() && !handler.getSlot(0).hasStack()) target=0;
     else if (AbstractFurnaceBlockEntity.canUseAsFuel(stack) && !handler.getSlot(1).hasStack()) target=1;
    }
    if (target < 0 || client.interactionManager == null) return true;
    int sync = handler.syncId;
    client.interactionManager.clickSlot(sync, source.id, 0, SlotActionType.PICKUP, client.player);
    client.interactionManager.clickSlot(sync, target, 1, SlotActionType.PICKUP, client.player);
    client.interactionManager.clickSlot(sync, source.id, 0, SlotActionType.PICKUP, client.player);
    return false;
   });
  });
 }
}
