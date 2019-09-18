package agent;


import task.World;
import ui.Loop;

public class Agent1 extends AAgent {
    public Agent1(World w, Long seed) {
        super(w, seed);
    }

    @Override
    protected int makeMove() {
        int r = SPS();
        if (r >= 0) return r;

        int x = printFail() + 1;  // Account for the one line SPS definitely prints (the line with its name).
        return RPS() + x;
    }

    public static void main(String[] args) {
        new Loop(Agent1.class).iterate();
    }
}
