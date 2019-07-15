package uk.gov.justice.digital.ndh.jpa.repository.requirementLookup;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RequirementLookupRepositoryCsvBackedTest {

    @Test(expected = IllegalStateException.class)
    public void requirementLookupCsvCannotContainDuplicates() throws IOException {
        new RequirementLookupRepositoryCsvBacked(new ClassPathResource("requirement_lookup_with_duplicates.csv"), new ClassPathResource("ignored_requirement_codes.json"));
    }

    @Test
    public void requirementLookupSucceedsForCodeAndSubCode() throws IOException {
        var repo = new RequirementLookupRepositoryCsvBacked(new ClassPathResource("requirement_lookup.csv"), new ClassPathResource("ignored_requirement_codes.json"));

        final Optional<RequirementLookup> actual = repo.findByReqTypeAndReqCodeAndSubCode("N", "G", "");
        assertThat(actual).isPresent();
        assertThat(actual.get().getSentenceAttributeCat()).isEqualTo("CJA_REQUIREMENT");
        assertThat(actual.get().getSentenceAttributeElm()).isEqualTo("DRUG_REHABILITATION");
    }

    @Test
    public void requirementLookupFallsBackToCodeIfSubCodeUnmapped() throws IOException {
        var repo = new RequirementLookupRepositoryCsvBacked(new ClassPathResource("requirement_lookup.csv"), new ClassPathResource("ignored_requirement_codes.json"));

        final Optional<RequirementLookup> actual = repo.findByReqTypeAndReqCodeAndSubCode("N", "G", "G03");
        assertThat(actual).isPresent();
        assertThat(actual.get().getSentenceAttributeCat()).isEqualTo("CJA_REQUIREMENT");
        assertThat(actual.get().getSentenceAttributeElm()).isEqualTo("DRUG_REHABILITATION");
    }

    @Test
    public void requirementLookupIsEmptyIfNeitherCodeNorSubcodeMapped() throws IOException {
        var repo = new RequirementLookupRepositoryCsvBacked(new ClassPathResource("requirement_lookup.csv"), new ClassPathResource("ignored_requirement_codes.json"));

        final Optional<RequirementLookup> actual = repo.findByReqTypeAndReqCodeAndSubCode("N", "Colin", "Jeremy");
        assertThat(actual).isEmpty();
    }

    @Test
    public void blacklistedRequirementsAreIgnored() throws IOException {
        var repo = new RequirementLookupRepositoryCsvBacked(new ClassPathResource("requirement_lookup.csv"), new ClassPathResource("ignored_requirement_codes.json"));

        final Optional<RequirementLookup> actual = repo.findByReqTypeAndReqCodeAndSubCode("N", "7", "");
        assertThat(actual).isEmpty();
    }
}