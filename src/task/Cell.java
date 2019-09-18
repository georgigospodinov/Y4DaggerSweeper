package task;

public class Cell {

    public static Cell createClue(int i, int j, int value) {
        return new Cell(i, j, value, false, false);
    }

    public static Cell createGold(int i, int j) {
        return new Cell(i, j, 0, true, false);
    }

    public static Cell createDagger(int i, int j) {
        return new Cell(i, j, 0, false, true);
    }

    public final int I, J, CLUE;
    public final boolean GOLD, DAGGER;
    private boolean covered = true, marked = false, resolved = false, nextToGold = false;

    public boolean isCovered() {
        return covered;
    }

    public void uncover() {
        covered = false;
        marked = false;
    }

    public boolean isMarked() {
        return marked;
    }

    public void mark() {
        marked = true;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void resolve() {
        resolved = true;
    }

    public boolean isNextToGold() {
        return nextToGold;
    }

    public void setNextToGold() {
        nextToGold = true;
    }

    private Cell(int i, int j, int clue, boolean gold, boolean dagger) {
        I = i;
        J = j;
        CLUE = clue;
        GOLD = gold;
        DAGGER = dagger;
    }

    public char displayCode() {
        if (marked) return 'M';
        if (covered) return 'C';
        if (DAGGER) return 'D';
        if (GOLD) return 'G';
        return (char) (CLUE + '0');
    }

    public String witGoldVisibility() {
        if (marked) return " M ";
        if (covered) return " C ";
        if (DAGGER) return " D ";
        char last = nextToGold ? 'm' : ' ';
        if (GOLD) return " G" + last;
        return " " + CLUE + last;
    }
}
