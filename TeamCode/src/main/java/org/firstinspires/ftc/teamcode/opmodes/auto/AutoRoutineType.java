package org.firstinspires.ftc.teamcode.opmodes.auto;

/**
 * The AUTO ladder from the strategy brief. Build them IN ORDER and only promote a rung to matches
 * when it works at least 8 out of 10 times.
 */
public enum AutoRoutineType {
    A0_SAFE("A0 Safe: drive into the LOADING ZONE (LEAVE 3 + PARK 5 = 8)"),
    A1_TIP_AND_PARK("A1 Tip & Park: fire 4 preloads into the up cell (3 tip it), park (20+3+5 = 28)"),
    A2_TIP_RELOAD_VOLLEY("A2 Tip, sweep 4 GARDEN POLLEN, fire them into the far cell, park"),
    A3_ROLE1("A3 role 1 (from S1): A2 but shoot from a Zone B spot clear of the partner"),
    A3_ROLE2("A3 role 2 (from S6): wait for tip 1, fire 4 preloads into the far cell, park");

    public final String description;

    AutoRoutineType(String description) {
        this.description = description;
    }

    /** True if this routine starts by shooting the preloads into the start (state A) cell. */
    public boolean shootsStartCellFirst() {
        return this == A1_TIP_AND_PARK || this == A2_TIP_RELOAD_VOLLEY || this == A3_ROLE1;
    }
}
