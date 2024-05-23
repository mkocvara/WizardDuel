package com.mk.wizardduel;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;

import com.mk.wizardduel.services.GameService;
import com.mk.wizardduel.utils.Vector2D;

/** A class which handles all of the game's inputs. Much of this class' implementation
 * has been taken from GestureDetector and adapted to work with multi-touch. */
public class GameInputManager extends 	  GestureDetector.SimpleOnGestureListener
										implements GestureDetector.OnDoubleTapListener,
													  ScaleGestureDetector.OnScaleGestureListener
{
	// Message type constants akin to those used by GestureDetector,
	// except using it for Message.arg1 in GameGestureHandler below
	private static final int SHOW_PRESS = 1;
	private static final int TAP = 2; // TODO unneeded?

	@SuppressLint("HandlerLeak")
		// Strange warning given this is exactly how it's implemented by Google in GestureDetector...
	private class GameGestureHandler extends Handler
	{
		GameGestureHandler() {
			super();
		}

		GameGestureHandler(@NonNull Handler handler) {
			super(handler.getLooper());
		}

		// Note:
		// > using Message.what for pointer ID (so that removeMessage(int) removes messages for a pointer)
		// > using Message.arg1 for event type

		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
				case SHOW_PRESS:
					if (!(msg.obj instanceof MotionEvent))
						break;

					onPointerShowPress((MotionEvent) msg.obj, msg.what);
					break;

				case TAP:
					// TODO
					/*/ If the user's finger is still down, do not count it as a tap
					if (mDoubleTapListener != null) {
						if (!mStillDown) {
							recordGestureClassification(
									TOUCH_GESTURE_CLASSIFIED__CLASSIFICATION__SINGLE_TAP);
							mDoubleTapListener.onSingleTapConfirmed(mCurrentDownEvent);
						} else {
							mDeferConfirmSingleTap = true;
						}
					}*/
					break;

				default:
					throw new RuntimeException("Unknown message " + msg); //never
			}
		}
	}

	// Members
	final private String TOUCH_DEBUG_TAG = "DEBUG:Touch"; // Logging tag
	private final GameService mGameService;
	private final ViewConfiguration mViewConfig;
	private final Handler mHandler;
	private VelocityTracker mVelocityTracker;

	public GameInputManager(GameService gameService, ViewConfiguration viewConfiguration)
	{
		mGameService = gameService;
		mViewConfig = viewConfiguration;
		mHandler = new GameGestureHandler();
			// TODO would be improved by running it in another thread, but alas, I lack the time to implement that
	}

	/** Called manually by GameView. Top-level touch event. */
	public boolean onTouch(@NonNull MotionEvent event)
	{
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);

		int eventType = event.getActionMasked();

		if ((eventType & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN ||
			 (eventType & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN)
		{
			int pointerIndex = event.getActionIndex();
			int pointerId = event.getPointerId(pointerIndex);
			onPointerDown(event, pointerId);
		}
		else if ((eventType & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP ||
			 (eventType & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP)
		{
			int pointerIndex = event.getActionIndex();
			int pointerId = event.getPointerId(pointerIndex);
			onPointerUp(event, pointerId);
		} else if ((eventType & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE)
		{
			onMove(event);
		}
		else if ((eventType & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_CANCEL)
		{
			onCancel(event);
		}

		return true;
	}


	// === Action events ===

	public void onPointerDown(@NonNull MotionEvent event, int pointerId)
	{
		Log.i(TOUCH_DEBUG_TAG, "Pointer down, id == " + pointerId);
		//mDownPointerIds.add(pointerId);

		Message message = Message.obtain(mHandler);
		message.what = pointerId;
		message.obj = event;
		message.arg1 = SHOW_PRESS;

		mHandler.sendMessageAtTime(message, event.getEventTime() + ViewConfiguration.getTapTimeout());
	}

	public void onPointerUp(@NonNull MotionEvent event, int pointerId)
	{
		Log.i(TOUCH_DEBUG_TAG, "Pointer up, id == " + pointerId);

		final int minFlingVelocity = mViewConfig.getScaledMinimumFlingVelocity() * 15; // times that because by default it is very low
		final int maxFlingVelocity = mViewConfig.getScaledMaximumFlingVelocity();

		mVelocityTracker.computeCurrentVelocity(1000, maxFlingVelocity);
		final Vector2D velocity = new Vector2D(mVelocityTracker.getXVelocity(pointerId), mVelocityTracker.getYVelocity(pointerId));

		if ((Math.abs(velocity.getLength()) > minFlingVelocity))
		{
			onPointerFling(event, pointerId, velocity);
		}
		else
		{
			mHandler.removeMessages(pointerId);
			mGameService.cancelFireball(pointerId);
		}
	}

	public void onMove(@NonNull MotionEvent event)
	{
		int numPointers = event.getPointerCount();

		for (int i = 0; i < numPointers; i++)
		{
			// Log.i(TOUCH_DEBUG_TAG, "onMove(), id:" + event.getPointerId(i) + " x:" + event.getX(i) + " y:" + event.getY(i));
			mGameService.moveFireball(event.getPointerId(i), event.getX(i), event.getY(i));
		}
	}

	private void onCancel(MotionEvent event)
	{
		mGameService.cancelAllFireballs();
	}


	// === Custom gesture events ===
	public void onPointerShowPress(@NonNull MotionEvent event, int pointerId)
	{
		Log.i(TOUCH_DEBUG_TAG, "onPointerShowPress() triggered for pointer with id == " + pointerId);

		int pointerIndex = event.findPointerIndex(pointerId);
		if (pointerIndex == -1)
			return;

		Vector2D pos = new Vector2D(event.getX(pointerIndex), event.getY(pointerIndex));
		mGameService.castFireball(pos, pointerId);
	}

	// Note: standard oFling() also gets the initial down event, but it doesn't seem necessary here.
	public void onPointerFling(@NonNull MotionEvent endEvent, int pointerId, Vector2D velocity)
	{
		Log.i(TOUCH_DEBUG_TAG, "Fling detected for pointer with id == " + pointerId);

		mGameService.releaseFireball(pointerId, velocity.getNormalized());
		mHandler.removeMessages(pointerId);
	}


	// === Native gesture events ===

	@Override
	public boolean onDoubleTap(@NonNull MotionEvent e)
	{
		Log.i(TOUCH_DEBUG_TAG, "onDoubleTap() triggered");
		return false;
	}

	@Override
	public boolean onScaleBegin(@NonNull ScaleGestureDetector detector)
	{
		// TODO shield create IF conditions are met
		Log.i(TOUCH_DEBUG_TAG, "onScaleBegin() triggered");
		return true;
	}

	@Override
	public boolean onScale(@NonNull ScaleGestureDetector detector)
	{
		// TODO Shield update IF exists
		//Log.i(TOUCH_DEBUG_TAG, "onScale() triggered");
		return false;
	}

	@Override
	public void onScaleEnd(@NonNull ScaleGestureDetector detector)
	{
		// TODO Destroy shield IF exists
		Log.i(TOUCH_DEBUG_TAG, "onScaleEnd() triggered");
	}
}
