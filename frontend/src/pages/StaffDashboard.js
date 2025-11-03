import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import {
  patientAPI,
  practitionerAPI,
  encounterAPI,
  observationAPI,
  messageAPI,
} from '../services/api';
import Navbar from '../components/Navbar';

const StaffDashboard = () => {
  const { user } = useAuth();
  const [practitioner, setPractitioner] = useState(null);
  const [patients, setPatients] = useState([]);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [patientData, setPatientData] = useState({
    encounters: [],
    observations: [],
  });
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showEncounterForm, setShowEncounterForm] = useState(false);
  const [showObservationForm, setShowObservationForm] = useState(false);

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

  useEffect(() => {
    fetchStaffData();
  }, [user]);

  const fetchStaffData = async () => {
    try {
      const practitionerRes = await practitionerAPI.getByUserId(user.userId);
      setPractitioner(practitionerRes.data);

      const patientsRes = await patientAPI.getAll();
      setPatients(patientsRes.data);

      const messagesRes = await messageAPI.getByUserId(user.userId);
      setMessages(messagesRes.data);

      setLoading(false);
    } catch (error) {
      console.error('Error fetching staff data:', error);
      setLoading(false);
    }
  };

  const fetchPatientData = async (patientId) => {
    try {
      const encountersRes = await encounterAPI.getByPatientId(patientId);
      const observationsRes = await observationAPI.getByPatientId(patientId);

      setPatientData({
        encounters: encountersRes.data,
        observations: observationsRes.data,
      });
    } catch (error) {
      console.error('Error fetching patient data:', error);
    }
  };

  const handleSelectPatient = (patient) => {
    setSelectedPatient(patient);
    fetchPatientData(patient.id);
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
        <h2>Staff Dashboard</h2>

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
              <h3>
                Patient: {selectedPatient.user?.firstName} {selectedPatient.user?.lastName}
              </h3>
              <p><strong>Personal Number:</strong> {selectedPatient.personalNumber}</p>
              <p><strong>Date of Birth:</strong> {selectedPatient.dateOfBirth}</p>
              <p><strong>Address:</strong> {selectedPatient.address}</p>
              <p><strong>Phone:</strong> {selectedPatient.phoneNumber}</p>
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

export default StaffDashboard;
