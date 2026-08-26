package io.yukkuric.hexjsneo.mixin_interface;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ActionRegistryEntry.class)
public interface MutableActionRegistryEntry {
    @Accessor("action")
    void setAction(Action newAction);
    @Accessor("prototype")
    void setPrototype(HexPattern newPattern);
}
