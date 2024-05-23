package com.mk.wizardduel;

import android.graphics.Canvas;
import android.graphics.Region;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.mk.wizardduel.gameobjects.GameObject;

import java.util.ArrayList;
import java.util.Iterator;

public class Game extends ViewModel
{
	public GameData gameData = new GameData();
	protected ArrayList<GameObject> mGameObjects = new ArrayList<>();

	private boolean mStarted = false;

	public Game() { }

	public void addObject(GameObject object)
	{
		if (!mGameObjects.contains(object))
		{
			mGameObjects.add(object);
		}
	}

	public int getNumObjects() { return mGameObjects.size(); }
	public ArrayList<AnimationDrawable> getAllAnims()
	{
		ArrayList<AnimationDrawable> anims = new ArrayList<>();
		for (GameObject go : mGameObjects)
		{
			AnimationDrawable anim = go.getAnim();
			if (anim != null)
				anims.add(anim);
		}
		return anims;
	}

	public boolean hasStarted() { return mStarted; }

	public void update( double deltaTime )
	{
		//Log.i("DEBUG", "Game.update() called.");
		mStarted = true;

		Iterator<GameObject> it = mGameObjects.iterator();
		while (it.hasNext())
		{
			GameObject go = it.next();
			if (go.isActive())
				go.update(deltaTime);

			if (go.getObjectState() == GameObject.State.REMOVED)
				it.remove();
		}

		handleCollisions();
	}

	public void draw(Canvas canvas)
	{
		for (GameObject go : mGameObjects)
		{
			if (go.isActive())
				go.draw(canvas);
		}
	}

	private void handleCollisions()
	{
		int numObjects = getNumObjects();
		GameObject go1, go2;
		for (int i = 0; i < numObjects-1; i++)
		{
			go1 = mGameObjects.get(i);
			if (!go1.isActive() || !go1.isCollideable())
				continue;

			for (int j = 1; j < numObjects; j++)
			{
				go2 = mGameObjects.get(j);
				if (!go2.isActive() || !go2.isCollideable() || go1 == go2)
					continue;

				Region collisionRegion1 = go1.getCollisionRegion();
				Region collisionRegion2 = go2.getCollisionRegion();

				boolean collisionDetected = collisionRegion1.op(collisionRegion2, Region.Op.INTERSECT);

				if (collisionDetected)
				{
					go1.handleCollision(go2, collisionRegion1);
					go2.handleCollision(go1, collisionRegion1);
				}
			}
		}
	}
}