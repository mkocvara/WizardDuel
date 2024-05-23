package com.mk.wizardduel.utils;

import android.graphics.PointF;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

//	SOURCE: https://gist.github.com/gunvirranu/6816d65c0231981787ebefd3bdb61f98 by @gunvirranu
//	Applied on top of PointF, adapted to use floats to work with the superclass, and @annotated.

/** A 2D vector class implemented as an extension of android.graphics.PointF. */
public class Vector2D extends PointF
{
	public Vector2D() { }

	public Vector2D(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public Vector2D(Vector2D v)
	{
		set(v);
	}

	public void set(@NonNull Vector2D v)
	{
		this.x = v.x;
		this.y = v.y;
	}

	public void setX(float x)
	{
		this.x = x;
	}

	public void setY(float y)
	{
		this.y = y;
	}

	public void setZero()
	{
		x = 0;
		y = 0;
	}

	public float[] getComponents()
	{
		return new float[]{x, y};
	}

	public double getLength()
	{
		return Math.sqrt(x * x + y * y);
	}

	public float getLengthSq()
	{
		return (x * x + y * y);
	}

	public float distanceSq(float vx, float vy)
	{
		vx -= x;
		vy -= y;
		return (vx * vx + vy * vy);
	}

	public float distanceSq(@NonNull Vector2D v)
	{
		float vx = v.x - this.x;
		float vy = v.y - this.y;
		return (vx * vx + vy * vy);
	}

	public double distance(float vx, float vy)
	{
		vx -= x;
		vy -= y;
		return Math.sqrt(vx * vx + vy * vy);
	}

	public double distance(@NonNull Vector2D v)
	{
		float vx = v.x - this.x;
		float vy = v.y - this.y;
		return Math.sqrt(vx * vx + vy * vy);
	}

	public double getAngle()
	{
		return Math.atan2(y, x);
	}

	public void normalize()
	{
		float magnitude = (float) getLength();
		x /= magnitude;
		y /= magnitude;
	}

	public Vector2D getNormalized()
	{
		float magnitude = (float) getLength();
		return new Vector2D(x / magnitude, y / magnitude);
	}

	@NonNull
	@Contract("_, _ -> new")
	public static Vector2D toCartesian(float magnitude, float angle)
	{
		return new Vector2D(magnitude * (float) Math.cos(angle), magnitude * (float) Math.sin(angle));
	}

	public void add(@NonNull Vector2D v)
	{
		this.x += v.x;
		this.y += v.y;
	}

	public void add(float vx, float vy)
	{
		this.x += vx;
		this.y += vy;
	}

	@NonNull
	@Contract("_, _ -> new")
	public static Vector2D add(@NonNull Vector2D v1, @NonNull Vector2D v2)
	{
		return new Vector2D(v1.x + v2.x, v1.y + v2.y);
	}

	public Vector2D getAdded(@NonNull Vector2D v)
	{
		return new Vector2D(this.x + v.x, this.y + v.y);
	}

	public void subtract(@NonNull Vector2D v)
	{
		this.x -= v.x;
		this.y -= v.y;
	}

	public void subtract(float vx, float vy)
	{
		this.x -= vx;
		this.y -= vy;
	}

	@NonNull
	@Contract("_, _ -> new")
	public static Vector2D subtract(@NonNull Vector2D v1, @NonNull Vector2D v2)
	{
		return new Vector2D(v1.x - v2.x, v1.y - v2.y);
	}

	public Vector2D getSubtracted(@NonNull Vector2D v)
	{
		return new Vector2D(this.x - v.x, this.y - v.y);
	}

	public void multiply(float scalar)
	{
		x *= scalar;
		y *= scalar;
	}

	public Vector2D getMultiplied(float scalar)
	{
		return new Vector2D(x * scalar, y * scalar);
	}

	public void divide(float scalar)
	{
		x /= scalar;
		y /= scalar;
	}

	public Vector2D getDivided(float scalar)
	{
		return new Vector2D(x / scalar, y / scalar);
	}

	public Vector2D getPerp()
	{
		return new Vector2D(-y, x);
	}

	public float dot(@NonNull Vector2D v)
	{
		return (this.x * v.x + this.y * v.y);
	}

	public float dot(float vx, float vy)
	{
		return (this.x * vx + this.y * vy);
	}

	@Contract(pure = true)
	public static float dot(@NonNull Vector2D v1, @NonNull Vector2D v2)
	{
		return v1.x * v2.x + v1.y * v2.y;
	}

	public float cross(@NonNull Vector2D v)
	{
		return (this.x * v.y - this.y * v.x);
	}

	public float cross(float vx, float vy)
	{
		return (this.x * vy - this.y * vx);
	}

	@Contract(pure = true)
	public static float cross(@NonNull Vector2D v1, @NonNull Vector2D v2)
	{
		return (v1.x * v2.y - v1.y * v2.x);
	}

	public double project(Vector2D v)
	{
		return (this.dot(v) / this.getLength());
	}

	public double project(float vx, float vy)
	{
		return (this.dot(vx, vy) / this.getLength());
	}

	public static double project(Vector2D v1, Vector2D v2)
	{
		return (dot(v1, v2) / v1.getLength());
	}

	public Vector2D getProjectedVector(Vector2D v)
	{
		return this.getNormalized().getMultiplied((float) (this.dot(v) / this.getLength()));
	}

	public Vector2D getProjectedVector(float vx, float vy)
	{
		return this.getNormalized().getMultiplied((float) (this.dot(vx, vy) / this.getLength()));
	}

	public static Vector2D getProjectedVector(@NonNull Vector2D v1, Vector2D v2)
	{
		return v1.getNormalized().getMultiplied((float) (Vector2D.dot(v1, v2) / v1.getLength()));
	}

	public void rotateBy(float angle)
	{
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		float rx = (float) (x * cos - y * sin);
		y = (float) (x * sin + y * cos);
		x = rx;
	}

	public Vector2D getRotatedBy(float angle)
	{
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);
		return new Vector2D(x * cos - y * sin, x * sin + y * cos);
	}

	public void rotateTo(float angle)
	{
		set(toCartesian((float) getLength(), angle));
	}

	public Vector2D getRotatedTo(float angle)
	{
		return toCartesian((float) getLength(), angle);
	}

	public void reverse()
	{
		x = -x;
		y = -y;
	}

	public Vector2D getReversed()
	{
		return new Vector2D(-x, -y);
	}

	@NonNull
	@Override
	public Vector2D clone()
	{
		return new Vector2D(x, y);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (obj instanceof Vector2D)
		{
			Vector2D v = (Vector2D) obj;
			return (x == v.x) && (y == v.y);
		}
		return false;
	}

	@NonNull
	@Override
	public String toString()
	{
		return "Vector2D[" + x + ", " + y + "]";
	}
}
