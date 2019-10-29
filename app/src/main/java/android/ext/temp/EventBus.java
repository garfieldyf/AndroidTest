package android.ext.temp;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import android.ext.util.Pools;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.Pool;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

/**
 * Class EventBus
 * @author Garfield
 */
public final class EventBus {
    /**
     * Each event has a thread mode, which determines in which thread the event method
     * is to be called by {@link EventBus}.
     */
    public static enum ThreadMode {
        /**
         * Events will be called in the same thread, which is posting the event.
         * This is the default thread mode, it avoids thread switching completely.
         */
        POST {
            @Override
            protected EventHandler newEventHandler() {
                return postHandler;
            }
        },

        /**
         * Events will be called in Android's main thread (UI thread). If the posting
         * thread is the main thread and delay time is <tt>0</tt>, event methods will
         * be called directly. Events using this mode must return quickly to avoid
         * blocking the main thread.
         */
        MAIN {
            @Override
            protected EventHandler newEventHandler() {
                return mainHandler;
            }
        },

        /**
         * Events will be called in a background thread. This is always independent
         * from the posting thread and the main thread. Posting events never wait for
         * event methods using this mode.
         */
        ASYNC {
            @Override
            protected EventHandler newEventHandler() {
                return asyncHandler;
            }
        },

        /**
         * Events will be called in a background thread. This is always independent
         * from the posting thread and the main thread. EventBus uses <b>a single</b>
         * background thread, that will deliver all its events sequentially. Unlike
         * {@link #SEQUENCE} mode, the same event can not be posting at the same time.
         */
        SINGLE {
            @Override
            protected EventHandler newEventHandler() {
                return new SingleHandler();
            }
        },

        /**
         * Events will be called in a background thread. If posting thread is not
         * the main thread or delay time is <tt>0</tt>, event methods will be called
         * directly in the posting thread. If the posting thread is the main thread
         * or delay time is not <tt>0</tt>, EventBus uses <b>a single</b> background
         * thread, that will deliver all its events sequentially.
         */
        HANDLER {
            @Override
            protected EventHandler newEventHandler() {
                return new ThreadHandler();
            }
        },

        /**
         * Events will be called in a background thread. This is always independent
         * from the posting thread and the main thread. Unlike {@link #ASYNC} mode,
         * EventBus uses <b>a single</b> background thread, that will deliver all its
         * events sequentially.
         */
        SEQUENCE {
            @Override
            protected EventHandler newEventHandler() {
                return new SequenceHandler();
            }
        },

        /**
         * Events will be called in a background thread. If posting thread is not the
         * main thread, event methods will be called directly in the posting thread.
         * If the posting thread is the main thread, EventBus uses <b>a single</b>
         * background thread, that will deliver all its events sequentially.
         */
        BACKGROUND {
            @Override
            protected EventHandler newEventHandler() {
                return new BackgoundHandler();
            }
        };

        /**
         * Returns a new instance of {@link EventHandler}.
         * @return A new instance of {@link EventHandler}.
         */
        protected abstract EventHandler newEventHandler();
    }

    /**
     * The event handler state of idle.
     */
    private static final int IDLE = 0;

    /**
     * The event handler state of running.
     */
    private static final int RUNNING = 1;

    /**
     * The work thread's name suffix.
     */
    private static final AtomicInteger nameSuffix = new AtomicInteger();

    /**
     * The token sequence used to generate the token.
     */
    private static final AtomicInteger tokenSequence = new AtomicInteger(Integer.MIN_VALUE);

    /**
     * The {@link ThreadMode#POST} thread mode event handler.
     */
    /* package */ static final EventHandler postHandler = new PostHandler();

    /**
     * The {@link ThreadMode#MAIN} thread mode event handler.
     */
    /* package */ static final MainHandler mainHandler = new MainHandler();

    /**
     * The {@link ThreadMode#ASYNC} thread mode event handler.
     */
    /* package */ static final EventHandler asyncHandler = new AsyncHandler();

