package com.indra.sofia2.ssap.kp.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.indra.sofia2.ssap.kp.KpToExtend;
import com.indra.sofia2.ssap.kp.logging.LogMessages;
import com.indra.sofia2.ssap.kp.utils.functions.Supplier;
import com.indra.sofia2.ssap.ssap.SSAPMessage;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyReturnMessage;

public class KpReporter {
	
	private static Logger log = Logger.getLogger(KpReporter.class);
	private int period = 5000;
	
	private final KpToExtend kp;

	private ScheduledExecutorService executor;
	private Supplier<SSAPMessage> supplier = null;
	
	private ScheduledFuture<?> futureTask;
	private Runnable runnable;
	
	public KpReporter(final KpToExtend kp, int period, Supplier<SSAPMessage> supplier) {
		this.kp = kp;
		this.period = period;
		this.supplier = supplier;
	}
	
	public synchronized KpReporter startReporting() {
		runnable = new Runnable() {
			@Override
			public void run() {
				if(kp.isPhysicalConnectionEstablished()) {
					send();
				}			
			}
		};
		executor = Executors.newScheduledThreadPool(1);
		futureTask = executor.scheduleWithFixedDelay(runnable, period, period, TimeUnit.MILLISECONDS);
		return this;
	}
	
	public synchronized KpReporter stopReporting() {
		if(!executor.isShutdown())
			executor.shutdownNow();
		
		return this;
	}
	
	//TODO: LOG + tratamiento de excepciones
	private synchronized void send() {
		try {
			SSAPMessage message = supplier.get();
			System.out.println(message.toJson());
			log.info(String.format(LogMessages.LOG_REQUEST_DATA, message.toJson()));
			SSAPMessage responseStatus = kp.send(message);
			
			SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseStatus.getBody());
			if(!returned.isOk())
				log.error(String.format(LogMessages.LOG_RESPONSE_ERROR, returned.toJson()));
			}
		catch (Exception e) {
			log.error(String.format("Error sending report %s", e.getMessage()));
		}
	}
	
	public int getPeriod() {
		return period;
	}

	public synchronized KpReporter setPeriod(int period) {
		this.period = period;
		if( futureTask!= null && !executor.isShutdown() ) {
			futureTask.cancel(true);
			futureTask = executor.scheduleWithFixedDelay(runnable, period, period, TimeUnit.MILLISECONDS);
		}
		return this;
	}
	
}