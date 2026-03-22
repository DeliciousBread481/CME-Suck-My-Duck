package com.hexagram2021.cme_suck_my_duck.containers;

import com.hexagram2021.cme_suck_my_duck.utils.TraceIdGenerator;
import com.hexagram2021.cme_suck_my_duck.utils.TraceLogger;

@SuppressWarnings("unchecked")
public abstract class AbstractWrappedContainer<W> {
	private static final String[] SIGNATURE_FILTERS;

	protected final W wrapped;
	protected final String traceId;

	protected AbstractWrappedContainer(W wrapped) {
		if(wrapped instanceof AbstractWrappedContainer) {
			this.wrapped = ((AbstractWrappedContainer<W>)wrapped).wrapped;
		} else {
			this.wrapped = wrapped;
		}

		this.traceId = TraceIdGenerator.generateTraceId();
	}

	protected AbstractWrappedContainer(W wrapped, String traceId) {
		if(wrapped instanceof AbstractWrappedContainer) {
			this.wrapped = ((AbstractWrappedContainer<W>)wrapped).wrapped;
		} else {
			this.wrapped = wrapped;
		}

		this.traceId = traceId;
	}

	protected void logQuery(String signature, boolean shouldLog) {
		if(signatureMatch(signature) && shouldLog) {
			TraceLogger.debug(this.traceId, "[Query] " + signature);
		}
	}
	protected void logIteration(String signature, boolean shouldLog) {
		if(signatureMatch(signature) && shouldLog) {
			TraceLogger.info(this.traceId, "[Iteration] " + signature);
		}
	}
	protected void logModify(String signature, boolean shouldLog) {
		if(signatureMatch(signature) && shouldLog) {
			TraceLogger.info(this.traceId, "[Modify] " + signature);
		}
	}

	private static boolean signatureMatch(String signature) {
		for(String filter: SIGNATURE_FILTERS) {
			if(signature.equals(filter)) {
				return true;
			}
		}
		return SIGNATURE_FILTERS.length == 0;
	}

	static {
		String signatureFilter = System.getProperty("cme_suck_my_duck.signature_filter");
		if(signatureFilter == null) {
			SIGNATURE_FILTERS = new String[0];
		} else {
			SIGNATURE_FILTERS = signatureFilter.split(";");
		}
	}
}