    /**
     * The map tokens to {@link Subscriber}s.
     */
    private final SparseArray<Subscriber> mSubscribers = new SparseArray<Subscriber>();

    /**
     * The {@link EventHandler} array.
     */
    private final EventHandler[] mEventHandlers = new EventHandler[ThreadMode.values().length];

    /**
     * The thread pool to execute asynchronous pending events. Calls to <em>execute</em>
     * will reuse previously constructed threads if available. If no existing thread is
     * available, a new thread will be created and added to the pool. Threads that have
     * not been used for <em>60</em> seconds are terminated and removed from the cache.
     */
    /* package */ static final Executor executor = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, threadName());
        }
    });

    /**
     * Registers the event subscriber with the default event method name "onEvent". The
     * subscriber must call {@link #unregister(int)} once they are no longer interested
     * in receiving events. <p>Note that the event handle method prototype:
     * <em>modifier void <b>onEvent</b>(int token, Object[] args)</em></p>
     * @param subscriber The event subscriber to register.
     * @return A unique token use to {@link #post} and {@link #postDelayed}.
     * @see #register(Object, String)
     * @see #register(int, Object)
     * @see #register(int, Object, String)
     * @see #unregister(int)
     * @see #unregisterAll()
     */
    public int register(Object subscriber) {
        final int token = generateToken();
        register(token, subscriber, "onEvent");
        return token;
    }

    /**
     * Like as {@link #register(Object)}, but allows to define a custom method name for
     * event handler methods. <p>Note that the event handle method prototype:
     * <em>modifier void <b>eventMethodName</b>(int token, Object[] args)</em></p>
     * @param subscriber The event subscriber to register.
     * @param methodName The event method name to register.
     * @return A unique token use to {@link #post} and {@link #postDelayed}.
     * @see #register(Object)
     * @see #register(int, Object)
     * @see #register(int, Object, String)
     * @see #unregister(int)
     * @see #unregisterAll()
     */
    public int register(Object subscriber, String methodName) {
        final int token = generateToken();
        register(token, subscriber, methodName);
        return token;
    }

    /**
     * Registers the event subscriber with the specified <tt>token</tt> and default
     * event method name "onEvent". The token must <b>not</b> already be registered.
     * The subscriber must call {@link #unregister(int)} once they are no longer
     * interested in receiving events. <p>Note that the event handle method prototype:
     * <em>modifier void <b>onEvent</b>(int token, Object[] args)</em></p>
     * @param token The token to register. generates by {@link #generateToken()}.
     * @param subscriber The event subscriber to register.
     * @see #register(Object)
     * @see #register(Object, String)
     * @see #register(int, Object, String)
     * @see #unregister(int)
     * @see #unregisterAll()
     */
    public void register(int token, Object subscriber) {
        register(token, subscriber, "onEvent");
    }

    /**
     * Like as {@link #register(int, Object)}, but allows to define a custom method
     * name for event handler methods. <p>Note that the event handle method prototype:
     * <em>modifier void <b>eventMethodName</b>(int token, Object[] args)</em></p>
     * @param token The token to register. generates by {@link #generateToken()}.
     * @param subscriber The event subscriber to register.
     * @param methodName The event method name to register.
     * @see #register(Object)
     * @see #register(Object, String)
     * @see #register(int, Object)
     * @see #unregister(int)
     * @see #unregisterAll()
     */
    public void register(int token, Object subscriber, String methodName) {
        synchronized (mSubscribers) {
            if (mSubscribers.get(token, null) != null) {
                throw new IllegalStateException(new StringBuilder("The token is already registered. [ token = ").append(token).append(", subscriber = ").append(subscriber).append(", method = ").append(methodName).append(" ]").toString());
            }

            mSubscribers.append(token, new Subscriber(subscriber, methodName));
        }
    }

    /**
     * Unregisters a previously registered event
     * subscriber with the specified <tt>token</tt>.
     * @param token The token to unregister.
     * @see #register(Object)
     * @see #register(Object, String)
     * @see #register(int, Object)
     * @see #register(int, Object, String)
     * @see #unregisterAll()
     */
    public void unregister(int token) {
        synchronized (mSubscribers) {
            mSubscribers.delete(token);
        }
    }

    /**
     * Unregisters all registered event subscribers.
     * @see #register(Object)
     * @see #register(Object, String)
     * @see #register(int, Object)
     * @see #register(int, Object, String)
     * @see #unregister(int)
     */
    public void unregisterAll() {
        synchronized (mSubscribers) {
            mSubscribers.clear();
        }
    }

    /**
     * Posts an event to this <tt>EventBus</tt> with the specified thread mode.
     * @param token A token to identify the event.
     * @param mode The {@link ThreadMode} that the event will be posted.
     * @param args The event method arguments. If the event method no arguments,
     * you can pass <em>(Object[])null</em> instead of allocating an empty array.
     * @see #postDelayed(int, ThreadMode, long, Object[])
     */
    public void post(int token, ThreadMode mode, Object... args) {
        postDelayed(token, mode, 0, args);
    }

    /**
     * Posts an event to this <tt>EventBus</tt> with the specified thread mode and delay time.
     * @param token A token to identify the event.
     * @param mode The {@link ThreadMode} that the event will be posted.
     * @param delayMillis The delay (in milliseconds) until the event method will be invoked.
     * This parameter can only be used {@link ThreadMode#MAIN} and {@link ThreadMode#HANDLER}
     * thread mode.
     * @param args The event method arguments. If the event method no arguments, you can pass
     * <em>(Object[])null</em> instead of allocating an empty array.
     * @see #post(int, ThreadMode, Object[])
     */
    public void postDelayed(int token, ThreadMode mode, long delayMillis, Object... args) {
        final Subscriber subscriber = getSubscriber(token);
        if (subscriber != null) {
            // Dispatch the event.
            EventHandler handler;
            synchronized (mEventHandlers) {
                final int index = mode.ordinal();
                if ((handler = mEventHandlers[index]) == null) {
                    mEventHandlers[index] = handler = mode.newEventHandler();
                }
            }

            handler.handleEvent(token, subscriber, args, delayMillis);
        }
    }

    /**
     * Generates the unique token use to {@link EventBus#register},
     * {@link EventBus#post} and {@link EventBus#postDelayed}.
     * @return The unique token.
     */
    public static int generateToken() {
        return tokenSequence.incrementAndGet();
    }

    /**
     * Returns the {@link Handler} associated
     * with the UI thread's message queue.
     * @return The <tt>Handler</tt>.
     */
    public static Handler getHandler() {
        return mainHandler;
    }

    /**
     * Returns the {@link Executor} associated with <tt>EventBus</tt>.
     * @return The <tt>Executor</tt>.
     */
    public static Executor getExecutor() {
        return executor;
    }

    /**
     * Runs the supplied <em>runnable</em> on the main thread. The
     * method will block only if the current thread is the main thread.
     * @param runnable The <tt>Runnable</tt> to run.
     * @see #runOnUIThreadSync(Callable)
     * @see #runOnUIThreadSync(Runnable)
     */
    public static void runOnUIThread(Runnable runnable) {
        mainHandler.runOnUIThread(runnable);
    }

    /**
     * Runs the supplied <em>runnable</em> on the main thread.
     * The method will block until the Runnable completes.
     * @param runnable The <tt>Runnable</tt> to run.
     * @see #runOnUIThread(Runnable)
     * @see #runOnUIThreadSync(Callable)
     */
    public static void runOnUIThreadSync(Runnable runnable) {
        try {
            mainHandler.runOnUIThreadSync(runnable);
        } catch (Exception e) {
            Log.e(EventBus.class.getName(), "Couldn't run - " + runnable, e);
        }
    }

    /**
     * Runs the supplied <em>callable</em> on the main thread.
     * The method will block until the Callable completes.
     * @param callable The <tt>Callable</tt> to run.
     * @return The <tt>Callable</tt> returned result if succeeded,
     * <tt>null</tt> otherwise.
     * @see #runOnUIThread(Runnable)
     * @see #runOnUIThreadSync(Runnable)
     */
    public static <T> T runOnUIThreadSync(Callable<T> callable) {
        try {
            return mainHandler.runOnUIThreadSync(callable);
        } catch (Exception e) {
            Log.e(EventBus.class.getName(), "Couldn't run - " + callable, e);
            return null;
        }
    }

    private Subscriber getSubscriber(int token) {
        synchronized (mSubscribers) {
            Subscriber subscriber = mSubscribers.get(token, null);
            if (subscriber != null && subscriber.objectRef.get() == null) {
                // Null value means that the subscriber.objectRef
                // was released by the GC and we need to delete it.
                Log.w(EventBus.class.getName(), "The subscriber was released by the GC.");
                subscriber = null;
                mSubscribers.delete(token);
            }

            return subscriber;
        }
    }

    /* package */ static String threadName() {
        return "EventBus-thread-" + nameSuffix.incrementAndGet();
    }

    /**
     * Nested interface EventHandler
     */
    private static interface EventHandler {
        /**
         * Handles the event with the specified parameters.
         * @param token The token, passed earlier by {@link EventBus#post} or
         * {@link EventBus#postDelayed}.
         * @param subscriber The {@link Subscriber} whose event method will be invoked.
         * @param args The event method arguments, passed earlier by {@link EventBus#post}
         * or {@link EventBus#postDelayed}.
         * @param delayMillis The delay (in milliseconds) until the event method will
         * be invoked. This parameter can only be used {@link ThreadMode#MAIN} and
         * {@link ThreadMode#HANDLER} thread mode.
         */
        void handleEvent(int token, Subscriber subscriber, Object[] args, long delayMillis);
    }

    /**
     * Nested class Subscriber
     */
    private static final class Subscriber {
        /* package */ final Method method;
        /* package */ final WeakReference<Object> objectRef;

        /**
         * Constructor
         * @param object The event subscriber.
         * @param methodName The event method name.
         */
        public Subscriber(Object object, String methodName) {
            objectRef = new WeakReference<Object>(object);
            if ((method = getDeclaredMethod(object.getClass(), methodName)) == null) {
                throw new IllegalArgumentException(new StringBuilder("Couldn't find method. [ subscriber = ").append(object).append(", method = ").append(methodName).append(" ]").toString());
            }

            method.setAccessible(true);
        }

        /**
         * Invokes the event method with the specified parameters.
         * @param args The arguments to the event method.
         */
        public void invoke(Object... args) {
            final Object object = objectRef.get();
            if (object != null) {
                try {
                    method.invoke(object, args);
                } catch (Exception e) {
                    throw new RuntimeException(new StringBuilder("Couldn't invoke method. [ subscriber = ").append(object).append(", method = ").append(method).append(" ]").toString(), e);
                }
            }
        }

        @Override
        public int hashCode() {
            final Object object = objectRef.get();
            return (object != null ? object.hashCode() : 0);
        }

        @Override
        public String toString() {
            final Object object = objectRef.get();
            return (object != null ? object.toString() : "null");
        }

        private static Method getDeclaredMethod(Class<?> clazz, String methodName) {
            for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
                try {
                    return clazz.getDeclaredMethod(methodName, int.class, Object[].class);
                } catch (NoSuchMethodException e) {
                }
            }

            return null;
        }
    }

    /**
     * Nested class PostHandler
     */
    /* package */ static final class PostHandler implements EventHandler {
        @Override
        public void handleEvent(int token, Subscriber subscriber, Object[] args, long delayMillis) {
            subscriber.invoke(token, args);
        }
    }

    /**
     * Nested class MainHandler
     */
    private static final class MainHandler extends Handler implements EventHandler {
        public MainHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleEvent(int token, Subscriber subscriber, Object[] args, long delayMillis) {
            if (delayMillis <= 0 && getLooper() == Looper.myLooper()) {
                // If delayMillis <= 0 and Posting thread is
                // main thread, call event method directly.
                subscriber.invoke(token, args);
            } else {
                // Posts the event to the message queue.
                postDelayed(PendingEvent.obtain(token, subscriber, args), delayMillis);
            }
        }

        /**
         * Runs the supplied <em>runnable</em> on the main thread. The
         * method will block only if the current thread is the main thread.
         * @param runnable The <tt>Runnable</tt> to run.
         */
        public void runOnUIThread(Runnable runnable) {
            if (getLooper() == Looper.myLooper()) {
                runnable.run();
            } else {
                post(runnable);
            }
        }

        /**
         * Runs the supplied <em>runnable</em> on the main thread.
         * The method will block until the Runnable completes.
         * @param runnable The <tt>Runnable</tt> to run.
         * @throws Exception if an error occurs while running to
         * <em>runnable</em>.
         */
        public void runOnUIThreadSync(Runnable runnable) throws Exception {
            if (getLooper() == Looper.myLooper()) {
                runnable.run();
            } else {
                runTask(new FutureTask<Object>(runnable, null));
            }
        }

        /**
         * Runs the supplied <em>callable</em> on the main thread.
         * The method will block until the Callable completes.
         * @param callable The <tt>Callable</tt> to run.
         * @return The <tt>Callable</tt> returned result.
         * @throws Exception if an error occurs while running to
         * <em>callable</em>.
         */
        public <T> T runOnUIThreadSync(Callable<T> callable) throws Exception {
            return (getLooper() == Looper.myLooper() ? callable.call() : runTask(new FutureTask<T>(callable)));
        }

        private <T> T runTask(FutureTask<T> task) throws Exception {
            post(task);
            return task.get();
        }
    }

    /**
     * Nested class AsyncHandler
     */
    /* package */ static final class AsyncHandler implements EventHandler {
        @Override
        public void handleEvent(int token, Subscriber subscriber, Object[] args, long delayMillis) {
            executor.execute(PendingEvent.obtain(token, subscriber, args));
        }
    }

    /**
     * Nested class SequenceHandler
     */
    private static class SequenceHandler implements EventHandler, Runnable {
        protected final AtomicInteger mState;
        protected final Queue<PendingEvent> mQueue;

        public SequenceHandler() {
            mState = new AtomicInteger(IDLE);
            mQueue = new ConcurrentLinkedQueue<PendingEvent>();
        }

        @Override
        public void handleEvent(int token, Subscriber subscriber, Object[] args, long delayMillis) {
            postEvent(PendingEvent.obtain(token, subscriber, args));
        }

        @Override
        public void run() {
            try {
                PendingEvent event;
                while ((event = mQueue.poll()) != null) {
                    event.run();
                }
            } finally {
                mState.set(IDLE);
            }
        }

        protected final void postEvent(PendingEvent event) {
            mQueue.offer(event);
            if (mState.compareAndSet(IDLE, RUNNING)) {
                // If executor is not running, run it.
                executor.execute(this);
            }
        }
    }

    /**
     * Nested class SingleHandler
     */
    /* package */ static final class SingleHandler extends SequenceHandler {
        @Override
        public void handleEvent(int token, Subscriber subscriber, Object[] args, long delayMillis) {
            final PendingEvent event = PendingEvent.obtain(token, subscriber, args);
            if (mQueue.contains(event)) {
                // If mQueue contains the event, discards it.
                Log.w(EventBus.class.getName(), "The EventBus contains the event - " + event + ", discards it.");
                event.recycle();
            } else {
                postEvent(event);
            }
        }

        @Override
        public void run() {
            try {
                PendingEvent event;
                while ((event = mQueue.peek()) != null) {
                    event.run();
                    mQueue.poll();
                }
            } finally {
                mState.set(IDLE);
            }
        }
    }

    /**
     * Nested class ThreadHandler
     */
    /* package */ static final class ThreadHandler implements EventHandler, Callback {
        private static final int MESSAGE_QUIT  = 0;
        private static final int MESSAGE_EVENT = 1;
        private Handler mHandler;

        @Override
        public void handleEvent(int token, Subscriber subscriber, Object[] args, long delayMillis) {
            if (delayMillis > 0 || Looper.getMainLooper() == Looper.myLooper()) {
                // If delayMillis > 0 or Posting thread is main thread
                // Posts the event to the handler thread message queue.
                postEvent(token, subscriber, args, delayMillis);
            } else {
                // Posting thread is not main thread, call event method
                // directly.
                subscriber.invoke(token, args);
            }
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_QUIT:
                synchronized (this) {
                    if (mHandler != null) {
                        mHandler.getLooper().quit();
                        mHandler = null;
                    }
                }
                break;

            case MESSAGE_EVENT:
                ((PendingEvent)msg.obj).run();
                synchronized (this) {
                    // Sends the delayed quit message, If the thread
                    // is not quit and the message queue is empty.
                    if (mHandler != null && !mHandler.hasMessages(MESSAGE_EVENT)) {
                        mHandler.sendEmptyMessageDelayed(MESSAGE_QUIT, 60000);
                    }
                }
                break;
            }

            return true;
        }

        private void postEvent(int token, Subscriber subscriber, Object[] args, long delayMillis) {
            synchronized (this) {
                if (mHandler == null) {
                    final HandlerThread thread = new HandlerThread(threadName());
                    thread.start();
                    mHandler = new Handler(thread.getLooper(), this);
                }

                // Removes the quit message, then send the event message.
                mHandler.removeMessages(MESSAGE_QUIT);
                mHandler.sendMessageDelayed(Message.obtain(mHandler, MESSAGE_EVENT, PendingEvent.obtain(token, subscriber, args)), delayMillis);
            }
        }
    }

    /**
     * Nested class BackgoundHandler
     */
    /* package */ static final class BackgoundHandler extends SequenceHandler {
        @Override
        public void handleEvent(int token, Subscriber subscriber, Object[] args, long delayMillis) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                // Posts the event to mQueue, if
                // posting thread is main thread.
                postEvent(PendingEvent.obtain(token, subscriber, args));
            } else {
                // Posting thread is not main thread,
                // call event method directly.
                subscriber.invoke(token, args);
            }
        }
    }

    /**
     * Nested class PendingEvent
     */
    private static final class PendingEvent implements Runnable {
        public Subscriber subscriber;
        public final Object[] params;

        public PendingEvent() {
            params = new Object[2];
        }

        /**
         * Retrieves a new <tt>PendingEvent</tt> object from the underlying pool.
         * @param token The token, passed earlier by {@link EventBus#post} or
         * {@link EventBus#postDelayed}.
         * @param subscriber The event subscriber.
         * @param args The event method arguments, passed earlier by {@link EventBus#post}
         * or {@link EventBus#postDelayed}.
         * @return A <tt>PendingEvent</tt> object.
         */
        public static PendingEvent obtain(int token, Subscriber subscriber, Object[] args) {
            final PendingEvent event = POOL.obtain();
            event.params[0]  = token;
            event.params[1]  = args;
            event.subscriber = subscriber;

            return event;
        }

        @Override
        public void run() {
            try {
                subscriber.invoke(params);
            } finally {
                recycle();
            }
        }

        @Override
        public boolean equals(Object other) {
            return (other instanceof PendingEvent && hashCode() == other.hashCode());
        }

        @Override
        public int hashCode() {
            return (subscriber != null ? subscriber.hashCode() + params[0].hashCode() : 0);
        }

        @Override
        public String toString() {
            return (subscriber != null ? new StringBuilder().append("{ token = ").append(params[0]).append(", subscriber = ").append(subscriber).append(" }").toString() : "{ token = null, subscriber = null }");
        }

        public void recycle() {
            params[0]  = null;
            params[1]  = null;
            subscriber = null;
            POOL.recycle(this);
        }

        private static final Pool<PendingEvent> POOL = Pools.synchronizedPool(Pools.newPool(new Factory<PendingEvent>() {
            @Override
            public PendingEvent newInstance() {
                return new PendingEvent();
            }
        }, 8));
    }
}
