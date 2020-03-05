package uk.ac.nott.cs.g53dia.agent;

import uk.ac.nott.cs.g53dia.library.*;

import java.util.List;

public class Helpers {
    public Point findClosest (Point cellLocation, List<Cell> cells) {
        return findClosest(cellLocation, cells, 0);
    }

    public Point findClosest (Point cellLocation, List<Cell> cells, int cellCharge) {
        int minDistance = cellLocation.distanceTo(cells.get(0).getPoint());
        Point closestPoint = cells.get(0).getPoint();

        if (!cells.isEmpty()) {
            for (int i=0; i!=cells.size(); i++) {
                int currentDistance = cellLocation.distanceTo(cells.get(i).getPoint());
                if (currentDistance < minDistance) {
                    minDistance = currentDistance;
                    closestPoint = cells.get(i).getPoint();
                }
            }
            if (cellCharge != 0) {
                cellCharge -= 4;
            }
            if (minDistance >= cellCharge) {
                return closestPoint;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
