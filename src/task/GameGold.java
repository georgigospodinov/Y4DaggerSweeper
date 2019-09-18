package task;

import java.util.LinkedHashSet;

import static util.PrintFormatting.NEW_LINE;

public class GameGold extends Game {

    private void determineNextToGold(Cell c) {
        Cell neighbour = getCell(c.I - 1, c.J);
        if (neighbour != null && neighbour.GOLD) {
            c.setNextToGold();
            return;
        }

        neighbour = getCell(c.I + 1, c.J);
        if (neighbour != null && neighbour.GOLD) {
            c.setNextToGold();
            return;
        }

        neighbour = getCell(c.I, c.J - 1);
        if (neighbour != null && neighbour.GOLD) {
            c.setNextToGold();
            return;
        }

        neighbour = getCell(c.I, c.J + 1);
        if (neighbour != null && neighbour.GOLD)
            c.setNextToGold();
    }

    public GameGold(String[][] map) {
        super(map);

        for (Cell[] row : this.map) {
            for (Cell c : row) {
                determineNextToGold(c);
            }
        }
    }

    public LinkedHashSet<Cell> getCardinalNeighbours(Cell c) {
        LinkedHashSet<Cell> neighbours = new LinkedHashSet<>();
        Cell n = getCell(c.I - 1, c.J);
        if (n != null) neighbours.add(n);

        n = getCell(c.I + 1, c.J);
        if (n != null) neighbours.add(n);

        n = getCell(c.I, c.J - 1);
        if (n != null) neighbours.add(n);

        n = getCell(c.I, c.J + 1);
        if (n != null) neighbours.add(n);

        return neighbours;
    }

    private String atLeastOneGold(LinkedHashSet<Cell> neighbours) {
        StringBuilder sb = new StringBuilder();

        for (Cell n : neighbours)
            if (n.isCovered() && !n.isMarked())
                sb.append(dimacsID(n) + " | ");

        if (sb.length() == 0) return null;
        return sb.substring(0, sb.length() - 3);
    }

    private String noGoldIn(LinkedHashSet<Cell> neighbours) {
        StringBuilder sb = new StringBuilder();

        for (Cell n : neighbours)
            if (n.isCovered() && !n.isMarked())
                sb.append("~" + dimacsID(n) + " & ");

        if (sb.length() == 0) return null;
        return sb.substring(0, sb.length() - 3);
    }

    private boolean goldFound(LinkedHashSet<Cell> neighbours) {
        for (Cell n : neighbours)
            if (!n.isCovered() && n.GOLD)
                return true;

        return false;
    }

    public String goldDimacs() {
        StringBuilder doContain = new StringBuilder();
        StringBuilder doesNotContain = new StringBuilder();
        for (Cell[] row : map) {
            for (Cell c : row) {
                if (c.isCovered()) continue;
                LinkedHashSet<Cell> neighbours = getCardinalNeighbours(c);

                if (c.isNextToGold()) {
                    if (!goldFound(neighbours)) {
                        String goldIn = atLeastOneGold(neighbours);
                        if (goldIn == null) continue;

                        doContain.append("(" + goldIn + ") & ");
                    }
                }
                else {
                    String noGold = noGoldIn(neighbours);
                    if (noGold == null) continue;

                    doesNotContain.append("(" + noGold + ") & ");
                }
            }
        }

        int dl = doContain.length();
        int dnl = doesNotContain.length();
        String left = dl > 0 ? doContain.substring(0, dl - 3) : null;
        String right = dnl > 0 ? doesNotContain.substring(0, dnl - 3) : null;
        if (left == null) return right;
        if (right == null) return left;
        return "(" + left + ") & (" + right + ")";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // Top line of "-"
        for (Cell c : map[0])
            sb.append("----");
        sb.append("-").append(NEW_LINE);

        for (Cell[] row : map) {
            // crete line of "|0m"
            for (Cell c : row)
                sb.append("|").append(c.witGoldVisibility());
            // that ends with "|"
            sb.append("|").append(NEW_LINE);

            for (Cell c : row)
                sb.append("----");
            sb.append("-").append(NEW_LINE);
        }
        return sb.toString();
    }
}
