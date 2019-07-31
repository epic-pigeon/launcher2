import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Collection<T> extends ArrayList<T> {
    public Collection(List<T> list) {
        super(list);
    }

    public Collection() {
        super();
    }

    @SafeVarargs
    public Collection(T ...values) {
        super(Arrays.asList(values));
    }

    public Collection(int k) {
        super(k);
    }

    public Collection<T> find(Function<T, Boolean> fn) {
        Collection<T> collection = new Collection<>();

        for (T o : this) {
            if (fn.apply(o)) collection.add(o);
        }

        return collection;
    }

    public T findFirst(Function<T, Boolean> fn) {
        for (T o : this) {
            if (fn.apply(o)) return o;
        }
        return null;
    }

    public Collection<T> merge(Collection<? extends T> collection) {
        this.addAll(collection);
        return this;
    }

    public Collection<T> mergeInto(Collection<T> collection) {
        return collection.merge(this);
    }

    public<E> Collection<E> map(Function<T, E> fn) {
        Collection<E> newCollection = new Collection<>();

        for(T element : this) {
            newCollection.add(fn.apply(element));
        }

        return newCollection;
    }

    public<E> Collection<E> mapAndUnite(Function<T, Collection<E>> fn) {
        Collection<E> newCollection = new Collection<>();

        for(T element : this) {
            newCollection.merge(fn.apply(element));
        }

        return newCollection;
    }

    public void forEach(Function<T, Boolean> fn) {
        for (T element: this) if (fn.apply(element)) return;
    }

    public void forEach(BiConsumer<? super T, Integer> fn) {
        for (int i = 0; i < size(); i++) fn.accept(get(i), i);
    }

    public void forEach(BiFunction<? super T, Integer, Boolean> fn) {
        for (int i = 0; i < size(); i++) if (fn.apply(get(i), i)) return;
    }

    public double sumUp(Function<T, Double> fn, double initialValue) {
        double value = initialValue;
        for (T element : this) value += fn.apply(element);
        return value;
    }

    public double sumUp(Function<T, Double> fn) {
        return sumUp(fn, 0);
    }

    public double sumUp(double initialValue) {
        return sumUp(Double.class::cast, initialValue);
    }

    public double sumUp() {
        return sumUp(0);
    }

    public boolean has(Function<T, Boolean> fn) {
        return findFirst(fn) != null;
    }

    public Collection<T> with(Collection<? extends T> collection) {
        return new Collection<T>().merge(this).merge(collection.map(e -> e));
    }

    public<E> Collection<E> to() {
        Collection<E> collection = new Collection<>();
        for (T element: this) try {
            collection.add((E) element);
        } catch (ClassCastException e) {
            collection.add(null);
        }
        return collection;
    }

    public<E> Collection<E> to(Class<E> clazz) {
        Collection<E> collection = new Collection<>();
        for (T element: this) try {
            collection.add((E) element);
        } catch (ClassCastException e) {
            collection.add(null);
        }
        return collection;
    }

    public String join(String separator) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            T element = this.get(i);
            result.append(element);
            if (i != size() - 1) result.append(separator);
        }
        return result.toString();
    }
}