package uk.nhs.digital.iucds.middleware;

import lombok.Data;

@Data
public class NHS111ReportData {

  private String title;
  private String patientName;
  private String dob;
  private String gender;
  private String nhsNo;
  private String localPatientId;
  private String homePhone;
  private String mobilePhone;
  private String emergencyPhone;
  private String homeAddress;
  private String gpAddress;
  private String patientsReportedCondition;
  private String specialPatientNotes;
  private String pathwaysDisposition;
  private String consultationSummary;
  private String pathwaysAssessment;
  private String adviceGiven;
  private String header;
  private String titlebar;
  private String footer;
}