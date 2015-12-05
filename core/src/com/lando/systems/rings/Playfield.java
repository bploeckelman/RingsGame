package com.lando.systems.rings;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

/**
 * Brian Ploeckelman created on 12/4/2015.
 */
public class Playfield implements Disposable {

    final int   numSegments;
    final int   sectorNumDivisions;
    final float sectorAngleSize;
    final float outerRadius;

    Array<Array<PolygonSprite>> segmentSprites;
    Array<Sector> sectors;
    Vector2 touchVec;

    Texture textureRed;
    Texture textureGreen;
    Texture textureBlue;
    Texture textureMagenta;

    public Playfield(int numSegments, int sectorNumDivisions, float sectorAngleSize, float outerRadius) {
        this.numSegments = numSegments;
        this.sectorNumDivisions = sectorNumDivisions;
        this.sectorAngleSize = sectorAngleSize;
        this.outerRadius = outerRadius;
        this.touchVec = new Vector2();

        generateSegments();
        generateSectors();
    }

    public void update(float delta) {

    }

    public void render(PolygonSpriteBatch batch) {
        for (Array<PolygonSprite> sectorSegments : segmentSprites) {
            for (PolygonSprite segmentSprite : sectorSegments) {
                segmentSprite.draw(batch);
            }
        }
    }

    public boolean handleTouch(float x, float y) {
        final float ox = 0;
        final float oy = 0;
        final float vx = ox - x;
        final float vy = oy - y;
        final float length = (float) Math.sqrt(vx*vx + vy*vy);
        final float radiusInner = outerRadius / (numSegments + 1);
        final float radiusSegment = (outerRadius - radiusInner) / numSegments;

        // broad phase (is touch in playfield?)
        if (length > outerRadius) {
            return false;
        }

        // mid phase (is touch in sector i?)
        for (Sector sector : sectors) {
            if (sector.handleTouch(x, y)) {
                // narrow phase (is touch in segment within sector i?)
                for (int i = 0; i < numSegments; ++i) {
                    final float r0 = radiusInner + (i + 0) * radiusSegment;
                    final float r1 = radiusInner + (i + 1) * radiusSegment;
                    if (r0 < length && length < r1) {
                        // TODO: Hit the i-th segment... do something about it (move it to the neighboring sector)
                        sector.segments.get(i).rotate(-sectorAngleSize);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void dispose() {
        textureRed.dispose();
        textureGreen.dispose();
        textureBlue.dispose();
        textureMagenta.dispose();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private void generateSegments() {
        // Generate single pixel solid colored textures to differentiate segments
        final Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGB888);
        pixmap.setColor(0xFF000000); pixmap.fill(); textureRed     = new Texture(pixmap);
        pixmap.setColor(0x00FF0000); pixmap.fill(); textureGreen   = new Texture(pixmap);
        pixmap.setColor(0x0000FF00); pixmap.fill(); textureBlue    = new Texture(pixmap);
        pixmap.setColor(0xFF00FF00); pixmap.fill(); textureMagenta = new Texture(pixmap);
        pixmap.dispose();

        final float radiusInner = outerRadius / (numSegments + 1);
        final float radiusSegment = (outerRadius - radiusInner) / numSegments;
        final float thetaStep = sectorAngleSize / sectorNumDivisions;

        // TODO: generate multiple sectors
        final int sectorIndex = 0;
        segmentSprites = new Array<Array<PolygonSprite>>();
        segmentSprites.add(new Array<PolygonSprite>());

        final Array<PolygonRegion> polygonRegions = new Array<PolygonRegion>();
        for (int segment = 0; segment < numSegments; ++segment) {
            float theta = 0f;

            final FloatArray vertices = new FloatArray();
            final ShortArray indices = new ShortArray();
            for (short i = 0; i <= sectorNumDivisions; ++i) {
                float di = radiusInner + segment * radiusSegment;

                float x1 = (di + 0 * radiusSegment) * MathUtils.cosDeg(theta);
                float y1 = (di + 0 * radiusSegment) * MathUtils.sinDeg(theta);
                float x2 = (di + 1 * radiusSegment) * MathUtils.cosDeg(theta);
                float y2 = (di + 1 * radiusSegment) * MathUtils.sinDeg(theta);
                vertices.addAll(x1, y1, x2, y2);

                if (i < sectorNumDivisions) {
                    short i0 = (short) (2 * i + 0);
                    short i1 = (short) (2 * i + 1);
                    short i2 = (short) (2 * i + 2);
                    short i3 = (short) (2 * i + 3);
                    indices.addAll(i0, i1, i3, i0, i3, i2);
                }

                theta += thetaStep;
            }

            final TextureRegion textureRegion;
            switch (segment % 4) {
                default:
                case 0: textureRegion = new TextureRegion(textureRed);     break;
                case 1: textureRegion = new TextureRegion(textureGreen);   break;
                case 2: textureRegion = new TextureRegion(textureBlue);    break;
                case 3: textureRegion = new TextureRegion(textureMagenta); break;
            }
//            final EarClippingTriangulator triangulator = new EarClippingTriangulator();
//            final ShortArray indices = triangulator.computeTriangles(vertices);
            final PolygonRegion polygonRegion = new PolygonRegion(textureRegion, vertices.toArray(), indices.toArray());
            final PolygonSprite polygonSprite = new PolygonSprite(polygonRegion);

            polygonRegions.add(polygonRegion);
            segmentSprites.get(sectorIndex).add(polygonSprite);
        }
    }

    private void generateSectors() {
        float theta = 0f;
        sectors = new Array<Sector>();
        for (Array<PolygonSprite> sectorSprites : segmentSprites) {
            sectors.add(new Sector(sectorSprites, theta, theta += sectorAngleSize));
        }
    }

}
