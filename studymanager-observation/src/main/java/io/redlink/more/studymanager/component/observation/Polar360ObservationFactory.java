package io.redlink.more.studymanager.component.observation;

import java.util.List;

import io.redlink.more.studymanager.component.observation.measurement.GenericMeasurementSets;
import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.properties.model.BooleanValue;
import io.redlink.more.studymanager.core.properties.model.ChoiceValue;
import io.redlink.more.studymanager.core.properties.model.IntegerValue;
import io.redlink.more.studymanager.core.properties.model.StringListValue;
import io.redlink.more.studymanager.core.properties.model.Value;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

public class Polar360ObservationFactory <C extends Observation<P>, P extends ObservationProperties> extends ObservationFactory<C, P>  {
    @Override
    public String getId() {
        return "polar360observation";
    }


    private static List<Value> properties = List.of(
        new IntegerValue("sampling_rate").setDefaultValue(1)
        .setDescription("Downsampling for online streaming"),
        new BooleanValue("Offline_recording").setDefaultValue(false).setDescription("Offline recording on device or online streaming"),
        new BooleanValue("Acc").setDefaultValue(false).setDescription("Collect acc data"),
        new BooleanValue("Temp").setDefaultValue(false).setDescription("Collect temp data"),
        new BooleanValue("Ppi").setDefaultValue(false).setDescription("Collect Ppi, if enabled, Hr cant be collected on online stream we collect ppi data"),
        new BooleanValue("Hr").setDefaultValue(false).setDescription("Collect HR data, if Hr collected Ppi cant be if both toggled HR will be collected"),
        new BooleanValue("continuous_recording").setDefaultValue(false).setDescription("Continous data collection with offline recording, restart recording after fetching records")
    ); 


    @Override
    public String getTitle() {
        return "Polar 360";
    }

    @Override
    public String getDescription() {
        return "Test ";
    }
    public List<Value> getProperties() {
        return properties;
    }
    @Override
    public Polar360Observation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new Polar360Observation(sdk, validate((P)properties));
    }

    @Override
    public MeasurementSet getMeasurementSet() {
        return GenericMeasurementSets.NOT_SPECIFIED;
    }
}
