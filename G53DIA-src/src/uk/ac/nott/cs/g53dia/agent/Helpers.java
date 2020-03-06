package uk.ac.nott.cs.g53dia.agent;

import uk.ac.nott.cs.g53dia.library.*;

import java.util.List;

public class Helpers {
    public Cell findClosest (Point cellLocation, List<Cell> cells) {
        return findClosest(cellLocation, cells, 0);
    }

    public Cell findClosest (Point cellLocation, List<Cell> cells, int cellCharge) {
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
            if (minDistance >= cellCharge || (minDistance < 5 && cellCharge < 100)) {
                return closestPoint;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
