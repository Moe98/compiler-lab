package nfa;

public enum Transition {
    ZERO("0"),
    ONE("1"),
    EPSILON("e");

    private final String name;

    Transition(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
