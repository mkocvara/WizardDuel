package com.mk.wizardduel.gameobjects;

import android.graphics.Canvas;

import android.graphics.RectF;
import android.graphics.Region;

import androidx.annotation.NonNull;

import com.mk.wizardduel.R;
import com.mk.wizardduel.WizardApplication;
import com.mk.wizardduel.utils.Vector2D;

public class Shield extends Spell
{
	private static final float WIDTH_DIP = 50f;
	private final Vector2D mStart, mEnd;

	public Shield(Wizard caster)
	{
		setDrawable(WizardApplication.getDrawableFromResourceId(R.drawable.shield_anim));
		setCaster(caster);

		//setCollisionInset(new Vector2D(5f, 0f));
		setCollideable(true);

		mStart = new Vector2D();
		mEnd = new Vector2D();

		setAnchor(0.5f, 0.5f);

		float width = WizardApplication.dipToPx(WIDTH_DIP);

		setWidth((int) width, true);
	}

	public void cast(Vector2D start, Vector2D end)
	{
		updatePosition(start, end);
		setActive(true);
	}

	public void updatePosition(Vector2D start, Vector2D end)
	{
		mStart.set(start);
		mEnd.set(end);
		calculateBoundsAndRotation();
	}

	public void stopCasting()
	{
		setActive(false);
	}

	@Override
	public RectF getWorldBounds()
	{
		RectF viewBounds = new RectF();
		viewBounds.left = (int)Math.min(mStart.x, mEnd.x);
		viewBounds.top = (int)Math.min(mStart.y, mEnd.y);
		viewBounds.right = (int)Math.max(mStart.x, mEnd.x);
		viewBounds.bottom = (int)Math.max(mStart.y, mEnd.y);

		int clipWidth = (int) (viewBounds.right - viewBounds.left);
		int drawWidth = getWidth();
		if (clipWidth < drawWidth)
		{
			int offset = drawWidth/2 - clipWidth/2;
			viewBounds.left -= offset;
			viewBounds.right += offset;
		}

		int clipHeight = (int) (viewBounds.bottom - viewBounds.top);
		int drawHeight = getWidth();
		if (clipHeight < drawHeight)
		{
			int offset = drawHeight/2 - clipHeight/2;
			viewBounds.top -= offset;
			viewBounds.bottom += offset;
		}

		return new RectF(viewBounds);
	}

	@Override
	public void draw(@NonNull Canvas canvas)
	{
		RectF clip = getWorldBounds();

		int save = canvas.save();
		canvas.clipRect(clip);
		super.draw(canvas);
		canvas.restoreToCount(save);
	}

	@Override
	public void handleCollision(GameObject other, Region overlapRegion)
	{
		// No collisions on the shield's part, unless I want to add shield drain when hit by a fireball in the future.
	}

	private void calculateBoundsAndRotation()
	{
		// Set pos to midway point between mStart and mEnd.
		Vector2D startToEnd = mEnd.getSubtracted(mStart);
		setPos(mStart.getAdded(startToEnd.getMultiplied(0.5f)));

		// Set rotation: 0 rotation is the direction [0, 1] (vertical)
		Vector2D noRotDir = new Vector2D(0f, 1f);
		double angle = startToEnd.getAngle() + noRotDir.getAngle();
		rotation = (int) Math.toDegrees(angle);
	}

	public Vector2D getSurfaceNormal()
	{
		Vector2D startToEnd = mEnd.getSubtracted(mStart);
		Vector2D surfaceNormal = startToEnd.getPerp();
		surfaceNormal.normalize();
		return surfaceNormal;
	}
}
