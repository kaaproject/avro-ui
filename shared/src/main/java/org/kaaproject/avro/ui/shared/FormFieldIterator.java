/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.avro.ui.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class FormFieldIterator<E> implements Iterator<E> {

    public static <T> Iterable<T> concatItemWithIterable(T value, Iterable<T> iterable) {
        List<Iterable<T>> inputs = new ArrayList<>();
        Iterable<T> input = singletoneIterable(value);
        inputs.add(input);
        if (iterable != null) {
            inputs.add(iterable);
        }
        return concat(inputs);
    }
    
    public static <T> Iterable<T> concatItemWithCollection(T value, Collection<? extends Iterable<T>> values) {
        List<Iterable<T>> inputs = new ArrayList<>();
        Iterable<T> input = singletoneIterable(value);
        inputs.add(input);
        if (values != null) {
            inputs.addAll(values);
        }
        return concat(inputs);
    }
    
    public static <T> FormFieldIterator<T> singletonIterator(final T value) {
        return new FormFieldIterator<T>() {
            
            boolean done;
            
            public boolean hasNext() {
                return !done;
            }

            public T next() {
                if (done) {
                    throw new NoSuchElementException();
                }
                done = true;
                return value;
            }
        };
    }
    
    static final FormFieldIterator<Object> EMPTY_ITERATOR = new FormFieldIterator<Object>() {
        public boolean hasNext() {
            return false;
        }

        public Object next() {
            throw new NoSuchElementException();
        }
    };

    @SuppressWarnings("unchecked")
    private static <T> FormFieldIterator<T> emptyIterator() {
        return (FormFieldIterator<T>) EMPTY_ITERATOR;
    }
    
    private static <T> Iterable<T> singletoneIterable(final T value) {
        return new IterableWithToString<T>() {
            @Override
            public Iterator<T> iterator() {
                return singletonIterator(value);
            }
        };
    }
    
    private static <T> Iterable<T> concat(
            final Iterable<? extends Iterable<? extends T>> inputs) {
        checkNotNull(inputs);
        return new IterableWithToString<T>() {
            public Iterator<T> iterator() {
                return concat(iterators(inputs));
            }
        };
    }
        
    private static <T> FormFieldIterator<Iterator<? extends T>> iterators(
            Iterable<? extends Iterable<? extends T>> iterables) {
          final Iterator<? extends Iterable<? extends T>> iterableIterator =
              iterables.iterator();
          return new FormFieldIterator<Iterator<? extends T>>() {
            public boolean hasNext() {
              return iterableIterator.hasNext();
            }
            public Iterator<? extends T> next() {
              return iterableIterator.next().iterator();
            }
          };
    }
    
    private static <T> FormFieldIterator<T> concat(
            final Iterator<? extends Iterator<? extends T>> inputs) {
        checkNotNull(inputs);
        return new FormFieldIterator<T>() {
            Iterator<? extends T> current = emptyIterator();

            public boolean hasNext() {
                boolean currentHasNext;
                while (!(currentHasNext = checkNotNull(current).hasNext())
                        && inputs.hasNext()) {
                    current = inputs.next();
                }
                return currentHasNext;
            }

            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return current.next();
            }
        };
    }
    
    private static String toString(Iterable<?> iterable) {
            return toString(iterable.iterator());
    }
    
    private static String toString(Iterator<?> iterator) {
        if (!iterator.hasNext()) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder();
        builder.append('[').append(iterator.next());
        while (iterator.hasNext()) {
            builder.append(", ").append(iterator.next());
        }
        return builder.append(']').toString();
    }
    
    private static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }
    
    abstract static class IterableWithToString<E> implements Iterable<E> {
        @Override
        public String toString() {
            return FormFieldIterator.toString(this);
        }
    }

    protected FormFieldIterator() {
    }

    public final void remove() {
        throw new UnsupportedOperationException();
    }

}
