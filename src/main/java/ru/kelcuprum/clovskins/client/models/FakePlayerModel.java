package ru.kelcuprum.clovskins.client.models;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class FakePlayerModel extends PlayerModel {
    public FakePlayerModel(ModelPart modelPart, boolean bl) {
        super(modelPart, bl);
    }

    public void setRotate(float yaw, float pitch){
        this.head.yRot = yaw;
        this.head.xRot = pitch;
    }

    public void swingArmsGently(long totalDeltaTick) {
        float f = Mth.sin(totalDeltaTick * 0.067F) * 0.05F;
        this.rightArm.zRot = f + 0.06F;
        this.rightArm.xRot = f;
//        this.rightSleeve.zRot = f + 0.03F;
        this.leftArm.zRot = -f - 0.06F;
        this.leftArm.xRot = -f ;
//        this.leftSleeve.zRot = -f - 0.03F;
    }
}
