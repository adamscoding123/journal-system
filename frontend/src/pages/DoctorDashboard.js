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
  const [selectedMessage, setSelectedMessage] = useState(null);
  const [showMessageDetail, setShowMessageDetail] = useState(false);
  const [replyContent, setReplyContent] = useState('');
  const [messageReplies, setMessageReplies] = useState([]);

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
      // Check if user and userId exist before making API calls
      if (!user || !user.userId) {
        console.error('User information not available');
        setLoading(false);
        return;
      }

      const practitionerRes = await practitionerAPI.getByUserId(user.userId);
      setPractitioner(practitionerRes.data);

      const patientsRes = await patientAPI.getAll();
      console.log('Patients loaded:', patientsRes.data); // Debug log
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
      console.log('fetchPatientData called with patientId:', patientId, 'Type:', typeof patientId); // Debug log
      
      // Check if patientId is valid before making API calls
      if (!patientId || patientId === 'null' || patientId === 'undefined') {
        console.error('Invalid patient ID provided:', patientId);
        setPatientData({
          encounters: [],
          observations: [],
          conditions: [],
        });
        return;
      }

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
    console.log('Patient selected:', patient); // Debug log
    if (!patient || !patient.id) {
      console.error('Invalid patient selected:', patient);
      return;
    }
    
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

  const handleSelectMessage = async (message) => {
    setSelectedMessage(message);
    setShowMessageDetail(true);
    
    // Mark message as read
    if (!message.isRead && message.receiver.id === user.userId) {
      try {
        await messageAPI.markAsRead(message.id);
        // Update local state
        setMessages(messages.map(msg => 
          msg.id === message.id ? { ...msg, isRead: true } : msg
        ));
      } catch (error) {
        console.error('Error marking message as read:', error);
      }
    }
    
    // Fetch replies
    try {
      const repliesRes = await messageAPI.getReplies(message.id);
      setMessageReplies(repliesRes.data);
    } catch (error) {
      console.error('Error fetching replies:', error);
    }
  };

  const handleSendReply = async (e) => {
    e.preventDefault();
    if (!replyContent.trim()) return;
    
    try {
      await messageAPI.create({
        receiver: { id: selectedMessage.sender.id },
        sender: { id: user.userId },
        subject: `Re: ${selectedMessage.subject}`,
        content: replyContent,
        parentMessage: { id: selectedMessage.id }
      });
      
      // Refresh replies
      const repliesRes = await messageAPI.getReplies(selectedMessage.id);
      setMessageReplies(repliesRes.data);
      setReplyContent('');
      
      // Refresh messages list
      const messagesRes = await messageAPI.getByUserId(user.userId);
      setMessages(messagesRes.data);
    } catch (error) {
      console.error('Error sending reply:', error);
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
                      {patient.user ? 
                        `${patient.user.firstName} ${patient.user.lastName}` : 
                        `${patient.firstName || ''} ${patient.lastName || ''}`.trim() || 'Anonymous Patient'}
                    </div>
                    <div className="message-sender">
                      Personal Number: {patient.personalNumber || 'N/A'}
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
                    onClick={() => handleSelectMessage(msg)}
                    style={{ cursor: 'pointer' }}
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
                  Patient: {selectedPatient.user ? 
                    `${selectedPatient.user.firstName} ${selectedPatient.user.lastName}` :
                    `${selectedPatient.firstName || ''} ${selectedPatient.lastName || ''}`.trim() || 'Anonymous Patient'}
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

        {showMessageDetail && selectedMessage && (
          <div className="modal">
            <div className="modal-content" style={{ maxWidth: '600px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h3>Message Details</h3>
                <button
                  className="btn btn-danger"
                  onClick={() => {
                    setShowMessageDetail(false);
                    setSelectedMessage(null);
                    setMessageReplies([]);
                    setReplyContent('');
                  }}
                >
                  Close
                </button>
              </div>

              <div style={{ marginBottom: '1rem', padding: '1rem', background: '#f5f5f5', borderRadius: '5px' }}>
                <p><strong>From:</strong> {selectedMessage.sender.username}</p>
                <p><strong>Subject:</strong> {selectedMessage.subject}</p>
                <p><strong>Date:</strong> {new Date(selectedMessage.sentAt).toLocaleString()}</p>
                <hr />
                <p><strong>Message:</strong></p>
                <p style={{ whiteSpace: 'pre-wrap' }}>{selectedMessage.content}</p>
              </div>

              {messageReplies.length > 0 && (
                <div style={{ marginBottom: '1rem' }}>
                  <h4>Conversation Thread:</h4>
                  {messageReplies.map(reply => (
                    <div key={reply.id} style={{ 
                      marginBottom: '0.5rem', 
                      padding: '0.5rem', 
                      background: reply.sender.id === user.userId ? '#e3f2fd' : '#f5f5f5',
                      borderRadius: '5px'
                    }}>
                      <p><strong>{reply.sender.username}</strong> - {new Date(reply.sentAt).toLocaleString()}</p>
                      <p>{reply.content}</p>
                    </div>
                  ))}
                </div>
              )}

              <form onSubmit={handleSendReply}>
                <div className="form-group">
                  <label>Reply:</label>
                  <textarea
                    value={replyContent}
                    onChange={(e) => setReplyContent(e.target.value)}
                    rows={4}
                    required
                    placeholder="Type your reply..."
                  />
                </div>
                <button type="submit" className="btn btn-primary">
                  Send Reply
                </button>
              </form>
            </div>
          </div>
        )}
      </div>
    </>
  );
};

export default DoctorDashboard;
