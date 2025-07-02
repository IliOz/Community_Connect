package com.example.myfinalproject.gamesActivities.Physics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.annotation.NonNull;

// Custom SurfaceView for a simple 2D ball game simulation with physics and touch controls.
public class BallGameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "BallGameSurfaceView";

    // ## Configuration Constants ##
    public static final float DEFAULT_BALL_RADIUS = 30f;
    public static final float DEFAULT_MASS_KG = 1.0f;
    public static final float GRAVITY_ACCELERATION = 9.81f; // m/s^2, for friction calculation
    public static final float USER_CONTROL_ACCELERATION_MAGNITUDE = 200f; // Pixels/s^2 for user touch control

    public static final float MAX_HORIZONTAL_VELOCITY = 350f; // Max speed in pixels/sec
    public static final float MAX_ARROW_LENGTH = 150f;        // Max display length of arrow body in pixels
    public static final float MIN_VISIBLE_ARROW_BODY_LENGTH = 8f; // Min display length for non-zero arrows

    public static final float ARROW_SCALE_VELOCITY = 0.5f;
    public static final float ARROW_SCALE_ACCELERATION = 0.8f;
    public static final float ARROW_HEAD_SIZE = 12f;
    public static final float ARROW_Y_OFFSET = -55f; // Vertical offset for drawing arrows relative to ball center

    // ## Instance Configuration ##
    private boolean isUserControlled = false; // If true, touch directly controls acceleration.
    private boolean applyFriction = false;    // If true, friction is applied to the ball's motion.
    private float currentFrictionCoefficient = 0.0f; // Current friction coefficient.
    private float currentMassKg = DEFAULT_MASS_KG;   // Current mass of the ball in kg.
    private boolean isHorizontalOnly = true; // If true, ball movement is restricted to horizontal.

    // ## State Variables ##
    private GameThread gameThread; // Manages the game loop.
    private Paint ballPaint, arrowPaintVelocity, arrowPaintAcceleration, textPaint; // Paints for drawing.
    private float ballX, ballY;         // Current position of the ball.
    private float velocityX = 0f;       // Current horizontal velocity of the ball.
    private float appliedAccelerationX = 0f; // Acceleration directly applied by user or initial impulse.

    private float initialX; // Initial X position of the ball, for checking if it has moved.
    private boolean ballHasMovedFlag = false; // True if the ball has moved beyond a threshold.
    private static final float MOVEMENT_THRESHOLD = 10f; // Min distance to be considered "moved".

    private int viewWidth, viewHeight; // Dimensions of the SurfaceView.
    private boolean surfaceReady = false; // True if the drawing surface is ready.
    private boolean touchIsActive = false; // True if a touch event is currently active.
    private float currentTouchX; // X-coordinate of the current touch event.

    // Constructor. Inputs: context (Context).
    public BallGameSurfaceView(Context context) {
        super(context);
        init();
    }

    // Constructor. Inputs: context (Context), attrs (AttributeSet).
    public BallGameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // Constructor. Inputs: context (Context), attrs (AttributeSet), defStyleAttr (int).
    public BallGameSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // Initializes paints and sets up SurfaceHolder callbacks. Inputs: none.
    private void init() {
        getHolder().addCallback(this);
        setFocusable(true);

        ballPaint = new Paint();
        ballPaint.setColor(Color.BLUE);
        ballPaint.setStyle(Paint.Style.FILL);
        ballPaint.setAntiAlias(true);

        arrowPaintVelocity = new Paint();
        arrowPaintVelocity.setColor(Color.GREEN);
        arrowPaintVelocity.setStrokeWidth(6f);
        arrowPaintVelocity.setStyle(Paint.Style.STROKE);
        arrowPaintVelocity.setAntiAlias(true);

        arrowPaintAcceleration = new Paint();
        arrowPaintAcceleration.setColor(Color.MAGENTA);
        arrowPaintAcceleration.setStrokeWidth(6f);
        arrowPaintAcceleration.setStyle(Paint.Style.STROKE);
        arrowPaintAcceleration.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(22f);
        textPaint.setAntiAlias(true);
    }

    // Configures the behavior of this ball simulation instance. Inputs: isUserControlled, applyFriction, mass, frictionCoefficient, horizontalOnly.
    public void configure(boolean isUserControlled, boolean applyFriction, float mass, float frictionCoefficient, boolean horizontalOnly) {
        this.isUserControlled = isUserControlled;
        this.applyFriction = applyFriction;
        this.currentMassKg = (mass > 0) ? mass : DEFAULT_MASS_KG;
        this.currentFrictionCoefficient = frictionCoefficient;
        this.isHorizontalOnly = horizontalOnly;

        Log.d(TAG, "Configured instance " + getId() + ": UserCtrl=" + this.isUserControlled +
                ", Friction=" + this.applyFriction + ", Mass=" + this.currentMassKg + ", Coeff=" + this.currentFrictionCoefficient);
        resetBallState();
    }

    // Resets the ball's position, velocity, and movement flags. Inputs: none.
    private void resetBallState() {
        if (viewWidth > 0 && viewHeight > 0) {
            ballX = viewWidth / 2f;
            ballY = viewHeight * (isHorizontalOnly ? 0.75f : 0.5f); // Position lower if horizontal only for better arrow visibility

            initialX = ballX;
            velocityX = 0f;
            appliedAccelerationX = 0f;
            ballHasMovedFlag = false;
            touchIsActive = false;
        } else {
            // Default position if view dimensions are not yet known
            ballX = DEFAULT_BALL_RADIUS * 2;
            ballY = DEFAULT_BALL_RADIUS * 2;
            initialX = ballX;
        }
    }

    // Checks if the ball has moved significantly from its initial position. Inputs: none.
    public boolean hasBallMoved() {
        return ballHasMovedFlag;
    }

    // Resumes the game thread if the surface is ready. Inputs: none.
    public void resume() {
        if (surfaceReady && (gameThread == null || !gameThread.isRunning())) {
            gameThread = new GameThread(getHolder(), this);
            gameThread.setRunning(true);
            gameThread.start();
        }
    }

    // Pauses the game thread and waits for it to finish. Inputs: none.
    public void pause() {
        if (gameThread != null) {
            gameThread.setRunning(false);
            boolean retry = true;
            while (retry) {
                try {
                    gameThread.join(100); // Wait up to 100ms for thread to die
                    retry = false;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Preserve interrupt status
                }
            }
        }
        gameThread = null;
    }

    // Called when the surface is first created; sets up dimensions and resumes game. Inputs: holder (SurfaceHolder).
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        surfaceReady = true;
        if (viewWidth == 0 && viewHeight == 0 && getWidth() > 0 && getHeight() > 0) { // Initialize dimensions if not already set
            viewWidth = getWidth();
            viewHeight = getHeight();
        }
        resetBallState();
        resume();
    }

    // Called when the surface changes format or size; updates dimensions. Inputs: holder, format, width, height.
    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        viewWidth = width;
        viewHeight = height;
        resetBallState(); // Reset ball state with new dimensions
    }

    // Called when the surface is destroyed; pauses the game. Inputs: holder (SurfaceHolder).
    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        surfaceReady = false;
        pause();
    }

    // Updates the game state (ball position, velocity, etc.) based on elapsed time. Inputs: deltaTimeMs (long).
    protected void update(long deltaTimeMs) {
        if (viewWidth == 0 || viewHeight == 0 || !surfaceReady) return; // Do nothing if view not ready

        float dt = deltaTimeMs / 1000.0f; // Convert delta time to seconds
        // Clamp dt to avoid extreme physics with variable frame rates
        dt = Math.max(dt, 1f / 120f); // Min dt (e.g., for 120 FPS)
        dt = Math.min(dt, 1f / 15f);  // Max dt (e.g., for 15 FPS)


        float netAccelerationX = this.appliedAccelerationX;

        // Apply friction if enabled and ball is moving
        if (this.applyFriction && Math.abs(this.velocityX) > 0.01f) { // Only apply friction if there's noticeable velocity
            float frictionForceMagnitude = this.currentFrictionCoefficient * this.currentMassKg * GRAVITY_ACCELERATION;
            float frictionAccelerationComponent = frictionForceMagnitude / this.currentMassKg; // a_friction = F_friction / m

            // Friction opposes motion
            if (this.velocityX > 0) {
                netAccelerationX -= frictionAccelerationComponent;
            } else {
                netAccelerationX += frictionAccelerationComponent;
            }
        }

        float oldVelocityX = this.velocityX;
        this.velocityX += netAccelerationX * dt; // v = v0 + a*t

        // Cap velocity to MAX_HORIZONTAL_VELOCITY
        this.velocityX = Math.max(-MAX_HORIZONTAL_VELOCITY, Math.min(this.velocityX, MAX_HORIZONTAL_VELOCITY));

        // Friction stop logic: if velocity crosses zero due to friction, or is very small
        if (this.applyFriction) {
            boolean stoppedByFrictionLogic = false;
            // Check if velocity sign changed due to friction (and not due to opposing applied acceleration)
            if ((oldVelocityX > 0 && this.velocityX <= 0) || (oldVelocityX < 0 && this.velocityX >= 0)) {
                // If applied acceleration was in the same direction as old velocity, or zero, then friction stopped it
                if (Math.signum(this.appliedAccelerationX) == 0 || Math.signum(this.appliedAccelerationX) == Math.signum(oldVelocityX)) {
                    // If applied acceleration is less than the friction component, friction can stop it
                    if(Math.abs(this.appliedAccelerationX) < Math.abs(netAccelerationX - this.appliedAccelerationX) || this.appliedAccelerationX == 0) {
                        this.velocityX = 0;
                        stoppedByFrictionLogic = true;
                    }
                }
            }
            // If velocity is very small and no active touch, consider it stopped
            if (Math.abs(this.velocityX) < 0.5f && !this.touchIsActive) {
                this.velocityX = 0;
                stoppedByFrictionLogic = true;
            }
            // If friction stopped it and user isn't touching, clear any residual applied acceleration
            if(stoppedByFrictionLogic && !this.touchIsActive){
                this.appliedAccelerationX = 0;
            }
        }


        // If ball has stopped and no touch is active, ensure applied acceleration is also zero (unless user-controlled)
        if (this.velocityX == 0 && !this.touchIsActive) {
            if(!isUserControlled) this.appliedAccelerationX = 0; // For non-user-controlled, stop applying force
        }


        this.ballX += this.velocityX * dt; // x = x0 + v*t (using updated velocity for this frame)

        // Wall collision - stop at wall
        if (ballX - DEFAULT_BALL_RADIUS <= 0) {
            ballX = DEFAULT_BALL_RADIUS;
            velocityX = 0;
            appliedAccelerationX = 0; // Stop any applied force on collision
        } else if (ballX + DEFAULT_BALL_RADIUS >= viewWidth) {
            ballX = viewWidth - DEFAULT_BALL_RADIUS;
            velocityX = 0;
            appliedAccelerationX = 0; // Stop any applied force on collision
        }

        // Update ballHasMovedFlag
        if (!ballHasMovedFlag) {
            if (Math.abs(ballX - initialX) > MOVEMENT_THRESHOLD) {
                ballHasMovedFlag = true;
            }
        }
    }

    // Renders the current game frame (ball, arrows, text) onto the canvas. Inputs: canvas (Canvas).
    public void renderFrame(Canvas canvas) {
        if (canvas == null) return;
        canvas.drawColor(Color.WHITE); // Clear screen

        if (viewWidth > 0 && viewHeight > 0) { // Ensure dimensions are valid
            // Draw ball
            canvas.drawCircle(ballX, ballY, DEFAULT_BALL_RADIUS, ballPaint);

            // Arrow drawing common setup
            float arrowStartY = ballY + ARROW_Y_OFFSET - DEFAULT_BALL_RADIUS; // Position arrows above the ball
            float arrowLineBaseX = ballX; // Arrows originate from ball's X center

            // Draw Velocity Arrow
            float rawVelocityArrowLength = velocityX * ARROW_SCALE_VELOCITY;
            float displayVelocityArrowLength = Math.max(-MAX_ARROW_LENGTH, Math.min(rawVelocityArrowLength, MAX_ARROW_LENGTH)); // Clamp arrow length
            if (Math.abs(velocityX) > 0.1f) { // Only draw if velocity is significant
                // Ensure a minimum visible length for non-zero arrows
                if (Math.abs(displayVelocityArrowLength) < MIN_VISIBLE_ARROW_BODY_LENGTH && displayVelocityArrowLength != 0) {
                    displayVelocityArrowLength = Math.signum(displayVelocityArrowLength) * MIN_VISIBLE_ARROW_BODY_LENGTH;
                }
                drawArrow(canvas, arrowLineBaseX, arrowStartY - 30, arrowLineBaseX + displayVelocityArrowLength, arrowStartY - 30, arrowPaintVelocity);
                canvas.drawText(String.format("V: %.1f", velocityX), arrowLineBaseX + displayVelocityArrowLength + (displayVelocityArrowLength < 0 ? -90 : 10), arrowStartY - 25, textPaint);
            }

            // Calculate net acceleration for display (including friction if applicable)
            float netAccelerationXDisplay = this.appliedAccelerationX;
            if (applyFriction && Math.abs(velocityX) > 0.01f) { // Consider friction if ball is moving
                float frictionComponent = currentFrictionCoefficient * GRAVITY_ACCELERATION; // Simplified: a_friction = mu*g
                if (velocityX > 0) netAccelerationXDisplay -= frictionComponent;
                else netAccelerationXDisplay += frictionComponent;
            }
            // If ball is stopped by friction (no applied force and very low velocity), display net accel as 0
            if (Math.abs(velocityX) < 0.1f && this.appliedAccelerationX == 0 && applyFriction) {
                netAccelerationXDisplay = 0;
            }


            // Draw Acceleration Arrow
            float rawAccelArrowLength = netAccelerationXDisplay * ARROW_SCALE_ACCELERATION;
            float displayAccelArrowLength = Math.max(-MAX_ARROW_LENGTH, Math.min(rawAccelArrowLength, MAX_ARROW_LENGTH)); // Clamp arrow length

            if (Math.abs(netAccelerationXDisplay) > 0.1f) { // Only draw if acceleration is significant
                // Ensure a minimum visible length for non-zero arrows
                if (Math.abs(displayAccelArrowLength) < MIN_VISIBLE_ARROW_BODY_LENGTH && displayAccelArrowLength != 0) {
                    displayAccelArrowLength = Math.signum(displayAccelArrowLength) * MIN_VISIBLE_ARROW_BODY_LENGTH;
                }
                drawArrow(canvas, arrowLineBaseX, arrowStartY, arrowLineBaseX + displayAccelArrowLength, arrowStartY, arrowPaintAcceleration);
                canvas.drawText(String.format("A: %.1f", netAccelerationXDisplay), arrowLineBaseX + displayAccelArrowLength + (displayAccelArrowLength < 0 ? -90 : 10), arrowStartY + 5, textPaint);
            }
        }
    }

    // Default onDraw, typically not used directly when using SurfaceView with a game loop. Inputs: canvas (Canvas).
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Game rendering is handled by renderFrame() called from the GameThread
    }

    // Helper method to draw an arrow on the canvas. Inputs: canvas, startX, startY, endX, endY, paint.
    private void drawArrow(Canvas canvas, float startX, float startY, float endX, float endY, Paint paint) {
        float bodyLength = Math.abs(endX - startX);
        if (bodyLength < 0.1f) return; // Don't draw if too short

        // Ensure a minimum body length for visibility if it's supposed to be drawn
        if (bodyLength < MIN_VISIBLE_ARROW_BODY_LENGTH) {
            endX = startX + Math.signum(endX - startX) * MIN_VISIBLE_ARROW_BODY_LENGTH;
        }

        canvas.drawLine(startX, startY, endX, endY, paint); // Draw arrow body
        float dx = endX - startX;

        if (dx == 0) return; // Avoid division by zero or issues with vertical arrows (though this is horizontal)

        // Draw arrowhead
        if (dx > 0) { // Pointing right
            canvas.drawLine(endX, endY, endX - ARROW_HEAD_SIZE, endY - ARROW_HEAD_SIZE / 2f, paint);
            canvas.drawLine(endX, endY, endX - ARROW_HEAD_SIZE, endY + ARROW_HEAD_SIZE / 2f, paint);
        } else { // Pointing left
            canvas.drawLine(endX, endY, endX + ARROW_HEAD_SIZE, endY - ARROW_HEAD_SIZE / 2f, paint);
            canvas.drawLine(endX, endY, endX + ARROW_HEAD_SIZE, endY + ARROW_HEAD_SIZE / 2f, paint);
        }
    }

    // Handles touch events to control the ball's acceleration. Inputs: event (MotionEvent).
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!surfaceReady) return false; // Ignore touch if surface not ready
        currentTouchX = event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                touchIsActive = true;
                // Apply acceleration based on touch position relative to the ball
                // This logic applies to both balls if they are configured to respond to touch for appliedAccelerationX
                if (currentTouchX < ballX - DEFAULT_BALL_RADIUS / 2) { // Touch left of ball
                    this.appliedAccelerationX = -USER_CONTROL_ACCELERATION_MAGNITUDE;
                } else if (currentTouchX > ballX + DEFAULT_BALL_RADIUS / 2) { // Touch right of ball
                    this.appliedAccelerationX = USER_CONTROL_ACCELERATION_MAGNITUDE;
                } else {
                    this.appliedAccelerationX = 0; // Touch on/near ball center, no acceleration
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { // Log only on initial press
                    Log.d(TAG, "Instance " + getId() + " : Touch Down/Move. Applied AccelX = " + this.appliedAccelerationX);
                }
                return true; // Event handled

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touchIsActive = false;
                this.appliedAccelerationX = 0; // Stop applying acceleration on touch release
                Log.d(TAG, "Instance " + getId() + ": Touch Up. Applied AccelX = 0");
                return true; // Event handled
        }
        return super.onTouchEvent(event); // Default handling for other event types
    }

    // Inner class for the game loop thread.
    public static class GameThread extends Thread {
        private final SurfaceHolder surfaceHolder;
        private final BallGameSurfaceView gameView;
        private volatile boolean running = false; // 'volatile' ensures visibility across threads
        private static final int TARGET_FPS = 60; // Target frames per second
        private static final long OPTIMAL_TIME_MS = 1000 / TARGET_FPS; // Optimal time per frame in ms

        // Constructor. Inputs: surfaceHolder (SurfaceHolder), gameView (BallGameSurfaceView).
        public GameThread(SurfaceHolder surfaceHolder, BallGameSurfaceView gameView) {
            super("GameThread-" + gameView.getId()); // Name the thread for easier debugging
            this.surfaceHolder = surfaceHolder;
            this.gameView = gameView;
        }

        // Sets the running state of the game loop. Inputs: isRunning (boolean).
        public void setRunning(boolean isRunning) {
            this.running = isRunning;
        }

        // Returns the current running state of the game loop. Inputs: none.
        public boolean isRunning() {
            return running;
        }

        // Main game loop; updates and renders frames at a target FPS. Inputs: none.
        @Override
        public void run() {
            long lastLoopTime = SystemClock.uptimeMillis();
            while (running) {
                long now = SystemClock.uptimeMillis();
                long deltaTime = now - lastLoopTime;
                lastLoopTime = now;

                if (deltaTime <= 0) deltaTime = 1; // Ensure positive delta time

                Canvas canvas = null;
                // Ensure surface is valid and ready before trying to lock canvas
                if (!surfaceHolder.getSurface().isValid() || !gameView.surfaceReady) {
                    try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); this.running = false;} // Sleep briefly and re-check
                    continue;
                }
                try {
                    canvas = surfaceHolder.lockCanvas(); // Try to lock the canvas for drawing
                    synchronized (surfaceHolder) { // Synchronize on holder to prevent race conditions
                        if (canvas != null) {
                            gameView.update(deltaTime); // Update game state
                            gameView.renderFrame(canvas); // Render the frame
                        }
                    }
                } catch (Exception e) { // Catch generic exceptions during lock/draw
                    Log.e(TAG, "Exception in game loop for " + getName(), e);
                } finally {
                    if (canvas != null) {
                        try {
                            surfaceHolder.unlockCanvasAndPost(canvas); // Always try to unlock
                        } catch (Exception e) { // Catch exceptions during unlock
                            Log.e(TAG, "Exception unlocking canvas for " + getName(), e);
                        }
                    }
                }

                // Frame rate regulation
                long loopTimeTaken = SystemClock.uptimeMillis() - now;
                if (loopTimeTaken < OPTIMAL_TIME_MS) {
                    try {
                        Thread.sleep(OPTIMAL_TIME_MS - loopTimeTaken); // Sleep to maintain target FPS
                    } catch (InterruptedException e) {
                        running = false; // Stop loop if interrupted
                        Thread.currentThread().interrupt(); // Preserve interrupt status
                    }
                }
            }
        }
    }
}