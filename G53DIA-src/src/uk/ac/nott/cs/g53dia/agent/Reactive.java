package uk.ac.nott.cs.g53dia.agent;
import uk.ac.nott.cs.g53dia.library.*;

import java.util.List;

import static uk.ac.nott.cs.g53dia.library.LitterAgent.MAX_CHARGE;

/**
 * This class includes method that reactive components from the DemoLitterAgent may use.
 *
 *
 */
public class Reactive {
    Helpers helpers = new Helpers();

    /**
     * This method checks whether it is appropriate to refuel at this timestep or not.
     * If it is appropriate to refuel, it will return the closest refuel point.
     *
     * @param cell The current cell the agent is on.
     * @param chargeLevel The amount of charge the agent has left.
     * @param rechargePoints A list of recharge points the agent has seen to be used to find the closest one.
     * @return A recharge point cell if the agent is to recharge, otherwise null.
     *
     */
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
