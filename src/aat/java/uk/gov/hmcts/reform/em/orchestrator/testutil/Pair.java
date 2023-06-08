package uk.gov.hmcts.reform.em.orchestrator.testutil;

public final class Pair<S, T> {

    final S first;
    final T second;

    private Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }

    public S getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }

    public static <S, T> Pair<S, T> of(S first, T second) {
        return new Pair<>(first, second);
    }

}

