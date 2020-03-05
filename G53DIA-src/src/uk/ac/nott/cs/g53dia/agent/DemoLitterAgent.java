package uk.ac.nott.cs.g53dia.agent;

import uk.ac.nott.cs.g53dia.agent.deliberative.Deliberative;
import uk.ac.nott.cs.g53dia.library.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple example LitterAgent
 * 
 * @author Julian Zappala
 */
/*
 * Copyright (c) 2011 Julian Zappala
 * 
 * See the file "license.terms" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
public class DemoLitterAgent extends LitterAgent {

	public DemoLitterAgent() {
		this(new Random());
	}

	/**
	 * The tanker implementation makes random moves. For reproducibility, it
	 * can share the same random number generator as the environment.
	 * 
	 * @param r
	 *            The random number generator.
	 */
	public DemoLitterAgent(Random r) {
		this.r = r;
	}

	Helpers helpers = new Helpers();
	Reactive reactive = new Reactive();
	Deliberative deliberative = new Deliberative();

	protected state currentState = state.FORAGE;

	public enum state {
		FORAGE,
		MOVE_TO_POINT,
		PICKUP_WASTE,
		PICKUP_RECYCLING,
		LITTER_DROP_OFF,
		REFUEL
	}

	private List<Point> points = new ArrayList<>();
	private List<state> stateList = new ArrayList<>();

	protected List<Cell> recyclingBins = new ArrayList<>();
	protected List<Cell> wasteBins = new ArrayList<>();
	protected List<Cell> recyclingStations = new ArrayList<>();
	protected List<Cell> wasteStations = new ArrayList<>();
	protected List<Cell> rechargePoints = new ArrayList<>();

	private void storeMapInfo(Cell[][] view) {
		for (int i=0; i != view.length; i++) {
			for (int j=0; j != view[i].length; j++) {
				Cell currentView = view[i][j];
				if(currentView instanceof RecyclingBin && !recyclingBins.contains(currentView)) {
					recyclingBins.add(currentView);
				} else if(currentView instanceof WasteBin && !wasteBins.contains(currentView)) {
					wasteBins.add(currentView);
				} else if(currentView instanceof RecyclingStation && !recyclingStations.contains(currentView)) {
					recyclingStations.add(currentView);
				} else if(currentView instanceof WasteStation && !wasteStations.contains(currentView)) {
					wasteStations.add(currentView);
				} else if(currentView instanceof RechargePoint && !rechargePoints.contains(currentView)) {
					rechargePoints.add(currentView);
				}
			}
		}
	}

	private state getNextState(Cell[][] view) {
		boolean isEqualPosition = false;
		Point currentPoint = getCurrentCell(view).getPoint();
		if (currentState == state.MOVE_TO_POINT) {
			isEqualPosition = getCurrentCell(view).getPoint().equals(points.get(0));
			if (isEqualPosition) {
				points.remove(0);
			}
		}
		if (currentState != state.MOVE_TO_POINT || isEqualPosition) {
			if (stateList.isEmpty()) {
				return state.FORAGE;
			} else {
				state tempState = stateList.get(0);
				stateList.remove(0);
				return tempState;
			}
		} else {
			return state.MOVE_TO_POINT;
		}
	}

	/*
	 * The following is a simple demonstration of how to write a tanker. The
	 * code below is very stupid and simply moves the tanker randomly until the
	 * charge agt is half full, at which point it returns to a charge pump.
	 */
	public Action senseAndAct(Cell[][] view, long timestep) {
		storeMapInfo(view);

		if (stateList.isEmpty()) {
			deliberative = new Deliberative();
			deliberative.setVars(getChargeLevel(), MAX_LITTER - getLitterLevel(), 10000 - timestep,
					getRecyclingLevel(), getWasteLevel(), recyclingBins, wasteBins, recyclingStations,
					wasteStations, rechargePoints, getCurrentCell(view), currentState);

			if (deliberative.getStateList().size() > 0) {
				points = deliberative.getPointList();
				stateList = deliberative.getStateList();
			}
		}

//		Point closestStation = reactive.reactiveStationStop(getCurrentCell(view),
//				getWasteLevel(), getRecyclingLevel(), wasteStations, recyclingStations);
//		if (closestStation != null && !getCurrentCell(view).getPoint().equals(closestStation)) {
//			stateList = new ArrayList<>();
//			points = new ArrayList<>();
//
//			points.add(0, closestStation);
//			stateList.add(0, state.LITTER_DROP_OFF);
//			currentState = state.MOVE_TO_POINT;
//		}

		Point closestRechargePoint = reactive.reactiveRefuel(getCurrentCell(view), getChargeLevel(), rechargePoints);
		if(closestRechargePoint != null) {
			if (currentState != state.REFUEL && !stateList.contains(state.REFUEL)) {
				stateList.add(0, currentState);

				points.add(0, closestRechargePoint);
				stateList.add(0, state.REFUEL);
				if(!stateList.contains(state.REFUEL)) {
					stateList.add(0, state.MOVE_TO_POINT);
				}
			}
		}

		currentState = getNextState(view);
//		if (points.size() > 0) {
//			System.out.println(points);
//			System.out.println(stateList);
//		}

		switch (currentState) {
			case REFUEL:
				return new RechargeAction();
			case LITTER_DROP_OFF:
				return new DisposeAction();
			case PICKUP_RECYCLING:
				RecyclingBin recyclingBin = (RecyclingBin) getCurrentCell(view);
				return new LoadAction(recyclingBin.getTask());
			case PICKUP_WASTE:
				WasteBin wasteBin = (WasteBin) getCurrentCell(view);
				return new LoadAction(wasteBin.getTask());
			case MOVE_TO_POINT:
				return new MoveTowardsAction(points.get(0));
			case FORAGE:
			default:
//				return new MoveAction(r.nextInt(8));
				return new MoveAction(4);
		}
	}
}
