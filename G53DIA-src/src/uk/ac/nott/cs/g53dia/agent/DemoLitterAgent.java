package uk.ac.nott.cs.g53dia.agent;

import uk.ac.nott.cs.g53dia.library.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the main agent class which extends the LitterAgent class.
 *
 * The class uses the other classes in the agent package to output appropriate actions for the agent.
 *
 */
public class DemoLitterAgent extends LitterAgent {

	public DemoLitterAgent(Random r) {
		this.r = r;
	}

	Reactive reactive = new Reactive();
	Deliberative deliberative = new Deliberative();

	protected state currentState = state.FORAGE;

	protected enum state {
		FORAGE,
		MOVE_TO_POINT,
		PICKUP_WASTE,
		PICKUP_RECYCLING,
		LITTER_DROP_OFF,
		REFUEL
	}

	private List<Cell> cellPoints = new ArrayList<>();
	private List<state> stateList = new ArrayList<>();

	protected List<Cell> recyclingBins = new ArrayList<>();
	protected List<Cell> wasteBins = new ArrayList<>();
	protected List<Cell> recyclingStations = new ArrayList<>();
	protected List<Cell> wasteStations = new ArrayList<>();
	protected List<Cell> rechargePoints = new ArrayList<>();

	/**
	 * This method is called at every timestep.
	 * It stores new bins, stations and recharge points it doesn't have saved into the relevent lists.
	 *
	 * @param view The 30x30 view scope of cells the agent can see.
	 *
	 */
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

	/**
	 * This method checks if the agent has completed the current action.
	 * If the action is completed, the next state on the stateList will be set as the currentState.
	 *
	 * @param view The 30x30 view scope of cells the agent can see.
	 * @return The new currentState the agent is now on.
	 *
	 */
	private state getNextState(Cell[][] view) {
		boolean isEqualPosition = false;
		if (currentState == state.MOVE_TO_POINT) {
			if (stateList.get(0) == state.MOVE_TO_POINT) {
				stateList.remove(0);
			}
			isEqualPosition = getCurrentCell(view).getPoint().equals(cellPoints.get(0).getPoint());
			if (isEqualPosition) {
				cellPoints.remove(0);
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

	/**
	 * This method is called by the simulator or evaluator in each timestep.
	 * This method uses every other class and method in the agent packageto figure out an appropriate action output.
	 *
	 * @param view The 30x30 view scope of cells the agent can see.
	 * @param timestep The timestep the run is currently on.
	 * @return The action the agent should complete next.
	 *
	 */
	public Action senseAndAct(Cell[][] view, long timestep) {
		storeMapInfo(view);

		boolean isEqualPosition = false;
		if (cellPoints.size() > 0) {
			isEqualPosition = getCurrentCell(view).getPoint().equals(cellPoints.get(0).getPoint());
		}
		if (stateList.isEmpty() || (currentState == state.MOVE_TO_POINT && (stateList.get(0) == state.PICKUP_RECYCLING
				|| stateList.get(0) == state.PICKUP_WASTE) && !isEqualPosition)) {
			deliberative = new Deliberative();

			deliberative.setVars(getChargeLevel(), MAX_LITTER - getLitterLevel(), 10000 - timestep,
					getRecyclingLevel(), getWasteLevel(), recyclingBins, wasteBins, recyclingStations,
					wasteStations, getCurrentCell(view));

			if (deliberative.getStateList().size() > 0) {
				if (currentState == state.MOVE_TO_POINT) {
					currentState = state.FORAGE;
				}
				cellPoints = deliberative.getPointList();
				stateList = deliberative.getStateList();
			}
		}

		Cell closestRechargePoint = reactive.reactiveRefuel(getCurrentCell(view), getChargeLevel(), rechargePoints);
		if(closestRechargePoint != null) {
			if (currentState != state.REFUEL && !stateList.contains(state.REFUEL)) {
				stateList.add(0, currentState);
				
				cellPoints.add(0, closestRechargePoint);
				stateList.add(0, state.REFUEL);
				stateList.add(0, state.MOVE_TO_POINT);
			}
		}

		currentState = getNextState(view);

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
				return new MoveTowardsAction(cellPoints.get(0).getPoint());
			case FORAGE:
			default:
				return new MoveAction(5);
		}
	}
}
