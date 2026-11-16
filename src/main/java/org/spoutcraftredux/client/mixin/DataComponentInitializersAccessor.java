package org.spoutcraftredux.client.mixin;

import java.util.List;
import net.minecraft.core.component.DataComponentInitializers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DataComponentInitializers.class)
public interface DataComponentInitializersAccessor {
    @Accessor("initializers")
    List<?> spoutcraftredux$getInitializers();
}
