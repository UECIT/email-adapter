package uk.nhs.digital.iucds.middleware.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.codec.binary.Base64;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.v24.datatype.ED;
import ca.uhn.hl7v2.model.v24.datatype.PL;
import ca.uhn.hl7v2.model.v24.datatype.TS;
import ca.uhn.hl7v2.model.v24.datatype.XAD;
import ca.uhn.hl7v2.model.v24.datatype.XCN;
import ca.uhn.hl7v2.model.v24.datatype.XPN;
import ca.uhn.hl7v2.model.v24.message.MDM_T02;
import ca.uhn.hl7v2.model.v24.segment.EVN;
import ca.uhn.hl7v2.model.v24.segment.MSH;
import ca.uhn.hl7v2.model.v24.segment.OBX;
import ca.uhn.hl7v2.model.v24.segment.PID;
import ca.uhn.hl7v2.model.v24.segment.PV1;
import ca.uhn.hl7v2.model.v24.segment.TXA;

public class MDMT02MessageBuilder {

  private final DateTimeFormatter FOMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
  private MDM_T02 _mdmt02Message;

  /*
   * You can pass in a domain object as a parameter when integrating with data from your application
   * here and I will leave that to you to explore on your own. I will use fictional data here for
   * illustration
   */

  public MDM_T02 Build(byte[] transform) throws HL7Exception, IOException {
    String currentDateTimeString = getCurrentTimeStamp();
    _mdmt02Message = new MDM_T02();
    _mdmt02Message.initQuickstart("MDM", "T02", "P");
    createMshSegment(currentDateTimeString);
    createEvnSegment(currentDateTimeString);
    createPidSegment();
    createPv1Segment();
    CreateTxaSegment();
    CreateObxSegment(transform);
    return _mdmt02Message;
  }

  private void createMshSegment(String currentDateTimeString) throws DataTypeException {
    MSH mshSegment = _mdmt02Message.getMSH();
    mshSegment.getFieldSeparator().setValue("|");
    mshSegment.getEncodingCharacters().setValue("^~\\&");
    mshSegment.getSendingApplication().getNamespaceID().setValue("Our System");
    mshSegment.getSendingFacility().getNamespaceID().setValue("Our Facility");
    mshSegment.getReceivingApplication().getNamespaceID().setValue("Their Remote System");
    mshSegment.getReceivingFacility().getNamespaceID().setValue("Their Remote Facility");
    mshSegment.getDateTimeOfMessage().getTimeOfAnEvent().setValue(currentDateTimeString);
    mshSegment.getMessageControlID().setValue(getSequenceNumber());
    mshSegment.getVersionID().getVersionID().setValue("2.4");
  }

  private void createEvnSegment(String currentDateTimeString) throws DataTypeException {
    EVN evn = _mdmt02Message.getEVN();
    evn.getEventTypeCode().setValue("T02");
    evn.getRecordedDateTime().getTimeOfAnEvent().setValue(currentDateTimeString);
  }

  private void createPidSegment() throws DataTypeException {
    PID pid = _mdmt02Message.getPID();
    XPN patientName = pid.getPatientName(0);
    patientName.getFamilyName().getSurname().setValue("Mouse");
    patientName.getGivenName().setValue("Mickey");
    pid.getPatientIdentifierList(0).getID().setValue("378785433211");
    XAD patientAddress = pid.getPatientAddress(0);
    patientAddress.getStreetAddress().getStreetOrMailingAddress().setValue("123 Main Street");
    patientAddress.getCity().setValue("Lake Buena Vista");
    patientAddress.getStateOrProvince().setValue("FL");
    patientAddress.getCountry().setValue("USA");
  }

  private void createPv1Segment() throws DataTypeException {
    PV1 pv1 = _mdmt02Message.getPV1();
    pv1.getPatientClass().setValue("O"); // to represent an 'Outpatient'
    PL assignedPatientLocation = pv1.getAssignedPatientLocation();
    assignedPatientLocation.getFacility().getNamespaceID().setValue("Some Treatment Facility Name");
    assignedPatientLocation.getPointOfCare().setValue("Some Point of Care");
    pv1.getAdmissionType().setValue("ALERT");
    XCN referringDoctor = pv1.getReferringDoctor(0);
    referringDoctor.getIDNumber().setValue("99999999");
    referringDoctor.getFamilyName().getSurname().setValue("Smith");
    referringDoctor.getGivenName().setValue("Jack");
    referringDoctor.getIdentifierTypeCode().setValue("456789");
    pv1.getAdmitDateTime().getTimeOfAnEvent().setValue(getCurrentTimeStamp());
  }

  private void CreateTxaSegment() throws DataTypeException {
    TXA txa = _mdmt02Message.getTXA();
    txa.getDocumentType().setValue("DS");
    txa.getActivityDateTime().getTimeOfAnEvent().setValue(getCurrentTimeStamp());
  }
  
  private void CreateObxSegment(byte[] transform) throws DataTypeException, IOException {
    OBX obx = _mdmt02Message.getOBX();
    obx.getObservationIdentifier().getIdentifier().setValue("Report");
    Varies value = obx.getObservationValue(0);
    ED encapsulatedData = new ED(_mdmt02Message);
    String base64EncodedStringOfPdfReport = new String(Base64.encodeBase64(transform));
    encapsulatedData.getEd1_SourceApplication().getHd1_NamespaceID().setValue("Our Java Application");
    encapsulatedData.getTypeOfData().setValue("AP"); //see HL7 table 0191: Type of referenced data
    encapsulatedData.getDataSubtype().setValue("PDF");
    encapsulatedData.getEncoding().setValue("Base64");
    
    encapsulatedData.getData().setValue(base64EncodedStringOfPdfReport);
    value.setData(encapsulatedData);
}
  private String getCurrentTimeStamp() {
    return LocalDateTime.now().format(FOMATTER);
  }

  private String getSequenceNumber() {
    String facilityNumberPrefix = "1234"; // some arbitrary prefix for the facility
    return facilityNumberPrefix.concat(getCurrentTimeStamp());
  }
}
