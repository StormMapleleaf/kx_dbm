package org.flywaydb.core.internal.util;

import java.util.Arrays;

public class Pair<L, R> implements Comparable<Pair<L, R>> {
        private final L left;

        private final R right;

    private Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

        public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<L, R>(left, right);
    }

        public L getLeft() {
        return left;
    }

        public R getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return left.equals(pair.left) && right.equals(pair.right);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{left, right});
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(Pair<L, R> o) {
        if (left instanceof Comparable<?>) {
            int l = ((Comparable<L>) left).compareTo(o.left);
            if (l != 0) {
                return l;
            }
        }
        if (right instanceof Comparable<?>) {
            int r = ((Comparable<R>) right).compareTo(o.right);
            if (r != 0) {
                return r;
            }
        }
        return 0;
    }
}