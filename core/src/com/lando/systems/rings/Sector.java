package com.lando.systems.rings;

import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Brian Ploeckelman created on 12/4/2015.
 */
public class Sector {

    public Array<PolygonSprite> segments;

    private float minAngle;
    private float maxAngle;

    public Sector(Array<PolygonSprite> segments, float minAngle, float maxAngle) {
        this.segments = segments;
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
    }

    public boolean handleTouch(float x, float y) {
        final float theta = MathUtils.atan2(y, x) * MathUtils.radiansToDegrees;
        final float angle = (theta + 360f) % 360; // convert theta from [-180,0,180] to [0,359]
        return (minAngle <= angle && angle <= maxAngle);
    }

}
