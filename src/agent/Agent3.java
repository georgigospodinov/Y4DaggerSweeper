package agent;

import task.Cell;
import task.GameGold;
import task.World;
import ui.Loop;

import java.util.LinkedHashSet;

import static ui.Loop.L;
import static util.PrintFormatting.print;

public class Agent3 extends Agent2 {
    public Agent3(World w, Long seed) {
        super(w, seed);
        game = new GameGold(w.map);
    }

    private int goldSPS() {
        print("Single Point Strategy for gold.");
        int v = game.applyUntilPositive(this::goldSpsOn);
        return v > 0 ? v + 1 : -1;
    }

    private int goldSpsOn(Cell c) {
        if (c.isCovered() || !c.isNextToGold()) return -1;
        LinkedHashSet<Cell> cardinal = ((GameGold) game).getCardinalNeighbours(c);
        Cell onlyCovered = null;
        for (Cell neighbour : cardinal) {
            if (neighbour.isCovered()) {
                // Cannot have both dagger and gold.
                if (neighbour.isMarked()) continue;

                // If this is not the only covered neighbour, then goldSPS cannot determine which one contains the Gold.
                if (onlyCovered != null)
                    return -1;
                else onlyCovered = neighbour;
            }
            // The gold for Cell c has already been found.
            else if (neighbour.GOLD)
                return -1;
        }

        if (onlyCovered == null) {
            print("How did this happen???");
            return -1;
        }

        print("Found gold");
        probe(onlyCovered);
        return 2;
    }

    private int goldATS() {
        print("Gold ATS:");
        String goldKB = null;
        try {
            goldKB = ((GameGold) game).goldDimacs();
        }
        catch (Throwable e) {
            L.log(e);
        }
        int[][] clauses = clauses(goldKB);
        if (clauses == null) return -1;

        LinkedHashSet<Cell> horizon = game.getHorizon();
        int linesPrinted = 1;
        for (Cell h : horizon) {
            int id = game.dimacsID(h);
            if (notSatisfiable(clauses, -id))
                linesPrinted += probe(h);
        }

        if (linesPrinted == 1) return -1;
        else return linesPrinted;
    }

    @Override
    protected int makeMove() {
        int r = goldSPS();
        if (r > 0) return r;

        int x = 0;
        x += printFail() + 1;  // Account for the one line goldSPS definitely prints (the line with its name).
        r = goldATS();
        if (r > 0) return r + x;

        x += printFail() + 1;  // Account for the one line goldATS definitely prints (the line with its name).
        r = SPS();
        if (r > 0) return r + x;

        x += printFail() + 1;  // Account for the one line SPS definitely prints (the line with its name).
        r = ATS();
        if (r >= 0) return r + x;

        x += printFail() + 1;  // Account for the one line ATS definitely prints (the line with its name).
        return RPS() + x;
    }

    public static void main(String[] args) {
        new Loop(Agent3.class).iterate();
    }
}
