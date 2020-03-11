package uk.ac.nott.cs.g53dia.agent;

import uk.ac.nott.cs.g53dia.agent.DemoLitterAgent;
import uk.ac.nott.cs.g53dia.agent.Helpers;
import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.Point;
import uk.ac.nott.cs.g53dia.library.RecyclingBin;
import uk.ac.nott.cs.g53dia.library.WasteBin;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the deliberative component of the agent.
 *
 * An object of the class is created in the DemoLitterAgent to help plan actions for the agent.
 *
 */
public class Deliberative {
    protected enum focus {
        NONE,
        WASTE,
        RECYCLING
    }

    private focus currentFocus = focus.NONE;

    private int currentCharge, binCapacity;
    private long timeLeft;
    private List<Cell> recyclingBins, wasteBins, recyclingPlants, wastePlants;

    private List<RecyclingBin> recyclingBinList;
    private List<WasteBin> wasteBinList;

    private Cell currentCell;

    private List<DemoLitterAgent.state> stateList = new ArrayList<>();
    private List<Cell> pointList = new ArrayList<>();

    private float highestScoreRatio = -1000;

    private Helpers helpers = new Helpers();

    /**
     * This method is called by the senseAndAct method from the DemoLitterAgent class for each timestep.
     * The purpose of this is to pass in all the agent state and our internal representation to be used for route planning.
     *
     */
    protected void setVars(int currentCharge, int binCapacity, long timeLeft, int recyclingLevel, int wasteLevel,
                        List<Cell> recyclingBins, List<Cell> wasteBins, List<Cell> recyclingPlants,
                        List<Cell> wastePlants, Cell currentCell) {

        this.currentCharge = currentCharge;
        this.binCapacity = binCapacity;
        this.timeLeft = timeLeft;

        this.recyclingBins = recyclingBins;
        this.wasteBins = wasteBins;
        this.recyclingPlants = recyclingPlants;
        this.wastePlants = wastePlants;

        this.currentCell = currentCell;

        if (wasteLevel > 0) {
            currentFocus = focus.WASTE;
        } else if (recyclingLevel > 0) {
            currentFocus = focus.RECYCLING;
        }

        search();
    }

    /**
     * This method is called by the setVars method.
     * It gets all the bins that aren't empty and calls the getBestRoute method to plan the best route for the agent.
     * 
     * The method will take the list of bins from the getBestRoute method and create actions for them to add to stateList.
     * The method will also add a litter drop off action to the closest respective station into the stateList queue.
     *
     */
    private void search() {
        List<Cell> currentSelection = new ArrayList<>();
        focus tempFocus = currentFocus;
        recyclingBinList = (List<RecyclingBin>)(List<?>) getFilledBins(recyclingBins);
        wasteBinList = (List<WasteBin>)(List<?>) getFilledBins(wasteBins);

        if (tempFocus != focus.WASTE) {
            getBestRoute(recyclingBinList, null, 0, 0, currentSelection, currentCell.getPoint(), false);
        }

        currentSelection = new ArrayList<>();
        if (tempFocus != focus.RECYCLING) {
            getBestRoute(null, wasteBinList, 0, 0, currentSelection, currentCell.getPoint(),false);
        }

        for (int i=0; i != pointList.size(); i++) {
            stateList.add(DemoLitterAgent.state.MOVE_TO_POINT);
            if (currentFocus == focus.RECYCLING) {
                stateList.add(DemoLitterAgent.state.PICKUP_RECYCLING);
            } else {
                stateList.add(DemoLitterAgent.state.PICKUP_WASTE);
            }
        }

        if (currentFocus != focus.NONE) {
            if (currentFocus == focus.RECYCLING) {
                pointList.add(helpers.findClosest(pointList.get(pointList.size() - 1).getPoint(), recyclingPlants));
            } else if (currentFocus == focus.WASTE) {
                pointList.add(helpers.findClosest(pointList.get(pointList.size() - 1).getPoint(), wastePlants));
            }
            stateList.add(DemoLitterAgent.state.MOVE_TO_POINT);
            stateList.add(DemoLitterAgent.state.LITTER_DROP_OFF);
        }
    }

