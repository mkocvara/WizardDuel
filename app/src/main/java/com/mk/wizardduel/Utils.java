package com.mk.wizardduel;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class Utils
{
	// Source: https://stackoverflow.com/a/36494192/25070270
	public static Bitmap makeFlippedBitmap(Bitmap source, boolean xFlip, boolean yFlip) {
		Matrix matrix = new Matrix();
		matrix.postScale(xFlip ? -1 : 1, yFlip ? -1 : 1, source.getWidth() / 2f, source.getHeight() / 2f);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}
}
