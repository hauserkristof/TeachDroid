/**
 * 
 */
package com.keba.kemro.kvs.teach.util;

import java.util.Vector;

import com.keba.kemro.kvs.teach.data.rc.KvtRcAdministrator;
import com.keba.kemro.teach.dfl.KTcDfl;
import com.keba.kemro.teach.dfl.structural.KMultikinematicListener;
import com.keba.kemro.teach.dfl.value.KStructVarWrapper;
import com.keba.kemro.teach.dfl.value.KVariableGroup;
import com.keba.kemro.teach.dfl.value.KVariableGroupListener;

/**
 * Class that provides information about the currently selected main mode of
 * operation (Automatic, manual, etc.) as well as the currently selected
 * reference system, tool, and the state of the safety subsystem.
 * 
 * @author ltz
 * 
 */
public class KvtMainModeAdministrator implements KMultikinematicListener, KVariableGroupListener, KvtTeachviewConnectionListener {

	private final Object						m_dlfLock			= new Object();
	private int									m_actualMainMode	= -1;
	private int									mSafetyState		= -1;

	private static KVariableGroup				mVarGroup;
	private static KStructVarWrapper			mMainModeVar;
	private static boolean						changed				= false;

	private static KStructVarWrapper			mSafetyStateVar;
	private static KvtMainModeAdministrator		instance;
	private static Vector<KvtMainModeListener>	m_listeners;

	public static enum SafetyState {
		eIconSafetyOK, eIconSafetyNOK, // obsolete (deprecated use detailed info
										// icons instead)
		eIconSafetyUnknown, eIconSafety2EStopInt, eIconSafety2EStopExt, eIconSafety2EStopBoth, eIconSafetyEStop, eIconSafetySafetyDoor, eIconSafetyEnableSwitch, eIconSafetyPowerRelease, eSafetyUnknown;
		private static SafetyState[]	allValues	= values();

		public static SafetyState fromOrdinal(int n) {
			if (n < 0)
				return SafetyState.eSafetyUnknown;
			return allValues[n];
		}
	};

	protected KvtMainModeAdministrator() {
		KvtSystemCommunicator.addConnectionListener(this);
		m_listeners = new Vector<KvtMainModeListener>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.keba.kemro.kvs.teach.util.KvtTeachviewConnectionListener#
	 * teachviewConnected()
	 */
	@Override
	public void teachviewConnected() {
		synchronized (m_dlfLock) {
			KTcDfl dfl = KvtSystemCommunicator.getTcDfl();
			if (dfl != null) {
				synchronized (dfl.getLockObject()) {
					mVarGroup = dfl.variable.createVariableGroup("KvtMainModeAdministrator");
					mVarGroup.addListener(this);
					mVarGroup.setPollInterval(250);
					dfl.structure.addMultikinematikListener(this);
				}
			}
			createVariable();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.keba.kemro.kvs.teach.util.KvtTeachviewConnectionListener#
	 * teachviewDisconnected()
	 */
	@Override
	public void teachviewDisconnected() {
		KTcDfl dfl = KvtSystemCommunicator.getTcDfl();
		if (dfl != null) {
			dfl.structure.removeMultikinematicListener(this);
			if (mVarGroup != null) {
				mVarGroup.release();
			}
			mVarGroup = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.keba.kemro.teach.dfl.value.KVariableGroupListener#changed(com.keba
	 * .kemro.teach.dfl.value.KStructVarWrapper)
	 */
	@Override
	public void changed(KStructVarWrapper _variable) {
		if (_variable.equals(mMainModeVar)) {
			Number mode = (Number) _variable.readActualValue(null);
			if (mode != null && mode.intValue() != m_actualMainMode) {
				m_actualMainMode = mode.intValue();
				for (KvtMainModeListener l : m_listeners) {
					l.mainModeChanged(m_actualMainMode);
				}
			}
		} else if (_variable.equals(mSafetyStateVar)) {
			Object v = _variable.readActualValue(null);
			if (v != null && v instanceof Number && ((Number) v).intValue() != mSafetyState) {
				mSafetyState = ((Number) v).intValue();
				for (KvtMainModeListener l : m_listeners) {
					l.safetyStateChanged(SafetyState.fromOrdinal(mSafetyState));
				}
			}
		}

	}

	public static SafetyState getSafetyState() {
		return SafetyState.fromOrdinal(instance.mSafetyState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.keba.kemro.teach.dfl.value.KVariableGroupListener#allActualValuesUpdated
	 * ()
	 */
	@Override
	public void allActualValuesUpdated() {
		changed(mMainModeVar);
		changed(mSafetyStateVar);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.keba.kemro.teach.dfl.structural.KMultikinematicListener#kinematikChanged
	 * ()
	 */
	@Override
	public void kinematikChanged() {
		createVariable();
	}

	/**
	 * 
	 */
	private synchronized void createVariable() {
		if (mVarGroup != null) {
			mVarGroup.release();
			KTcDfl dfl = KvtSystemCommunicator.getTcDfl();
			if (dfl != null) {
				int kin = KvtMultiKinematikAdministrator.getKinematicIndex();
				if (kin >= 0) {
					mMainModeVar = dfl.variable.createKStructVarWrapper(KvtRcAdministrator.RCDATA_PREFIX + "gRcData.selectedMainMode");

					if (mMainModeVar == null) { // try the gRcData-path
						mMainModeVar = dfl.variable.createKStructVarWrapper(KvtRcAdministrator.RCDATA_PREFIX + "gRcDataRobot[" + kin
								+ "].selectedMainMode");
					}

					mSafetyStateVar = dfl.variable.createKStructVarWrapper(KvtRcAdministrator.RCDATA_PREFIX + "gRcData.userIcon[4]");

					if (mMainModeVar != null) {
						mVarGroup.add(mMainModeVar);
					}

					if (mSafetyStateVar != null) {
						mVarGroup.add(mSafetyStateVar);
					}

					mVarGroup.addListener(this);
					mVarGroup.activate();
					Object mm = mMainModeVar.readActualValue(null);

				}
			}
		}
	}

	/**
	 * adds a listener to the administrator which is notified, whenever the main
	 * mode changes
	 * 
	 * @param _l
	 *            the new listener
	 * @return true if the listener could be added, false if it already was
	 *         registered
	 */
	public static boolean addListener(KvtMainModeListener _l) {
		if (m_listeners == null)
			m_listeners = new Vector<KvtMainModeListener>();

		if (!m_listeners.contains(_l)) {
			m_listeners.addElement(_l);
			return true;
		}
		return false;

	}

	public static int getMainMode() {
		if (instance != null)
			return instance.m_actualMainMode;
		return -1;
	}

	public static KvtMainModeAdministrator init() {
		if (instance == null) {
			instance = new KvtMainModeAdministrator();
		}

		return instance;
	}

	public static interface KvtMainModeListener {
		void mainModeChanged(int _newMainMode);

		/**
		 * Called when the safety state of the robot has changed (i.e. a safety
		 * door violation is present)
		 * 
		 * @param _state
		 *            The new {@link SafetyState}.
		 */
		void safetyStateChanged(SafetyState _state);
	}

	/**
	 * @param _l
	 */
	public static boolean removeListener(KvtMainModeListener _l) {
		if (m_listeners.contains(_l)) {
			m_listeners.removeElement(_l);
			return true;
		}
		return false;
	}
}
