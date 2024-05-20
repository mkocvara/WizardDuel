package com.mk.wizardduel.gameobjects;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;

public abstract class GameObject
{
	public enum State
	{
		ACTIVE,
		INACTIVE,
		REMOVED
	}

	public static Paint defaultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	public Paint paint = GameObject.defaultPaint;
	public Point pos = new Point();
	public PointF anchor = new PointF(0.f,0.f);
	public PointF scale = new PointF(1.f, 1.f);
	/**
	 * Rotation in degrees.
	 */
	public float rotation = 0.f;
	public boolean collidable = false;

	protected Drawable mDrawable;

	private RectF mCachedViewBounds = null;
	private Matrix mCachedBitmapTransform = null;
	private Region mChachedCollisionRegion = null;
	private int mHeight = -1, mWidth = -1;
	private State mOjectState = State.INACTIVE;

	/**
	 * Calculates this object's bounds as a Rect.
	 * @return Rect object representing the contextualised bounds.
	 */
	public RectF getViewBounds()
	{
		if (mCachedViewBounds != null)
			return mCachedViewBounds;

		RectF viewBounds = new RectF();
		viewBounds.right = mWidth;
		viewBounds.bottom = mHeight;

		Matrix transform = getTransform(mWidth, mHeight);
		transform.mapRect(viewBounds);

		mCachedViewBounds = viewBounds;
		return viewBounds;
	}

	/**
	 * Calculates a <code>Region</code> encompassing the object, which can be used for collision detection.
	 * Unlike a <code>Rect</code>, a <code>Region</code> represents the object's rotation.
	 * @return <code>Rect</code> object representing the contextualised bounds.
	 */
	public Region getCollisionRegion()
	{
		if (mChachedCollisionRegion != null)
			return mChachedCollisionRegion;

		float[] corners = {
				0.f, 		0.0f,		// left,		top
				mWidth, 	0.0f,		// right,	top
				mWidth, 	mHeight,	// right, 	bottom
				0.f, 		mHeight 	// left, 	bottom
		};
		Matrix transform = getTransform(mWidth, mHeight);
		transform.mapPoints(corners);

		Path path = new Path();
		path.moveTo(corners[0], corners[1]);
		path.lineTo(corners[2], corners[3]);
		path.lineTo(corners[4], corners[5]);
		path.lineTo(corners[6], corners[7]);
		path.lineTo(corners[0], corners[1]);

		RectF bounds = getViewBounds();
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

		mChachedCollisionRegion = colRegion;
		return colRegion;
	}

	public void setHeight(int newHeight) { setHeight(newHeight, false); }
	public void setHeight(int newHeight, boolean maintainAspectRatio)
	{
		if (maintainAspectRatio && mWidth != -1 && mHeight != -1)
		{
			int prevHeight = mHeight;
			int prevWidth = mWidth;
			float scale = (float)newHeight / (float)prevHeight;
			mWidth = (int)(prevWidth * scale);
		}

		mHeight = newHeight;
	}

	public void setWidth(int newWidth) { setWidth(newWidth, false); }
	public void setWidth(int newWidth, boolean maintainAspectRatio)
	{
		if (maintainAspectRatio && mWidth != -1 && mHeight != -1)
		{
			int prevHeight = mHeight;
			int prevWidth = mWidth;
			float scale = (float)newWidth / (float)prevWidth;
			mHeight = (int)(prevHeight * scale);
		}

		mWidth = newWidth;
	}

	public State getObjectState() { return mOjectState; }
	public void setActive(boolean active) {
		if (mOjectState == State.REMOVED)
			return;

		mOjectState = active ? State.ACTIVE : State.INACTIVE;
	}
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
		mCachedViewBounds = null;
		mCachedBitmapTransform = null;
		mChachedCollisionRegion = null;
	}

	/**
	 * Draws the object's drawable on the <code>canvas</code> using the provided <code>paint</code>.
	 * @param canvas Canvas object to draw on.
	 * @param overridePaint Paint object to draw the drawable with.
	 */
	public abstract void draw(Canvas canvas, Paint overridePaint);
	/**
	 * Draws the object's drawable on the <code>canvas</code> using the objects own paint.
	 * @param canvas Canvas object to draw on.
	 */
	public final void draw(Canvas canvas) { draw(canvas, paint); }

	public abstract void handleCollision(GameObject other);

	protected Matrix getTransform(Bitmap bitmap)
	{
		if (mCachedBitmapTransform != null)
			return mCachedBitmapTransform;

		mCachedBitmapTransform = getTransform(bitmap.getWidth(), bitmap.getHeight());
		return mCachedBitmapTransform;
	}
	protected Matrix getTransform(float width, float height)
	{
		Matrix transform = new Matrix();

		// Adjust scale using desired height & width
		float scaleX = 0, scaleY = 0;
		if (mWidth != -1)
			scaleX = (float)mWidth / width;

		if (mHeight != -1)
			scaleY = (float)mHeight / height;

		// > Default to maintaining aspect ratio if only one is set
		if (mWidth == -1 && mHeight != -1)
			scaleX = scaleY;
		else if (mWidth != -1 && mHeight == -1)
			scaleY = scaleX;

		scaleX *= scale.x;
		scaleY *= scale.y;

		// Determine anchor offsets
		float anchorOffsetX = -(anchor.x * width  * scaleX);
		float anchorOffsetY = -(anchor.y * height * scaleY);

		// Perform transformations
		transform.setScale(scaleX, scaleY);
		transform.postTranslate(anchorOffsetX, anchorOffsetY);
		transform.postRotate(rotation);
		transform.postTranslate(pos.x, pos.y);

		return transform;
	}

	protected void transformAndDrawBitmap(Canvas canvas, Paint paint, Bitmap bitmap)
	{
		Matrix transform = getTransform(bitmap);
		canvas.drawBitmap(bitmap, transform, paint);

		/*/ DEBUG TEST collision region by drawing it. -- It draws the inverse,
		// somewhat sure if it's the draw's and not the region's fault though.
		// Will probably find out for sure when I make the fireballs and implement
		// actual collisions.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			canvas.clipOutPath(getCollisionRegion().getBoundaryPath());
			canvas.drawColor(0x44ff1122);
		}
		*/
	}
}
