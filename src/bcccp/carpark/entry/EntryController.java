//File : EntryController.java
//Student Id : 11620657
//Student Name : Abdul moiz.Shaik
//Course Name : ITC 515 Professional Programming Principal
//Assessment Item : Assignment 2
//Instructor Name : Recep Ulusoy
//Date : 21 August 2017
//Due on : 25 August 2017


package bcccp.carpark.entry;

import bcccp.carpark.Carpark;
import bcccp.carpark.ICarSensor;
import bcccp.carpark.ICarSensorResponder;
import bcccp.carpark.ICarpark;
import bcccp.carpark.ICarparkObserver;
import bcccp.carpark.IGate;
import bcccp.tickets.adhoc.IAdhocTicket;

/**
 * This file used to Enter the car from the car park. This file has setState method to set the state of the car park, 
 * log method used to create the log file, ticketInserted method, ticketTaken method, carEventDetected method, 
 * buttonPushed method, notifyCarparkEvent method used when the car exit,
 * change the state accordingly.
 * 
 * @author 11620657
 *
 */
public class EntryController 
		implements ICarSensorResponder,
				   ICarparkObserver,
		           IEntryController {
	
	private IGate entryGate;
	private ICarSensor outsideSensor; 
	private ICarSensor insideSensor;
	private IEntryUI ui;
	
	private ICarpark carpark;
	private IAdhocTicket  adhocTicket = null;
	private long entryTime;
	private String seasonTicketId = null;

	private enum STATE {IDLE, BLOCKED, EXITED, REJECTED, WAITING, EXITING, PROCESSED, TAKEN, ISSUED, VALIDATED, FULL}
	private STATE state;
	private STATE initState;

	/**
	 * Create EntryController constructor with five parameters.
	 * 
	 * @param carpark
	 * @param entryGate
	 * @param os
	 * @param is
	 * @param ui
	 */
	public EntryController(Carpark carpark, IGate entryGate, 
			ICarSensor os, 
			ICarSensor is,
			IEntryUI ui) {
		this.carpark = carpark;
		this.entryGate = entryGate;
		this.outsideSensor = os;
		this.insideSensor = is;
		this.ui = ui;
		
	}


	/**
	 * This method used to set status when 
	 * button push for the car park entry 
	 */
	@Override
	public void buttonPushed() {
		if (state.equals(STATE.WAITING)) {
			if (!carpark.isFull()) {
				adhocTicket = carpark.issueAdhocTicket();

				int ticketNo = adhocTicket.getTicketNo();
				String carParkId = adhocTicket.getCarparkId();
				entryTime = System.currentTimeMillis();
				String barCode = adhocTicket.getBarcode();
				ui.printTicket(carParkId, ticketNo, entryTime, barCode);
				setState(STATE.ISSUED);
			} else {
				setState(STATE.FULL);
			}
		} else {
			ui.beep();
		}
	}


	/**
	 * This method is override method and used to set the status of the car park
	 * when ticket inserted.
	 */
	@Override
	public void ticketInserted(String barcode) {
		if (state.equals(STATE.WAITING)) {
			if (carpark.isSeasonTicketValid(barcode) && !carpark.isSeasonTicketInUse(barcode)) {
				this.seasonTicketId = barcode;
				setState(STATE.VALIDATED);
			}
		} else {
			ui.beep();
		}
	}


	/**
	 * This method is override method and used to set the status of the car park
	 * when ticket taken.
	 */
	@Override
	public void ticketTaken() {
		if (state.equals(STATE.ISSUED) || state.equals(STATE.VALIDATED)) {
			setState(STATE.TAKEN);
		} else {
			ui.beep();
		}
	}


	/**
	 * This method used to notify the car park event to the user
	 */
	@Override
	public void notifyCarparkEvent() {
		if (state.equals(STATE.FULL)) {
			if (!carpark.isFull()) {
				setState(STATE.WAITING);
			}
		}
	}

	/**
	 * This method used to create the log file
	 * 
	 * @param message
	 */
	private void log(String message) {
		System.out.println("Exit Controller : " + message);
	}

	/**
	 * This method used to set the state
	 * 
	 * @param idle
	 */
	private void setState(STATE newState) {
		switch (newState) {
		case BLOCKED:
			log("set State : BLOCKED");
			state = STATE.BLOCKED;
			ui.display("BLOCKED");
			break;

		case IDLE:
			log("set State : IDLE");
			state = STATE.IDLE;
			ui.display("IDLE");
			break;
		default:
			break;
			// In-progress to do the other status
			// Need to do EXITED, REJECTED, WAITING, EXITING, PROCESSED, TAKEN, ISSUED, VALIDATED, FULL
			// status as like above
		}
	}
	// This method used to do the car park event
	@Override
	public void carEventDetected(String detectorId, boolean detected) {
		log("Car Event Detected: " + detectorId + "Car Detected" + detected);
		switch (state) {
		case BLOCKED:
			if (detectorId.equals(insideSensor.getId()) && !detected) {
				setState(initState);
			}
			break;
		case IDLE:
			if (detectorId.equals(insideSensor.getId()) && detected) {
				setState(STATE.WAITING);
			} else if (detectorId.equals(outsideSensor.getId()) && detected) {
				setState(STATE.BLOCKED);
			}
		case EXITED:
			if (detectorId.equals(insideSensor.getId()) && detected) {
				setState(STATE.EXITING);
			} else if (detectorId.equals(outsideSensor.getId()) && detected) {
				setState(STATE.IDLE);
			}
			// In-progress to do the further development
		default:
			break;
		}
	}

}
