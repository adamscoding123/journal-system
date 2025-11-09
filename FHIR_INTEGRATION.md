# HAPI FHIR Integration Guide

This document describes how the Journal System has been integrated with the HAPI FHIR server.

## Overview

The system now supports fetching and storing medical data using the FHIR R4 standard through the HAPI FHIR server at https://hapi-fhir.app.cloud.cbh.kth.se/fhir

## Configuration

### Enable/Disable FHIR Integration

In `backend/src/main/resources/application.properties`:

```properties
# Enable FHIR integration (set to false to use only local database)
fhir.enabled=true

# FHIR server URL
fhir.server.base-url=https://hapi-fhir.app.cloud.cbh.kth.se/fhir
```

## Architecture

### FHIR Service Layer

Located in `com.journalsystem.service.fhir`:
- `PatientFhirService` - Handles FHIR Patient resources
- `ObservationFhirService` - Handles FHIR Observation resources
- `EncounterFhirService` - Handles FHIR Encounter resources
- `ConditionFhirService` - Handles FHIR Condition resources
- `PractitionerFhirService` - Handles FHIR Practitioner resources

### Converters

Located in `com.journalsystem.converter`:
- `PatientFhirConverter` - Converts between local Patient entity and FHIR Patient resource
- `ObservationFhirConverter` - Converts between local Observation entity and FHIR Observation
- `ConditionFhirConverter` - Converts between local Condition entity and FHIR Condition
- `EncounterFhirConverter` - Converts between local Encounter entity and FHIR Encounter

### Integration Strategy

The system uses a **hybrid approach**:
1. Local database remains the primary data store for user authentication and relationships
2. When FHIR is enabled, medical data is synchronized with the FHIR server
3. Read operations check FHIR first, then fall back to local database
4. Write operations update both local database and FHIR server

## Testing the Integration

### Test Endpoints

A test controller is available at `/api/fhir-test`:

1. **Check FHIR Server Status**
   ```
   GET /api/fhir-test/status
   ```
   Returns connection status and resource counts from FHIR server.

2. **List FHIR Patients**
   ```
   GET /api/fhir-test/patients
   ```
   Returns all patients from the FHIR server.

3. **Get Patient Details**
   ```
   GET /api/fhir-test/patients/{patientId}
   ```
   Returns patient details and counts of related resources.

### Testing with Existing Services

The existing patient endpoints now use FHIR when enabled:
- `GET /api/patients` - Returns patients from FHIR server
- `GET /api/patients/{id}` - Fetches patient from FHIR server
- `PUT /api/patients/{id}` - Updates both local DB and FHIR server

## Data Mapping

### Patient Resource

| Local Field | FHIR Field | Notes |
|-------------|------------|-------|
| personalNumber | identifier (system: http://electronichealth.se/identifier/personnummer) | Swedish personal number |
| firstName/lastName | name | From User entity |
| dateOfBirth | birthDate | |
| address | address | |
| phoneNumber | telecom | |
| bloodType | extension | Custom extension |
| allergies | extension | Custom extension |
| medications | extension | Custom extension |

### Observation Resource

| Local Field | FHIR Field | Notes |
|-------------|------------|-------|
| observationType | code.text | |
| value | valueQuantity or valueString | Numeric values use Quantity |
| unit | valueQuantity.unit | |
| observationDate | effectiveDateTime | |
| notes | note | |

## Future Improvements

1. **Complete Service Integration**: Integrate FHIR with ObservationService, EncounterService, and ConditionService
2. **Pagination Support**: Handle FHIR bundles with pagination for large datasets
3. **Error Handling**: Improve error handling and fallback mechanisms
4. **Caching**: Implement caching for frequently accessed FHIR resources
5. **Bidirectional Sync**: Ensure changes in FHIR server are reflected in local database
6. **Standard Coding Systems**: Use standard coding systems (SNOMED CT, ICD-10) for conditions and observations

## Troubleshooting

### Common Issues

1. **FHIR Server Connection Failed**
   - Check the server URL in application.properties
   - Ensure the server is accessible from your network

2. **Data Not Syncing**
   - Verify `fhir.enabled=true` in application.properties
   - Check logs for FHIR service errors

3. **Missing Dependencies**
   - Run `mvn clean install` to ensure HAPI FHIR dependencies are downloaded