package org.firstinspires.ftc.teamcode.util;

/**
 * Which of OUR HIVE's two CELLS is facing up (and is therefore the one we can score in).
 *
 *  A = match-start state. RED: the AUDIENCE cell (tags 34-37) is up, opening faces the audience wall,
 *      so red shoots from the audience half ("Zone A"). BLUE: the far/SCORING cell (tags 42-45) is up,
 *      blue shoots from the far half.
 *  B = after an odd number of TIPs of our HIVE. The other cell is up and faces the other end wall,
 *      so the shooting zone FLIPS to the other half of the field ("Zone B").
 *
 * The up cell's opening faces OUTWARD toward its own end wall, tilted 30 degrees up, so it can only
 * be scored from the half of the field it faces (R2 section 6.2).
 */
public enum HiveState {
    A, B;

    public HiveState other() {
        return this == A ? B : A;
    }
}
