package com.mk.wizardduel.utils;

public class Utils
{
	public static int clamp(int value, int min, int max)
	{
		return Math.max(Math.min(value, max), min);
	}

	public static long clamp(long value, long min, long max)
	{
		return Math.max(Math.min(value, max), min);
	}

	public static float clamp(float value, float min, float max)
	{
		return Math.max(Math.min(value, max), min);
	}

	public static double clamp(double value, double min, double max)
	{
		return Math.max(Math.min(value, max), min);
	}
}
