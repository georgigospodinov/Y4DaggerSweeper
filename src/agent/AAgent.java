package agent;

import task.Cell;
import task.Game;
import task.World;

import java.util.LinkedList;
import java.util.Random;

import static util.PrintFormatting.print;

public abstract class AAgent {
    protected final Random generator;
    private int life = 1;
    protected int daggersFound = 0;
    protected Game game;

    public boolean isDead() {
        return life < 1;
    }

    public boolean hasWon() {
        return game.isWon();
    }

    public AAgent(World w, Long seed) {
        game = new Game(w.map);
        generator = new Random(seed);
    }

    public int firstMove() {
        print("Probing top left.");
        int r = probe(game.getTopLeft());
        print(game);
        return game.toStringLineCount() + r + 2;
    }

    protected abstract int makeMove();

    public int moveAndShowBoard() {
        int r = makeMove();
        print(game);
        return game.toStringLineCount() + r + 1;
    }

    protected int probe(Cell c) {
        c.uncover();
        if (c.GOLD) {
            print(c.J + " " + c.I + "  contained gold. +1 Life");
            life++;
        }
        else if (c.DAGGER) {
            print(c.J + " " + c.I + "  contained dagger. -1 Life");
            daggersFound++;
            life--;
        }
        else print(c.J + " " + c.I + "  clue: " + c.CLUE);

        return 1;
    }

    public int RPS() {
        Cell covered = game.getRandomUnknown(generator);
        print("Random probing");
        return 1 + probe(covered);
    }

    public int SPS() {
        print("Single Point Strategy: ");
        int v = game.applyUntilPositive(this::spsOn);
        return v > 0 ? v + 1 : -1;
    }

    // Single Point Strategy on Cell c.
    private int spsOn(Cell c) {
        if (c.isCovered() || c.isResolved() || c.DAGGER) return -1;
        // For the rest of this method, c is uncovered, unresolved, and contains a clue.

        LinkedList<Cell> neighbours = game.getNeighboursOf(c);

        int remainingDaggers = c.CLUE, nNMarked = 0;
        for (Cell neighbour : neighbours) {
            if (neighbour.isMarked())
                remainingDaggers--;
            else if (neighbour.isCovered())
                nNMarked++;
            else if (neighbour.DAGGER)
                remainingDaggers--;
        }

        if (remainingDaggers == 0) {
            boolean printed = false;
            int r = 0;
            for (Cell neighbour : neighbours) {
                if (neighbour.isCovered() && !neighbour.isMarked()) {
                    if (!printed) {
                        print(c.J + " " + c.I + ", " + c.CLUE + "  All daggers are found. Probing neighbours:");
                        r++;
                        printed = true;
                    }
                    r += probe(neighbour);
                }
            }
            c.resolve();
            // Only succeed if at least one was probed.
            if (r > 0) return r;
        }

        else if (remainingDaggers == nNMarked) {
            boolean printed = false;
            int r = 0;
            for (Cell neighbour : neighbours) {
                if (neighbour.isCovered() && !neighbour.isMarked()) {
                    if (!printed) {
                        print(c.J + " " + c.I + ", " + c.CLUE + "  All non-marked neighbours must be daggers. Marking: ");
                        r++;
                        printed = true;
                    }
                    print(neighbour.J + " " + neighbour.I);
                    r++;
                    neighbour.mark();
                    daggersFound++;
                }
            }
            c.resolve();
            // Only succeed if at least one was marked.
            if (r > 0) return r;
        }

        return -1;
    }

    protected int printFail() {
        print("Failed! Resorting to:");
        return 1;
    }
}
