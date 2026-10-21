package org.firstinspires.ftc.teamcode.opmodes.auto;

import org.firstinspires.ftc.teamcode.config.FieldConstants.ParkSlot;
import org.firstinspires.ftc.teamcode.config.FieldConstants.StartSpot;

/**
 * The AUTO ladder from the strategy brief. Build them IN ORDER and only promote a rung to matches
 * when it works at least 8 out of 10 times. A2 and A3 assume a TUNED path follower (pedro/Constants
 * FORESIGHT_TUNED, no 0.5 speed cap) and A3 needs a rehearsal with the partner.
 *
 * Descriptions are alliance-neutral: "start cell" = the cell that is up at the start, "second cell" = the
 * other one (it comes up after TIP 1 and is shot from Zone B, the end of the field opposite our start wall).
 */
public enum AutoRoutineType {
    A0_SAFE("A0 Safe: drive into the LOADING ZONE (LEAVE 3 + PARK 5 = 8)"),
    A1_TIP_AND_PARK("A1 Tip & Park: fire 4 preloads into the start cell (3 tip it), hold until the spill lands, park (20+3+5 = 28)"),
    A2_TIP_RELOAD_VOLLEY("A2: tip, sweep 4 GARDEN POLLEN, fire them into the second cell from Zone B, park"),
    A3_ROLE1("A3 role 1 (S1): A2, but waits at the corridor end until 17 s, shoots from the role-1 spot, parks INNER"),
    A3_ROLE2("A3 role 2 (S6): wait for TIP 1, fire 4 preloads into the second cell (by 9 s), park CORNER");

    public final String description;

    AutoRoutineType(String description) {
        this.description = description;
    }

    /** True if this routine starts by shooting the preloads into the start (state A) cell. */
    public boolean shootsStartCellFirst() {
        return this == A1_TIP_AND_PARK || this == A2_TIP_RELOAD_VOLLEY || this == A3_ROLE1;
    }

    /**
     * Starts this routine is built for. Shoot-first routines from S5/S6/S7 would drive through the HIVE,
     * and A3 role 2 needs the second cell in front of it (S6; S7 is accepted but its aim is outside a
     * +-28 deg turret - AutoSelector warns). MainAuto runs a NO-MOTION plan for any other combination.
     */
    public boolean canStartFrom(StartSpot start) {
        switch (this) {
            case A1_TIP_AND_PARK:
            case A2_TIP_RELOAD_VOLLEY:
            case A3_ROLE1:
                return start == StartSpot.S1 || start == StartSpot.S2 || start == StartSpot.S3 || start == StartSpot.S4;
            case A3_ROLE2:
                return start == StartSpot.S6 || start == StartSpot.S7;
            case A0_SAFE:
            default:
                return true;
        }
    }

    /** The start spot a newly selected routine moves the selector to (null = keep the current one). */
    public StartSpot homeStart() {
        if (shootsStartCellFirst()) return StartSpot.S1;
        if (this == A3_ROLE2) return StartSpot.S6;
        return null;
    }

    /** A3 roles always use their own slot; null = use the INIT "Park" choice. */
    public ParkSlot fixedParkSlot() {
        if (this == A3_ROLE1) return ParkSlot.INNER;
        if (this == A3_ROLE2) return ParkSlot.CORNER;
        return null;
    }
}
