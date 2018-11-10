package uk.gov.justice.digital.ndh.api.nomis.elite2;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Builder
@Value
public class InmateDetail {
    private Long bookingId;
    private String bookingNo;
    private String offenderNo;
    private String firstName;
    private String middleName;
    private String lastName;
    private String agencyId;
    private Long assignedLivingUnitId;
    private boolean activeFlag;
    private String religion;
    private List<String> alertsCodes;
    private Long activeAlertCount;
    private Long inactiveAlertCount;
    private AssignedLivingUnit assignedLivingUnit;
    private Long facialImageId;
    private LocalDate dateOfBirth;
    private Integer age;
    private PhysicalAttributes physicalAttributes;
    private List<PhysicalCharacteristic> physicalCharacteristics;
    private List<ProfileInformation> profileInformation;
    private List<PhysicalMark> physicalMarks;
    private List<Assessment> assessments;
    private Long assignedOfficerId;

}