    /**
     * Recursive method that does a depth first search to output the best route for the agent.
     * The output is a list of either recycling or waste bins that the search method can add actual agent states for.
     *
     */
    private void getBestRoute(List<RecyclingBin> recyclingBins, List<WasteBin> wasteBins, int currentScore,
                              int chargeUsed, List<Cell> currentSelection, Point currentPoint, boolean recursion) {
        if (recyclingBins != null && recyclingBins.size() > 0) {
            for (int i=0; i < recyclingBins.size(); i++) {
                if (!recursion) {
                    currentScore = 0;
                    chargeUsed = 0;
                    currentSelection = new ArrayList<>();
                    currentPoint = this.currentCell.getPoint();
                    recyclingBins = recyclingBinList;
                }
                recursion = false;
                List<RecyclingBin> tempRecyclingBins = recyclingBins;
                List<Cell> tempCurrentSelection = currentSelection;

                int tempScore = tempRecyclingBins.get(i).getTask().getRemaining();
                if (tempScore > binCapacity - currentScore) {
                    tempScore = binCapacity - currentScore;
                }
                Cell binLocation = tempRecyclingBins.get(i);
                int tempCharge = currentPoint.distanceTo(binLocation.getPoint());

                if(tempScore + currentScore < binCapacity && tempCharge + chargeUsed <= currentCharge
                        && tempCharge + chargeUsed <= timeLeft) {
                    currentScore += tempScore;
                    chargeUsed += tempCharge;

                    tempCurrentSelection.add(binLocation);
                    tempRecyclingBins.remove(i);

                    if (tempRecyclingBins.size() > 0) {
                        getBestRoute(tempRecyclingBins, null, currentScore, chargeUsed, tempCurrentSelection, binLocation.getPoint(),true);
                    }

                    Cell stationPoint = helpers.findClosest(binLocation.getPoint(), recyclingPlants);
                    int stationCost = binLocation.getPoint().distanceTo(stationPoint.getPoint());
                    if (chargeUsed > 0 && (float)currentScore/(chargeUsed+stationCost) >= highestScoreRatio) {
                        highestScoreRatio = (float)currentScore/(chargeUsed+stationCost);
                        pointList = tempCurrentSelection;
                        currentFocus = focus.RECYCLING;
                    }
                }
            }
        }

        if (wasteBins != null && wasteBins.size() > 0) {

            for (int i=0; i < wasteBins.size(); i++) {
                if (!recursion) {
                    currentScore = 0;
                    chargeUsed = 0;
                    currentSelection = new ArrayList<>();
                    currentPoint = this.currentCell.getPoint();
                    wasteBins = wasteBinList;
                }
                recursion = false;
                List<WasteBin> tempWasteBins = wasteBins;
                List<Cell> tempCurrentSelection = currentSelection;

                int tempScore = tempWasteBins.get(i).getTask().getRemaining();
                if (tempScore > binCapacity - currentScore) {
                    tempScore = binCapacity - currentScore;
                }
                Cell binLocation = tempWasteBins.get(i);
                int tempCharge = currentPoint.distanceTo(binLocation.getPoint());

                if(tempScore + currentScore < binCapacity && tempCharge + chargeUsed <= currentCharge
                        && tempCharge + chargeUsed <= timeLeft) {
                    currentScore += tempScore;
                    chargeUsed += tempCharge;

                    tempCurrentSelection.add(binLocation);
                    tempWasteBins.remove(i);

                    if (tempWasteBins.size() > 0) {
                        getBestRoute(null, tempWasteBins, currentScore, chargeUsed, tempCurrentSelection, binLocation.getPoint(), true);
                    }

                    Cell stationPoint = helpers.findClosest(binLocation.getPoint(), wastePlants);
                    int stationCost = binLocation.getPoint().distanceTo(stationPoint.getPoint());
                    if (chargeUsed > 0 && (float)currentScore/(chargeUsed+stationCost) >= highestScoreRatio) {
                        highestScoreRatio = (float)currentScore/(chargeUsed+stationCost);
                        pointList = tempCurrentSelection;
                        currentFocus = focus.WASTE;
                    }
                }
            }
        }
    }

    /**
     * Goes through a list of cells of either waste or recycling bins and return a list of bins which aren't empty.
     *
     * @param bins A list of Cell objects of either waste or recycling bins.
     * @return A list of Cell objects of either waste or recycling bins that have non-empty tasks in them.
     *
     */
    protected List<Cell> getFilledBins(List<Cell> bins) {
        List<Cell> binList = new ArrayList<>();
        for (Cell bin : bins) {
            if (bin instanceof WasteBin) {
                WasteBin wasteBin = (WasteBin) bin;
                if (wasteBin.getTask() != null && wasteBin.getTask().getRemaining() > 0) {
                    binList.add(wasteBin);
                }
            } else {
                RecyclingBin recyclingBin = (RecyclingBin) bin;
                if (recyclingBin.getTask() != null && recyclingBin.getTask().getRemaining() > 0) {
                    binList.add(recyclingBin);
                }
            }
        }
        return binList;
    }

    protected List<DemoLitterAgent.state> getStateList() {
        return stateList;
    }

    protected List<Cell> getPointList() {
        return pointList;
    }
}