// Do not remove the include below
#include "SeRGIoDemo.h"

#include "Crypto.h"
#include "Log.h"


//The setup function is called once at startup of the sketch
void setup()
{
    Serial1.begin(9600);
    logstartd();

    //crypto_init();

    //test_dh();

    test_crypto();

    logendd();
}

// The loop function is called in an endless loop
void loop()
{
    logstartv();

    logendv();
}
