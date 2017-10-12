package de.thomaskrille.dropwizard_template_config;

/**
 * Factory to create model with help of using configuration supplied
 */
public interface DataModelFactory {

    /**
     * Create data model, the result must not be null
     * @return new data model
     */
    Object createDataModel();

}
