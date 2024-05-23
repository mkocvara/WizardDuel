package com.mk.wizardduel.gameobjects;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.mk.wizardduel.utils.Vector2D;

public abstract class GameObject
{
	public enum State
	{
		ACTIVE,
		INACTIVE,
		REMOVED
	}
	/**
	 * Rotation in degrees.
	 */
	public float rotation = 0.f;

	private final Vector2D mPos = new Vector2D(0.f,0.f);
	private final Vector2D mAnchor = new Vector2D(0.f,0.f);
	private final Vector2D mScale = new Vector2D(1.f, 1.f);

	private Drawable mDrawable;
	private @ColorInt int mTint = 0xFFFFFFFF;

	private Matrix mCachedTransform = null;
	private RectF mCachedWorldBounds = null;
	private Region mCachedCollisionRegion = null;
	private int mHeight = -1, mWidth = -1;
	private State mOjectState = State.INACTIVE;
	private boolean mCollideable = false;
	private final Vector2D mCollisionInset = new Vector2D(0f, 0f);

	public State getObjectState() { return mOjectState; }

	public Vector2D getPos() { return new Vector2D(mPos); }
	public Vector2D getAnchor() { return new Vector2D(mAnchor); }
	public Vector2D getScale() { return new Vector2D(mScale); }

	public int getHeight() {
		if (mHeight != -1)
			return mHeight;

		if (mDrawable != null)
			return mHeight = mDrawable.getIntrinsicHeight();

		setActive(false);
		return 0;
	}

	public int getWidth() {
		if (mWidth != -1)
			return mWidth;

		if (mDrawable != null)
			return mWidth = mDrawable.getIntrinsicWidth();

		setActive(false);
		return 0;
	}

	/**
	 * Calculates this object's bounds as a Rect within the world (GameView).
	 * @return Rect object representing the contextualised bounds.
	 */
	public RectF getWorldBounds()
	{
		if (mCachedWorldBounds != null)
			return new RectF(mCachedWorldBounds);

		RectF viewBounds = new RectF();
		viewBounds.right = getWidth();
		viewBounds.bottom = getHeight();

		Matrix transform = getTransform();
		transform.mapRect(viewBounds);

		mCachedWorldBounds = viewBounds;
		return new RectF(viewBounds);
	}

	public Vector2D getCentrePoint()
	{
		RectF worldBounds = getWorldBounds();
		return new Vector2D(worldBounds.centerX(), worldBounds.centerY());
	}

	/**
	 * Calculates a <code>Region</code> encompassing the object, which can be used for collision detection.
	 * Unlike a <code>Rect</code>, a <code>Region</code> represents the object's rotation.
	 * @return <code>Rect</code> object representing the contextualised bounds.
	 */
	public Region getCollisionRegion()
	{
		if (mCachedCollisionRegion != null)
			return new Region(mCachedCollisionRegion);

		int h = getHeight();
		int w = getWidth();

		float[] corners = {
				0.f, 	0.0f,	// left,		top
				w, 	0.0f,	// right,	top
				w, 	h,		// right, 	bottom
				0.f, 	h 		// left, 	bottom
		};
		Matrix transform = getTransform();
		transform.mapPoints(corners);

		Path path = new Path();
		path.moveTo(corners[0], corners[1]);
		path.lineTo(corners[2], corners[3]);
		path.lineTo(corners[4], corners[5]);
		path.lineTo(corners[6], corners[7]);
		path.lineTo(corners[0], corners[1]);

		RectF bounds = getWorldBounds();
		bounds.left -= mCollisionInset.x;
		bounds.right -= mCollisionInset.x;
		bounds.top -= mCollisionInset.y;
		bounds.bottom -= mCollisionInset.y;

		Rect clipRect = new Rect(
				(int) bounds.left,
				(int) bounds.top,
				(int) bounds.right,
				(int) bounds.bottom)
				;

		Region clipRegion = new Region();
		clipRegion.set(clipRect);

		Region colRegion = new Region();
		colRegion.setPath(path, clipRegion);

		mCachedCollisionRegion = colRegion;
		return new Region(colRegion);
	}

	public @ColorInt int getTint() { return mTint; }
	public boolean isCollideable() { return mCollideable; }
	public boolean isActive() { return mOjectState == State.ACTIVE; }
	public Vector2D getCollisionInset() { return mCollisionInset; }

