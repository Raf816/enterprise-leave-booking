package com.staffs.leavebooking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Spring Modulith Architecture")
class ModularityTest {

    @Test
    @DisplayName("Application modules should be detected correctly")
    void shouldDetectModules() {
        ApplicationModules modules = ApplicationModules.of(LeavebookingApplication.class);

        assertThat(modules.stream().count()).isGreaterThanOrEqualTo(4);

        modules.forEach(module ->
                System.out.println("Module detected: " + module.getName()));
    }
}
