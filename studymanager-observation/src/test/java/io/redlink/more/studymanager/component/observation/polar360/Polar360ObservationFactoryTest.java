/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.observation.polar360;

import io.redlink.more.studymanager.component.observation.measurement.GenericMeasurementSets;
import io.redlink.more.studymanager.component.observation.polar360.acc.Polar360AccObservation;
import io.redlink.more.studymanager.component.observation.polar360.acc.Polar360AccObservationFactory;
import io.redlink.more.studymanager.component.observation.polar360.hr.Polar360HrObservation;
import io.redlink.more.studymanager.component.observation.polar360.hr.Polar360HrObservationFactory;
import io.redlink.more.studymanager.component.observation.polar360.ppi.Polar360PpiObservation;
import io.redlink.more.studymanager.component.observation.polar360.ppi.Polar360PpiObservationFactory;
import io.redlink.more.studymanager.component.observation.polar360.temp.Polar360TempObservation;
import io.redlink.more.studymanager.component.observation.polar360.temp.Polar360TempObservationFactory;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

class Polar360ObservationFactoryTest {

    // --- ACC ---

    @Test
    void acc_factory_returns_correct_id() {
        assertEquals("polar360observation:acc", new Polar360AccObservationFactory().getId());
    }

    @Test
    void acc_factory_returns_correct_title() {
        assertEquals("Polar 360 Accelerometer", new Polar360AccObservationFactory().getTitle());
    }

    @Test
    void acc_factory_returns_correct_description() {
        assertEquals("Collect accelerometer (ACC) data from Polar 360 device",
                new Polar360AccObservationFactory().getDescription());
    }

    @Test
    void acc_factory_has_two_properties() {
        assertEquals(2, new Polar360AccObservationFactory().getProperties().size());
    }

    @Test
    void acc_factory_properties_have_expected_ids() {
        var props = new Polar360AccObservationFactory().getProperties();
        assertEquals("Offline_recording", props.get(0).getId());
        assertEquals("continuous_recording", props.get(1).getId());
    }

    @Test
    void acc_factory_returns_not_specified_measurement_set() {
        assertEquals(GenericMeasurementSets.NOT_SPECIFIED, new Polar360AccObservationFactory().getMeasurementSet());
    }

    @Test
    void acc_factory_creates_correct_observation_type() throws ConfigurationValidationException {
        MoreObservationSDK sdk = mock(MoreObservationSDK.class);
        ObservationProperties properties = mock(ObservationProperties.class);
        assertInstanceOf(Polar360AccObservation.class,
                new Polar360AccObservationFactory().create(sdk, properties));
    }

    // --- HR ---

    @Test
    void hr_factory_returns_correct_id() {
        assertEquals("polar360observation:hr", new Polar360HrObservationFactory().getId());
    }

    @Test
    void hr_factory_returns_correct_title() {
        assertEquals("Polar 360 Heart Rate", new Polar360HrObservationFactory().getTitle());
    }

    @Test
    void hr_factory_returns_correct_description() {
        assertEquals("Collect heart rate (HR) data from Polar 360 device",
                new Polar360HrObservationFactory().getDescription());
    }

    @Test
    void hr_factory_has_two_properties() {
        assertEquals(2, new Polar360HrObservationFactory().getProperties().size());
    }

    @Test
    void hr_factory_properties_have_expected_ids() {
        var props = new Polar360HrObservationFactory().getProperties();
        assertEquals("Offline_recording", props.get(0).getId());
        assertEquals("continuous_recording", props.get(1).getId());
    }

    @Test
    void hr_factory_returns_not_specified_measurement_set() {
        assertEquals(GenericMeasurementSets.NOT_SPECIFIED, new Polar360HrObservationFactory().getMeasurementSet());
    }

    @Test
    void hr_factory_creates_correct_observation_type() throws ConfigurationValidationException {
        MoreObservationSDK sdk = mock(MoreObservationSDK.class);
        ObservationProperties properties = mock(ObservationProperties.class);
        assertInstanceOf(Polar360HrObservation.class,
                new Polar360HrObservationFactory().create(sdk, properties));
    }

    // --- PPI ---

    @Test
    void ppi_factory_returns_correct_id() {
        assertEquals("polar360observation:ppi", new Polar360PpiObservationFactory().getId());
    }

    @Test
    void ppi_factory_returns_correct_title() {
        assertEquals("Polar 360 PPI", new Polar360PpiObservationFactory().getTitle());
    }

    @Test
    void ppi_factory_returns_correct_description() {
        assertEquals("Collect pulse-to-pulse interval (PPI) data from Polar 360 device",
                new Polar360PpiObservationFactory().getDescription());
    }

    @Test
    void ppi_factory_has_two_properties() {
        assertEquals(2, new Polar360PpiObservationFactory().getProperties().size());
    }

    @Test
    void ppi_factory_properties_have_expected_ids() {
        var props = new Polar360PpiObservationFactory().getProperties();
        assertEquals("Offline_recording", props.get(0).getId());
        assertEquals("continuous_recording", props.get(1).getId());
    }

    @Test
    void ppi_factory_returns_not_specified_measurement_set() {
        assertEquals(GenericMeasurementSets.NOT_SPECIFIED, new Polar360PpiObservationFactory().getMeasurementSet());
    }

    @Test
    void ppi_factory_creates_correct_observation_type() throws ConfigurationValidationException {
        MoreObservationSDK sdk = mock(MoreObservationSDK.class);
        ObservationProperties properties = mock(ObservationProperties.class);
        assertInstanceOf(Polar360PpiObservation.class,
                new Polar360PpiObservationFactory().create(sdk, properties));
    }

    // --- TEMP ---

    @Test
    void temp_factory_returns_correct_id() {
        assertEquals("polar360observation:temp", new Polar360TempObservationFactory().getId());
    }

    @Test
    void temp_factory_returns_correct_title() {
        assertEquals("Polar 360 Temperature", new Polar360TempObservationFactory().getTitle());
    }

    @Test
    void temp_factory_returns_correct_description() {
        assertEquals("Collect temperature (TEMP) data from Polar 360 device",
                new Polar360TempObservationFactory().getDescription());
    }

    @Test
    void temp_factory_has_two_properties() {
        assertEquals(2, new Polar360TempObservationFactory().getProperties().size());
    }

    @Test
    void temp_factory_properties_have_expected_ids() {
        var props = new Polar360TempObservationFactory().getProperties();
        assertEquals("Offline_recording", props.get(0).getId());
        assertEquals("continuous_recording", props.get(1).getId());
    }

    @Test
    void temp_factory_returns_not_specified_measurement_set() {
        assertEquals(GenericMeasurementSets.NOT_SPECIFIED, new Polar360TempObservationFactory().getMeasurementSet());
    }

    @Test
    void temp_factory_creates_correct_observation_type() throws ConfigurationValidationException {
        MoreObservationSDK sdk = mock(MoreObservationSDK.class);
        ObservationProperties properties = mock(ObservationProperties.class);
        assertInstanceOf(Polar360TempObservation.class,
                new Polar360TempObservationFactory().create(sdk, properties));
    }

    // --- Factory IDs are distinct ---

    @Test
    void all_factory_ids_are_distinct() {
        var ids = java.util.List.of(
                new Polar360AccObservationFactory().getId(),
                new Polar360HrObservationFactory().getId(),
                new Polar360PpiObservationFactory().getId(),
                new Polar360TempObservationFactory().getId()
        );
        assertEquals(4, ids.stream().distinct().count());
    }
}
