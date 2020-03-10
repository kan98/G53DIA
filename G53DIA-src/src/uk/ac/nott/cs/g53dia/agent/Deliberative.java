package uk.ac.nott.cs.g53dia.agent;

import uk.ac.nott.cs.g53dia.agent.DemoLitterAgent;
import uk.ac.nott.cs.g53dia.agent.Helpers;
import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.Point;
import uk.ac.nott.cs.g53dia.library.RecyclingBin;
import uk.ac.nott.cs.g53dia.library.WasteBin;

import java.util.ArrayList;
import java.util.List;

public class Deliberative {
    public enum focus {
        NONE,
        WASTE,
        RECYCLING
    }

    focus currentFocus = focus.NONE;

    int currentCharge, binCapacity, litterLevel;
    long timeLeft;
    List<Cell> recyclingBins, wasteBins, recyclingPlants, wastePlants, rechargePoints;

    List<RecyclingBin> recyclingBinList;
    List<WasteBin> wasteBinList;

    Cell currentCell;

    List<DemoLitterAgent.state> stateList = new ArrayList<>();
    List<Cell> pointList = new ArrayList<>();

    float highestScoreRatio = -1000;

    Helpers helpers = new Helpers();

    public void setVars(int currentCharge, int binCapacity, long timeLeft, int recyclingLevel, int wasteLevel,
                        List<Cell> recyclingBins, List<Cell> wasteBins, List<Cell> recyclingPlants,
                        List<Cell> wastePlants, List<Cell> rechargePoints, Cell currentCell) {

        this.currentCharge = currentCharge;
        this.binCapacity = binCapacity;
        this.timeLeft = timeLeft;

        this.recyclingBins = recyclingBins;
        this.wasteBins = wasteBins;
        this.recyclingPlants = recyclingPlants;
        this.wastePlants = wastePlants;
        this.rechargePoints = rechargePoints;

        this.currentCell = currentCell;

        if (wasteLevel > 0) {
            currentFocus = focus.WASTE;
        } else if (recyclingLevel > 0) {
            currentFocus = focus.RECYCLING;
        }

        this.litterLevel = recyclingLevel + wasteLevel;

        search();
    }

    private void search() {
        focus tempFocus = currentFocus;
        recyclingBinList = (List<RecyclingBin>)(List<?>) getFilledBins(recyclingBins);
        wasteBinList = (List<WasteBin>)(List<?>) getFilledBins(wasteBins);

        if(litterLevel < binCapacity) {
            List<Cell> currentSelection = new ArrayList<>();
            if (tempFocus != focus.WASTE) {
                getBestRoute(recyclingBinList, null, litterLevel, 0, currentSelection, currentCell.getPoint());
            }

            currentSelection = new ArrayList<>();
            if (tempFocus != focus.RECYCLING) {
                getBestRoute(null, wasteBinList, litterLevel, 0, currentSelection, currentCell.getPoint());
            }
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
            List<Cell> plantsList = null;
            if (currentFocus == focus.RECYCLING) {
                plantsList = recyclingPlants;
            } else if (currentFocus == focus.WASTE) {
                plantsList = wastePlants;
            }
            if (pointList.size() > 0) {
                pointList.add(helpers.findClosest(pointList.get(pointList.size() - 1).getPoint(), plantsList));
            } else {
                pointList.add(helpers.findClosest(currentCell.getPoint(), plantsList));
            }
            stateList.add(DemoLitterAgent.state.MOVE_TO_POINT);
            stateList.add(DemoLitterAgent.state.LITTER_DROP_OFF);
        }
    }

    private void getBestRoute(List<RecyclingBin> recyclingBins, List<WasteBin> wasteBins, int currentScore,
                              int chargeUsed, List<Cell> currentSelection, Point currentPoint) {

        if (currentScore < binCapacity) {
            if (recyclingBins != null && recyclingBins.size() > 0) {
                float currentBestScore = 0;
                int currentBestIndex = 0;
                int tempScore = 0;
                int tempCharge = 0;
                Cell binLocation = null;

                for (int i=0; i < recyclingBins.size(); i++) {

                    tempScore = recyclingBins.get(i).getTask().getRemaining();
                    if (tempScore > binCapacity - currentScore) {
                        tempScore = binCapacity - currentScore;
                    }
                    binLocation = recyclingBins.get(i);
                    tempCharge = currentPoint.distanceTo(binLocation.getPoint());

                    if ((float)tempScore/tempCharge > currentBestScore) {
                        currentBestScore = (float)tempScore/tempCharge;
                        currentBestIndex = i;
                    }
                }
                currentSelection.add(recyclingBins.get(currentBestIndex));
                recyclingBins.remove(currentBestIndex);
                currentScore += tempScore;
                chargeUsed += tempCharge;
                if (recyclingBins.size() > 0 && currentScore <= binCapacity) {
                    getBestRoute(recyclingBins, null, currentScore, chargeUsed, currentSelection, binLocation.getPoint());
                }
                Cell closestPlant = helpers.findClosest(binLocation.getPoint(), recyclingPlants);
                int stationCost = binLocation.getPoint().distanceTo(closestPlant.getPoint());
                if ((float)currentScore/(chargeUsed+stationCost) > highestScoreRatio && currentScore <= binCapacity) {
                    highestScoreRatio = (float)currentScore/(chargeUsed+stationCost);
                    pointList = currentSelection;
                    currentFocus = focus.RECYCLING;
                }
            }

            if (wasteBins != null && wasteBins.size() > 0) {
                float currentBestScore = 0;
                int currentBestIndex = 0;
                int tempScore = 0;
                int tempCharge = 0;
                Cell binLocation = null;
                for (int i=0; i < wasteBins.size(); i++) {

                    tempScore = wasteBins.get(i).getTask().getRemaining();
                    if (tempScore > binCapacity - currentScore) {
                        tempScore = binCapacity - currentScore;
                    }
                    binLocation = wasteBins.get(i);
                    tempCharge = currentPoint.distanceTo(binLocation.getPoint());

                    if ((float)tempScore/tempCharge > currentBestScore) {
                        currentBestScore = (float)tempScore/tempCharge;
                        currentBestIndex = i;
                    }
                }
                currentSelection.add(wasteBins.get(currentBestIndex));
                wasteBins.remove(currentBestIndex);
                currentScore += tempScore;
                chargeUsed += tempCharge;
                if (wasteBins.size() > 0 && currentScore <= binCapacity) {
                    getBestRoute(null, wasteBins, currentScore, chargeUsed, currentSelection, binLocation.getPoint());
                }
                Cell closestPlant = helpers.findClosest(binLocation.getPoint(), wastePlants);
                int stationCost = binLocation.getPoint().distanceTo(closestPlant.getPoint());
                if ((float)currentScore/(chargeUsed+stationCost) > highestScoreRatio && currentScore <= binCapacity) {
                    highestScoreRatio = (float)currentScore/(chargeUsed+stationCost);
                    pointList = currentSelection;
                    currentFocus = focus.WASTE;
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

    public List<Cell> getPointList() {
        return pointList;
    }

    public focus getCurrentFocus() {
        return currentFocus;
    }
}