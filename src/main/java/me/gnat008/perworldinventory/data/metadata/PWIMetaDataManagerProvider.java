package me.gnat008.perworldinventory.data.metadata;

import ch.jalu.injector.Injector;

import javax.inject.Inject;
/**
 * Since {@link me.gnat008.perworldinventory.data.FileWriter} and {@link PWIMetaDataManager} is in a dependency loop.
 * This provider gets the manager after the {@link me.gnat008.perworldinventory.data.FileWriter} has been instantiated.
 */
public class PWIMetaDataManagerProvider {
    @Inject
    private Injector injector;

    /**
     * @return the {@link PWIMetaDataManager} instance.
     */
    public PWIMetaDataManager getInstance() {
        return injector.getSingleton(PWIMetaDataManager.class);
    }
}
