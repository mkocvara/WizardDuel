package com.mk.wizardduel.gameobjects;

import android.graphics.Canvas;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import com.mk.wizardduel.utils.Vector2D;

/** This is a bit of an abstract game object, which invisibly sits at the borders
 *  of the playable area, giving them collideable properties. */
public class Boundary extends GameObject
{
	private final boolean mReflective;
	private final Vector2D mSurfaceNormal;

	public Boundary(Rect bounds, Vector2D surfaceNormal) { this(bounds, surfaceNormal, false); }
	public Boundary(Rect bounds, Vector2D surfaceNormal, boolean reflective)
	{
		setHeight(bounds.bottom - bounds.top);
		setWidth(bounds.right - bounds.left);
		setPos(bounds.left, bounds.top);

		mSurfaceNormal = surfaceNormal.getNormalized();

		surfaceNormal.normalize();

		mReflective = reflective;

		setCollideable(true);
		setActive(true);
	}

	public boolean isReflective() { return mReflective; }
	public Vector2D getSurfaceNormal() { return mSurfaceNormal; }

	@Override
	public void draw(@NonNull Canvas canvas)
	{
		// Do not draw.
	}

	@Override
	public void handleCollision(GameObject other)
	{
		// Nothing here, handled in Fireball to ensure each object
		// duly handles its own responses to collisions.
	}
}
