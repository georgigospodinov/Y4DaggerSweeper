package task;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.function.Function;

import static ui.Loop.L;
import static util.PrintFormatting.NEW_LINE;
import static util.PrintFormatting.print;

public class Game {

    private static final int DIMACS_THRESHOLD = 5;
    protected Cell[][] map;
    private int daggersTotal = 0;

    public Cell getTopLeft() {
        return map[0][0];
    }

    public Game(String[][] map) {
        int n = map.length;
        this.map = new Cell[n][];

        for (int i = 0; i < n; i++) {
            int m = map[i].length;
            this.map[i] = new Cell[m];
            for (int j = 0; j < m; j++) {
                switch (map[i][j]) {
                    case "d":
                        this.map[i][j] = Cell.createDagger(i, j);
                        daggersTotal++;
                        break;
                    case "g":
                        this.map[i][j] = Cell.createGold(i, j);
                        break;
                    default:
                        int value = Integer.parseInt(map[i][j]);
                        this.map[i][j] = Cell.createClue(i, j, value);
                        break;
                }
            }  // for each cell in row
        }  // for each row
    }

    public boolean isWon() {
        // If there is a covered non-dagger, then the game is not won.
        for (Cell[] row : map)
            for (Cell c : row)
                if (c.isCovered() && !c.DAGGER)
                    return false;

        return true;
    }

    public Cell getRandomUnknown(Random generator) {
        int i, j;
        boolean acceptable;
        do {
            i = generator.nextInt(map.length);
            j = generator.nextInt(map[i].length);
            // The cell must be covered and not marked.
            acceptable = map[i][j].isCovered() && !map[i][j].isMarked();
        }
        while (!acceptable);

        return map[i][j];
    }

    public int applyUntilPositive(Function<Cell, Integer> function) {
        for (Cell[] row : map)
            for (Cell c : row) {
                Integer r = function.apply(c);
                if (r > 0)
                    return r;
            }

        return -1;
    }

    protected Cell getCell(int i, int j) {
        if (i < 0 || i >= map.length) return null;
        if (j < 0 || j >= map[i].length) return null;
        return map[i][j];
    }

    public LinkedList<Cell> getNeighboursOf(Cell c) {
        LinkedList<Cell> neighbours = new LinkedList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Cell n = getCell(c.I + i, c.J + j);
                if (n == null) continue;
                neighbours.addLast(n);
            }
        }
        return neighbours;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // Top line of "-"
        for (Cell c : map[0])
            sb.append("--");
        sb.append("-").append(NEW_LINE);

        for (Cell[] row : map) {
            // crete line of "|0"
            for (Cell c : row)
                sb.append("|").append(c.displayCode());
            // that ends with "|"
            sb.append("|").append(NEW_LINE);

            for (Cell c : row)
                sb.append("--");
            sb.append("-").append(NEW_LINE);
        }
        return sb.toString();
    }

    public int toStringLineCount() {
        return map.length * 2 + 1;
    }

    public int cellCount() {
        return map.length * map[0].length;
    }

    private ArrayList<Cell> getUnmarkedCovered() {
        ArrayList<Cell> cells = new ArrayList<>();
        for (Cell[] row : map) {
            for (Cell c : row) {
                if (c.isCovered() && !c.isMarked())
                    cells.add(c);
            }
        }

        return cells;
    }

    public LinkedHashSet<Cell> getHorizon() {
        LinkedHashSet<Cell> cells = new LinkedHashSet<>();
        for (Cell[] row : map) {
            for (Cell c : row) {
                if (c.isCovered()) continue;

                LinkedList<Cell> neighbours = getNeighboursOf(c);
                neighbours.stream().filter(Cell::isCovered).forEach(cells::add);
            }
        }

        return cells;
    }

    public int dimacsID(Cell c) {
        return c.I * map.length + c.J;
    }

    // Current Indexes For ALL; track the indexes of the cell which should be daggers.
    private ArrayList<Integer> cifa;

    private void resetIndexes(int n) {
        cifa = new ArrayList<>(n);
        for (int i = 0; i < n; i++)
            cifa.add(i);
    }

    private boolean moveIndexes(int size) {
        int n = cifa.size();

        if (cifa.get(0) == size - n) {
            return false;
        }
        int i = n - 1;
        //  Find the right-most element that has not reached its last index
        while (i >= 0 && cifa.get(i) == size - n + i) i--;
        if (i == -1) {
            print("THIS SHOULD NOT HAVE HAPPENED! THIS CASE IS SUPPOSED TO HAVE BEEN AVOIDED EARLIER!");
        }

        // Move this index one up
        cifa.set(i, cifa.get(i) + 1);
        i++;

        // Set the rest to the following indexes.
        while (i < n) {
            cifa.set(i, cifa.get(i - 1) + 1);
            i++;
        }

        return true;
    }

    private String possibility(ArrayList<Cell> cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            Cell c = cells.get(i);
            if (!cifa.contains(i))
                sb.append("~");
            sb.append(dimacsID(c));
            sb.append(" & ");
        }

        return sb.substring(0, sb.length() - 3);
    }

    // In the cells Collection, exactly n of them are true. The others are false.
    public String dimacs(int n, ArrayList<Cell> cells) {
        int size = cells.size();
        if (size == 0 || n == 0) return null;
        if (size - n > DIMACS_THRESHOLD && n > DIMACS_THRESHOLD)
            return null;

        StringBuilder sb = new StringBuilder();
        resetIndexes(n);
        try {
            do sb.append("(" + possibility(cells) + ") | ");
            while (moveIndexes(size));
        }
        catch (OutOfMemoryError oome) {
            L.log(oome);
        }
        if (sb.length() == 0) return null;
        return sb.substring(0, sb.length() - 3);
    }

    private String perCellDimacs(Cell c) {
        if (c.isResolved()) return null;

        LinkedList<Cell> neighbours = getNeighboursOf(c);
        ArrayList<Cell> potential = new ArrayList<>();
        int remaining = c.CLUE;
        for (Cell n : neighbours) {
            if (!n.isCovered()) {
                // Count uncovered daggers
                if (n.DAGGER) remaining--;
                continue;
            }

            // Count covered marked daggers
            if (n.isMarked()) remaining--;
                // Potential daggers
            else potential.add(n);
        }

        return dimacs(remaining, potential);
    }

    private String dimacsForAllCells() {
        StringBuilder sb = new StringBuilder();
        for (Cell[] row : map) {
            for (Cell c : row) {
                // Ignore covered cells, and uncovered daggers.
                if (c.isCovered() || c.DAGGER) continue;
                String d = perCellDimacs(c);
                if (d == null) continue;
                sb.append("(" + d + ") & ");
            }
        }

        int l = sb.length();
        if (l == 0) return null;
        return sb.substring(0, l - 3);
    }

    public String toDimacs(int daggersFound) {
        int remaining = daggersTotal - daggersFound;
        ArrayList<Cell> cells = getUnmarkedCovered();
        String KB = dimacs(remaining, cells);
        String s = dimacsForAllCells();
        if (KB == null) return s;
        if (s == null) return KB;
        return "(" + KB + ") & (" + s + ")";
    }
}
