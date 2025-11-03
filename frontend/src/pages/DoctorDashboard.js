import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import {
  patientAPI,
  practitionerAPI,
  encounterAPI,
  observationAPI,
  conditionAPI,
  messageAPI,
} from '../services/api';
import Navbar from '../components/Navbar';

const DoctorDashboard = () => {
  const { user } = useAuth();
  const [practitioner, setPractitioner] = useState(null);
  const [patients, setPatients] = useState([]);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [patientData, setPatientData] = useState({
    encounters: [],
    observations: [],
    conditions: [],
  });
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showEncounterForm, setShowEncounterForm] = useState(false);
  const [showObservationForm, setShowObservationForm] = useState(false);
  const [showConditionForm, setShowConditionForm] = useState(false);
  const [showPatientEditForm, setShowPatientEditForm] = useState(false);

  const [editPatientData, setEditPatientData] = useState({
    bloodType: '',
    allergies: '',
    medications: '',
    address: '',
    phoneNumber: '',
  });

  const [newEncounter, setNewEncounter] = useState({
    encounterType: '',
    reasonForVisit: '',
    notes: '',
  });

  const [newObservation, setNewObservation] = useState({
    observationType: '',
    value: '',
    unit: '',
    notes: '',
  });

  const [newCondition, setNewCondition] = useState({
    diagnosis: '',
    severity: '',
    status: '',
    notes: '',
  });

  useEffect(() => {
    fetchDoctorData();
  }, [user]);

  const fetchDoctorData = async () => {
    try {
      const practitionerRes = await practitionerAPI.getByUserId(user.userId);
      setPractitioner(practitionerRes.data);

      const patientsRes = await patientAPI.getAll();
      setPatients(patientsRes.data);

      const messagesRes = await messageAPI.getByUserId(user.userId);
      setMessages(messagesRes.data);

      setLoading(false);
    } catch (error) {
      console.error('Error fetching doctor data:', error);
      setLoading(false);
    }
  };

  const fetchPatientData = async (patientId) => {
    try {
      const encountersRes = await encounterAPI.getByPatientId(patientId);
      const observationsRes = await observationAPI.getByPatientId(patientId);
      const conditionsRes = await conditionAPI.getByPatientId(patientId);

      setPatientData({
        encounters: encountersRes.data,
        observations: observationsRes.data,
        conditions: conditionsRes.data,
      });
    } catch (error) {
      console.error('Error fetching patient data:', error);
    }
  };

  const handleSelectPatient = (patient) => {
    setSelectedPatient(patient);
    setEditPatientData({
      bloodType: patient.bloodType || '',
      allergies: patient.allergies || '',
      medications: patient.medications || '',
      address: patient.address || '',
      phoneNumber: patient.phoneNumber || '',
    });
    fetchPatientData(patient.id);
  };

  const handleUpdatePatient = async (e) => {
    e.preventDefault();
    try {
      await patientAPI.update(selectedPatient.id, editPatientData);
      setShowPatientEditForm(false);
      // Refresh patient list
      const patientsRes = await patientAPI.getAll();
      setPatients(patientsRes.data);
      // Update selected patient
      const updatedPatient = patientsRes.data.find(p => p.id === selectedPatient.id);
      setSelectedPatient(updatedPatient);
    } catch (error) {
      console.error('Error updating patient:', error);
    }
  };

  const handleCreateEncounter = async (e) => {
    e.preventDefault();
    try {
      await encounterAPI.create({
        patient: { id: selectedPatient.id },
        practitioner: { id: practitioner.id },
        encounterDate: new Date().toISOString(),
        encounterType: newEncounter.encounterType,
        reasonForVisit: newEncounter.reasonForVisit,
        notes: newEncounter.notes,
        status: 'Completed',
      });
      setShowEncounterForm(false);
      setNewEncounter({ encounterType: '', reasonForVisit: '', notes: '' });
      fetchPatientData(selectedPatient.id);
    } catch (error) {
      console.error('Error creating encounter:', error);
    }
  };

  const handleCreateObservation = async (e) => {
    e.preventDefault();
    try {
      await observationAPI.create({
        patient: { id: selectedPatient.id },
        practitioner: { id: practitioner.id },
        observationType: newObservation.observationType,
        value: newObservation.value,
        unit: newObservation.unit,
        notes: newObservation.notes,
        observationDate: new Date().toISOString(),
      });
      setShowObservationForm(false);
      setNewObservation({ observationType: '', value: '', unit: '', notes: '' });
      fetchPatientData(selectedPatient.id);
    } catch (error) {
      console.error('Error creating observation:', error);
    }
  };

  const handleCreateCondition = async (e) => {
    e.preventDefault();
    try {
      await conditionAPI.create({
        patient: { id: selectedPatient.id },
        practitioner: { id: practitioner.id },
        diagnosis: newCondition.diagnosis,
        severity: newCondition.severity,
        status: newCondition.status,
        notes: newCondition.notes,
        diagnosisDate: new Date().toISOString(),
      });
      setShowConditionForm(false);
      setNewCondition({ diagnosis: '', severity: '', status: '', notes: '' });
      fetchPatientData(selectedPatient.id);
    } catch (error) {
      console.error('Error creating condition:', error);
    }
  };

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="loading">Loading...</div>
      </>
    );
  }

  return (
    <>
      <Navbar />
      <div className="container">
        <h2>Doctor Dashboard</h2>

        <div className="dashboard">
          <div className="card">
            <h3>Patients List</h3>
            {patients.length === 0 ? (
              <p>No patients found.</p>
            ) : (
              <ul className="message-list">
                {patients.map((patient) => (
                  <li
                    key={patient.id}
                    className="message-item"
                    onClick={() => handleSelectPatient(patient)}
                    style={{ cursor: 'pointer' }}
                  >
                    <div className="message-subject">
                      {patient.user?.firstName} {patient.user?.lastName}
                    </div>
                    <div className="message-sender">
                      Personal Number: {patient.personalNumber}
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="card">
            <h3>Messages</h3>
            {messages.length === 0 ? (
              <p>No messages.</p>
            ) : (
              <ul className="message-list">
                {messages.slice(0, 5).map((msg) => (
                  <li
                    key={msg.id}
                    className={`message-item ${!msg.isRead ? 'unread' : ''}`}
                  >
                    <div className="message-subject">{msg.subject}</div>
                    <div className="message-sender">From: {msg.sender.username}</div>
                    <div className="message-date">
                      {new Date(msg.sentAt).toLocaleString()}
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

        {selectedPatient && (
          <>
            <div className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h3>
                  Patient: {selectedPatient.user?.firstName} {selectedPatient.user?.lastName}
                </h3>
                <button
                  className="btn btn-primary"
                  onClick={() => setShowPatientEditForm(!showPatientEditForm)}
                >
                  {showPatientEditForm ? 'Cancel' : 'Edit Patient Info'}
                </button>
              </div>

              {showPatientEditForm ? (
                <form onSubmit={handleUpdatePatient} style={{ marginTop: '1rem' }}>
                  <div className="form-group">
                    <label>Address</label>
                    <input
                      type="text"
                      value={editPatientData.address}
                      onChange={(e) =>
                        setEditPatientData({ ...editPatientData, address: e.target.value })
                      }
                    />
                  </div>
                  <div className="form-group">
                    <label>Phone Number</label>
                    <input
                      type="text"
                      value={editPatientData.phoneNumber}
                      onChange={(e) =>
                        setEditPatientData({ ...editPatientData, phoneNumber: e.target.value })
                      }
                    />
                  </div>
                  <div className="form-group">
                    <label>Blood Type</label>
                    <input
                      type="text"
                      value={editPatientData.bloodType}
                      onChange={(e) =>
                        setEditPatientData({ ...editPatientData, bloodType: e.target.value })
                      }
                      placeholder="e.g., A+, O-, AB+"
                    />
                  </div>
                  <div className="form-group">
                    <label>Allergies</label>
                    <textarea
                      value={editPatientData.allergies}
                      onChange={(e) =>
                        setEditPatientData({ ...editPatientData, allergies: e.target.value })
                      }
                      placeholder="List any allergies..."
                    />
                  </div>
                  <div className="form-group">
                    <label>Medications</label>
                    <textarea
                      value={editPatientData.medications}
                      onChange={(e) =>
                        setEditPatientData({ ...editPatientData, medications: e.target.value })
                      }
                      placeholder="List current medications..."
                    />
                  </div>
                  <button type="submit" className="btn btn-success">
                    Save Changes
                  </button>
                </form>
              ) : (
                <div style={{ marginTop: '1rem' }}>
                  <p><strong>Personal Number:</strong> {selectedPatient.personalNumber}</p>
                  <p><strong>Date of Birth:</strong> {selectedPatient.dateOfBirth}</p>
                  <p><strong>Address:</strong> {selectedPatient.address || 'N/A'}</p>
                  <p><strong>Phone:</strong> {selectedPatient.phoneNumber || 'N/A'}</p>
                  <p><strong>Blood Type:</strong> {selectedPatient.bloodType || 'N/A'}</p>
                  <p><strong>Allergies:</strong> {selectedPatient.allergies || 'None'}</p>
                  <p><strong>Medications:</strong> {selectedPatient.medications || 'None'}</p>
                </div>
              )}
            </div>

            <div className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h3>Diagnoses</h3>
                <button
                  className="btn btn-success"
                  onClick={() => setShowConditionForm(!showConditionForm)}
                >
                  {showConditionForm ? 'Cancel' : 'Add Diagnosis'}
                </button>
              </div>

              {showConditionForm && (
                <form onSubmit={handleCreateCondition} style={{ marginTop: '1rem' }}>
                  <div className="form-group">
                    <label>Diagnosis</label>
                    <input
                      type="text"
                      value={newCondition.diagnosis}
                      onChange={(e) =>
                        setNewCondition({ ...newCondition, diagnosis: e.target.value })
                      }
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Severity</label>
                    <select
                      value={newCondition.severity}
                      onChange={(e) =>
                        setNewCondition({ ...newCondition, severity: e.target.value })
                      }
                      required
                    >
                      <option value="">Select severity</option>
                      <option value="Mild">Mild</option>
                      <option value="Moderate">Moderate</option>
                      <option value="Severe">Severe</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Status</label>
                    <input
                      type="text"
                      value={newCondition.status}
                      onChange={(e) =>
                        setNewCondition({ ...newCondition, status: e.target.value })
                      }
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Notes</label>
                    <textarea
                      value={newCondition.notes}
                      onChange={(e) =>
                        setNewCondition({ ...newCondition, notes: e.target.value })
                      }
                    />
                  </div>
                  <button type="submit" className="btn btn-success">
                    Add Diagnosis
                  </button>
                </form>
              )}

              <div style={{ marginTop: '1rem' }}>
                {patientData.conditions.length === 0 ? (
                  <p>No diagnoses recorded.</p>
                ) : (
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Diagnosis</th>
                        <th>Severity</th>
                        <th>Status</th>
                        <th>Date</th>
                      </tr>
                    </thead>
                    <tbody>
                      {patientData.conditions.map((condition) => (
                        <tr key={condition.id}>
                          <td>{condition.diagnosis}</td>
                          <td>{condition.severity}</td>
                          <td>{condition.status}</td>
                          <td>{new Date(condition.diagnosisDate).toLocaleDateString()}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>

            <div className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h3>Observations</h3>
                <button
                  className="btn btn-success"
                  onClick={() => setShowObservationForm(!showObservationForm)}
                >
                  {showObservationForm ? 'Cancel' : 'Add Observation'}
                </button>
              </div>

              {showObservationForm && (
                <form onSubmit={handleCreateObservation} style={{ marginTop: '1rem' }}>
                  <div className="form-group">
                    <label>Type</label>
                    <input
                      type="text"
                      value={newObservation.observationType}
                      onChange={(e) =>
                        setNewObservation({ ...newObservation, observationType: e.target.value })
                      }
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Value</label>
                    <input
                      type="text"
                      value={newObservation.value}
                      onChange={(e) =>
                        setNewObservation({ ...newObservation, value: e.target.value })
                      }
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Unit</label>
                    <input
                      type="text"
                      value={newObservation.unit}
                      onChange={(e) =>
                        setNewObservation({ ...newObservation, unit: e.target.value })
                      }
                    />
                  </div>
                  <div className="form-group">
                    <label>Notes</label>
                    <textarea
                      value={newObservation.notes}
                      onChange={(e) =>
                        setNewObservation({ ...newObservation, notes: e.target.value })
                      }
                    />
                  </div>
                  <button type="submit" className="btn btn-success">
                    Add Observation
                  </button>
                </form>
              )}

              <div style={{ marginTop: '1rem' }}>
                {patientData.observations.length === 0 ? (
                  <p>No observations recorded.</p>
                ) : (
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Type</th>
                        <th>Value</th>
                        <th>Unit</th>
                        <th>Date</th>
                      </tr>
                    </thead>
                    <tbody>
                      {patientData.observations.map((obs) => (
                        <tr key={obs.id}>
                          <td>{obs.observationType}</td>
                          <td>{obs.value}</td>
                          <td>{obs.unit}</td>
                          <td>{new Date(obs.observationDate).toLocaleDateString()}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>

            <div className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h3>Encounters</h3>
                <button
                  className="btn btn-success"
                  onClick={() => setShowEncounterForm(!showEncounterForm)}
                >
                  {showEncounterForm ? 'Cancel' : 'Add Encounter'}
                </button>
              </div>

              {showEncounterForm && (
                <form onSubmit={handleCreateEncounter} style={{ marginTop: '1rem' }}>
                  <div className="form-group">
                    <label>Type</label>
                    <input
                      type="text"
                      value={newEncounter.encounterType}
                      onChange={(e) =>
                        setNewEncounter({ ...newEncounter, encounterType: e.target.value })
                      }
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Reason for Visit</label>
                    <input
                      type="text"
                      value={newEncounter.reasonForVisit}
                      onChange={(e) =>
                        setNewEncounter({ ...newEncounter, reasonForVisit: e.target.value })
                      }
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Notes</label>
                    <textarea
                      value={newEncounter.notes}
                      onChange={(e) =>
                        setNewEncounter({ ...newEncounter, notes: e.target.value })
                      }
                    />
                  </div>
                  <button type="submit" className="btn btn-success">
                    Add Encounter
                  </button>
                </form>
              )}

              <div style={{ marginTop: '1rem' }}>
                {patientData.encounters.length === 0 ? (
                  <p>No encounters recorded.</p>
                ) : (
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Date</th>
                        <th>Type</th>
                        <th>Reason</th>
                      </tr>
                    </thead>
                    <tbody>
                      {patientData.encounters.map((encounter) => (
                        <tr key={encounter.id}>
                          <td>{new Date(encounter.encounterDate).toLocaleDateString()}</td>
                          <td>{encounter.encounterType}</td>
                          <td>{encounter.reasonForVisit}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          </>
        )}
      </div>
    </>
  );
};

export default DoctorDashboard;
