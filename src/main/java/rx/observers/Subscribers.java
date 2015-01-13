/**
 * Copyright 2014 Netflix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.observers;

import rx.Observer;
import rx.Subscriber;
import rx.functions.Action;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Actions;

/**
 * Helper methods and utilities for creating and working with {@link Subscriber} objects.
 */
public final class Subscribers {

    private static final Subscriber<Object> EMPTY = from(Observers.empty());

    private Subscribers() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * Returns an inert {@link Subscriber} that does nothing in response to the emissions or notifications from
     * any {@code Observable} it subscribes to. This is different, however, from an {@code EmptyObserver}, in
     * that it will throw an exception if its {@link Subscriber#onError onError} method is called (whereas
     * {@code EmptyObserver} will swallow the error in such a case).
     *
     * @return an inert {@code Observer}
     */
    @SuppressWarnings("unchecked")
    public static <T> Subscriber<T> empty() {
        return (Subscriber<T>)EMPTY;
    }

    /**
     * Converts an {@link Observer} into a {@link Subscriber}.
     *
     * @param o
     *          the {@link Observer} to convert
     * @return a {@link Subscriber} version of {@code o}
     */
    public static <T> Subscriber<T> from(final Observer<? super T> o) {
        return new Subscriber<T>() {

            @Override
            public void onCompleted() {
                try {
                    o.onCompleted();
                }
                finally {
                    unsubscribe();
                }
            }

            @Override
            public void onError(Throwable e) {
                try {
                    o.onError(e);
                }
                finally {
                    unsubscribe();
                }
            }

            @Override
            public void onNext(T t) {
                o.onNext(t);
            }

        };
    }

    /**
     * Creates a {@link Subscriber} that receives the emissions of any {@code Observable} it subscribes to via
     * {@link Subscriber#onNext onNext} but ignores {@link Subscriber#onError onError} and
     * {@link Subscriber#onCompleted onCompleted} notifications.
     *
     * @param onNext
     *          a function that handles each item emitted by an {@code Observable}
     * @throws IllegalArgument Exception
     *          if {@code onNext} is {@code null}
     * @return a {@code Subscriber} that calls {@code onNext} for each emitted item from the {@code Observable}
     *         the {@code Subscriber} subscribes to
     */
    public static final <T> Subscriber<T> create(final Action1<? super T> onNext) {
        return create(onNext, Observers.ON_ERROR_NOT_IMPLEMENTED_ACTION);
    }

    /**
     * Creates an {@link Subscriber} that receives the emissions of any {@code Observable} it subscribes to via
     * {@link Subscriber#onNext onNext} and handles any {@link Subscriber#onError onError} notification but
     * ignores an {@link Subscriber#onCompleted onCompleted} notification.
     * 
     * @param onNext
     *          a function that handles each item emitted by an {@code Observable}
     * @param onError
     *          a function that handles an error notification if one is sent by an {@code Observable}
     * @throws IllegalArgument Exception
     *          if either {@code onNext} or {@code onError} are {@code null}
     * @return an {@code Subscriber} that calls {@code onNext} for each emitted item from the {@code Observable}
     *         the {@code Subscriber} subscribes to, and calls {@code onError} if the {@code Observable}
     *         notifies of an error
     */
    public static final <T> Subscriber<T> create(final Action1<? super T> onNext, final Action1<Throwable> onError) {
        return create(onNext, onError, Actions.empty());
    }

    /**
     * Creates an {@link Subscriber} that receives the emissions of any {@code Observable} it subscribes to via
     * {@link Subscriber#onNext onNext} and handles any {@link Subscriber#onError onError} or
     * {@link Subscriber#onCompleted onCompleted} notifications.
     * 
     * @param onNext
     *          a function that handles each item emitted by an {@code Observable}
     * @param onError
     *          a function that handles an error notification if one is sent by an {@code Observable}
     * @param onComplete
     *          a function that handles a sequence complete notification if one is sent by an {@code Observable}
     * @throws IllegalArgument Exception
     *          if either {@code onNext}, {@code onError}, or {@code onComplete} are {@code null}
     * @return an {@code Subscriber} that calls {@code onNext} for each emitted item from the {@code Observable}
     *         the {@code Subscriber} subscribes to, calls {@code onError} if the {@code Observable} notifies
     *         of an error, and calls {@code onComplete} if the {@code Observable} notifies that the observable
     *         sequence is complete
     */
    public static final <T> Subscriber<T> create(final Action1<? super T> onNext, final Action1<Throwable> onError, final Action0 onComplete) {
        if (onNext == null) {
            throw new IllegalArgumentException("onNext can not be null");
        }
        if (onError == null) {
            throw new IllegalArgumentException("onError can not be null");
        }
        if (onComplete == null) {
            throw new IllegalArgumentException("onComplete can not be null");
        }

        return new Subscriber<T>() {

            @Override
            public final void onCompleted() {
                try {
                    onComplete.call();
                }
                finally {
                    unsubscribe();
                }
            }

            @Override
            public final void onError(Throwable e) {
                try {
                    onError.call(e);
                }
                finally {
                    unsubscribe();
                }
            }

            @Override
            public final void onNext(T args) {
                onNext.call(args);
            }

        };
    }

}
