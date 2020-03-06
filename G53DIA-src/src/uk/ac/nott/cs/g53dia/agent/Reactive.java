package uk.ac.nott.cs.g53dia.agent;
import uk.ac.nott.cs.g53dia.library.*;

import java.util.List;

import static uk.ac.nott.cs.g53dia.library.LitterAgent.MAX_CHARGE;

public class Reactive {
    Helpers helpers = new Helpers();

    protected Cell reactiveRefuel(Cell cell, int chargeLevel, List<Cell> rechargePoints) {
        if ((chargeLevel <= MAX_CHARGE * 0.3) && !(cell instanceof RechargePoint)) {
            Cell closestRechargePoint = helpers.findClosest(cell.getPoint(), rechargePoints, chargeLevel);
            if (closestRechargePoint != null) {
                return closestRechargePoint;
            }
        }
        return null;
    }
}
