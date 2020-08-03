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

package net.minecraftforge.mixin.resources;

import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.VanillaPack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Map;

@Mixin(VanillaPack.class)
public abstract class VanillaPackMixin {

    @Shadow @Final private static Map<ResourcePackType, FileSystem> FILE_SYSTEMS_BY_PACK_TYPE;

    @Shadow
    private static String getPath(final ResourcePackType packTypeIn, final ResourceLocation locationIn) {
        throw new RuntimeException("Mixin shadow method");
    }

    @Redirect(method = "getInputStreamVanilla(Lnet/minecraft/resources/ResourcePackType;Lnet/minecraft/util/ResourceLocation;)Ljava/io/InputStream;", at = @At(
            value = "INVOKE",
            target = "Ljava/net/URL;openStream()Ljava/io/InputStream;"
    ))
    public InputStream getInputStreamVanilla(final URL url, final ResourcePackType type, final ResourceLocation location) {
        final String path = getPath(type, location);
        return this.forge$getExtraInputStream(type, path);
    }

    /**
     * @author Jamie Mansfield
     * @reason ForgeGradle splits the jar in two, one for the game's source code and another
     *         for the game's assets - this requires changes to the game to find assets in
     *         the extra jar.
     */
    @Nullable
    @Overwrite
    protected InputStream getInputStreamVanilla(final String path) {
        return this.forge$getExtraInputStream(ResourcePackType.SERVER_DATA, "/" + path);
    }

    public InputStream forge$getExtraInputStream(final ResourcePackType type, final String path) {
        final FileSystem fs = FILE_SYSTEMS_BY_PACK_TYPE.get(type);
        if (fs != null) {
            try {
                return Files.newInputStream(fs.getPath(path));
            }
            catch (final IOException ignored) {
            }
        }

        return VanillaPack.class.getResourceAsStream(path);
    }

}
