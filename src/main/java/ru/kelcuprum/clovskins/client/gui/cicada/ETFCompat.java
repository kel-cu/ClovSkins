package ru.kelcuprum.clovskins.client.gui.cicada;

import traben.entity_texture_features.features.ETFRenderContext;

public class ETFCompat {
    public static void preventRenderLayerIssue() {
        ETFRenderContext.preventRenderLayerTextureModify();
    }
}
