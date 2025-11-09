import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import {
  patientAPI,
  encounterAPI,
  observationAPI,
  conditionAPI,
  messageAPI,
  practitionerAPI,
} from '../services/api';
import Navbar from '../components/Navbar';

const PatientDashboard = () => {
  const { user } = useAuth();
  const [patient, setPatient] = useState(null);
  const [encounters, setEncounters] = useState([]);
  const [observations, setObservations] = useState([]);
  const [conditions, setConditions] = useState([]);
  const [messages, setMessages] = useState([]);
  const [practitioners, setPractitioners] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showMessageForm, setShowMessageForm] = useState(false);
  const [selectedMessage, setSelectedMessage] = useState(null);
  const [showMessageDetail, setShowMessageDetail] = useState(false);
  const [messageReplies, setMessageReplies] = useState([]);
  const [newMessage, setNewMessage] = useState({
    receiverId: '',
    subject: '',
    content: '',
  });

  useEffect(() => {
    fetchPatientData();
  }, [user]);

  const fetchPatientData = async () => {
    try {
      // Check if user and userId exist before making API calls
      if (!user || !user.userId) {
        console.error('User information not available');
        setLoading(false);
        return;
      }

      const patientRes = await patientAPI.getByUserId(user.userId);
      setPatient(patientRes.data);

      const encountersRes = await encounterAPI.getByPatientId(patientRes.data.id);
      setEncounters(encountersRes.data);

      const observationsRes = await observationAPI.getByPatientId(patientRes.data.id);
      setObservations(observationsRes.data);

      const conditionsRes = await conditionAPI.getByPatientId(patientRes.data.id);
      setConditions(conditionsRes.data);

      const messagesRes = await messageAPI.getByUserId(user.userId);
      setMessages(messagesRes.data);

      const practitionersRes = await practitionerAPI.getAll();
      setPractitioners(practitionersRes.data);

      setLoading(false);
    } catch (error) {
      console.error('Error fetching patient data:', error);
      setLoading(false);
    }
  };

  const handleSendMessage = async (e) => {
    e.preventDefault();
    try {
      await messageAPI.create({
        sender: { id: user.userId },
        receiver: { id: parseInt(newMessage.receiverId) },
        subject: newMessage.subject,
        content: newMessage.content,
      });
      setShowMessageForm(false);
      setNewMessage({ receiverId: '', subject: '', content: '' });
      fetchPatientData();
    } catch (error) {
      console.error('Error sending message:', error);
    }
  };

  const handleSelectMessage = async (message) => {
    setSelectedMessage(message);
    setShowMessageDetail(true);
    
    // Mark message as read if patient is receiver
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
        <h2>Patient Dashboard</h2>

        <div className="card">
          <h3>My Information</h3>
          {patient && (
            <div>
              <p><strong>Name:</strong> {patient.user?.firstName} {patient.user?.lastName}</p>
              <p><strong>Personal Number:</strong> {patient.personalNumber}</p>
              <p><strong>Date of Birth:</strong> {patient.dateOfBirth}</p>
              <p><strong>Address:</strong> {patient.address}</p>
              <p><strong>Phone:</strong> {patient.phoneNumber}</p>
              <p><strong>Blood Type:</strong> {patient.bloodType || 'N/A'}</p>
              <p><strong>Allergies:</strong> {patient.allergies || 'None'}</p>
              <p><strong>Medications:</strong> {patient.medications || 'None'}</p>
            </div>
          )}
        </div>

        <div className="card">
          <h3>My Diagnoses</h3>
          {conditions.length === 0 ? (
            <p>No diagnoses recorded.</p>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Diagnosis</th>
                  <th>Severity</th>
                  <th>Status</th>
                  <th>Date</th>
                  <th>Notes</th>
                </tr>
              </thead>
              <tbody>
                {conditions.map((condition) => (
                  <tr key={condition.id}>
                    <td>{condition.diagnosis}</td>
                    <td>{condition.severity}</td>
                    <td>{condition.status}</td>
                    <td>{new Date(condition.diagnosisDate).toLocaleDateString()}</td>
                    <td>{condition.notes}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="card">
          <h3>My Observations</h3>
          {observations.length === 0 ? (
            <p>No observations recorded.</p>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Type</th>
                  <th>Value</th>
                  <th>Unit</th>
                  <th>Date</th>
                  <th>Notes</th>
                </tr>
              </thead>
              <tbody>
                {observations.map((obs) => (
                  <tr key={obs.id}>
                    <td>{obs.observationType}</td>
                    <td>{obs.value}</td>
                    <td>{obs.unit}</td>
                    <td>{new Date(obs.observationDate).toLocaleDateString()}</td>
                    <td>{obs.notes}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="card">
          <h3>My Encounters</h3>
          {encounters.length === 0 ? (
            <p>No encounters recorded.</p>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Type</th>
                  <th>Practitioner</th>
                  <th>Reason</th>
                  <th>Notes</th>
                </tr>
              </thead>
              <tbody>
                {encounters.map((encounter) => (
                  <tr key={encounter.id}>
                    <td>{new Date(encounter.encounterDate).toLocaleDateString()}</td>
                    <td>{encounter.encounterType}</td>
                    <td>
                      {encounter.practitioner?.user?.firstName}{' '}
                      {encounter.practitioner?.user?.lastName}
                    </td>
                    <td>{encounter.reasonForVisit}</td>
                    <td>{encounter.notes}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h3>Messages</h3>
            <button className="btn btn-primary" onClick={() => setShowMessageForm(!showMessageForm)}>
              {showMessageForm ? 'Cancel' : 'New Message'}
            </button>
          </div>

          {showMessageForm && (
            <form onSubmit={handleSendMessage} style={{ marginTop: '1rem' }}>
              <div className="form-group">
                <label>To (Practitioner)</label>
                <select
                  value={newMessage.receiverId}
                  onChange={(e) => setNewMessage({ ...newMessage, receiverId: e.target.value })}
                  required
                >
                  <option value="">Select practitioner</option>
                  {practitioners.map((prac) => (
                    <option key={prac.id} value={prac.user.id}>
                      {prac.user.firstName} {prac.user.lastName} - {prac.specialization}
                    </option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Subject</label>
                <input
                  type="text"
                  value={newMessage.subject}
                  onChange={(e) => setNewMessage({ ...newMessage, subject: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Message</label>
                <textarea
                  value={newMessage.content}
                  onChange={(e) => setNewMessage({ ...newMessage, content: e.target.value })}
                  required
                />
              </div>
              <button type="submit" className="btn btn-success">Send Message</button>
            </form>
          )}

          <div style={{ marginTop: '1rem' }}>
            {messages.length === 0 ? (
              <p>No messages.</p>
            ) : (
              <ul className="message-list">
                {messages.map((msg) => (
                  <li
                    key={msg.id}
                    className={`message-item ${!msg.isRead ? 'unread' : ''}`}
                    onClick={() => handleSelectMessage(msg)}
                    style={{ cursor: 'pointer' }}
                  >
                    <div className="message-subject">{msg.subject}</div>
                    <div className="message-sender">
                      {msg.sender.id === user.userId ? 'To: ' : 'From: '}
                      {msg.sender.id === user.userId ? msg.receiver.username : msg.sender.username}
                    </div>
                    <div className="message-date">
                      {new Date(msg.sentAt).toLocaleString()}
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

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
                  }}
                >
                  Close
                </button>
              </div>

              <div style={{ marginBottom: '1rem', padding: '1rem', background: '#f5f5f5', borderRadius: '5px' }}>
                <p><strong>{selectedMessage.sender.id === user.userId ? 'To' : 'From'}:</strong> {
                  selectedMessage.sender.id === user.userId ? 
                  selectedMessage.receiver.username : 
                  selectedMessage.sender.username
                }</p>
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

              {/* Only show new message button if this is a conversation the patient started */}
              {selectedMessage.sender.id === user.userId && messageReplies.length > 0 && (
                <div style={{ marginTop: '1rem', textAlign: 'center' }}>
                  <button 
                    className="btn btn-primary"
                    onClick={() => {
                      setShowMessageDetail(false);
                      setShowMessageForm(true);
                      setNewMessage({
                        receiverId: selectedMessage.receiver.id.toString(),
                        subject: `Re: ${selectedMessage.subject}`,
                        content: ''
                      });
                    }}
                  >
                    Send New Message
                  </button>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </>
  );
};

export default PatientDashboard;
