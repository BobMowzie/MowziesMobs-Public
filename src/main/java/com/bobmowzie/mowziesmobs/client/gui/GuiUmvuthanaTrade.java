package com.bobmowzie.mowziesmobs.client.gui;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.EntityUmvuthanaMinion;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.trade.Trade;
import com.bobmowzie.mowziesmobs.server.inventory.ContainerUmvuthanaTrade;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class GuiUmvuthanaTrade extends AbstractContainerScreen<ContainerUmvuthanaTrade> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "textures/gui/container/umvuthana.png");

    private final EntityUmvuthanaMinion umvuthana;

    public GuiUmvuthanaTrade(ContainerUmvuthanaTrade screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.umvuthana = screenContainer.getUmvuthana();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
//        if (barakoaya.getAnimation() == IAnimatedEntity.NO_ANIMATION) {
//            if (isHovering(13, 23, 8, 14, mouseX, mouseY)) {
//                barakoaya.setAnimation(EntityBarakoaVillager.ATTACK_ANIMATION);
//                barakoaya.setAnimationTick(3);
//            }
//        } TODO
        return super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int x, int y) {
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (umvuthana != null) {
            umvuthana.renderingInGUI = true;
            // x and y values are chosen as the first and last pixel of the black (entity) box of the gui texture
            // The two x and y values determine the size for the 'GuiGraphics#enableScissor' call (their middle point is also where the entity will be rendered)
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, leftPos + 8, topPos + 8, leftPos + 59, topPos + 69, 20, 0.25f, x, y, umvuthana);
            umvuthana.renderingInGUI = false;
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int x, int y) {
        String title = I18n.get("entity.mowziesmobs.umvuthana.trade");
        guiGraphics.drawString(font, title, (int) (imageWidth / 2f - font.width(title) / 2f + 26), 6, 4210752, false);
        guiGraphics.drawString(font, I18n.get("container.inventory"), 8, imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        if (umvuthana != null && umvuthana.isOfferingTrade()) {
            Trade trade = umvuthana.getOfferingTrade();
            ItemStack input = trade.getInput();
            ItemStack output = trade.getOutput();
            guiGraphics.pose().pushPose();

            guiGraphics.pose().translate(0, 0, 100);
            guiGraphics.renderItem(input, leftPos + 80, topPos + 24);
            guiGraphics.renderItemDecorations(font, input, leftPos + 80, topPos + 24);
            guiGraphics.renderItem(output, leftPos + 134, topPos + 24);
            guiGraphics.renderItemDecorations(font, output, leftPos + 134, topPos + 24);

            if (isHovering(80, 24, 16, 16, mouseX, mouseY)) {
                guiGraphics.renderTooltip(font, input, mouseX, mouseY);
            } else if (isHovering(134, 24, 16, 16, mouseX, mouseY)) {
                guiGraphics.renderTooltip(font, output, mouseX, mouseY);
            }
            guiGraphics.pose().popPose();
        }
    }
}
