package org.revapi.classif.match.util;

public enum Operator {
    EQ, NE, LT, LE, GT, GE;

    public <T extends Comparable<T>> boolean satisfied(T o1, T o2) {
        switch (this) {
            case EQ:
                return o1.compareTo(o2) == 0;
            case NE:
                return o1.compareTo(o2) != 0;
            case LT:
                return o1.compareTo(o2) < 0;
            case LE:
                return o1.compareTo(o2) <= 0;
            case GT:
                return o1.compareTo(o2) > 0;
            case GE:
                return o1.compareTo(o2) >= 0;
            default:
                return true;
        }
    }
}
