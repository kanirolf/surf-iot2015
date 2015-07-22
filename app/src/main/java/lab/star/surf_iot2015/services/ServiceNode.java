package lab.star.surf_iot2015.services;

import java.util.EnumSet;

import lab.star.surf_iot2015.DataReaderService;
import lab.star.surf_iot2015.ListenerService;
import lab.star.surf_iot2015.ReminderService;
import lab.star.surf_iot2015.SensorTogglerService;

/**
 * <p>A ServiceNode is considered to be a node which a service resource, as indicated
 * by ServiceType, passes through. Objects that use a Service can "wrap" themselves around the
 * ServiceNode, thereby having access to services through the ServiceNode and becoming
 * ServiceNode.Wrapper instances. Each ServiceNode.Wrapper instance can be notified of service
 * acquisition through onServicesAcquired(). An object that contains at least one ServiceNode.Wrapper
 * instance must itself be a ServiceNode.Wrapper.
 *
 * <p>A ServiceNode instance is considered to be in a valid state once it has the services
 * it needs to function properly. Therefore, when subclassing ServiceNode,
 * <tt>defineServicesNeeded()</tt> will define the services needed as an instance of an
 * EnumSet<ServiceType>. Once a service of ServiceType is acquired, the ServiceType corresponding
 * to the service will be removed. Once all services are acquired (i.e. ServiceNode instance is in
 * a valid state,) <tt>onServicesAcquired()</tt> is called.
 *
 * <p>To access service instances once onServicesAcquired() is called, use the accessor method
 * corresponding to the service, e.g. to get ListenerService, use getListenerService;
 *
 * @author kanirolf
 * @version 0.2.1a
 * @since 2015-07-21
 */
public class ServiceNode {

    private EnumSet<ServiceType> servicesNeeded;

    private DataReaderService dataReaderService;
    private ListenerService listenerService;
    private SensorTogglerService sensorTogglerService;
    private ReminderService reminderService;

    private final Wrapper wrapper;

    public ServiceNode(Wrapper wrapper){
        this.wrapper = wrapper;

        servicesNeeded = this.wrapper.defineServicesNeeded();
    }

    /**
     * This is meant to be called by a parent ServiceNode instance to notify this ServiceNode instance
     * that a DataReaderService has been acquired.
     *
     * @param dataReaderService The DataReaderService instance to be passed to this ServiceNode.
     */
    public void giveDataReaderService(DataReaderService dataReaderService){
        this.dataReaderService = dataReaderService;
        markAcquired(ServiceType.DATA_READER_SERVICE);
    }

    /**
     * This is meant to be called by a parent ServiceNode instance to notify this ServiceNode instance
     * that a ListenerService has been acquired.
     *
     * @param listenerService The ListenerService instance to be passed to this ServiceNode.
     */
    public void giveListenerService(ListenerService listenerService){
        this.listenerService = listenerService;
        markAcquired(ServiceType.LISTENER_SERVICE);
    }


    /**
     * This is meant to be called by a parent ServiceNode instance to notify this ServiceNode instance
     * that a SensorTogglerService has been acquired.
     *
     * @param sensorTogglerService The SensorTogglerService instance to be passed to this ServiceNode.
     */
    public void giveSensorTogglerService(SensorTogglerService sensorTogglerService){
        this.sensorTogglerService = sensorTogglerService;
        markAcquired(ServiceType.SENSOR_TOGGLER_SERVICE);
    }

    /**
     * This is meant to be called by a parent ServiceNode instance to notify this ServiceNode instance
     * that a ReminderService has been acquired.
     *
     * @param reminderService The ReminderService instance to be passed to this ServiceNode.
     */
    public void giveReminderService(ReminderService reminderService){
        this.reminderService = reminderService;
        markAcquired(ServiceType.REMINDER_SERVICE);
    }

    /**
     * Call this to get the acquired DataReaderService instance. Note that this is null if
     * SensorType.DATA_READER_SERVICE is not indicated in defineServicesNeeded().
     *
     * @return DataReaderService The DataReaderService instance contained within this class.
     */
    public DataReaderService getDataReaderService() {
        return dataReaderService;
    }

    /**
     * Call this to get the acquired ListenerService instance. Note that this is null if
     * SensorType.LISTENER_SERVICE is not indicated in defineServicesNeeded().
     *
     * @return ListenerService The ListenerService instance contained within this class.
     */
    public ListenerService getListenerService() {
        return listenerService;
    }

    /**
     * Call this to get the acquired SensorTogglerService instance. Note that this is null if
     * SensorType.SENSOR_TOGGLER_SERVICE is not indicated in defineServicesNeeded().
     *
     * @return SensorTogglerService The SensorTogglerService instance contained within this class.
     */
    public SensorTogglerService getSensorTogglerService() {
        return sensorTogglerService;
    }

    /**
     * Call this to get the acquired ReminderService instance. Note that this is null if
     * SensorType.REMINDER_SERVICE is not indicated in defineServicesNeeded().
     *
     * @return ReminderService The ReminderService instance contained within this class.
     */
    public ReminderService getReminderService() {
        return reminderService;
    }

    /**
     * Called to update the services currently acquired by removing them from servicesNeeded.
     * Once servicesNeeded is empty (i.e. no more services are needed,) onServicesAcquired()
     * is called.
     *
     * @param serviceType
     */
    private void markAcquired(ServiceType serviceType){
        this.servicesNeeded.remove(serviceType);
        if (this.servicesNeeded.isEmpty()){
            wrapper.onServicesAcquired();
        }
    }

    public interface Wrapper {

        EnumSet<ServiceType> defineServicesNeeded();
        void onServicesAcquired();

        ServiceNode getUnderlyingNode();
    }
}
