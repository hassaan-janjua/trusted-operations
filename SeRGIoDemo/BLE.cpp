/*
 * ble.c
 *
 *  Created on: 27 Nov 2017
 *      Author: hassaanjanjua
 */

#include "BLE.h"

#include "Arduino.h"

#include "Log.h"


const static char     DEVICE_NAME[] = "SeRGIoDemo";
bool connected = false;
BLE ble;

EncHRMService *encHRMServicePtr;
CHARACTERISTICATTR_TYPE arrayCharacteristicValue = {0};

void connection_callback(const Gap::ConnectionCallbackParams_t *params)
{
    logstartd();
    connected = true;
}


static void disconnection_callback(Gap::Handle_t handle, Gap::DisconnectionReason_t reason)
{
    logstartd();
    connected = false;
    arrayCharacteristicValue[0] = 0;
    ble.startAdvertising();
}


/**
 * This callback allows the LEDService to receive updates to the ledState Characteristic.
 *
 * @param[in] params
 *     Information about the characterisitc being updated.
 */
void on_data_written_callback(const GattWriteCallbackParams *params) {
    logstartd();

    if ((params->handle == encHRMServicePtr->getValueHandle()) && (params->len > 1)) {
        logd("param received");
    }

    logendd();
}

/**
 * This function is called when the ble initialization process has failed
 */
void on_ble_init_error(BLE &ble, ble_error_t error)
{
    logstartd();
}

void data_sent_callback(unsigned count)
{
    logstartd();
}

void data_read_callback(const GattReadCallbackParams *eventDataP)
{
    logstartd();
}

void updates_enabled_callback(GattAttribute::Handle_t attributeHandle)
{
    logstartd();
}

void updates_disabled_callback(GattAttribute::Handle_t attributeHandle)
{
    logstartd();
}

void confirmation_received_callback(GattAttribute::Handle_t attributeHandle)
{
    logstartd();
}


void ble_configure(void)
{
    // Initialize BLE
    ble.init();

    // Setup BLE callback functions
    ble.onDisconnection(disconnection_callback);

    ble.gattServer().onDataWritten(on_data_written_callback);

    ble.gattServer().onDataSent(data_sent_callback);

    ble.gattServer().onDataRead(data_read_callback);

    ble.gattServer().onUpdatesEnabled(updates_enabled_callback);

    ble.gattServer().onUpdatesDisabled(updates_disabled_callback);

    ble.gattServer().onConfirmationReceived(confirmation_received_callback);

    ble.gap().onConnection(connection_callback);

    logmd("isOnDataReadAvailable: ", ble.gattServer().isOnDataReadAvailable());

    encHRMServicePtr = new EncHRMService(ble, arrayCharacteristicValue);

    /* setup advertising */
    ble.accumulateAdvertisingPayload(GapAdvertisingData::BREDR_NOT_SUPPORTED | GapAdvertisingData::LE_GENERAL_DISCOVERABLE);
    ble.accumulateAdvertisingPayload(GapAdvertisingData::COMPLETE_LOCAL_NAME, (uint8_t *)DEVICE_NAME, sizeof(DEVICE_NAME));

    ble.setAdvertisingType(GapAdvertisingParams::ADV_CONNECTABLE_UNDIRECTED);

    // set device name
    ble.setDeviceName((const uint8_t *)DEVICE_NAME);

    // set tx power,valid values are -40, -20, -16, -12, -8, -4, 0, 4
    ble.setTxPower(4);

    // set adv_interval, 100ms in multiples of 0.625ms.
    ble.setAdvertisingInterval(160);

    // set adv_timeout, in seconds
    ble.setAdvertisingTimeout(0);

    // start advertising
    ble.startAdvertising();

}
