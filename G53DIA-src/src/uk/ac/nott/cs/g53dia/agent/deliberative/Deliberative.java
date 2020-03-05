package uk.ac.nott.cs.g53dia.agent.deliberative;

import uk.ac.nott.cs.g53dia.agent.DemoLitterAgent;
import uk.ac.nott.cs.g53dia.agent.Helpers;
import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.Point;
import uk.ac.nott.cs.g53dia.library.RecyclingBin;
import uk.ac.nott.cs.g53dia.library.WasteBin;

import java.util.ArrayList;
import java.util.List;

public class Deliberative {
    enum focus {
        NONE,
        WASTE,
        RECYCLING
    }

    focus currentFocus = focus.NONE;

    int currentCharge, binCapacity;
    long timeLeft;
    List<Cell> recyclingBins, wasteBins, recyclingPlants, wastePlants, rechargePoints;

    List<RecyclingBin> recyclingBinList;
    List<WasteBin> wasteBinList;

    Cell currentCell;

    List<DemoLitterAgent.state> stateList = new ArrayList<>();
    List<Point> pointList = new ArrayList<>();

    float highestScoreRatio = -1000;

    public void setVars(int currentCharge, int binCapacity, long timeLeft, int recyclingLevel, int wasteLevel,
            List<Cell> recyclingBins, List<Cell> wasteBins, List<Cell> recyclingPlants,
            List<Cell> wastePlants, List<Cell> rechargePoints, Cell currentCell, DemoLitterAgent.state currentState) {

        this.currentCharge = currentCharge;
        this.binCapacity = binCapacity;
        this.timeLeft = timeLeft;

        this.recyclingBins = recyclingBins;
        this.wasteBins = wasteBins;
        this.recyclingPlants = recyclingPlants;
        this.wastePlants = wastePlants;
        this.rechargePoints = rechargePoints;

        this.currentCell = currentCell;

        search();
    }

    private void search() {
        List<Point> currentSelection = new ArrayList<>();
        focus tempFocus = currentFocus;
        if (tempFocus != focus.WASTE) {
            recyclingBinList = (List<RecyclingBin>)(List<?>) getFilledBins(recyclingBins);
            getBestRoute(recyclingBinList, null, 0, 0, currentSelection, currentCell.getPoint(), false);
        }

        currentSelection = new ArrayList<>();
        if (tempFocus != focus.RECYCLING) {
            wasteBinList = (List<WasteBin>)(List<?>) getFilledBins(wasteBins);
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

        Helpers helpers = new Helpers();
        if (currentFocus != focus.NONE) {
            if (currentFocus == focus.RECYCLING) {
                pointList.add(helpers.findClosest(pointList.get(pointList.size() - 1), recyclingPlants));
            } else if (currentFocus == focus.WASTE) {
                pointList.add(helpers.findClosest(pointList.get(pointList.size() - 1), wastePlants));
            }
            stateList.add(DemoLitterAgent.state.MOVE_TO_POINT);
            stateList.add(DemoLitterAgent.state.LITTER_DROP_OFF);
        }
    }

    private void getBestRoute(List<RecyclingBin> recyclingBins, List<WasteBin> wasteBins, int currentScore,
                                int chargeUsed, List<Point> currentSelection, Point currentPoint, boolean recursion) {
        if (recyclingBins != null && recyclingBins.size() > 0) {

            for (int i=0; i < recyclingBins.size(); i++) {
                if (!recursion) {
                    currentScore = 0;
                    chargeUsed = 0;
                    currentSelection = new ArrayList<>();
                    currentPoint = this.currentCell.getPoint();
                    recyclingBins = recyclingBinList;
                } else {
                    recursion = false;
                }
                List<RecyclingBin> tempRecyclingBins = recyclingBins;
                List<Point> tempCurrentSelection = currentSelection;

                int tempScore = tempRecyclingBins.get(i).getTask().getRemaining();
                Point binLocation = tempRecyclingBins.get(i).getPoint();
                int tempCharge = currentPoint.distanceTo(binLocation);

                if(tempScore + currentScore < binCapacity && tempCharge + chargeUsed <= currentCharge
                    && tempCharge + chargeUsed <= timeLeft) {
                    currentScore += tempScore;
                    chargeUsed += tempCharge;

                    tempCurrentSelection.add(binLocation);
                    tempRecyclingBins.remove(i);

                    if (chargeUsed > 0 && (float)currentScore/chargeUsed >= highestScoreRatio) {
                        highestScoreRatio = (float)currentScore/chargeUsed;
                        pointList = tempCurrentSelection;
                        currentFocus = focus.RECYCLING;
                    }
                    if (tempRecyclingBins.size() > 0) {
                        getBestRoute(tempRecyclingBins, null, currentScore, chargeUsed, tempCurrentSelection, binLocation,true);
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
                } else {
                    recursion = false;
                }
                List<WasteBin> tempWasteBins = wasteBins;
                List<Point> tempCurrentSelection = currentSelection;

                int tempScore = tempWasteBins.get(i).getTask().getRemaining();
                Point binLocation = tempWasteBins.get(i).getPoint();
                int tempCharge = currentPoint.distanceTo(binLocation);

                if(tempScore + currentScore < binCapacity && tempCharge + chargeUsed <= currentCharge
                        && tempCharge + chargeUsed <= timeLeft) {
                    currentScore += tempScore;
                    chargeUsed += tempCharge;

                    tempCurrentSelection.add(binLocation);
                    tempWasteBins.remove(i);

                    if (chargeUsed > 0 && (float)currentScore/chargeUsed >= highestScoreRatio) {
                        highestScoreRatio = (float)currentScore/chargeUsed;
                        pointList = tempCurrentSelection;
                        currentFocus = focus.WASTE;
                    }
                    if (tempWasteBins.size() > 0) {
                        getBestRoute(null, tempWasteBins, currentScore, chargeUsed, tempCurrentSelection, binLocation, true);
                    }
                }
            }
        }
    }

    private List<Cell> getFilledBins(List<Cell> bins) {
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

    public List<DemoLitterAgent.state> getStateList() {
        return stateList;
    }

    public List<Point> getPointList() {
        return pointList;
    }
}