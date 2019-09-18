package agent;

import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.formulas.Literal;
import org.logicng.io.parsers.PropositionalParser;
import org.sat4j.core.VecInt;
import org.sat4j.pb.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import task.Cell;
import task.World;
import ui.Loop;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.*;

import static ui.Loop.L;
import static util.PrintFormatting.print;

public class Agent2 extends AAgent {
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    private static <T> T timedCall(Callable<T> c) {
        FutureTask<T> t = new FutureTask<>(c);
        THREAD_POOL.execute(t);
        try {
            return t.get(5, TimeUnit.SECONDS);
        }
        catch (Exception ignored) {
        }

        return null;
    }

    private final LinkedHashMap<String, Integer> code = new LinkedHashMap<>();
    private final PropositionalParser p = new PropositionalParser(new FormulaFactory());
    private int nextInt;

    public Agent2(World w, Long seed) {
        super(w, seed);
    }

    public int encode(String text) {
        if (!code.containsKey(text)) {
            code.put(text, nextInt);
            nextInt++;
        }

        return code.get(text);
    }

    private int[][] encodeInts(Formula cnf) {
        code.clear();
        int n = cnf.numberOfOperands();
        nextInt = game.cellCount();
        int[][] encoded = new int[n][];
        int i = 0;
        for (Formula f : cnf) {
            int noa = (int) f.numberOfAtoms();
            encoded[i] = new int[noa];
            int j = 0;
            for (Literal lit : f.literals()) {
                String literal = lit.toString();
                boolean negative = literal.startsWith("~");
                if (negative)
                    literal = literal.substring(1);
                try {
                    encoded[i][j] = Integer.parseInt(literal);
                }
                catch (NumberFormatException e) {
                    encoded[i][j] = encode(literal);
                }
                if (negative)
                    encoded[i][j] = -encoded[i][j];
                j++;
            }

            i++;
        }

        return encoded;
    }

    private int numberOfAtoms = 0;

    protected int[][] clauses(String KB) {
        if (KB == null) return null;
        Formula f = timedCall(() -> p.parse(KB).cnf());
        if (f == null) return null;
        numberOfAtoms = (int) f.numberOfAtoms();

        return encodeInts(f);
    }

    protected boolean notSatisfiable(int[][] KB, int inspected) {
        ISolver solver = SolverFactory.newDefault();
        solver.newVar(numberOfAtoms);
        solver.setExpectedNumberOfClauses(KB.length + 1);
        try {
            for (int[] clause : KB)
                solver.addClause(new VecInt(clause));
            solver.addClause(new VecInt(new int[]{inspected}));
        }
        catch (ContradictionException e) {
            L.log(e);
            return false;
        }

        Boolean satisfiable = timedCall(solver::isSatisfiable);
        return satisfiable != null && !satisfiable;
    }

    protected int ATS() {
        print("SAtisfiability Test Strategy:");
        String KB = null;
        try {
            KB = game.toDimacs(daggersFound);
        }
        catch (Throwable e) {
            L.log(e);
        }
        int[][] clauses = clauses(KB);
        if (clauses == null) return -1;

        LinkedHashSet<Cell> horizon = game.getHorizon();
        LinkedHashSet<Cell> marked = new LinkedHashSet<>();
        int linesPrinted = 1;
        for (Cell h : horizon) {
            int id = game.dimacsID(h);
            if (notSatisfiable(clauses, -id)) {
                print("Marking " + h.J + " " + h.I);
                linesPrinted++;
                h.mark();
                daggersFound++;
                marked.add(h);
            }
        }

        // Remove marked from horizon.
        horizon.removeAll(marked);

        for (Cell h : horizon) {
            int id = game.dimacsID(h);
            if (notSatisfiable(clauses, id)) {
                print("Probing");
                probe(h);
                return linesPrinted + 2;
            }
        }

        if (linesPrinted == 1) return -1;
        else return linesPrinted;
    }

    @Override
    protected int makeMove() {
        int r = SPS();
        if (r >= 0) return r;

        int x = 0;
        x += printFail() + 1;  // Account for the one line SPS definitely prints (the line with its name).
        r = ATS();
        if (r >= 0) return r + x;

        x += printFail() + 1;  // Account for the one line ATS definitely prints (the line with its name).
        return RPS() + x;
    }

    public static void main(String[] args) {
        new Loop(Agent2.class).iterate();
    }
}
