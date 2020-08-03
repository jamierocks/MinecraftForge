/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.mixin.server;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.BrandingControl;
import net.minecraftforge.fml.hooks.BasicEventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    /**
     * @author Jamie Mansfield
     * @reason Use the Forge branding
     */
    @Overwrite
    public String getServerModName() {
        return BrandingControl.getServerBranding();
    }

    @Inject(method = "tick",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/Util;nanoTime()J"),
            slice = @Slice(to = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;updateTimeLightAndEntities(Ljava/util/function/BooleanSupplier;)V")
            ))
    private void preTick(final BooleanSupplier hasTimeLeft, final CallbackInfo ci) {
        BasicEventHooks.onPreServerTick();
    }

    // todo: should this be called within the profiler, same as preTick?
    @Inject(method = "tick", at = @At("RETURN"))
    private void postTick(final BooleanSupplier hasTimeLeft, final CallbackInfo ci) {
        BasicEventHooks.onPostServerTick();
    }

}
