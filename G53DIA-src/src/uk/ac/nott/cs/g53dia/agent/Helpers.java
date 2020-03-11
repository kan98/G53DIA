package uk.ac.nott.cs.g53dia.agent;

import uk.ac.nott.cs.g53dia.library.*;

import java.util.List;

/**
 * This class contains helper methods that can be used from throughout the agent package.
 *
 */
public class Helpers {

    /**
     * Finds the closest cell from a list of cells based on a certain Point.
     *
     * @param cellLocation Current point to compare cells to.
     * @param cells List of cells to consider to find the closest to the Point.
     * @return The cell that is closest to the Point.
     *
     */
    protected Cell findClosest (Point cellLocation, List<Cell> cells) {
        return findClosest(cellLocation, cells, 0);
    }

    /**
     * Same as the method above but with an extra parameter to optimise the agent recharging.
     *
     * @param cellCharge Used in conditions to see if the agent should recharge depending on how much charge is left.
     *
     */
    protected Cell findClosest (Point cellLocation, List<Cell> cells, int cellCharge) {
        int minDistance = cellLocation.distanceTo(cells.get(0).getPoint());
        Cell closestPoint = cells.get(0);

        if (!cells.isEmpty()) {
            for (int i=0; i!=cells.size(); i++) {
                int currentDistance = cellLocation.distanceTo(cells.get(i).getPoint());
                if (currentDistance < minDistance) {
                    minDistance = currentDistance;
                    closestPoint = cells.get(i);
                }
            }
            if (cellCharge != 0) {
                cellCharge -= 2;
            }
            if (minDistance >= cellCharge || (minDistance < 5 && cellCharge < 60)
                    || (minDistance < 2 && cellCharge < 80)) {
                return closestPoint;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
