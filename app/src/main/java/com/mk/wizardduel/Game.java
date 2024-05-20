package com.mk.wizardduel;

import android.graphics.Canvas;
import android.graphics.Region;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.mk.wizardduel.gameobjects.GameObject;

import java.util.ArrayList;
import java.util.Iterator;

public class Game extends ViewModel
{
	protected ArrayList<GameObject> mGameObjects = new ArrayList<>();

	public Game()
	{
		Log.i("DEBUG", "Game constructed.");
	}

	public void addObject(GameObject object)
	{
		if (!mGameObjects.contains(object))
			mGameObjects.add(object);
	}

	public int getNumObjects() { return mGameObjects.size(); }

	public void update( /*double deltaTime*/ )
	{
		Log.i("DEBUG", "Game.update() called.");

		Iterator<GameObject> it = mGameObjects.iterator();
		while (it.hasNext())
		{
			GameObject go = it.next();
			if (go.getObjectState() == GameObject.State.ACTIVE)
				go.update();

			if (go.getObjectState() == GameObject.State.REMOVED)
				it.remove();
		}

		handleCollisions();
	}

	public void draw(Canvas canvas)
	{
		for (GameObject go : mGameObjects)
		{
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
			if (go1.getObjectState() != GameObject.State.ACTIVE || !go1.collidable)
				continue;

			for (int j = 1; j < numObjects; j++)
			{
				go2 = mGameObjects.get(j);
				if (go2.getObjectState() != GameObject.State.ACTIVE || !go2.collidable)
					continue;

				Region collisionRegion1 = go1.getCollisionRegion();
				Region collisionRegion2 = go2.getCollisionRegion();

				boolean collisionDetected = collisionRegion1.op(collisionRegion2, Region.Op.INTERSECT);

				if (collisionDetected)
				{
					go1.handleCollision(go2);
					go2.handleCollision(go1);
				}
			}
		}
	}
}