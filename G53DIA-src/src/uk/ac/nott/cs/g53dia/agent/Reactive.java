package uk.ac.nott.cs.g53dia.agent;
import uk.ac.nott.cs.g53dia.library.*;

import java.util.List;

import static uk.ac.nott.cs.g53dia.library.LitterAgent.MAX_CHARGE;

public class Reactive {
    Helpers helpers = new Helpers();

    protected Point reactiveStationStop(Cell view, int wasteLevel, int recyclingLevel,
                                        List<Cell> wasteStations, List<Cell> recyclingStations) {
        Point closestStation = null;
        int litterCap = 100;

        if (wasteLevel > litterCap && wasteStations.size() > 0) {
            closestStation = helpers.findClosest(view.getPoint(), wasteStations);
            if (closestStation != null) {
                return closestStation;
            }
        } else if (recyclingLevel > litterCap && recyclingStations.size() > 0) {
            closestStation = helpers.findClosest(view.getPoint(), recyclingStations);
            if (closestStation != null) {
                return closestStation;
            }
        }
        return null;
    }

    protected Point reactiveRefuel(Cell cell, int chargeLevel, List<Cell> rechargePoints) {
        if ((chargeLevel <= MAX_CHARGE * 0.3) && !(cell instanceof RechargePoint)) {
            Point closestRechargePoint = helpers.findClosest(cell.getPoint(), rechargePoints, chargeLevel);
            if (closestRechargePoint != null) {
                return closestRechargePoint;
            }
        }
        return null;
    }

}
