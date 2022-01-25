
#ifndef __ENC_HRM_SERVICE_H__
#define __ENC_HRM_SERVICE_H__
#include <BLE_API.h>

#include "Log.h"

#define CUSTOME_CHARACTERISTIC_LENGTH 20
typedef unsigned char CHARACTERISTICATTR_TYPE[CUSTOME_CHARACTERISTIC_LENGTH];
typedef ReadWriteArrayGattCharacteristic<unsigned char, CUSTOME_CHARACTERISTIC_LENGTH> ENCHRMCHAR_TYPE;

static const uint8_t ENCHRM_SERVICE_UUID[] =
    {0x00, 0x00, 0xA0, 0x00, 0x00, 0x00, 0x10, 0x00, 0x80, 0x00, 0x00, 0x80, 0x5F, 0x9B, 0x34, 0xFB};
static const uint8_t ENCHRM_STATE_CHARACTERISTIC_UUID[] =
    {0x00, 0x00, 0xA0, 0x01, 0x00, 0x00, 0x10, 0x00, 0x80, 0x00, 0x00, 0x80, 0x5F, 0x9B, 0x34, 0xFB};

class EncHRMService {
public:

    EncHRMService(BLEDevice &_ble, CHARACTERISTICATTR_TYPE initialValueForENCHRMCharacteristic) :
        ble(_ble), enchrmChar(ENCHRM_STATE_CHARACTERISTIC_UUID, initialValueForENCHRMCharacteristic, GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_NOTIFY)
    {
        GattCharacteristic *charTable[] = {&enchrmChar};
        GattService         ledService(ENCHRM_SERVICE_UUID, charTable, sizeof(charTable) / sizeof(GattCharacteristic *));
        ble.addService(ledService);
    }

    GattAttribute::Handle_t getValueHandle() const {
        return enchrmChar.getValueHandle();
    }

    GattAttribute::Handle_t getValueAttributeHandle() const {
        return enchrmChar.getValueAttribute().getHandle();
    }

    void printNotificationStatus()
    {
        logstart();
        bool e;
        ble.gattServer().areUpdatesEnabled(enchrmChar, &e);

        Serial1.print("areUpdatesEnabled: ");
        Serial1.println(e);

        logend();
    }


private:
    BLEDevice       &ble;
    ENCHRMCHAR_TYPE enchrmChar;
};

#endif /* #ifndef __ENC_HRM_SERVICE_H__ */
