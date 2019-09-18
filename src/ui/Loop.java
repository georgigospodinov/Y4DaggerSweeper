package ui;

import agent.AAgent;
import task.World;
import util.Logger;
import util.WrappedReader;

import java.lang.reflect.Constructor;
import java.util.Random;

import static util.PrintFormatting.print;

public class Loop {

    public static final Logger L = new Logger("log.txt");
    private static final Random R = new Random();
    private static final WrappedReader STDIN = new WrappedReader(L);
    private static final String EXIT = "EXIT";
    private static final int Q = 'Q';

    private final Constructor<? extends AAgent> agentConstructor;
    private AAgent agent;

    private void clearLines(int linesCount) {
        int n = 65;
        for (int i = 0; i < linesCount; i++) {
            System.out.print("\033[1A");
            for (int j = 0; j < n; j++) {
                System.out.print(" ");
            }
            System.out.print('\r');
        }
    }

    public Loop(Class<? extends AAgent> agentClass) {
        Constructor<? extends AAgent> c = null;
        try {
            c = agentClass.getConstructor(World.class, Long.class);
        }
        catch (Exception e) {
            L.log(e);
        }

        agentConstructor = c;
    }

    private void instantiateAgent(World w) {
        print("Enter agent random seed [leave blank for random seed]: ");
        long seed;
        try {
            seed = Long.parseLong(STDIN.readLine());
        }
        catch (NumberFormatException e) {
            seed = R.nextLong();
        }
        try {
            agent = agentConstructor.newInstance(w, seed);
        }
        catch (Exception e) {
            L.log(e);
        }
    }

    private void nextIteration(World w) {
        instantiateAgent(w);

        int linesWritten = agent.firstMove();
        while (true) {

            if (agent.isDead()) {
                print("Agent died!\n");
                break;
            }
            else if (agent.hasWon()) {
                print("Agent won!\n");
                break;
            }

            int r = STDIN.read();
            if (r == Q) {
                print("Force-Quit");
                break;
            }
            if (r == '\n') {
                // Account for user pressing enter.
                clearLines(linesWritten + 1);
                linesWritten = agent.moveAndShowBoard();
            }
        }
    }

    public void iterate() {
        String input;
        World w;
        boolean lastIterationSucceeded;
        print("");

        do {
            print("Enter world to operate on [or 'exit' to exit the program]:");
            input = STDIN.readLine().toUpperCase();
            try {
                w = World.valueOf(input);
                nextIteration(w);
                lastIterationSucceeded = true;
            }
            catch (IllegalArgumentException e) {
                lastIterationSucceeded = false;
            }

            if (lastIterationSucceeded) continue;

            if (input.equals(EXIT))
                print("Closing logger...");
            else {
                clearLines(3);
                print("Failed to parse world.");
            }

        }
        while (!input.equals(EXIT));

        L.close();
    }
}