	public void setPos(Vector2D pos) { mPos.set(pos); }
	public void setPos(float x, float y) { mPos.set(x, y); }
	public void setAnchor(Vector2D anchor) { mAnchor.set(anchor); }
	public void setAnchor(float x, float y) { mAnchor.set(x, y); }
	public void setScale(Vector2D scale) { mScale.set(scale); }
	public void setScale(float x, float y) { mScale.set(x, y); }

	public void setDrawable(Drawable drawable)
	{
		mDrawable = drawable;
		mDrawable.setTint(mTint);
	}

	public void setHeight(int newHeight) { setHeight(newHeight, false); }
	public void setHeight(int newHeight, boolean maintainAspectRatio)
	{
		if (maintainAspectRatio)
		{
			int prevHeight = getHeight();
			int prevWidth = getWidth();
			float scale = (float)newHeight / (float)prevHeight;
			mWidth = (int)(prevWidth * scale);
		}

		mHeight = newHeight;
	}

	public void setWidth(int newWidth) { setWidth(newWidth, false); }
	public void setWidth(int newWidth, boolean maintainAspectRatio)
	{
		if (maintainAspectRatio)
		{
			int prevHeight = getHeight();
			int prevWidth = getWidth();
			float scale = (float)newWidth / (float)prevWidth;
			mHeight = (int)(prevHeight * scale);
		}

		mWidth = newWidth;
	}

	public void resetDimensions()
	{
		mHeight = mDrawable.getIntrinsicHeight();
		mWidth = mDrawable.getIntrinsicWidth();
	}

	public void setTint(@ColorInt int tint)
	{
		mTint = tint;

		if (mDrawable == null)
			return;

		mDrawable.setTint(tint);
	}

	protected void setActive(boolean active) {
		if (mOjectState == State.REMOVED)
			return;

		mOjectState = active ? State.ACTIVE : State.INACTIVE;
	}

	protected void setCollideable(boolean collideable) { mCollideable = collideable; }
	protected void setCollisionInset(Vector2D inset) { mCollisionInset.set(inset); }

	public void destroy() { mOjectState = State.REMOVED; }

	/**
	 * Returns the object's drawable as AnimationDrawable, if it is one.
	 * @return AnimationDrawable associated with this object, or null if there isn't one.
	 */
	public AnimationDrawable getAnim()
	{
		if (mDrawable instanceof AnimationDrawable)
			return  (AnimationDrawable) mDrawable;
		else
			return null;
	}

	/**
	 * Called every frame by the Game manager. Any inner calculations or actions should happen here.
	 */
	public void update(double deltaTime)
	{
		mCachedWorldBounds = null;
		mCachedCollisionRegion = null;
		mCachedTransform = null;
	}

	/**
	 * Draws the object's drawable on the <code>canvas</code> using the object's tint.
	 * @param canvas Canvas object to draw on.
	 */
	public void draw(@NonNull Canvas canvas)
	{
		Matrix transform = getTransform();

		Rect bounds = new Rect(0, 0, getWidth(), getHeight());
		RectF mappedBounds = new RectF(bounds);
		transform.mapRect(mappedBounds);
		Rect drawBounds = new Rect();
		mappedBounds.round(drawBounds);

		mDrawable.setBounds(drawBounds);

		canvas.save();
		canvas.rotate(rotation, drawBounds.exactCenterX(), drawBounds.exactCenterY());
		mDrawable.draw(canvas);
		canvas.restore();
	}

	public abstract void handleCollision(GameObject other, Region overlapRegion);

	protected Matrix getTransform()
	{
		if (mCachedTransform != null)
			return new Matrix(mCachedTransform);

		Matrix transform = new Matrix();

		// Determine anchor offsets
		float anchorOffsetX = -(mAnchor.x * getWidth());
		float anchorOffsetY = -(mAnchor.y * getHeight());

		// Perform transformations
		transform.setTranslate(anchorOffsetX, anchorOffsetY);
		transform.postScale(mScale.x, mScale.y);
		transform.postTranslate(mPos.x, mPos.y);

		mCachedTransform = transform;
		return new Matrix(transform);
	}
}
