Domain_Types.xsd changed with simple type CRONumber being up to 12 chars from 11

<xs:simpleType name="CRONumber">
       <xs:restriction base="xs:string">
            <xs:maxLength value="12"/>
       </xs:restriction>
</xs:simpleType>