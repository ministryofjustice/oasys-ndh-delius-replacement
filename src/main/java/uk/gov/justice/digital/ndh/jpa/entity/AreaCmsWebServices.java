package uk.gov.justice.digital.ndh.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Data
@Entity
@Table(name = "AREA_CMS_WEB_SERVICES")
public class AreaCmsWebServices {
    @Id
    @Column(name = "AREA_ID")
    private String areaId;
    @Column(name = "AREA_NAME")
    private String areaName;
    @Column(name = "CMS_TYPE")
    private String cmsType;
    @Column(name = "IP_ADDRESS")
    private String ipAddress;
    @Column(name = "PORT")
    private Long port;
    @Column(name = "REACHABLE")
    private String reachable;
    @Column(name = "IP_ADDRESS2")
    private String ipAddress2;

}
